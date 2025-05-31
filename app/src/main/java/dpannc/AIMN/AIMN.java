package dpannc.AIMN;

import java.sql.SQLException;
import java.util.*;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;

import dpannc.Vector;
import dpannc.database.DB;
import dpannc.database.DBiterator;
import dpannc.Progress;

public class AIMN {
    private DB db;
    private int n;
    private int d;
    private double sensitivity, epsilon, delta;
    private int T, K;
    private double c, lambda, r, alpha, beta, adjSen, threshold, etaU, etaQ;

    private Random random = new Random(100);
    private Noise noise = new Noise(100);
    private boolean DP = false;

    private static final String nodesTable = "nodesTable";

    private Map<Integer, List<Vector>> gaussiansAtLevel; // precomputed Gaussian vectors for each level
    private Set<String> nodes; // all nodes in tree - enables pruning
    private Map<String, Integer> counts; // raw counts
    private Map<String, Integer> noisyCounts; // counts with noise added
    private List<String> remainderBucket; // for vectors that are not assigned a node
    private List<String> query; // for returning result of a query
    private int queryCount; // for accumulating query counts
    private int queryNoisyCount; // for accumulating noisy query counts
    int emptyBuckets;

    public AIMN(int n, int d, double s, double c, double sensitivity, double epsilon, double delta, DB db)
            throws SQLException {
        this.db = db;
        this.n = n;
        this.d = d;
        this.c = c;
        this.r = s * (1.0 / Math.pow(log(n, 10), 1.0 / 8.0));
        lambda = (2.0 * Math.sqrt(2.0 * c)) / (c * c + 1.0);
        K = (int) Math.sqrt(ln(n));
        alpha = 1.0 - ((r * r) / 2.0); // cosine
        beta = Math.sqrt(1.0 - (alpha * alpha)); // sine
        adjSen = 2.0;
        threshold = (adjSen / epsilon) * ln(1.0 + (Math.exp(epsilon / 2.0) - 1.0) / delta);
        etaU = Math.sqrt((ln(n) / K)) * (lambda / r);
        etaQ = alpha * etaU - 2.0 * beta * Math.sqrt(ln(K));
        double F_etaU = 0.5 * Erf.erfc(etaU / Math.sqrt(2));
        T = (int) (10.0 * ln(K) / F_etaU);

        nodes = new HashSet<>();
        counts = new HashMap<>();
        noisyCounts = new HashMap<>();
        remainderBucket = new ArrayList<>();
        db.initTable(nodesTable);

        gaussiansAtLevel = new HashMap<>();
        for (int level = 0; level < K; level++) {
            List<Vector> gaussians = new ArrayList<>();
            gaussiansAtLevel.put(level, gaussians);
        }
        // generateGaussians();
    }

    public void populateFromDB(String table) throws Exception {
        Progress.newStatusBar("Loading vectors into AIMN", n);
        try (DBiterator it = db.iterator(table)) {
            int counter = 0;
            while (it.hasNext()) {
                Vector v = it.next();
                insert(v);
                counter++;
                Progress.updateStatusBar(counter);
            }
            Progress.printAbove(n - remainderBucket.size() + " vectors succesfully loaded into AIMN ("
                    + remainderBucket.size() + " in remainder)");
        }
        Progress.clearStatus();
        if (DP) {
            addNoise();
        }
    }

    // private void insert(Vector v) throws SQLException {
    //     int level = 0;
    //     String path = "R"; // starting point, not stored in DB

    //     while (level < k) {
    //         boolean accepted = false;
    //         List<Vector> gaussians = gaussiansAtLevel.get(level);
    //         for (int i = 0; i < T; i++) {
    //             Vector g = gaussians.get(i);
    //             if (v.dot(g) >= etaU) {
    //                 path += ":" + i;
    //                 nodes.add(path);
    //                 if (level == k - 1)
    //                     counts.put(path, counts.getOrDefault(path, 0) + 1);
    //                 level++;
    //                 accepted = true;
    //                 break;
    //             }
    //         }

    //         if (!accepted) {
    //             remainderBucket.add(v.getLabel());
    //             return;
    //         }
    //     }
    //     db.insertRow(v.getLabel(), path, nodesTable);
    // }

    private void insert(Vector v) throws SQLException {
        int level = 0;
        String path = "R"; // starting point, not stored in DB

        while (level < K) {
            boolean accepted = false;
            List<Vector> gaussians = gaussiansAtLevel.get(level);
            for (int i = 0; i < T; i++) {
                Vector g;
                // only generate gaussian when we need them
                if (i == gaussians.size()) {
                    g = new Vector(d).randomGaussian(random);
                    gaussiansAtLevel.get(level).add(g);
                } else {
                    g = gaussians.get(i);
                }

                if (v.dot(g) >= etaU) {
                    path += ":" + i;
                    nodes.add(path);
                    if (level == K - 1)
                        counts.put(path, counts.getOrDefault(path, 0) + 1);
                    level++;
                    accepted = true;
                    break;
                }
            }

            if (!accepted) {
                remainderBucket.add(v.getLabel());
                return;
            }
        }
        db.insertRow(v.getLabel(), path, nodesTable);
    }

