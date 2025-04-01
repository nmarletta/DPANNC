package dpannc;

import java.util.Random;

public class Noise {
    private final Random RANDOM;
    
    public Noise() {
        RANDOM = new Random();
    }

    public Noise(int SEED) {
        RANDOM = new Random(SEED);
    }
     

    public double TLap(double sensitivity, double epsilon, double delta) {
        double u = RANDOM.nextDouble();
        double lambda = sensitivity / epsilon;
        double A = (sensitivity / epsilon) * Math.log(1.0 + (Math.exp(epsilon) - 1.0) / (2.0 * delta));
        if (u < 0.5) { // negative/left side
            return -lambda * Math.log(1 - 2 * u * (1 - Math.exp(-A / lambda)));
        } else { // positive/right side
            return lambda * Math.log(1 - 2 * (1 - u) * (1 - Math.exp(-A / lambda)));
        }
    }

    public double Lap(double scale) {
        double u = RANDOM.nextDouble() - 0.5;
        return -scale * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
    }
}
