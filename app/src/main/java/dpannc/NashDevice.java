package dpannc;

import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

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
        int d = 3;
        int dPrime = 2;

        Random rnd1 = new Random();
        Random rnd2 = new Random();


        Vector v1 = new Vector(d).randomGaussian(rnd1);
        // Vector v2 = v1.sampleWithDot(1, rnd1);
        Vector v2 = v1.sampleWithDistance(3, rnd1);


        NashDevice nd = new NashDevice(d, dPrime, rnd2);
        Vector w1 = nd.transform(v1);
        Vector w2 = nd.transform(v2);

        System.out.println("v1.mag: " + v1.magnitude() + ", v2.mag: " + v2.magnitude());
        System.out.println("w1.mag: " + w1.magnitude() + ", w2.mag: " + w2.magnitude());


    }

}
