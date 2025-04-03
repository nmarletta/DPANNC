package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import dpannc.Vector;

public class DataGenerator {
    public static void main(String[] args) {
    }

    public static void generateRandomGaussian(Path filePath, int d, int amount, Random random) {
        int count = 0;
        try (FileWriter writer = new FileWriter(filePath.toAbsolutePath().toString());) {
            while (count < amount) {
                Vector v = new Vector(d).randomGaussian(random).setLabel("" + count);
                writer.write(v.toString() + "\n");
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Vectors generated...");
    }

    public static void generateRandom(Path filePath, int d, int amount, double range, Random random) {
        int count = 0;
        try (FileWriter writer = new FileWriter(filePath.toAbsolutePath().toString());) {
            while (count < amount) {
                Vector v = new Vector(d).random(random, range).setLabel("" + count);
                writer.write(v.toString() + "\n");
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Vectors generated...");
    }

    public static void atDistanceOnSphere(Path filepath, Vector v, int n, double dist, Random random) {
        try (FileWriter writer = new FileWriter(filepath.toAbsolutePath().toString());) {
            for (int i = 0; i < n; i++) {
                Vector w = v.sampleOnSphere(dist, random);
                double actualDist = w.distance(v);
                if (Math.abs(actualDist-dist) > 0.0001) System.out.println(actualDist + " : " + dist);
                w.setLabel("" + i);
                writer.write(w.toString() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void atDistanceInSpace(Path filepath, Vector v, int n, double dist, Random random) {
        try (FileWriter writer = new FileWriter(filepath.toAbsolutePath().toString());) {
            for (int i = 0; i < n; i++) {
                Vector w = v.sampleInSpace(dist, random);
                double actualDist = w.distance(v);
                if (Math.abs(actualDist-dist) > 0.0001) System.out.println(actualDist + " : " + dist);
                w.setLabel("" + i);
                writer.write(w.toString() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
