package dpannc.AIMN;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import dpannc.EXP.DataGenerator;
import dpannc.Vector;

public class Runner {
    static int SEED = 123;

    public static void main(String[] args) throws Exception {
        int SEED = 100;
        Random random = new Random(SEED);
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int n = 10000;
        int d = 10;
        double c = 1.1;
        Path filePath = Paths.get("resources", "generated", d + "D_" + n + ".txt");
        DataGenerator.generateRandom(filePath, d, n, 10, random);
        AIMN AIMN = new AIMN(n, d, c, sensitivity, epsilon, delta);
        AIMN.populateFromFile(n, d, filePath);

        // Vector q = new Vector(new double[] {0, 0, 0});
        Vector q = new Vector(d).random(random, 10);
        int result = AIMN.query(q);
        System.out.println("result: " + result);
    }

    // public static void exp() throws Exception {
    //     double sensitivity = 1.0;
    //     double epsilon = 0.4;
    //     double delta = 0.2;
    //     AIMNc ds = new AIMNc(sensitivity, epsilon, delta);

    //     int d = 2;
    //     double c = 1.2;

    //     // Path filePath = Paths.get("resources", "fasttext", "en-300d.txt");
    //     // Path filePath = Paths.get("resources", "GloVe-6B", "glove.6B.300d.txt");
    //     Path filePath = Paths.get("resources", "generated", "points.txt");
    //     // Vector testq = new
    //     // Vector(d).setLabel("test").randomGaussian(SEED).normalize();
    //     // int n = DataGenerator.generateFile(filePath, testq, 20, dists);
    //     int n = 4000;
    //     ds.updateParameters(n, d, c, epsilon, delta);
    //     ds.populate(n, d, filePath);
    //     Brute bf = new Brute(n, d, c);
    //     List<Vector> queryVectors = getQueryVectors();

    //     try (FileWriter writer = new FileWriter("app/src/main/results/test.csv")) {
    //     writer.write("n, d, c, r, <r(?), <r(!), <cr(?), <cr(!), >cr\n");
    //     int rep = 20;
    //     for (int i = 0; i < 1; i++) {
    //         printProgress(i, rep, 5);
    //         Vector q = queryVectors.get(i).normalize();
    //         ds.query(q);
    //         bf.query(q, filePath);
    //         List<Vector> result = DataExtractor.getQuery(q);
    //         System.out.println("size: " + result.size());
    //         double r = ds.getR();
    //         int result1a = getDistsCount(0, r, q, result);
    //         int result1b = DataExtractor.getDistsCount(0, r);
    //         int result2a = getDistsCount(r, c * r, q, result);
    //         int result2b = DataExtractor.getDistsCount(r, c * r);
    //         int result3 = getDistsCount(c * r, 3, q, result);
    //         int result3b = DataExtractor.getDistsCount(c*r, 6);

    //         writer.write(q.getLabel() + ": " + n + " " + d + " " + c + " " + r + " " + result1a + " " + result1b + " " + result2a + " "
    //                 + result2b + " " + result3 + "\n");

    //         // writer.write(q.getLabel() + ": " + n + " " + d + " " + c + " " + r + " " + result1b + " " + result2b + " "
    //         //         + result3b + "\n");
    //         // DB.emptyTable("dists");
    //     }
    // } catch (IOException e) {
    //     e.printStackTrace();
    // }

    // }

    private static void printProgress(int c, int n, int step) {
        double progress = (double) c / n * 100;
        double epsilon = 1e-9;
        if (Math.abs(progress % step) < epsilon) {
            System.out.println((int) progress + "% completed");
        }
    }
}