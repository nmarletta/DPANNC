package dpannc.EXP;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import dpannc.NashDevice;
import dpannc.Vector;
import dpannc.database.DB;

public class NashDeviceExperiment {
    public static void main(String[] args) throws Exception {
        // expND();
        exp1();
    }

    public static void expND() throws Exception {
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 10000;
        int d = 300;

        // Path filepath = Paths.get("resources", "generated", d + "D_" + n + ".txt");
        // DataGenerator.generateRandom(filepath, d, n, 10, random);

        Path filepath = Paths.get("resources", "fasttext", "dk-300d.txt");

        DB db = new DB("dpannc");

        String table1 = "v1";
        db.loadVectorsIntoDB(table1, filepath, n, d);

        String table2 = "v2";
        db.loadVectorsIntoDB(table2, filepath, n, d);

        NashDevice nd = new NashDevice(d, d, SEED, random);
        db.applyNashTransform(nd, table2);

        int N = 1;
        double meanSum = 0;
        double medianSum = 0;
        double msdSum = 0;
        double madSum = 0;

        for (int i = 0; i < N; i++) {
            Vector q = db.getRandomVector(table1, random);
            Result dists1 = new Result(q, table1, db, false);
            Result dists2 = new Result(q, table2, db, false);
            Result change = dists1.changeBetween(dists2);
            meanSum += change.mean();
            medianSum += change.median();
            msdSum += change.msd();
            madSum += change.mad();
            db.loadResultIntoDB("change", change);
        }
        System.out.println("mean: " + meanSum / N);
        System.out.println("median: " + medianSum / N);
        System.out.println("msd: " + msdSum / N);
        System.out.println("mad: " + madSum / N);
    }

    public static void exp1() throws Exception {
        String name = "exp1";
        Path filepathTarget = Paths.get("results/nash", name + ".txt");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("xy\n"); // flip
            writer.write("r / deviation\n"); // title
            writer.write("r\n"); // x-axis
            writer.write("deviation\n"); // y-axis

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 1000; // sample size
            int d = 300; // dimensions
            Vector q = new Vector(d).randomGaussian(random);

            // generate dataset of vectors
            Path filepathSource = Paths.get("resources/generated", d + "D_" + n + ".txt");
            double min = 0.001;
            double max = 1.9;
            double inc = 0.05;
            for (double dist = min; dist < max; dist += inc) {
                DataGenerator.atDistanceOnSphere(filepathSource, q, n, dist, random);

                // load vectors into database - two identical tables
                DB db = new DB("dpannc");
                String table1 = "v1";
                String table2 = "v2";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                db.loadVectorsIntoDB(table2, filepathSource, n, d);

                // apply Nash Transform on one table
                NashDevice nd = new NashDevice(d, d, SEED, random);
                db.applyNashTransform(nd, table2);

                // calculate results
                Result dists1 = new Result(q, table1, db, false);
                Result dists2 = new Result(q, table2, db, false);
                Result change = dists1.changeBetween(dists2);

                // write result to file
                writer.write(dist + "," + change.median() + "\n"); 
            }

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }
}
