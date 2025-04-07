package dpannc;

import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

public class NashDevice {
    int dPrime; // target lower dimension
    double sigma; // scaling parameter
    List<Vector> randomGaussians; // list of random Gaussian vectors

    public NashDevice(int d, int dPrime, double sigma, Random random) {
        this.dPrime = dPrime;
        this.sigma = sigma;
        this.randomGaussians = new ArrayList<>();

        // generate dPrime random Gaussian vectors in d-dimensional space
        for (int i = 0; i < Math.ceil((0.0 + dPrime) / 2); i++) {
            randomGaussians.add(new Vector(d).randomGaussian(random));
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
            transformedComponents[i++] = scale * Math.cos(sigma * dotProduct);
            if (i < dPrime) {
                transformedComponents[i++] = scale * Math.sin(sigma * dotProduct);
            }
        }
        return new Vector(transformedComponents).setLabel(x.getLabel());
    }

    public static void main(String[] args) {
        int d = 10; // input dimensionality
        int dPrime = 10; // output dimensionality
        double sigma = 1.0;
        int n = 100; // number of vectors
        String fileName = "NashDeviceExperiment.txt";

        Random rnd1 = new Random(10);
        Random rnd2 = new Random();

        // Reference vector v
        Vector v1 = new Vector(d).randomGaussian(rnd1);
        Vector v2 = v1.sampleWithDot(1, rnd1);

        // Generate transformed vectors
        NashDevice nd = new NashDevice(d, dPrime, sigma, rnd2);
        Vector w1 = nd.transform(v1);
        Vector w2 = nd.transform(v2);

        System.out.println("v1.mag: " + v1.magnitude() + ", v2.mag: " + v2.magnitude());
        System.out.println("w1.mag: " + w1.magnitude() + ", w2.mag: " + w2.magnitude());
        System.out.println("v1.dot(v2): " + v1.dot(v2));
        System.out.println("w1.dot(w2): " + w1.dot(w2));

        // for (int i = 1; i < 100; i++) {
        // NashDevice nd = new NashDevice(d, d, i, rnd);
        // Vector q = nd.transform(v);
        // System.out.println("sigma: " + i + ", mag: " + q.magnitude());
        // }

    }

}
