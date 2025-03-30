package dpannc;

import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

public class NashDevice {
    int dPrime; // target lower dimension
    double sigma; // scaling parameter
    List<Vector> randomGaussians; // list of random Gaussian vectors

    public NashDevice(int d, int dPrime, double sigma) {
        this.dPrime = dPrime;
        this.sigma = sigma;
        this.randomGaussians = new ArrayList<>();

        // generate dPrime random Gaussian vectors in d-dimensional space
        for (int i = 0; i < Math.ceil((0.0 + dPrime) / 2); i++) {
            randomGaussians.add(new Vector(d).randomGaussian());
        }
    }

    /**
     * applies 'Randomized Nash Device' embedding to a high-dimensional vector
     * 
     * @param x high-dimensional vector
     * @return transformed d'-dimensional vector
     */
    public Vector transform(Vector x) {
        double[] transformedComponents = new double[dPrime];

        int i = 0;
        int g = 0;
        while (i < dPrime) {
            double scale = Math.sqrt(2.0 / dPrime);
            double dotProduct = randomGaussians.get(g++).dot(x);
            transformedComponents[i++] = scale * Math.cos(sigma * dotProduct) / sigma;
            if (i < dPrime) {
                transformedComponents[i++] = scale * Math.sin(sigma * dotProduct) / sigma;
            }
        }
        return new Vector(transformedComponents).setLabel(x.getLabel()).normalize();
    }

    public static void main(String[] args) {
        int d = 10; // input dimensionality
        int dPrime = 4; // output dimensionality
        double sigma = 1.0;
        int n = 100; // number of vectors
        String fileName = "NashDeviceExperiment.txt";

        NashDevice nd = new NashDevice(d, dPrime, sigma);

        // Reference vector v
        Vector v = new Vector(d).randomGaussian();
        Vector w = nd.transform(v);

        // Generate original vectors
        List<Vector> originalVectors = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Vector x = new Vector(d).randomGaussian().normalize().setLabel("x" + i);
            originalVectors.add(x);
        }

        // Generate transformed vectors
        List<Vector> transformedVectors = new ArrayList<>();
        for (Vector x : originalVectors) {
            Vector y = nd.transform(x).setLabel("y" + x.getLabel().substring(1));
            transformedVectors.add(y);
        }

        double[] errors = new double[n];
        double errorSum = 0;

        // Write results to CSV
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("original_label original_distance transformed_label transformed_distance\n");
            for (int i = 0; i < n; i++) {
                Vector orig = originalVectors.get(i);
                Vector trans = transformedVectors.get(i);
                double dOrig = orig.distance(v);
                double dTrans = trans.distance(w); // NOTE: must transform `v` to compare fairly, see below
                double ratio = dTrans / dOrig;
                double err = Math.abs(ratio - 1.0); // relative error
                errorSum += err;
                errors[i] = err;

                writer.write(String.format("%s %.6f %s %.6f %.6f\n",
                        orig.getLabel(), dOrig,
                        trans.getLabel(), dTrans,
                        err));
            }
            System.out.println("CSV written to " + fileName);
            // Compute mean
            double mean = errorSum / n;

            // Compute variance
            double variance = 0;
            for (double e : errors) {
                variance += Math.pow(e - mean, 2);
            }
            variance /= n;

            // Stddev
            double stddev = Math.sqrt(variance);

            System.out.printf("Mean error: %.6f\n", mean);
            System.out.printf("Variance: %.6f\n", variance);
            System.out.printf("Std Dev: %.6f\n", stddev);
            System.out.println("CSV written to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
