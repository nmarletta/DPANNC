package dpannc;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Random;

public class DataGenerator {
    static Random rand = new Random();
    public static void main(String[] args) {
        Vector v = new Vector(new double[] {1.0, 0.0, 0.0}).normalize();
        double distance = 0.7f;
        Vector generated = DataGenerator.generateNormalisedVectorAtDistance(v, distance);
        System.out.println(generated.distance(v));
    }

    public static int generateFile(Path filePath, Vector v, int amount, double[] dists) {
        int count = 0;
        try (FileWriter writer = new FileWriter(filePath.toAbsolutePath().toString());) {
            while (count < amount) {
                double dist = dists[count % dists.length];
                Vector v1 = generateNormalisedVectorAtDistance(v, dist);
                double actualDist = v1.distance(v);
                v1.setLabel(count + ":" + actualDist);
                writer.write(v1.toString() + "\n");
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("counttt: " + count);
        return count;
    }

    public static Vector generateVectorAtDistance(Vector v, double distance) {
        Random rand = new Random();
        Vector perturbation = new Vector(v.getDimensions());

        // Generate random perturbation using Gaussian distribution
        for (int i = 0; i < v.getDimensions(); i++) {
            perturbation.setNext((double) rand.nextGaussian());
        }

        // Normalize the perturbation to unit length
        perturbation = perturbation.normalize();

        // Scale the perturbation to the desired distance
        perturbation.multiply(distance);

        // Create a new vector by adding the perturbation to the current vector
        Vector newVector = v.clone();
        for (int i = 0; i < v.getDimensions(); i++) {
            newVector.get()[i] += perturbation.get()[i];
        }

        return newVector;
    }

    public static Vector generateNormalizedVectorAtDistance(Vector v, double distance) {
        double normV = v.magnitude();

        // Ensure distance is valid
        if (distance < 0 || normV + 1 < distance || Math.abs(normV - 1) > distance) {
            throw new IllegalArgumentException("No intersection: distance is out of valid range.");
        }

        // Compute the center of the intersection circle
        double alpha = (1 + (normV * normV) - (distance * distance)) / (2 * normV);
        Vector center = v.clone();
        center.multiply(alpha / normV); // Move v toward the unit sphere

        // Compute the radius of the intersection circle
        double radius = Math.sqrt(1 - (alpha * alpha));

        // Generate a random vector orthogonal to v
        Random rand = new Random();
        Vector randomDirection = new Vector(v.getDimensions());

        // Fill with random Gaussian values
        for (int i = 0; i < v.getDimensions(); i++) {
            randomDirection.setNext(rand.nextGaussian());
        }
        randomDirection = randomDirection.normalize();

        // Make it perpendicular to v using Gram-Schmidt
        double dotProduct = 0;
        for (int i = 0; i < v.getDimensions(); i++) {
            dotProduct += randomDirection.get()[i] * v.get()[i];
        }
        for (int i = 0; i < v.getDimensions(); i++) {
            randomDirection.get()[i] -= (dotProduct / (normV * normV)) * v.get()[i];
        }
        randomDirection = randomDirection.normalize();

        // Choose a random angle
        double angle = (double) (2 * Math.PI * rand.nextDouble());
        Vector result = center.clone();
        for (int i = 0; i < v.getDimensions(); i++) {
            result.get()[i] += radius * (double) Math.cos(angle) * randomDirection.get()[i];
        }

        return result.normalize(); // Ensure it stays on the unit sphere
    }

    public static Vector generateNormalisedVectorAtDistance(Vector p, double r) {
        int n = p.getDimensions();
        if (n == 2) return selectPointOnCircle(p, r);
    
        double normP = p.magnitude();
        if (Math.abs(normP - 1.0f) > 1e-6) {
            throw new IllegalArgumentException("Point p must be on the unit sphere (norm = 1)");
        }
        if (r >= 2.0f) { // 2.0 is the max Euclidean distance on a unit sphere
            throw new IllegalArgumentException("r must be less than 2.0 (the sphere's diameter).");
        }
    
        // Step 1: Generate a perpendicular vector
        Vector tangent = stablePerpendicularVector(p);
        // System.out.println("p dot tangent (should be 0): " + p.dot(tangent));
        // System.out.println("tangent magnitude (should be 1): " + tangent.magnitude());
    
        // Step 2: Convert Euclidean distance r to geodesic angle theta
        double theta = 2.0f * (double) Math.asin(r / 2.0f);
        
        // Step 3: Compute the new point along the geodesic
        Vector u = p.clone().multiply((double) Math.cos(theta))
                .add(tangent.clone().multiply((double) Math.sin(theta)));
    
        // Step 4: Normalize for numerical stability
        u = u.normalize();
    
        // Debugging
        // System.out.println("Original point p: " + p);
        // System.out.println("New point u: " + u);
        System.out.printf("Requested distance r: %.6f\n", r);
        System.out.printf("Distance from p to u (should be r): %.6f\n", p.distance(u));
        // System.out.printf("Norm of u (should be 1): %.6f\n", u.magnitude());
    
        return u;
    }
    
    

    private static Vector randomPerpendicularVector(Vector p) {
        int n = p.getDimensions();
        Vector randomVec = new Vector(n).randomGaussian().normalize(); // Random unit vector
    
        // Compute projection onto p
        double dot = p.dot(randomVec);
        Vector parallelComponent = p.clone().multiply(dot);
    
        // Subtract parallel component to get perpendicular vector
        Vector d = randomVec.clone().divide(parallelComponent);
        d.normalize(); // Ensure it's unit-length
    
        return d;
    }
    
    private static Vector stablePerpendicularVector(Vector p) {
        int n = p.getDimensions();
        Vector perpVec = new Vector(n);
    
        // Pick an index i where p[i] is nonzero, swap two coordinates
        for (int i = 0; i < n - 1; i++) {
            if (p.getC(i) != 0) {
                perpVec.get()[i] = -p.getC(i + 1);
                perpVec.get()[i + 1] = p.getC(i);
                break;
            }
        }
        return perpVec.normalize();
    }

    public static Vector selectPointOnCircle(Vector p, double r) {
        if (p.getDimensions() != 2) {
            throw new IllegalArgumentException("This method only works for 2D vectors.");
        }
        if (Math.abs(p.magnitude() - 1.0f) > 1e-6) {
            throw new IllegalArgumentException("Point p must be on the unit circle.");
        }
        
        // Find a perpendicular vector
        double x = p.getC(0);
        double y = p.getC(1);
        
        // Perpendicular vectors: (-y, x) or (y, -x)
        Vector tangent = new Vector(new double[]{-y, x});
        
        // Choose a random sign (clockwise or counterclockwise)
        if (new Random().nextBoolean()) {
            tangent.multiply(-1.0f);
        }
        
        // Move in the perpendicular direction by distance r
        Vector candidateU = p.clone().set(tangent.multiply(r).set(p));
        
        // Normalize to keep it on the unit circle
        return candidateU.normalize();
    }
    
}
