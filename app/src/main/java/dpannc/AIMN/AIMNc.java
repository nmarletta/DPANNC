package dpannc.AIMN;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import dpannc.Vector;
import dpannc.Noise;

public class AIMNc {
    int n;
    int d;
    double sensitivity;
    double epsilon;
    double delta;
    int T, remainder;
    double c, lambda, r, K, alpha, beta, adjSen, threshold, etaU, etaQ;
    Node root;

    Random random = new Random(100);
    Noise noise = new Noise(100);
    boolean DP = false;

    Map<String, Vector> nodes; // ID (path) -> gaussian vectors
    Map<String, List<String>> buckets; // ID (path) -> list of vectors
    List<Node> leafNodes; // for quicker access to leafs when adding noise
    List<String> remainderBucket; // for vectors that are not assigned a node
    List<String> query; // for returning result of a query

    public AIMNc(int n, int d, double c, double sensitivity, double epsilon, double delta) {
        remainder = 0;
        this.n = n;
        T = d;
        this.c = c;
        lambda = (2 * Math.sqrt(2 * c)) / (c * c + 1);
        r = 1 / Math.pow(log(n, 10), 1.0 / 8.0);
        K = Math.sqrt(ln(n));
        alpha = 1 - ((r * r) / 2); // cosine
        beta = Math.sqrt(1 - (alpha * alpha)); // sine
        adjSen = 2;
        threshold = (adjSen / epsilon) * ln(1 + (Math.exp(epsilon / 2) - 1) / delta);
        etaU = Math.sqrt((ln(n) / K)) * (lambda / r);
        etaQ = alpha * etaU - 2 * beta * Math.sqrt(ln(K));

        root = new Node(0, "0", this);
        printSettings();

        nodes = new HashMap<String, Vector>();
        buckets = new HashMap<String, List<String>>();
        leafNodes = new ArrayList<Node>();
        remainderBucket = new ArrayList<String>();
    }

    public void populate(int n, int d, Path filePath) throws Exception {
        if (n < 1)
            throw new Exception("Invalid n");
        if (d < 2)
            throw new Exception("Invalid d");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toAbsolutePath().toString()))) {
            String line;
            int counter = 0;
            while ((line = reader.readLine()) != null && counter < n) {
                Vector vector = Vector.fromString(line);
                root.insertPoint(vector);
                printProgress(counter, n, 10);
                counter++;
            }
            if (DP)
                addNoise();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Insertion complete.");
    }

    private void addNoise() {
        for (Node n : leafNodes) {
            n.addNoise();
        }
    }

    public int query(Vector q) {
        if (q == null) throw new IllegalArgumentException("cannot query a null vector");
        if (q.get().length != d) throw new IllegalArgumentException("query dimensionality needs to be the same as data");
        query = new ArrayList<String>();
        return root.query(q);
    }

    public List<String> queryList() throws Exception {
        if (query.isEmpty()) throw new Exception("run 'query(Vector)' first");
        return query;
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

    private void printProgress(int c, int n, int step) {
        double progress = (double) c / n * 100;
        double margin = 1e-9;
        if (Math.abs(progress % step) < margin) {
            System.out.println((int) progress + "% completed");
        }
    }

    public static double log(double N, int base) {
        return Math.log(N) / Math.log(base);
    }

    public static double ln(double N) {
        return Math.log(N);
    }

    public void printSettings() {
        System.out.println("n: " + n);
        System.out.println("c: " + c);
        System.out.println("lambda: " + lambda);
        System.out.println("r: " + r);
        System.out.println("K: " + K);
        System.out.println("alpha: " + alpha);
        System.out.println("beta: " + beta);
        System.out.println("threshold: " + threshold);
        System.out.println("etaU: " + etaU);
        System.out.println("etaQ: " + etaQ);
    }
}