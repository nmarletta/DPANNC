// package dpannc.HY;

// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.IOException;
// import java.nio.file.Path;
// import java.util.*;

// import dpannc.Noise;
// import dpannc.Vector;

// public class HY {
//     private double sensitivity;
//     private double epsilon;
//     private double delta;
//     private int n;
//     private int d; // dimensions
//     private double u; // largest coordinate in the dataset
//     private double H; // max height of tree
//     private double beta; // failure probability
//     private double nt;

//     private Cell root;

//     public HY(double sensitivity, double epsilon, double delta) {
//         this.sensitivity = sensitivity;
//         this.epsilon = epsilon;
//         this.delta = delta;
//         beta = 0.01;
//     }

//     public void populate(int n, int d, Path filepath) {
//         Collection<Vector> dataset = loadfile(n, filepath);
//         this.d = d;
//         this.n = n;
//         nt = n + (4 / epsilon) * log(2 / beta, 10) + Noise.Lap(4 / epsilon);
//         H = log(nt, 10);
//         System.out.println("n: " + n + ", d: " + d + ", nt: " + nt + ", H: " + H);
//         Box b = new Box(d, dataset);
//         root = new Cell(b);
//         root.shrink(0, beta);
//     }

//     public void print() {
//         root.printTree("");
//     }

//     public class Cell {
//         double size; // length of the longest side of the outer box
//         Box inner;
//         Box outer;
//         int count;

//         Cell left;
//         Cell right;

//         public Cell(Box box) {
//             left = right = null;
//             outer = box;
//             inner = null;
//             count = box.count;
//         }

//         public Cell(Box outerBox, Box innerBox) {
//             left = right = null;
//             outer = outerBox;
//             inner = innerBox;
//             count = outer.count - inner.count;
//         }

//         public double size() {
//             return size;
//         }

//         public int count() {
//             return count;
//         }

//         public void split(int h) {
//             Box[] split = outer.split();
//             left = new Cell(split[0]);
//             right = new Cell(split[1]);

//             if (left.count > 1 && left.size > 1) {
//                 left.shrink(h, beta);
//             }

//             if (right.count > 1 && right.size > 1) {
//                 right.shrink(h, beta);
//             }
//         }

//         public void shrink(int hVal, double beta) {
//             int h = hVal + 1;
//             double eh = Math.pow(3.0 / 4.0, H - h) * epsilon / 100;
//             double R = count + Noise.Lap(1 / eh);
//             System.out.println("h: " + h + ", eh: " + eh + ", R: " + R);
//             if (R < 370 * (log((2 * nt * d) / (beta * delta), 10) + log(log(u, 10), 10)) / eh) {
//                 System.out.println("true R");
//                 return;
//             }
//             System.out.println("false R");
//             double T = (2.0 / 3.0) * count + Noise.Lap(1 / eh);
//             Box bl = null, br = null, bc = outer;
//             System.out.println("T: " + T + ", count: " + count);
//             while (bc.count + Noise.Lap(1.0 / eh) >= T && bc.size > 1.0) {
//                 Box[] split = outer.split();
//                 bl = split[0];
//                 br = split[1];
//                 bc = (bl.count > br.count) ? bl : br;
//                 if (inner != null && bc.equals(inner)) {
//                     // if there's ALREADY an inner box && bc is shrunk into the same size as inner
//                     return; // leaf
//                 }
//             }

//             if (bl != null && br != null) {
//                 bc = bl.count + Noise.Lap(1 / eh) > br.count + Noise.Lap(1 / eh) ? bl : br;
//             }

//             left = new Cell(outer, bc);
//             right = new Cell(bc);

//             if (left.count > 1 && left.size > 1)
//                 left.split(h);
//             if (right.count > 1 && right.size > 1)
//                 right.split(h);
//         }

//         public void printTree(String path) {
//             if (left != null) left.printTree(path + "L");
//             if (right != null) right.printTree(path + "R");
//             if (right == null && left == null) System.out.println(path + " - " + count);
//         }
//     }

//     public static double log(double N, int base) {
//         return Math.log(N) / Math.log(base);
//     }

//     public static double ln(double N) {
//         return Math.log(N);
//     }

//     public Collection<Vector> loadfile(int n, Path filepath) {
//         Collection<Vector> data = new ArrayList<Vector>();
//         try (BufferedReader reader = new BufferedReader(new FileReader(filepath.toAbsolutePath().toString()))) {
//             String line;
//             int counter = 0;
//             while ((line = reader.readLine()) != null && counter < n) {
//                 Vector vector = Vector.fromString(line);
//                 data.add(vector);
//                 // printProgress(counter, n, 10);
//                 counter++;
//             }
//             // root.addNoise();
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//         System.out.println("Insertion complete.");
//         return data;
//     }
// }
