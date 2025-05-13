package dpannc.AIMN;

import java.sql.SQLException;
import java.util.*;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;

import dpannc.Vector;
import dpannc.database.DB;
import dpannc.database.DBiterator;
import dpannc.Noise;
import dpannc.Progress;

public class AIMN {
    private DB db;
    private int n;
    private int d;
    private double sensitivity, epsilon, delta;
    private int T, k;
    private double c, lambda, r, K, alpha, beta, adjSen, threshold, etaU, etaQ;

    private Random random = new Random(100);
    private Noise noise = new Noise(100);
    private boolean DP = false;

    private static final String nodesTable = "nodesTable";

    private Map<Integer, List<Vector>> gaussiansAtLevel;
    private Map<String, Integer> nodes;
    private List<String> remainderBucket; // for vectors that are not assigned a node
    private List<String> query; // for returning result of a query

    public AIMN(int n, int d, double s, double c, double sensitivity, double epsilon, double delta, DB db)
            throws SQLException {
        this.db = db;
        this.n = n;
        this.d = d;
        this.c = c;
        this.r = s * (1.0 / Math.pow(log(n, 10), 1.0 / 8.0));
        lambda = (2.0 * Math.sqrt(2.0 * c)) / (c * c + 1.0);
        K = Math.sqrt(ln(n));
        k = (int) K;
        alpha = 1.0 - ((r * r) / 2.0); // cosine
        beta = Math.sqrt(1.0 - (alpha * alpha)); // sine
        adjSen = 2.0;
        threshold = (adjSen / epsilon) * ln(1.0 + (Math.exp(epsilon / 2.0) - 1.0) / delta);
        etaU = Math.sqrt((ln(n) / K)) * (lambda / r);
        etaQ = alpha * etaU - 2.0 * beta * Math.sqrt(ln(K));
        double F_etaU = 0.5 * Erf.erfc(etaU / Math.sqrt(2));
        T = (int) (10.0 * ln(K) / F_etaU);

        gaussiansAtLevel = new HashMap<>();
        nodes = new HashMap<>();
        remainderBucket = new ArrayList<>();
        db.initTable(nodesTable);
        generateGaussians();
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

    private void insert(Vector v) throws SQLException {
        int level = 0;
        String path = "R"; // starting point, not stored in DB

        while (level < k) {
            boolean accepted = false;
            List<Vector> gaussians = gaussiansAtLevel.get(level);
            for (int i = 0; i < T; i++) {
                Vector g = gaussians.get(i);
                if (v.dot(g) >= etaU) {
                    path += ":" + i;
                    nodes.put(path, nodes.getOrDefault(path, 0) + 1);
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
        Progress.newStatusBar("Adding noise to counts", nodes.values().size());
        int i = 0;
        for (Integer count : nodes.values()) {
            count = count + (int) noise.TLap(sensitivity, epsilon / adjSen, delta / adjSen);
            if (count <= threshold) {
                count = 0;
            }
            i++;
            Progress.updateStatusBar(i);
        }
        Progress.printAbove("Noise added to " + nodes.values().size() + " counts");
    }

    public int query(Vector q) throws Exception {
        if (q == null)
            throw new IllegalArgumentException("cannot query a null vector");
        if (q.get().length != d)
            throw new IllegalArgumentException(
                    "query dimensionality needs to be the same as data: " + q.get().length + "!= " + d);

        Progress.printAbove("Querying vector: " + q.getLabel());
        query = new ArrayList<>();
        int count = query(q, 0, "R");
        return count;
    }

    private int query(Vector q, int level, String path) throws Exception {
        if (level == k) {
            List<String> vectors = db.getColumnWhereEquals("data", path, nodesTable,
                    "label");
            query.addAll(vectors);
            return nodes.getOrDefault(path, 0); // getOrDefault should not be necessary
        }

        int count = 0;
        List<Vector> gaussians = gaussiansAtLevel.get(level);

        for (int i = 0; i < gaussians.size(); i++) {
            Vector g = gaussians.get(i);
            String nextPath = path + ":" + i;
            // if (!nodes.containsKey(nextPath)) break;
            if (q.dot(g) >= etaQ && nodes.containsKey(nextPath)) {
                count += query(q, level + 1, nextPath);
            }
        }
        return count;
    }

    // calculating dot products once and using cartesian product to fetch leaf nodes
    public int query2(Vector q) throws Exception {
        if (q == null)
            throw new IllegalArgumentException("cannot query a null vector");
        if (q.get().length != d)
            throw new IllegalArgumentException(
                    "query dimensionality needs to be the same as data: " + q.get().length + "!= " + d);

        Progress.printAbove("Querying vector: " + q.getLabel());
        List<Set<Integer>> queryGaussians = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            queryGaussians.add(new HashSet<>());
        }

        // HashMap<Vector, Double> dotProducts = new HashMap<>();
        Progress.newStatusBar("query: calculating dot products", k * T);
        int counter = 0;
        for (int i = 0; i < k; i++) {
            List<Vector> gaussians = gaussiansAtLevel.get(i);
            for (int j = 0; j < gaussians.size(); j++) {
                if (q.dot(gaussians.get(j)) >= etaQ) {
                    queryGaussians.get(i).add(j);
                }
                Progress.updateStatusBar(++counter);
            }
        }
        Progress.clearStatus();

        query = new ArrayList<>();
        int count = 0;
        List<String> queryNodes = CartesianProduct.cp(queryGaussians);
        Progress.newStatusBar("query: getting vectors", queryNodes.size());
        int pg = 0;
        for (String path : queryNodes) {
            List<String> vectors = db.getColumnWhereEquals("data", path, nodesTable,
                    "label");
            query.addAll(vectors);
            count += nodes.getOrDefault(path, 0);
            Progress.updateStatusBar(++pg);
        }
        Progress.clearStatus();
        return count;
    }

    public int query3(Vector q) throws Exception {
        if (q == null)
            throw new IllegalArgumentException("cannot query a null vector");
        if (q.get().length != d)
            throw new IllegalArgumentException("query dimensionality mismatch");
    
        Progress.printAbove("Querying vector: " + q.getLabel());
    
        // precompute which gaussians that accepts q at each level
        List<Set<Integer>> queryGaussians = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Set<Integer> accepted = new HashSet<>();
            List<Vector> gaussians = gaussiansAtLevel.get(i);
            for (int j = 0; j < gaussians.size(); j++) {
                if (q.dot(gaussians.get(j)) >= etaQ) {
                    accepted.add(j);
                }
            }
            if (accepted.isEmpty()) return 0; // prune everything
            queryGaussians.add(accepted);
        }

        query = new ArrayList<>();
    
        // recursively explore paths using precomputed accepted indices
        return query3("R", 0, queryGaussians);
    }
    
    private int query3(String path, int level, List<Set<Integer>> queryGaussians) throws SQLException {
        if (level == k) {
            List<String> vectors = db.getColumnWhereEquals("data", path, nodesTable,
                    "label");
            query.addAll(vectors);
            return nodes.getOrDefault(path, 0);
        }
    
        int count = 0;
        for (int i : queryGaussians.get(level)) {
            String nextPath = path + ":" + i;
            if (nodes.containsKey(nextPath)) {
                count += query3(nextPath, level + 1, queryGaussians);
            }
        }
        return count;
    }

    private void generateGaussians() {
        for (int level = 0; level < k; level++) {
            List<Vector> gaussians = new ArrayList<>();
            for (int i = 0; i < T; i++) {
                gaussians.add(new Vector(d).randomGaussian(random).setLabel("G:" + level + ":" + i));
            }
            gaussiansAtLevel.put(level, gaussians);
        }
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