    private void addNoise() throws Exception {
        Progress.newStatusBar("Adding noise to counts", counts.values().size());
        int i = 0;
        emptyBuckets = 0;
        for (String node : counts.keySet()) {
            int rawCount = counts.get(node);
            int noisyCount = rawCount + (int) noise.TLap(sensitivity, epsilon / adjSen, delta / adjSen);
            if (noisyCount <= threshold) {
                noisyCount = 0;
                emptyBuckets++;
            }
            noisyCounts.put(node, noisyCount);
            i++;
            Progress.updateStatusBar(i);
        }
        Progress.clearStatus();
        Progress.printAbove("Noise added to " + counts.values().size() + " counts");
    }

    public int query(Vector q) throws Exception {
        if (q == null)
            throw new IllegalArgumentException("cannot query a null vector");
        if (q.get().length != d)
            throw new IllegalArgumentException(
                    "query dimensionality needs to be the same as data: " + q.get().length + "!= " + d);

        Progress.printAbove("Querying vector: " + q.getLabel());
        query = new ArrayList<>();
        queryCount = 0;
        queryNoisyCount = 0;

        int count = query(q, 0, "R");
        return count;
    }

    private int query(Vector q, int level, String node) throws Exception {
        if (level == K) {
            List<String> vectors = db.getColumnWhereEquals("data", node, nodesTable,
                    "label");
            query.addAll(vectors);
            queryCount = counts.getOrDefault(node, 0);
            queryNoisyCount = noisyCounts.getOrDefault(node, 0);
            return queryCount; // getOrDefault should not be necessary
        }

        int count = 0;
        List<Vector> gaussians = gaussiansAtLevel.get(level);

        for (int i = 0; i < gaussians.size(); i++) {
            Vector g = gaussians.get(i);
            String nextNode = node + ":" + i;
            if (nodes.contains(nextNode) && q.dot(g) >= etaQ) {
                count += query(q, level + 1, nextNode);
            }
        }
        return count;
    }

    // precomputes dotproducts and saves all indexes of accepted Gaussians
    // paths to leaf nodes are then generated recursively
    public int queryFast(Vector q) throws Exception {
        if (q == null)
            throw new IllegalArgumentException("cannot query a null vector");
        if (q.get().length != d)
            throw new IllegalArgumentException("query dimensionality mismatch");

        Progress.printAbove("Query vector: " + q.getLabel());

        // precompute which gaussians that accepts q at each level
        List<Set<Integer>> queryGaussians = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            Set<Integer> accepted = new HashSet<>();
            List<Vector> gaussians = gaussiansAtLevel.get(i);
            for (int j = 0; j < gaussians.size(); j++) {
                if (q.dot(gaussians.get(j)) >= etaQ) {
                    accepted.add(j);
                }
            }
            if (accepted.isEmpty())
                return 0; // prune everything
            queryGaussians.add(accepted);
        }

        Progress.newStatus("Running query... ");
        query = new ArrayList<>();

        // recursively explore paths using precomputed accepted indices
        int count = queryFast("R", 0, queryGaussians);
        Progress.clearStatus();
        return count;
    }

    private int queryFast(String path, int level, List<Set<Integer>> queryGaussians) throws Exception {
        if (level == K) {
            List<String> vectors = db.getColumnWhereEquals("data", path, nodesTable,
                    "label");
            query.addAll(vectors);
            int leafCount = counts.getOrDefault(path, 0);
            queryCount += leafCount;
            queryNoisyCount += noisyCounts.getOrDefault(path, 0);
            return leafCount;
        }

        int count = 0;
        for (int i : queryGaussians.get(level)) {
            String nextPath = path + ":" + i;
            if (nodes.contains(nextPath)) {
                count += queryFast(nextPath, level + 1, queryGaussians);
            }
        }
        return count;
    }

    public List<String> queryList() throws Exception {
        if (query.isEmpty())
            throw new Exception("query list is empty");
        return query;
    }

    public int remainder() {
        return remainderBucket.size();
    }

    public double getN() {
        return n;
    }

    public double getC() {
        return c;
    }

    public double getR() {
        return r;
    }

    public void DP(boolean b) {
        DP = b;
    }

    public int[] gaussians() {
        int[] list = new int[K];
        for (int l = 0; l < K; l++) {
            list[l] = gaussiansAtLevel.get(l).size();
        }
        return list;
    }

    public int emptyBuckets() {
        return emptyBuckets;
    }

    public int buckets() {
        return counts.size();
    }

    public int getCount() {
        return queryCount;
    }

    public double noiseThreshold() {
        return threshold;
    }

    public int getNoisyCount() {
        return queryNoisyCount;
    }

    public static double log(double N, int base) {
        return Math.log(N) / Math.log(base);
    }

    public static double ln(double N) {
        return Math.log(N);
    }

    public String getSettingsString() {
        StringBuilder sb = new StringBuilder();

        sb.append("==== AIMN Settings ====\n");

        appendSetting(sb, "n", n);
        appendSetting(sb, "d", d);
        appendSetting(sb, "c", c);
        appendSetting(sb, "lambda", lambda);
        appendSetting(sb, "r", r);
        appendSetting(sb, "K", K);
        appendSetting(sb, "alpha", alpha);
        appendSetting(sb, "beta", beta);
        appendSetting(sb, "threshold", threshold);
        appendSetting(sb, "etaU", etaU);
        appendSetting(sb, "etaQ", etaQ);
        appendSetting(sb, "T", T);

        sb.append("=======================\n");

        return sb.toString();
    }

    private void appendSetting(StringBuilder sb, String name, double value) {
        sb.append(String.format("%-11s %11.3f%n", name + ":", value));
    }

    private void appendSetting(StringBuilder sb, String name, int value) {
        sb.append(String.format("%-11s %11d%n", name + ":", value));
    }

}
