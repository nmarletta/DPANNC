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

public class AIMNclean {
    private DB db;
    private int n;
    private int d;
    private double sensitivity, epsilon, delta;
    private int T, k;
    private double c, lambda, r, K, alpha, beta, adjSen, threshold, etaU, etaQ;
    private int remainder;

    private Random random = new Random(100);
    private Noise noise = new Noise(100);
    private boolean DP = false;

    private Map<Integer, List<Vector>> gaussiansAtLevel;
    private Map<String, Integer> nodes;

    public AIMNclean(int n, int d, double s, double c, double sensitivity, double epsilon, double delta, DB db)
            throws SQLException {
        this.db = db;
        this.n = n;
        this.d = d;
        this.c = c;
        this.r = 1.0 / Math.pow(log(n, 10), 1.0 / 8.0);
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
        generateGaussians();
    }

    public void populateFromDB(String table) throws Exception {
        Progress.newStatusBar("Loading vectors into AIMN", n);
        remainder = 0;
        try (DBiterator it = db.iterator(table)) {
            int counter = 0;
            while (it.hasNext()) {
                Vector v = it.next();
                insert(v);
                counter++;
                Progress.updateStatusBar(counter);
            }
            Progress.printAbove(n - remainder + " vectors succesfully loaded into AIMN ("
                    + remainder + " in remainder)");
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
                remainder++;
                return;
            }
        }
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
        int count = query(q, 0, "R");
        return count;
    }

    private int query(Vector q, int level, String path) throws Exception {
        if (level == k) {
            return nodes.getOrDefault(path, 0); // getOrDefault should not be necessary
        }

        int count = 0;
        List<Vector> gaussians = gaussiansAtLevel.get(level);

        for (int i = 0; i < gaussians.size(); i++) {
            Vector g = gaussians.get(i);
            String nextPath = path + ":" + i;
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
        for (int i = 0; i < k; i++) {
            queryGaussians.add(new HashSet<>());
        }

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

        // Convert sets to lists for indexable Cartesian product
        List<List<Integer>> queryGaussiansList = new ArrayList<>();
        for (Set<Integer> set : queryGaussians) {
            if (set.isEmpty())
                return 0; // early exit — empty Cartesian product
            queryGaussiansList.add(new ArrayList<>(set));
        }

        int[] indices = new int[queryGaussians.size()];
        int count = 0;
        boolean done = false;

        while (!done) {
            StringBuilder sb = new StringBuilder("R");
            for (int i = 0; i < indices.length; i++) {
                sb.append(":").append(queryGaussiansList.get(i).get(indices[i]));
            }
            String path = sb.toString();
            count += nodes.getOrDefault(path, 0);

            // Move to next combination
            int pos = indices.length - 1;
            while (pos >= 0) {
                indices[pos]++;
                if (indices[pos] < queryGaussians.get(pos).size()) {
                    break;
                }
                indices[pos] = 0;
                pos--;
            }
            if (pos < 0)
                done = true;
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

    public int remainder() {
        return remainder;
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