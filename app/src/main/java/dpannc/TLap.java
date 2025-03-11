package dpannc;

import java.util.Random;

public class TLap {
    private static final Random RANDOM = new Random();

    public static double generate(double sensitivity, double epsilon, double delta) {
        double u = RANDOM.nextDouble();
        double lambda = sensitivity / epsilon;
        double A = (sensitivity / epsilon) * Math.log(1.0 + (Math.exp(epsilon) - 1.0) / (2.0 * delta));
        if (u < 0.5) { // negative/left side
            return -lambda * Math.log(1 - 2 * u * (1 - Math.exp(-A / lambda)));
        } else { // positive/right side
            return lambda * Math.log(1 - 2 * (1 - u) * (1 - Math.exp(-A / lambda)));
        }
    }

    public static float generate(float sensitivity, float epsilon, float delta) {
        float u = RANDOM.nextFloat();
        float lambda = sensitivity / epsilon;
        float A = (sensitivity / epsilon) * (float) Math.log(1.0 + (Math.exp(epsilon) - 1.0) / (2.0 * delta));
        if (u < 0.5) { // negative/left side
            return -lambda * (float) Math.log(1 - 2 * u * (1 - Math.exp(-A / lambda)));
        } else { // positive/right side
            return lambda * (float) Math.log(1 - 2 * (1 - u) * (1 - Math.exp(-A / lambda)));
        }
    }
}
