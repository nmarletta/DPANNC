package dpannc.HY;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import dpannc.Noise;
import dpannc.Vector;

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
    private double ctrl; // multiplier for noise used for partitioning (set to 0 in testing)

    private Cell root;

    public HY(double sensitivity, double epsilon, double delta) {
        this.sensitivity = sensitivity;
        this.epsilon = epsilon;
        this.delta = delta;
        beta = 0.01;
        ctrl = 0;
    }

    public int query(Vector q, double alpha, double Diameter) {
        return root.query(q, alpha, Diameter);
    }

    public void populate(int n, int d, Path filepath) {
        Collection<Vector> dataset = loadfile(n, filepath);
        this.d = d;
        this.n = n;
        nt = n + (4 / epsilon) * log(2 / beta, 10) + Noise.Lap(4 / epsilon);
        H = log(nt, 10);
        // System.out.println("n: " + n + ", d: " + d + ", nt: " + nt + ", H: " + H);
        Box b = new Box(d, dataset);
        root = new Cell(b);
        root.recShrink(0, beta);
    }

    public static void main(String[] args) {
        Collection<Vector> dataset = new ArrayList<>();
        dataset.add(new Vector(new double[] { -9.0, 10.0 }));
        dataset.add(new Vector(new double[] { 9.0, -10.0 }));
        dataset.add(new Vector(new double[] { 5.0, 5.0 }));
        dataset.add(new Vector(new double[] { -5.0, -5.0 }));
        dataset.add(new Vector(new double[] { -2.0, -3.0 }));
        dataset.add(new Vector(new double[] { 4.0, -6.0 }));
        dataset.add(new Vector(new double[] { 7.0, -2.0 }));

        Box rootBox = new Box(2, dataset);
        // System.out.println("root size: " + rootBox.size);
        HY tree = new HY(1.0, 0.5, 0.01);
        tree.setNoiseCtrl(0);
        HY.Cell root = tree.new Cell(rootBox);
        double eh = Math.pow(3.0 / 4.0, tree.H - 0) * tree.epsilon / 100;
        Box[] result = root.shrink(0, 0.01);
        System.out.println(result[0].toString());
        System.out.println(result[1].toString());

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
        }

        public Cell(Box outerBox, Box innerBox) {
            left = right = null;
            outer = outerBox;
            inner = innerBox;
            count = outer.count - inner.count;
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
            if (outer.isSubsetOfBall(q, radius - alpha * radius * 2))
                return count;
            if (isLeaf)
                return 0;

            int result = 0;
            if (left != null) result += left.query(q, alpha, radius);
            if (right != null) result += right.query(q, alpha, radius);
            return result;
        }

        public void recShrink(int h, double beta) {
            Box[] shrinkResult = null;
            int i = 1;
            while (shrinkResult == null) {
                shrinkResult = shrink(h + i, beta);
                i++;
            }

            if (shrinkResult[1] == null) {
                outer = shrinkResult[0];
                return;
            }

            left = new Cell(shrinkResult[0], shrinkResult[1]); // outer minus inner
            right = new Cell(shrinkResult[1]); // inner

            if (left.count > 1 && left.size > 1)
                left.split(h);
            if (right.count > 1 && right.size > 1)
                right.split(h);
        }

        public void split(int h) {
            Box[] split = outer.split();
            left = new Cell(split[0]);
            right = new Cell(split[1]);

            if (left.count > 1 && left.size > 1) {
                left.recShrink(h, beta);
            }

            if (right.count > 1 && right.size > 1) {
                right.recShrink(h, beta);
            }
        }

        public Box[] shrink(int h, double beta) {
            double eh = Math.pow(3.0 / 4.0, H - h) * epsilon / 100;
            double R = count + (Noise.Lap(1 / eh) * ctrl);
            if (R < 370 * (log((2 * nt * d) / (beta * delta), 10) + log(log(u, 10), 10)) / eh) {
                return null;
            }
            double T = (2.0 / 3.0) * count + (Noise.Lap(1 / eh) * ctrl);
            Box bl = null, br = null, bc = outer, newOuter = outer.emptyClone();
            while (bc.count + (Noise.Lap(1.0 / eh) * ctrl) >= T && bc.size > 1.0) {
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
                bc = bl.count + (Noise.Lap(1 / eh) * ctrl) > br.count + (Noise.Lap(1 / eh) * ctrl) ? bl : br;
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

    public void setNoiseCtrl(double val) {
        ctrl = val;
    }

    public Collection<Vector> loadfile(int n, Path filepath) {
        Collection<Vector> data = new ArrayList<Vector>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath.toAbsolutePath().toString()))) {
            String line;
            int counter = 0;
            while ((line = reader.readLine()) != null && counter < n) {
                Vector vector = Vector.fromString(line);
                data.add(vector);
                // printProgress(counter, n, 10);
                counter++;
            }
            // root.addNoise();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Insertion complete.");
        return data;
    }
}
