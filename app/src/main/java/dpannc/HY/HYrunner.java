package dpannc.HY;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import dpannc.DataGenerator;
import dpannc.NashDevice;
import dpannc.Vector;

public class HYrunner {
    public static void main(String[] args) throws Exception {
        int SEED = 100;
        Random random = new Random(SEED);
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int n = 10000;
        int d = 3;
        Path filePath = Paths.get("resources", "generated", d + "D_" + n + ".txt");
        DataGenerator.generateRandom(filePath, d, n, 10, random);
        HY hy = new HY(sensitivity, epsilon, delta);
        hy.populateFromFile(n, d, filePath);
        // hy.print();

        // Vector q = new Vector(new double[] {0, 0, 0});
        Vector q = new Vector(3).random(random, 10);
        int result = hy.query(q, 0.05, 2.0);
        System.out.println("result: " + result);
    }
}
