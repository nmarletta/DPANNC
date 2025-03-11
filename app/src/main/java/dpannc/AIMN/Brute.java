package dpannc.AIMN;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import dpannc.Vector;
import dpannc.database.DB;

public class Brute {
    static double sensitivity = 1;
    static double epsilon = 0.5;
    static double delta = 0.2;

    static int n, d, remainder;
    static double c, lambda, r, K, alpha, beta, adjSen, threshold, etaU, etaQ;
    List<Vector> vectors;

    public Brute(int n, int d, double c) {
        this.n = n;
        remainder = 0;
        vectors = new ArrayList<>();
        this.d = d;
        this.c = c;
        lambda = (2 * Math.sqrt(2 * c)) / (c * c + 1);
        r = 1.0; //1 / Math.pow(log(n, 10), 1.0 / 8.0);
        K = Math.sqrt(ln(n));
        alpha = 1 - ((r * r) / 2); // cosine
        beta = Math.sqrt(1 - (alpha * alpha)); // sine
        adjSen = 2;
        threshold = (adjSen / epsilon) * ln(1 + (Math.exp(epsilon / 2) - 1) / delta);
        etaU = Math.sqrt((ln(n) / K)) * (lambda / r);
        etaQ = alpha * etaU - 2 * beta * Math.sqrt(ln(K));
        // printSettings();
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
                DB.insertToDists(q.getLabel(), v.getLabel(), distance);
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

    public static void printSettings() {
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