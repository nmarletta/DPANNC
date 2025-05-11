package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import dpannc.Vector;


public class DataGenerator {
    public static void main(String[] args) {
        uniformDistribution();
    }
    public static void uniformDistribution() {
        int SEED = 100;
        Random random = new Random(SEED);
        int n = 1_000_000;
        int d = 300;
        Path filepathTarget = Paths.get("app/resources/generated/uniformNormalized_" + n / 1000 + "K_" + d + "D.txt");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            for (int i = 0; i < n; i++) {
                Vector v = new Vector(d).randomGaussian(random).normalize().setLabel("v" + i);
                writer.write(v.toString() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
