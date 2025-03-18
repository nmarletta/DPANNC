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

    private Cell root;

    public HY(double sensitivity, double epsilon, double delta) {
        this.sensitivity = sensitivity;
        this.epsilon = epsilon;
        this.delta = delta;
        beta = 0.01;
    }

    public void populate(int n, int d, Path filepath) {
        Collection<Vector> dataset = loadfile(n, filepath);
        this.d = d;
        this.n = n;
        nt = n + (4 / epsilon) * log(2 / beta, 10) + Noise.Lap(4 / epsilon);
        H = log(nt, 10);

        Box b = new Box(d, dataset);
        root = new Cell(b);
        root.shrink(0, beta);
    }

    private class Cell {
        double size; // length of the longest side of the outer box
        Box inner;
        Box outer;
        int count;

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

        public void split(int h) {
            Box[] split = outer.split();
            left = new Cell(split[0]);
            right = new Cell(split[1]);

            if (left.count > 1 && left.size > 1) {
                left.shrink(h, beta);
            }

            if (right.count > 1 && right.size > 1) {
                right.shrink(h, beta);
            }
        }

        public void shrink(int hVal, double beta) {
            int h = hVal + 1;
            double eh = Math.pow(3 / 4, H - h) * epsilon / 100;
            double R = count + Noise.Lap(1 / eh);
            if (R < 370 * (log((2 * nt * d) / (beta * delta), 10) + log(log(u, 10), 10)) / eh) {
                return;
            }
            double T = (2 / 3) * count + Noise.Lap(1 / eh);
            Box bl = null, br = null, bc = outer;

            while (bc.count + Noise.Lap(1 / eh) >= T && bc.size > 1) {
                Box[] split = outer.split();
                bl = split[0];
                br = split[1];
                bc = (bl.count > br.count) ? bl : br;
                if (inner != null && bc.equals(inner)) {
                    // if there's ALREADY an inner box && bc is shrunk into the same size as inner
                    return; // leaf
                }
            }

            if (bl != null && br != null) {
                bc = bl.count + Noise.Lap(1 / eh) > br.count + Noise.Lap(1 / eh) ? bl : br;
            }

            left = new Cell(outer, bc);
            right = new Cell(bc);

            if (left.count > 1 && left.size > 1)
                left.split(h);
            if (right.count > 1 && right.size > 1)
                right.split(h);
        }
    }

    private class Box {
        int d;
        int count;
        double size;
        Collection<Vector> data;
        double[] min;
        double[] max;

        public Box(double[] min, double[] max) {
            count = 0;
            this.min = min;
            this.max = max;
            d = min.length;
        }

        public Box(int d, Collection<Vector> dataset) {
            if (dataset.isEmpty()) {
                throw new IllegalArgumentException("Dataset cannot be empty");
            }

            // int d = dataset.iterator().next().getDimensions(); // alternativt

            min = new double[d];
            max = new double[d];

            Vector first = dataset.iterator().next();
            for (int i = 0; i < d; i++) {
                min[i] = first.getC(i);
                max[i] = first.getC(i);
            }

            for (Vector v : dataset) {
                for (int i = 1; i < v.getDimensions(); i++) {
                    min[i] = Math.min(min[i], v.getC(i));
                    max[i] = Math.max(max[i], v.getC(i));
                }
            }

            count = dataset.size();
            data = new ArrayList<>(dataset);
        }

        public int longestIndex() {
            double currLongest = 0;
            int longestIndex = 0;
            for (int i = 0; i < d; i++) {
                double length = max[i] - min[i];
                if (length > currLongest) {
                    longestIndex = i;
                    currLongest = length;
                }
            }
            return longestIndex;
        }

        public Box[] split() {
            int splitIndex = longestIndex();
            double splitValue = (max[splitIndex] - min[splitIndex]) / 2;
            Box leftBox = this.clone().setMax(splitIndex, splitValue);
            Box rightBox = this.clone().setMin(splitIndex, splitValue);

            for (Vector v : data) {
                if (v.getC(splitIndex) <= splitValue) {
                    leftBox.addPoint(v);
                } else {
                    rightBox.addPoint(v);
                }
            }

            leftBox.count = leftBox.data.size();
            rightBox.count = rightBox.data.size();

            return new Box[] { leftBox, rightBox };
        }

        public void addPoint(Vector v) {
            data.add(v);
            count++;
        }

        public Box setMin(int index, double value) {
            min[index] = value;
            return this;
        }

        public Box setMax(int index, double value) {
            max[index] = value;
            return this;
        }

        public Box clone() {
            Box copy = new Box(Arrays.copyOf(min, d), Arrays.copyOf(max, d));
            copy.count = this.count;
            copy.size = this.size;

            // if (this.data != null) {
            // copy.data = new ArrayList<>(this.data);
            // }
            return copy;
        }

        public boolean equals(Box box) {
            double margin = 1e-9;
            for (int i = 0; i < d; i++) {
                if (Math.abs(min[i] - box.min[i]) > margin || Math.abs(max[i] - box.max[i]) > margin) {
                    return false;
                }
            }
            return true;
        }
    }

    public static double log(double N, int base) {
        return Math.log(N) / Math.log(base);
    }

    public static double ln(double N) {
        return Math.log(N);
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

// public Box shrink(int h, double beta) {
// double eh = Math.pow(3 / 4, H - h) * epsilon / 100;
// double R = count + Noise.Lap(1 / eh);
// if (R < 370 * (log((2 * nt * d) / (beta * delta), 10) + log(log(u, 10), 10))
// / eh) {
// return null;
// }
// double T = (2 / 3) * count + Noise.Lap(1 / eh);

// Box bl = null;
// Box br = null;
// Box bc = outer;

// while (bc.count + Noise.Lap(1 / eh) >= T && bc.size > 1) {
// Box[] split = outer.split();
// bl = split[0];
// br = split[1];
// bc = (bl.count > br.count) ? bl : br;
// if (inner != null && bc.equals(inner)) {
// return outer;
// }
// }

// if (bl != null && br != null) {
// bc = bl.count + Noise.Lap(1 / eh) > br.count + Noise.Lap(1 / eh) ? bl : br;
// }

// return bc;
// }
