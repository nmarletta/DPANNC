package dpannc.AIMN;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import dpannc.NashDevice;
import dpannc.Vector;
// import dpannc.VectorComparators;
import dpannc.database.DB;
// import dpannc.DataGenerator;

public class Runner {
    static int SEED = 123;

    public static void main(String[] args) throws Exception {
        // exp();
        NashDevice nd = new NashDevice(300, 200, 1);
    }

    private static int getDistsCount(double min, double max, Vector q, List<Vector> vectors) {
        int count = 0;
        for (Vector v : vectors) {
            double dist = q.normalize().distance(v.normalize());
            System.out.println(dist);
            if (dist >= min && dist < max) {
                count++;
            }
        }
        return count;
    }

    public static void exp() throws Exception {
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        AIMN ds = new AIMN(sensitivity, epsilon, delta);

        int d = 2;
        double c = 1.2;

        // Path filePath = Paths.get("resources", "fasttext", "en-300d.txt");
        // Path filePath = Paths.get("resources", "GloVe-6B", "glove.6B.300d.txt");
        Path filePath = Paths.get("resources", "generated", "points.txt");
        // Vector testq = new
        // Vector(d).setLabel("test").randomGaussian(SEED).normalize();
        // int n = DataGenerator.generateFile(filePath, testq, 20, dists);
        int n = 400000;
        ds.updateParameters(n, d, c, epsilon, delta);
        ds.populate(n, d, filePath);
        Brute bf = new Brute(n, d, c);
        List<Vector> queryVectors = getQueryVectors();

        try (FileWriter writer = new FileWriter("app/src/main/results/test.csv")) {
        writer.write("n, d, c, r, <r(?), <r(!), <cr(?), <cr(!), >cr\n");
        int rep = 20;
        for (int i = 0; i < 1; i++) {
            printProgress(i, rep, 5);
            Vector q = queryVectors.get(i).normalize();
            ds.query(q);
            bf.query(q, filePath);
            List<Vector> result = DataExtractor.getQuery(q);
            System.out.println("size: " + result.size());
            double r = ds.getR();
            int result1a = getDistsCount(0, r, q, result);
            int result1b = DataExtractor.getDistsCount(0, r);
            int result2a = getDistsCount(r, c * r, q, result);
            int result2b = DataExtractor.getDistsCount(r, c * r);
            int result3 = getDistsCount(c * r, 3, q, result);
            int result3b = DataExtractor.getDistsCount(c*r, 6);

            writer.write(q.getLabel() + ": " + n + " " + d + " " + c + " " + r + " " + result1a + " " + result1b + " " + result2a + " "
                    + result2b + " " + result3 + "\n");

            // writer.write(q.getLabel() + ": " + n + " " + d + " " + c + " " + r + " " + result1b + " " + result2b + " "
            //         + result3b + "\n");
            // DB.emptyTable("dists");
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    }

    private static void printProgress(int c, int n, int step) {
        double progress = (double) c / n * 100;
        double epsilon = 1e-9;
        if (Math.abs(progress % step) < epsilon) {
            System.out.println((int) progress + "% completed");
        }
    }

    public static List<Vector> getQueryVectors() {
        Path filePath = Paths.get("resources", "generated", "queries.txt");
        List<Vector> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toAbsolutePath().toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                int d = parts.length - 1;
                String word = parts[0];
                Vector vector = new Vector(d);
                for (int i = 1; i <= d; i++) {
                    vector.setNext(Double.parseDouble(parts[i]));
                }
                vector.normalize().setLabel(word);
                list.add(vector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}