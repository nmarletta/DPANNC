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

public class RunningtimeExperiments {
    public static void main(String[] args) throws Exception {
        exp3();
    }

    // runningtime for increasing dataset size n
    public static void exp1() throws Exception {
        String name = "time1";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int d = 300;
        int dPrime = 300;
        double c = 1.5;
        double s = 1.0;
        int reps = 10;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int[] nValues = new int[] { 100_000, 200_000, 300_000, 400_000, 500_000, 600_000, 700_000, 800_000, 900_000,
                1_000_000 };

        // progress bar
        Progress.newBar("Experiment " + name, 3 + nValues.length + nValues.length * (2 * reps));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("n, runningtime\n");

            for (int n : nValues) {
                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                // process vectors
                NashDevice nd1 = new NashDevice(d, dPrime, new Random(SEED));
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    // v.multiply(scale);
                    v = nd1.transform(v);
                    return v.dataString();
                }, table1);
                Progress.updateBar(++pg);

                // initiate AIMN and populate
                AIMNclean aimn = new AIMNclean(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table1);
                double r = aimn.getR();
                Progress.updateBar(++pg);

                // ensure that test for each c is the same
                Random random = new Random(SEED);

                double time = 0;

                // results
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Progress.newStatus("Querying...");
                    Vector q1 = db.getRandomVector(table1, random);
                    Timer timer = new Timer();
                    aimn.query2(q1);
                    time += timer.check() / reps;
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%d,%.2f\n",
                        n, time));
            }
            
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# s: " + s + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# brute force used nashed data\n");
            writer.write("# aimn used nashed data\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    // runningtime for increasing d
    public static void exp2() throws Exception {
        String name = "time2";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        double c = 1.5;
        double s = 1.0;
        int reps = 10;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int[] dPrimeValues = new int[] { 5, 25, 50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000 };

        // progress bar
        Progress.newBar("Experiment " + name, 3 + dPrimeValues.length + dPrimeValues.length * (2 * reps));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("n, runningtime\n");

            for (int dPrime : dPrimeValues) {
                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                // process vectors
                NashDevice nd1 = new NashDevice(d, dPrime, new Random(SEED));
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    // v.multiply(scale);
                    v = nd1.transform(v);
                    return v.dataString();
                }, table1);
                Progress.updateBar(++pg);

                // initiate AIMN and populate
                AIMNclean aimn = new AIMNclean(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table1);
                double r = aimn.getR();
                Progress.updateBar(++pg);

                // ensure that test for each c is the same
                Random random = new Random(SEED);

                double time = 0;

                // results
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Progress.newStatus("Querying...");
                    Vector q1 = db.getRandomVector(table1, random);
                    Timer timer = new Timer();
                    aimn.query(q1);
                    time += timer.check() / reps;
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%d,%.2f\n",
                        dPrime, time));
            }
            
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# s: " + s + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# brute force used nashed data\n");
            writer.write("# aimn used nashed data\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    // for increasing n using the optimized query mechanism
    public static void exp3() throws Exception {
        String name = "time3";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int d = 300;
        int dPrime = 300;
        double c = 1.5;
        double s = 1.0;
        int reps = 1;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int[] nValues = new int[] { 50_000};//, 200_000, 300_000, 400_000, 500_000, 600_000, 700_000, 800_000, 900_000, 1_000_000 };

        // progress bar
        Progress.newBar("Experiment " + name, 3 + nValues.length + nValues.length * (2 * reps));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("n, runningtime\n");

            for (int n : nValues) {
                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                // process vectors
                NashDevice nd1 = new NashDevice(d, dPrime, new Random(SEED));
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    // v.multiply(scale);
                    v = nd1.transform(v);
                    return v.dataString();
                }, table1);
                Progress.updateBar(++pg);

                // initiate AIMN and populate
                AIMNclean aimn = new AIMNclean(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table1);
                double r = aimn.getR();
                Progress.updateBar(++pg);

                // ensure that test for each c is the same
                Random random = new Random(SEED);

                double time = 0;

                // results
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    // Progress.newStatus("Querying...");
                    Vector q1 = db.getRandomVector(table1, random);
                    Timer timer = new Timer();
                    int count = aimn.query(q1);
                    time += timer.check() / reps;
                    // Progress.clearStatus();
                    Progress.updateBar(++pg);
                    Progress.printAbove("count: " + count + ", time: " + time);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%d,%.2f\n",
                        n, time));
            }
            
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# s: " + s + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# brute force used nashed data\n");
            writer.write("# aimn used nashed data\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }
}