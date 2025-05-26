package dpannc.AIMN;

import java.util.*;

import dpannc.Vector;
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

    public NashDevice(int d, int dPrime) {
        this.dPrime = dPrime;
        this.sigma = 1.0;
        this.randomGaussians = new ArrayList<>();
        int SEED = 100;
        // generate dPrime random Gaussian vectors in d-dimensional space
        for (int i = 0; i < Math.ceil((0.0 + dPrime) / 2); i++) {
            Random random = new Random(SEED + i * dPrime);
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

    public static double getDistortion(int d, int dPrime, double r, double percentile, Random random) {
        NashDevice nd = new NashDevice(dPrime, dPrime, random);
        Result distortions = new Result();
        
        int n = 1000;
        for (int i = 0; i < n; i++) {
            // generate vectors
            Vector q1 = new Vector(d).randomGaussian(random);
            Vector v1 = q1.sampleWithDistance(r, random);

            // apply Nash Transform
            Vector q2 = nd.transform(q1);
            Vector v2 = nd.transform(v1);

            // save distances after transformation
            double transformedDistance = q2.distance(v2);
            double distortion = transformedDistance / r;
            distortions.add("" + i, distortion);
        }

        return distortions.percentile(percentile);
    }

    public static double getDistortionFactor(int d, int dPrime, double r, double percentile, Random random) {
        NashDevice nd = new NashDevice(d, dPrime, random);
        Result distances = new Result();
        
        int n = 1000;
        for (int i = 0; i < n; i++) {
            // generate vectors
            Vector q1 = new Vector(d).randomGaussian(random);
            Vector v1 = q1.sampleWithDistance(r, random);

            // apply Nash Transform
            Vector q2 = nd.transform(q1);
            Vector v2 = nd.transform(v1);

            // save distances after transformation
            double transformedDistance = q2.distance(v2);
            distances.add("" + i, transformedDistance);
        }
        double g = distances.percentile(percentile);
        // System.out.println("g: " + g);
        return g/r;
    }

    public static void main(String[] args) {
        double r = 1.5 * 0.818;
        System.out.println(getDistortion(300, 300, r, 0.1, new Random(100)));
        System.out.println(getDistortionFactor(300, 300, r, 0.1, new Random(100)));
    }
}
