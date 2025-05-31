package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import dpannc.Progress;
import dpannc.Vector;
import dpannc.AIMN.AIMNclean;
import dpannc.AIMN.NashDevice;
import dpannc.database.DB;
import dpannc.EXP.Timer;

public class ComplexityExperiments {
    public static void main(String[] args) throws Exception {
        exp3();
    }

    // runningtime for increasing dataset size n
    public static void exp1() throws Exception {
        String name = "complexity1";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int d = 300;
        int dPrime = 300;
        double c = 1.5;
        double s = 1.0;
        int reps = 5;
        double sensitivity = 1.0;
        double epsilon = 10.0;
        
        int[] nValues = new int[] { 100_000, 200_000, 300_000, 400_000, 500_000, 600_000, 700_000, 800_000, 900_000,
                1_000_000 };

        // progress bar
        Progress.newBar("Experiment " + name, nValues.length * (1 + 4 * reps));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {

            // CSV header
            writer.write("Running time vs dataset size\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("n, embedding, insertion, query, queryFast, gaussians, leafs, nodes \n");

            for (int n : nValues) {
                double delta = 1/n;

                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                // ensure that test for each c is the same
                Random random = new Random(SEED);

                double timeProcess = 0;
                double timeBuild = 0;
                double timeQuery1 = 0;
                double timeQuery2 = 0;

                double gaussians = 0;
                double leafs = 0;
                double nodes = 0;

                // results
                for (int i = 0; i < reps; i++) {

                    // process vectors
                    Timer timer = new Timer();
                    NashDevice nd1 = new NashDevice(d, dPrime, new Random(SEED));
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        // v.multiply(scale);
                        v = nd1.transform(v);
                        return v.dataString();
                    }, table1);
                    timeProcess += timer.check() / reps;
                    Progress.updateBar(++pg);

                    // initiate AIMN and populate
                    AIMNclean aimn = new AIMNclean(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(true);
                    timer = new Timer();
                    aimn.populateFromDB(table1);
                    timeBuild += timer.check() / reps;
                    double r = aimn.getR();
                    Progress.updateBar(++pg);

                    // choose and run query
                    Vector q1 = db.getRandomVector(table1, random);
                    timer = new Timer();
                    aimn.queryFast(q1);
                    timeQuery1 += timer.check() / reps;
                    Progress.updateBar(++pg);

                    timer = new Timer();
                    aimn.queryFast(q1);
                    timeQuery2 += timer.check() / reps;
                    Progress.updateBar(++pg);

                    gaussians += aimn.gaussians() / reps;
                    leafs += aimn.buckets() / reps;
                    nodes += aimn.nodes() / reps;
                }

                // write result to file
                writer.write(String.format(Locale.US, "%d, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f\n",
                        n, timeProcess, timeBuild, timeQuery1, timeQuery2, gaussians, leafs, nodes));
            }

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# s: " + s + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# datafile: " + filepathSource + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    // runningtime for increasing d
    public static void exp2() throws Exception {
        String name = "complexity2";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        double c = 1.5;
        double s = 1.0;
        int reps = 10;
        double sensitivity = 1.0;
        double epsilon = 10.0;
        double delta = 1 / n;
        int[] dPrimeValues = new int[] {5, 10, 50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000 };

        // progress bar
        Progress.newBar("Experiment " + name, dPrimeValues.length * (5 * reps));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {

            // CSV header
            writer.write("Running time vs. dimensionality \n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write(
                    "d', embedding, insertion, query, queryFast, gaussians, leafs, nodes \n");

            for (int dPrime : dPrimeValues) {

                // ensure that test for each c is the same
                Random random = new Random(SEED);

                double timeProcess = 0;
                double timeBuild = 0;
                double timeQuery1 = 0;
                double timeQuery2 = 0;

                double gaussians = 0;
                double leafs = 0;
                double nodes = 0;

                // results
                for (int i = 0; i < reps; i++) {
                    // load vectors to DB
                    String table1 = "vectors1";
                    db.loadVectorsIntoDB(table1, filepathSource, n, d);
                    Progress.updateBar(++pg);

                    Timer timer = new Timer();
                    NashDevice nd1 = new NashDevice(d, dPrime);
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        v = nd1.transform(v);
                        return v.dataString();
                    }, table1);
                    timeProcess += timer.check() / reps;
                    Progress.updateBar(++pg);

                    // initiate AIMN and populate
                    AIMNclean aimn = new AIMNclean(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(true);
                    timer = new Timer();
                    aimn.populateFromDB(table1);
                    timeBuild += timer.check() / reps;
                    double r = aimn.getR();
                    Progress.updateBar(++pg);

                    // choose and run query
                    Vector q1 = db.getRandomVector(table1, random);
                    timer = new Timer();
                    aimn.queryFast(q1);
                    timeQuery1 += timer.check() / reps;
                    Progress.updateBar(++pg);

                    timer = new Timer();
                    aimn.queryFast(q1);
                    timeQuery2 += timer.check() / reps;
                    Progress.updateBar(++pg);

                    gaussians += aimn.gaussians() / reps;
                    leafs += aimn.buckets() / reps;
                    nodes += aimn.nodes() / reps;
                }

                // write result to file
                writer.write(String.format(Locale.US, "%d, %.2f, %.2f, %.2f, %.2f, %.0f, %.0f, %.0f\n",
                        dPrime, timeProcess, timeBuild, timeQuery1, timeQuery2, gaussians, leafs, nodes));
            }

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# s: " + s + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    // runningtime for increasing approximation factor c
    public static void exp3() throws Exception {
        String name = "complexity3";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        int dPrime = 300;
        double s = 1.0;
        int reps = 10;
        double sensitivity = 1.0;
        double epsilon = 10.0;
        double delta = 1 / n;
        double[] cValues = new double[] { 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0 };

        // progress bar
        Progress.newBar("Experiment " + name, cValues.length * (1 + 4 * reps));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {

            // CSV header
            writer.write("Running time vs. dimensionality \n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write(
                    "c, embedding, insertion, query, queryFast, gaussians, leafs, nodes \n");

            for (double c : cValues) {

                // ensure that test for each c is the same
                Random random = new Random(SEED);

                double timeProcess = 0;
                double timeBuild = 0;
                double timeQuery1 = 0;
                double timeQuery2 = 0;

                double gaussians = 0;
                double leafs = 0;
                double nodes = 0;

                // results
                for (int i = 0; i < reps; i++) {
                    // load vectors to DB
                    String table1 = "vectors1";
                    db.loadVectorsIntoDB(table1, filepathSource, n, d);
                    Progress.updateBar(++pg);

                    Timer timer = new Timer();
                    NashDevice nd1 = new NashDevice(d, dPrime, new Random(SEED));
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        v = nd1.transform(v);
                        return v.dataString();
                    }, table1);
                    timeProcess += timer.check() / reps;
                    Progress.updateBar(++pg);

                    // initiate AIMN and populate
                    AIMNclean aimn = new AIMNclean(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(true);
                    timer = new Timer();
                    aimn.populateFromDB(table1);
                    timeBuild += timer.check() / reps;
                    double r = aimn.getR();
                    Progress.updateBar(++pg);

                    // choose and run query
                    Vector q1 = db.getRandomVector(table1, random);
                    timer = new Timer();
                    aimn.queryFast(q1);
                    timeQuery1 += timer.check() / reps;
                    Progress.updateBar(++pg);

                    timer = new Timer();
                    aimn.queryFast(q1);
                    timeQuery2 += timer.check() / reps;
                    Progress.updateBar(++pg);

                    gaussians += aimn.gaussians() / reps;
                    leafs += aimn.buckets() / reps;
                    nodes += aimn.nodes() / reps;
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.2f, %.2f, %.2f, %.2f, %.2f, %.0f, %.0f, %.0f\n",
                        c, timeProcess, timeBuild, timeQuery1, timeQuery2, gaussians, leafs, nodes));
            }

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# s: " + s + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }
}