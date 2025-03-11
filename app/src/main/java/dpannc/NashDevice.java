package dpannc;

import java.util.*;

public class NashDevice {
    private int dPrime; // target lower dimension
    private double sigma; // scaling parameter
    private List<Vector> randomGaussians; // list of random Gaussian vectors

    public NashDevice(int d, int dPrime, double sigma) {
        this.dPrime = dPrime;
        this.sigma = sigma;
        this.randomGaussians = new ArrayList<>();

        // generate dPrime random Gaussian vectors in d-dimensional space
        for (int i = 0; i < Math.ceil(dPrime / 2); i++) {
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

        int i = 0; int g = 0;
        while (i < dPrime) {
            double dotProduct = randomGaussians.get(g++).dot(x);
            transformedComponents[i++] = Math.cos(sigma * dotProduct) / sigma;
            if (i < dPrime) {
                transformedComponents[i++] = Math.sin(sigma * dotProduct) / sigma;
            }
        }
        return new Vector(transformedComponents).setLabel(x.getLabel());
    }
}
