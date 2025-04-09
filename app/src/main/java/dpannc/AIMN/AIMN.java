package dpannc.AIMN;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

import dpannc.Vector;
import dpannc.database.DB;
import dpannc.database.DBiterator;
import dpannc.Noise;

public class AIMN {
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
    boolean DP = true;

    Map<String, Vector> nodes; // ID (path) -> gaussian vectors
    Map<String, List<String>> buckets; // ID (path) -> list of vectors
    List<Node> leafNodes; // for quicker access to leafs when adding noise
    List<String> remainderBucket; // for vectors that are not assigned a node
    List<String> query; // for returning result of a query

    public AIMN(int n, int d, double r, double c, double sensitivity, double epsilon, double delta) {
        remainder = 0;
        this.n = n;
        this.d = d;
        this.c = c;
        this.r = r;
        // r = 1 / Math.pow(log(n, 10), 1.0 / 8.0);
        lambda = (2 * Math.sqrt(2 * c)) / (c * c + 1);
        K = Math.sqrt(ln(n));
        alpha = 1 - ((r * r) / 2); // cosine
        beta = Math.sqrt(1 - (alpha * alpha)); // sine
        adjSen = 2;
        threshold = (adjSen / epsilon) * ln(1 + (Math.exp(epsilon / 2) - 1) / delta);
        etaU = Math.sqrt((ln(n) / K)) * (lambda / r);
        etaQ = alpha * etaU - 2 * beta * Math.sqrt(ln(K));
        T = (int) (10 * ln(K) / (Math.exp(-Math.pow(n, 2)/2)));
        root = new Node(0, "0");
        printSettings();

        nodes = new HashMap<String, Vector>();
        buckets = new HashMap<String, List<String>>();
        leafNodes = new ArrayList<Node>();
        remainderBucket = new ArrayList<String>();
    }

    public void populateFromDB(String table, DB db) throws SQLException {
        try (DBiterator it = db.iterator(table)) {
            int counter = 0;
            while (it.hasNext()) {
                Vector v = it.next();
                root.insertPoint(v);
                printProgress(counter, n, 10);
                counter++;
            }
            if (DP)
                addNoise();
        }
    }

    public void populateFromFile(int n, int d, Path filePath) throws Exception {
        if (n < 1)
            throw new Exception("Invalid n");
        if (d < 2)
            throw new Exception("Invalid d");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toAbsolutePath().toString()))) {
            String line;
            int counter = 0;
            while ((line = reader.readLine()) != null && counter < n) {
                String label = line.substring(0, line.indexOf(' '));
                String data = line.substring(line.indexOf(' ') + 1);
                Vector vector = Vector.fromString(label, data);
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
        if (q == null)
            throw new IllegalArgumentException("cannot query a null vector");
        if (q.get().length != d)
            throw new IllegalArgumentException(
                    "query dimensionality needs to be the same as data: " + q.get().length + "!= " + d);
        query = new ArrayList<String>();
        Vector c = q.clone().normalize();
        return root.query(c);
    }

    public List<String> queryList() throws Exception {
        if (query.isEmpty()) throw new Exception("query list is empty");
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

    public void DP(boolean b) {
        DP = b;
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
        System.out.println("d: " + d);
        System.out.println("c: " + c);
        System.out.println("lambda: " + lambda);
        System.out.println("r: " + r);
        System.out.println("K: " + K);
        System.out.println("alpha: " + alpha);
        System.out.println("beta: " + beta);
        System.out.println("threshold: " + threshold);
        System.out.println("etaU: " + etaU);
        System.out.println("etaQ: " + etaQ);
        System.out.println("T: " + T);
    }

    public class Node {
        private int level;
        private int count;
        private int noisyCount;
        private boolean isLeaf;
        private String id;
        private Vector g;
        protected List<Node> childNodes;

        public Node(int level, String id) {
            this.level = level;
            this.isLeaf = (level >= K);
            this.count = 0;
            this.noisyCount = 0;
            this.id = id;

            g = new Vector(d).randomGaussian(random);

            childNodes = new ArrayList<>();
        }

        public void insertPoint(Vector v) {
            if (isLeaf) {
                buckets.computeIfAbsent(id, k -> new ArrayList<>()).add(v.getLabel());
                count++;
            } else {
                if (!sendToChildNode(v)) {
                    remainder++;
                }
            }
        }

        private boolean sendToChildNode(Vector v) {
            for (int i = 0; i < childNodes.size(); i++) {
                Node n = childNodes.get(i);
                if (n.accepts(v, etaU)) {
                    n.insertPoint(v);
                    return true;
                }
            }

            while (childNodes.size() < T) {
                int currID = childNodes.size();// < 1 ? 0 : childNodes.size();
                Node n = new Node(level + 1, id + ":" + currID);
                childNodes.add(n);
                if (n.accepts(v, etaU)) {
                    n.insertPoint(v);
                    return true;
                }
            }
            return false;
        }

        public int query(Vector q) {
            if (isLeaf) {
                if (count > 0) {
                    query.addAll(buckets.get(id));
                }
                if (count < 1 && buckets.get(id) != null) System.out.println("ggg");
                return count;
            } else {
                int total = 0;
                for (Node n : childNodes) {
                    if (n.accepts(q, etaQ)) {
                        total += n.query(q);
                    }
                }
                return total;
            }
        }

        public void addNoise() {
            count = count + (int) noise.TLap(sensitivity, epsilon / adjSen, delta / adjSen);
            if (noisyCount <= threshold) {
                noisyCount = 0;
            }
        }

        private boolean accepts(Vector v, double etaU) {
            return g.dot(v) >= etaU;
        }

        // public List<Node> getChildNodes() {
        // return childNodes;
        // }

        public Vector gaussian() {
            return g;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public String id() {
            return id;
        }

        public int count() {
            return count;
        }
    }
}