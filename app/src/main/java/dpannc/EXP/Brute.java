package dpannc.EXP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import dpannc.Vector;


public class Brute {

    int n, d;
    double c, r;
    List<Vector> vectors;

    public Brute(int n, int d, double c, double r) {
        this.n = n;
        vectors = new ArrayList<>();
        this.d = d;
        this.c = c;
        this.r = r;
    }

    public void insert(Vector v) {
        vectors.add(v);
    }

    public int query(Vector q, Path filePath) {
        int counter = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toAbsolutePath().toString()))) {
            String line;
            while ((line = reader.readLine()) != null && counter < n) {
                Vector v = Vector.fromString(line).normalize();
                if (v.get().length != d) System.out.println("brut not d: " + v.getLabel() + " : " + v.get().length + ", D: " + d);
                double distance = q.normalize().distance(v.normalize());
                if (distance <= r) {
                    counter++;
                }

                // printProgress(counter, n, 10);
                counter++;
            }
            // root.addNoise();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return counter;
    }

    public int getSize() {
        return vectors.size();
    }

    public static double log(double N, int base) {
        return Math.log(N) / Math.log(base);
    }

    public static double ln(double N) {
        return Math.log(N);
    }

    private static void printProgress(int c, int n, int step) {
        double progress = (double) c / n * 100;
        double epsilon = 1e-9;
        if (Math.abs(progress % step) < epsilon) {
            System.out.println((int) progress + "% completed");
        }
    }
}