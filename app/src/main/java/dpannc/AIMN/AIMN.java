package dpannc.AIMN;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import dpannc.Vector;
import dpannc.Noise;
import dpannc.database.DB;

public class AIMN {
        int n;
        int d;
    
        double sensitivity;
        double epsilon;
        double delta;
        int T, remainder;
        double c, lambda, r, K, alpha, beta, adjSen, threshold, etaU, etaQ;
        Node root;
        boolean saveData;

        boolean usePredeterminedNodes = false;
        Map<String, Vector> nodes;

        public AIMN(double sensitivity, double epsilon, double delta) {
            this.sensitivity = sensitivity;
            this.epsilon = epsilon;
            this.delta = delta;
            saveData = true;
        }
    
        public void updateParameters(int n, int d, double c, double epsilon, double delta) {
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
    
            root = new Node(0, "0");
            printSettings();
        }
    
        public class Node {
            private int level;
            private int count;
            private int noisyCount;
            private boolean isBucket;
            private String id;
            private Vector g;
            protected List<Node> childNodes;
    
            public Node(int level, String id) {
                this.level = level;
                this.isBucket = (level >= K);
                this.count = 0;
                this.noisyCount = 0;
                this.id = id;
                if (usePredeterminedNodes) {
                    g = nodes.getOrDefault(id, new Vector(T).randomGaussian());
                } else {
                    g = new Vector(T).randomGaussian();
                }
                childNodes = new ArrayList<>();
            }
    
            public void insertPoint(Vector v) {
                if (isBucket) {
                    if (saveData) {
                        DB.insertToVectors(v.getLabel(), v.toString(), id);
                    }
                    count++;
                } else {
                    if (!sendToChildNode(v)) {
                        DB.insertToVectors(v.getLabel(), v.toString(), id + ":R");
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
                    // childNodes.add(n);
                    if (n.accepts(v, etaU)) {
                        childNodes.add(n);
                        n.insertPoint(v);
                        return true;
                    }
                }
                return false;
            }
    
            public int query(Vector q) {
                if (isBucket) {
                    if (saveData) {
                        DB.insertToQueries(id(), q.getLabel(), q.toString());
                    }
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
                if (level >= K) {
                    noisyCount = count + (int) Noise.TLap(sensitivity, epsilon / adjSen, delta / adjSen);
                    if (noisyCount <= threshold) {
                        noisyCount = 0;
                    }
                    // System.out.println("nodeID: " + id + ", count: " + count + ", ncount: " + noisyCount);
                } else {
                    for (Node n : childNodes) {
                        n.addNoise();
                    }
                }
            }
    
            private boolean accepts(Vector v, double etaU) {
                return g.dot(v) >= etaU;
            }
    
            public List<Node> getChildNodes() {
                return childNodes;
            }
    
            public Vector gaussian() {
                return g;
            }
    
            public boolean isBucket() {
                return isBucket;
            }
    
    
            public String id() {
                return id;
            }
    
            public int count() {
                return count;
            }
        }
    
        public void usePreNode(Map<String, Vector> nodes) {
            usePredeterminedNodes = true;
            this.nodes = nodes;
        }

        public void populate(int n, int d, Path filePath) throws Exception {
            if (n < 1)
                throw new Exception("Invalid n");
            if (d < 2)
                throw new Exception("Invalid d");
    
            this.n = n;
            this.d = d;
    
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toAbsolutePath().toString()))) {
    
                String line;
                int counter = 0;
                while ((line = reader.readLine()) != null && counter < n) {
                    Vector vector = Vector.fromString(line);
                    root.insertPoint(vector);
                    printProgress(counter, n, 10);
                    counter++;
                }
                // root.addNoise();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Insertion complete.");
            if (saveData) {
                System.out.println("Saving nodes to DB...");
                saveTreeToDB(root);
                System.out.println("Saving complete.");
            }
        }
    
        public int query(Vector q) {
            return root.query(q);
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
    
        // public int sizePercentage() {
        // return (int) (100 / Math.pow(T, K) * nodeCount);
        // }
    
        public void saveTreeToDB(Node n) {
            DB.insertToNodes(n.id(), n.isBucket(), n.gaussian().toString(), n.count());
            for (Node c : n.getChildNodes()) {
                // if (!c.isBucket) {
                    saveTreeToDB(c);
                // }
            }
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

    // public void printAcc() {
    //     System.out.println("" + Math.pow(n, 1+p)); n−1+p+o(1)
    // }
}