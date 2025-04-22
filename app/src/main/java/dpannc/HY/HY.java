package dpannc.HY;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

import dpannc.Noise;
import dpannc.Vector;
import dpannc.database.DB;
import dpannc.database.DBiterator;

public class HY {
    private double sensitivity;
    private double epsilon;
    private double delta;
    private int n;
    private int d; // dimensions
    private double u; // largest coordinate in the dataset
    private double H; // max height of tree
    private double beta; // failure probability
    private double nt;

    private boolean DP = false; // differential privacy
    private Random random = new Random(100);
    private Noise noise = new Noise(100);
    private Cell root;

    private Map<String, Integer> nodes;
    private List<String> query;

    public HY(double sensitivity, double epsilon, double delta, DB db) {
        this.sensitivity = sensitivity;
        this.epsilon = epsilon;
        this.delta = delta;
        beta = 0.01;
    }

    public int query(Vector q, double alpha, double radius) {
        query = new ArrayList<String>();
        return root.query(q, alpha, radius);
    }

    public List<String> queryList() throws Exception {
        // if (query.isEmpty()) throw new Exception("query list is empty");
        return query;
    }

    public void populateFromDB(int n, int d, String table, DB db) throws Exception {
        List<Vector> dataset = new ArrayList<Vector>();
        this.d = d;
        this.n = n;
        nt = DP ? n + (4 / epsilon) * log(2 / beta, 10) + noise.Lap(4 / epsilon) : n;
        H = log(nt, 10);

        DBiterator it = db.iterator(table);
        int counter = 0;
        while (it.hasNext()) {
            Vector v = it.next();
            dataset.add(v);
            counter++;
        }

        Box b = new Box(d, dataset);
        root = new Cell(b);
        root.recShrink(0, beta);
        // System.out.println("Data structure complete...");
    }

    public void populateFromFile(int n, int d, Path filepath) {
        Collection<Vector> dataset = loadfile(n, filepath);
        this.d = d;
        this.n = n;
        nt = DP ? n + (4 / epsilon) * log(2 / beta, 10) + noise.Lap(4 / epsilon) : n; 
        H = log(nt, 10);
        Box b = new Box(d, dataset);
        root = new Cell(b);
        root.recShrink(0, beta);
        // System.out.println("Data structure complete...");
    }

    public void DP(boolean b) {
        DP = b;
    }

    public void print() {
        root.printTree("");
    }

    public class Cell {
        double size; // length of the longest side of the outer box
        Box inner;
        Box outer;
        int count;
        boolean isLeaf;
        Cell left;
        Cell right;

        public Cell(Box box) {
            left = right = null;
            outer = box;
            inner = null;
            count = box.count;
            size = box.size;
            isLeaf = count < 1 || size < 1 ? true : false;
        }

        public Cell(Box outerBox, Box innerBox) {
            left = right = null;
            outer = outerBox;
            inner = innerBox;
            count = outer.count;
            size = outer.size;
            isLeaf = count < 1 || size < 1 ? true : false;
        }

        public double size() {
            return size;
        }

        public int count() {
            return count;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public int query(Vector q, double alpha, double radius) {
            if (!outer.intersectsBall(q, radius - alpha * radius * 2))
                return 0;
            if (outer.isSubsetOfBall(q, radius - alpha * radius * 2)) {
                query.addAll(outer.list());
                return count;
            }
            if (isLeaf)
                return 0;

            int result = 0;
            if (left != null)
                result += left.query(q, alpha, radius);
            if (right != null)
                result += right.query(q, alpha, radius);
            return result;
        }

        public void recShrink(int h, double beta) {
            if (isLeaf) return;
            Box[] shrinkResult = null;
            int i = 0;
            while (shrinkResult == null) {
                i++;
                shrinkResult = shrink(h + i, beta);
            }

            if (shrinkResult[1] == null) {
                outer = shrinkResult[0];
                return;
            }

            left = new Cell(shrinkResult[0], shrinkResult[1]); // outer minus inner
            right = new Cell(shrinkResult[1]); // inner
            left.split(h+i);
            right.split(h+i);
        }

        public void split(int h) {
            if (isLeaf) return;
            Box[] split = outer.split();
            left = new Cell(split[0]);
            right = new Cell(split[1]);
            left.recShrink(h, beta);
            right.recShrink(h, beta);
        }

        public Box[] shrink(int h, double beta) {
            double eh = Math.pow(3.0 / 4.0, H - h) * epsilon / 100;
            double R = DP ? count + noise.Lap(1 / eh) : count;
            if (R < 370 * (log((2 * nt * d) / (beta * delta), 10) + log(log(u, 10), 10)) / eh) {
                return null;
            }

            double T = DP ? (2.0 / 3.0) * count + noise.Lap(1 / eh) : (2.0 / 3.0) * count;
            Box bl = null, br = null, bc = outer, newOuter = outer.emptyClone();

            while (getNoisyCount(bc, DP, 1 / eh) >= T && bc.size > 1.0) {
                Box[] split = bc.split();
                bl = split[0];
                br = split[1];

                // bc becomes the majority box
                if (bl.count > br.count) {
                    bc = bl;
                    newOuter.addData(br.data);
                } else {
                    bc = br;
                    newOuter.addData(bl.data);
                }

                // if there's ALREADY an inner box && bc is shrunk into the same size as inner
                if (inner != null && bc.equals(inner)) {
                    return new Box[] { outer, null }; // leaf
                }
            }

            if (bl != null && br != null) {
                bc = getNoisyCount(bl, DP, 1 / eh) > getNoisyCount(br, DP, 1 / eh) ? bl : br;
            }

            return new Box[] { newOuter, bc };
        }

        public void printTree(String path) {
            if (left != null)
                left.printTree(path + "L");
            if (right != null)
                right.printTree(path + "R");
            if (right == null && left == null)
                System.out.println(path + " - " + count);
        }
    }

    public static double log(double N, int base) {
        return Math.log(N) / Math.log(base);
    }

    public static double ln(double N) {
        return Math.log(N);
    }

    private double getNoisyCount(Box b, boolean DP, double val) {
        return DP ? b.count + noise.Lap(val) : b.count;
    }

    public Collection<Vector> loadfile(int n, Path filepath) {
        Collection<Vector> vectors = new ArrayList<Vector>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath.toAbsolutePath().toString()))) {
            String line;
            int counter = 0;
            while ((line = reader.readLine()) != null && counter < n) {
                String label = line.substring(0, line.indexOf(' '));
                String data = line.substring(line.indexOf(' ') + 1);
                Vector vector = Vector.fromString(label, data);
                vectors.add(vector);
                // printProgress(counter, n, 10);
                counter++;
            }
            // root.addNoise();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Vectors loaded...");
        return vectors;
    }
}
