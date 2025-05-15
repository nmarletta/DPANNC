package dpannc;

import java.util.*;
import dpannc.EXP.Result;

public class NashDevice {
    int dPrime; // target lower dimension
    double sigma; // scaling parameter
    List<Vector> randomGaussians; // list of random Gaussian vectors

    public NashDevice(int d, int dPrime, Random random) {
        this.dPrime = dPrime;
        this.sigma = 1.0;
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

        double scale = 1.0 / Math.sqrt(dPrime / 2);
        Vector v = new Vector(transformedComponents).multiply(scale);
        return v.setLabel(x.getLabel());
    }

    public static void main(String[] args) {
        Random random = new Random();
        int d = 30;
        int dPrime = 40;
        NashDevice nd = new NashDevice(d, dPrime, random);
        Vector v1 = new Vector(d).randomGaussian(random);
        Vector w1 = v1.sampleWithDistance(10, random);
        double dist1 = v1.distance(w1);
        System.out.println(dist1);
        Vector v2 = nd.transform(v1);
        Vector w2 = nd.transform(w1);
        double dist2 = v2.distance(w2);
        System.out.println(dist2);
        System.out.println("mag: " + v2.magnitude());
    }

    public static double getTransformedDistance(int d, int dPrime, double r, double percentile, Random random) {
        NashDevice nd = new NashDevice(dPrime, dPrime, random);
        Result transformedDists = new Result();
        
        int n = 1000;
        for (int i = 0; i < n; i++) {
            // generate vectors
            Vector q1 = new Vector(d).randomGaussian(random);
            Vector v1 = q1.sampleWithDistance(r, random);

            // apply Nash Transform
            Vector q2 = nd.transform(q1);
            Vector v2 = nd.transform(v1);

            // save distances after transformation
            transformedDists.add("" + i, q2.distance(v2));
        }

        return transformedDists.percentile(percentile);
    }
}
