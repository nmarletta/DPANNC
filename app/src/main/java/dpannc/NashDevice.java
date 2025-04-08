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

    // /**
    //  * applies 'Randomized Nash Device' embedding to a high-dimensional vector
    //  * https://people.eecs.berkeley.edu/~brecht/papers/07.rah.rec.nips.pdf
    //  * @param x high-dimensional vector
    //  * @return transformed d'-dimensional vector
    //  */
    // public Vector transform(Vector x) {
    //     double[] transformedComponents = new double[dPrime];

    //     int i = 0;
    //     int g = 0;
    //     while (i < dPrime) {
    //         double dotProduct = randomGaussians.get(g++).dot(x);
    //         transformedComponents[i++] = Math.sqrt(2.0 / dPrime) * Math.cos(sigma * dotProduct);
    //         if (i < dPrime) {
    //             transformedComponents[i++] = Math.sqrt(2.0 / dPrime) * Math.sin(sigma * dotProduct);
    //         }
    //     }
    //     return new Vector(transformedComponents).setLabel(x.getLabel());
    // }

    // /**
    //  * applies 'Randomized Nash Device' embedding to a high-dimensional vector
    //  * 
    //  * @param x high-dimensional vector
    //  * @return transformed d'-dimensional vector
    //  */
    // public Vector transform(Vector x) {
    //     double[] transformedComponents = new double[dPrime];

    //     int i = 0;
    //     int g = 0;
    //     while (i < dPrime) {
    //         double dotProduct = randomGaussians.get(g++).dot(x);
    //         transformedComponents[i++] = 1.0 / sigma * Math.cos(sigma * dotProduct);
    //         if (i < dPrime) {
    //             transformedComponents[i++] = 1.0 / sigma * Math.sin(sigma * dotProduct);
    //         }
    //     }
    //     return new Vector(transformedComponents).normalize().setLabel(x.getLabel());
    // }

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
