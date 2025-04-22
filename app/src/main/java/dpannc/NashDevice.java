package dpannc;

import java.util.*;

public class NashDevice {
    int dPrime; // target lower dimension
    double sigma; // scaling parameter
    List<Vector> randomGaussians; // list of random Gaussian vectors

    public NashDevice(int d, int dPrime, Random random) {
        this.dPrime = dPrime;
        this.sigma = 1.0 / Math.sqrt(2.0);
        this.randomGaussians = new ArrayList<>();

        // generate dPrime random Gaussian vectors in d-dimensional space
        for (int i = 0; i < Math.ceil((0.0 + dPrime) / 2); i++) {
            randomGaussians.add(new Vector(d).randomGaussian(random));
        }
    }

    public Vector transform(Vector x) {
        double[] transformedComponents = new double[dPrime];
    
        int i = 0;
        int g = 0;
    
        while (i < dPrime) {
            double dotProduct = randomGaussians.get(g++).dot(x);
    
            double cos = (1.0 / sigma) * Math.cos(sigma * dotProduct);
            double sin = (1.0 / sigma) * Math.sin(sigma * dotProduct);
    
            transformedComponents[i++] = cos;
            if (i < dPrime) {
                transformedComponents[i++] = sin;
            }
        }
    
        double scale = 1.0 / Math.sqrt(dPrime);
        Vector v = new Vector(transformedComponents).multiply(scale);
        return v.setLabel(x.getLabel());
    }
    
    public static void main(String[] args) {
        Random random = new Random();
        int d = 10;
        NashDevice nd = new NashDevice(d, d, random);
        Vector v = new Vector(d).randomGaussian(random).setMagnitude(2);
        Vector w = nd.transform(v);
        System.out.println(w.magnitude());
    }
}

