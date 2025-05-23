package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.math3.special.Erf;

import dpannc.Progress;
import dpannc.Vector;
import dpannc.AIMN.*;
import dpannc.database.DB;

public class AccuracyExperiments {

    public static void main(String[] args) throws Exception {
        // exp6();
        // System.out.println(0.5 * Erf.erfc(2.4 / Math.sqrt(2)));
    }

    // increasing c with transformation
    public static void exp1a() throws Exception {
        String name = "accuracy1an";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        int dPrime = 300;
        int reps = 10;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double[] cValues = new double[] { 1.01, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8,
                1.9, 2.0, 2.1, 2.2, 2.3, 2.4 };

        // progress bar
        Progress.newBar("Experiment " + name, 2 + cValues.length * (1 + reps * 2));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("c, " + stats.statsHeader() + ", " + stats.prHeader() + "\n");

            // load vectors to DB
            String table1 = "vectors1";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            Progress.updateBar(++pg);

            // process vectors
            NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                v.normalize();
                v = nd.transform(v);
                return v.dataString();
            }, table1);
            Progress.updateBar(++pg);

            for (double c : cValues) {
                stats.reset();
                // initiate AIMN and populate
                AIMN aimn = new AIMN(n, dPrime, 1, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table1);
                double r = aimn.getR();
                Progress.updateBar(++pg);

                // ensure that test for each c is the same
                Random random = new Random(SEED);

                // results
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Vector q1 = db.getRandomVector(table1, random);
                    int count = aimn.queryFast(q1);
                    Progress.printAbove("count: " + count);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    Progress.updateBar(++pg);

                    // calculate distances
                    Result dists = new Result().loadDistancesBetween(q1, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(dists, queryList, r, c * r);
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.2f,%s,%s\n",
                        c, stats.stats(), stats.pr()));
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
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

    // increasing c with normalization
    public static void exp1b() throws Exception {
        String name = "accuracy1b";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        int dPrime = 300;
        int reps = 10;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double[] cValues = new double[] { 1.01, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8,
                1.9, 2.0, 2.1, 2.2, 2.3, 2.4 };

        // progress bar
        Progress.newBar("Experiment " + name, 2 + cValues.length * (1 + reps * 2));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("c, " + stats.statsHeader() + ", " + stats.prHeader() + "\n");

            // load vectors to DB
            String table1 = "vectors1";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            Progress.updateBar(++pg);

            // process vectors
            NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                // v = nd.transform(v);
                v.normalize();
                return v.dataString();
            }, table1);
            Progress.updateBar(++pg);

            for (double c : cValues) {
                stats.reset();
                // initiate AIMN and populate
                AIMN aimn = new AIMN(n, dPrime, 1, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table1);
                double r = aimn.getR();
                Progress.updateBar(++pg);

                // ensure that test for each c is the same
                Random random = new Random(SEED);

                // results
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Vector q1 = db.getRandomVector(table1, random);
                    int count = aimn.queryFast(q1);
                    Progress.printAbove("count: " + count);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    Progress.updateBar(++pg);

                    // calculate distances
                    Result dists = new Result().loadDistancesBetween(q1, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(dists, queryList, r, c * r);
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.2f,%s,%s\n",
                        c, stats.stats(), stats.pr()));
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
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

    // query range modification and finescaling
    public static void exp2a() throws Exception {
        String name = "accuracy2a";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        int dPrime = 300;
        double c = 1.5;
        int k = 1000;
        int reps = 10;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double[] sValues = new double[] { 0.9, 0.905, 0.91, 0.915, 0.92, 0.925, 0.93, 0.935, 0.94, 0.945, 0.95, 0.955, 1.0 };
        // double[] sValues = new double[] { 0.9, 0.905, 0.91, 0.915, 0.92, 0.925, 0.93,
        // 0.935, 0.94, 0.945, 0.95, 1.0 };

        // progress bar
        Progress.newBar("Experiment " + name, reps * (sValues.length * 6));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("scaling factor, " + stats.statsHeader() + ", " + stats.prHeader() + "\n");

            
            for (double s : sValues) {
                stats.reset();
                Random random = new Random(SEED);
                for (int i = 0; i < reps; i++) {
                    // load vectors to DB
                    String table1 = "vectors1";
                    db.loadVectorsIntoDB(table1, filepathSource, n, d);
                    Progress.updateBar(++pg);

                    // find r for k-nearest neighbors and compute scaling factor
                    Vector q1 = db.getRandomVector(table1, random);
                    Result dists = new Result().loadDistancesBetween(q1, table1, db);
                    double initial_r = dists.distanceToKNearest(k);
                    double target_r = 1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0);
                    double scalingFactor = target_r / initial_r;
                    Progress.updateBar(++pg);

                    // process vectors
                    NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        v.multiply(scalingFactor);
                        v = nd.transform(v);
                        return v.dataString();
                    }, table1);
                    Progress.updateBar(++pg);

                    // update q to transformed version
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table1);

                    // initiate AIMN and populate
                    AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(false);
                    aimn.populateFromDB(table1);
                    double r = aimn.getR();
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    Progress.updateBar(++pg);

                    // calculate distances
                    Result distances = new Result().loadDistancesBetween(q2, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(distances, queryList, r, c * r);
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                // write result to file
                writer.write(String.format(Locale.US, "%.3f,%s,%s\n",
                        s, stats.stats(), stats.pr()));
            }

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial r for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    public static void exp2b() throws Exception {
        String name = "accuracy2b";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        int dPrime = 300;
        double c = 1.5;
        int k = 5000;
        int reps = 1;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double[] sValues = new double[] { 0.75, 0.775, 0.8, 0.825, 0.85, 0.875, 0.9, 1.0 };

        // progress bar
        Progress.newBar("Experiment " + name, reps * (sValues.length * 6));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("scaling factor, " + stats.statsHeader() + ", " + stats.prHeader() + "\n");

            for (double s : sValues) {
                stats.reset();
                Random random = new Random(SEED);
                for (int i = 0; i < reps; i++) {
                    // load vectors to DB
                    String table1 = "vectors1";
                    db.loadVectorsIntoDB(table1, filepathSource, n, d);
                    Progress.updateBar(++pg);

                    // find r for k-nearest neighbors and compute scaling factor
                    Vector q1 = db.getRandomVector(table1, random);
                    Result dists = new Result().loadDistancesBetween(q1, table1, db);
                    double initial_cr = dists.distanceToKNearest(k);
                    double target_cr = c * (1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0));
                    double scalingFactor = target_cr / initial_cr;
                    Progress.updateBar(++pg);

                    // process vectors
                    NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        v.multiply(scalingFactor);
                        v = nd.transform(v);
                        return v.dataString();
                    }, table1);
                    Progress.updateBar(++pg);

                    // update q to transformed version
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table1);

                    // initiate AIMN and populate
                    AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(false);
                    aimn.populateFromDB(table1);
                    double r = aimn.getR();
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    Progress.updateBar(++pg);

                    // calculate distances
                    Result distances = new Result().loadDistancesBetween(q2, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(distances, queryList, r, c * r);
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                // write result to file
                writer.write(String.format(Locale.US, "%.3f,%s,%s\n",
                        s, stats.stats(), stats.pr()));
            }

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial cr for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    // for increasing dataset size n
    public static void exp3() throws Exception {
        String name = "accuracy3";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int d = 300;
        int dPrime = 300;
        double c = 1.2;
        double s = 1.0;
        int reps = 1;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int[] nValues = new int[] { 100_000, 200_000, 300_000, 400_000, 500_000, 600_000, 700_000, 800_000, 900_000,
                1_000_000 };

        // progress bar
        int sum = Arrays.stream(nValues).sum() / 100_000;
        Progress.newBar("Experiment " + name, nValues.length * (3 * sum + reps * sum * 2));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("n, " + stats.statsHeader() + ", " + stats.prHeader() + "\n");

            for (int n : nValues) {
                stats.reset();
                int pgStep = n / 100_000;

                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                pg += pgStep;
                Progress.updateBar(pg);

                // process vectors
                NashDevice nd1 = new NashDevice(d, dPrime, new Random(SEED));
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    // v.multiply(scale);
                    v = nd1.transform(v);
                    return v.dataString();
                }, table1);
                pg += pgStep;
                Progress.updateBar(pg);

                // initiate AIMN and populate
                AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table1);
                double r = aimn.getR();
                pg += pgStep;
                Progress.updateBar(pg);

                // ensure that test for each c is the same
                Random random = new Random(SEED);

                // results
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Vector q1 = db.getRandomVector(table1, random);
                    aimn.queryFast(q1);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    pg += pgStep;
                    Progress.updateBar(pg);

                    // calculate distances
                    Result dists = new Result().loadDistancesBetween(q1, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(dists, queryList, r, c * r);
                    Progress.clearStatus();

                    pg += pgStep;
                    Progress.updateBar(pg);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%d,%s,%s\n",
                        n, stats.stats(), stats.pr()));
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

    public static void exp4() throws Exception {
        String name = "accuracy4";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        double c = 1.5;
        int k = 100;
        int reps = 10;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int[] dPrimeValues = new int[] { 5, 10, 15, 20, 25, 50, 100, 200, 300 };

        // progress bar
        Progress.newBar("Experiment " + name, reps * (dPrimeValues.length * 6));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("d', " + stats.statsHeader() + ", " + stats.prHeader() + "\n");

            for (int dPrime : dPrimeValues) {
                stats.reset();
                Random random = new Random(SEED);

                for (int i = 0; i < reps; i++) {
                    // load vectors to DB
                    String table1 = "vectors1";
                    db.loadVectorsIntoDB(table1, filepathSource, n, d);
                    Progress.updateBar(++pg);

                    // find r for k-nearest neighbors and compute scaling factor
                    Vector q1 = db.getRandomVector(table1, random);
                    Result dists = new Result().loadDistancesBetween(q1, table1, db);
                    double initial_r = dists.distanceToKNearest(k);
                    double target_r = 1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0);
                    double scalingFactor = target_r / initial_r;
                    Progress.updateBar(++pg);

                    // process vectors
                    NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        v.multiply(scalingFactor);
                        v = nd.transform(v);
                        return v.dataString();
                    }, table1);
                    Progress.updateBar(++pg);

                    // update q to transformed version
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table1);
                    double s = NashDevice.getDistortionFactor(d, dPrime, target_r, 0.5, new Random(SEED));

                    // initiate AIMN and populate
                    AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(false);
                    aimn.populateFromDB(table1);
                    double r = aimn.getR();
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    Progress.updateBar(++pg);

                    // calculate distances
                    Result distances = new Result().loadDistancesBetween(q2, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(distances, queryList, r, c * r);
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                // write result to file
                writer.write(String.format(Locale.US, "%d,%s,%s\n",
                        dPrime, stats.stats(), stats.pr()));
            }

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial r for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    public static void exp5() throws Exception {
        String name = "accuracy5";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        int dPrime = 300;
        double c = 1.5;
        int reps = 1;
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int[] kValues = new int[] { 100, 500, 1000, 2500, 5000 };

        // progress bar
        Progress.newBar("Experiment " + name, reps * (kValues.length * 6));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("accuracy\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write("k, " + stats.statsHeader() + ", " + stats.prHeader() + "\n");

            for (int k : kValues) {
                stats.reset();
                Random random = new Random(SEED);

                for (int i = 0; i < reps; i++) {
                    // load vectors to DB
                    String table1 = "vectors1";
                    db.loadVectorsIntoDB(table1, filepathSource, n, d);
                    Progress.updateBar(++pg);

                    // find r for k-nearest neighbors and compute scaling factor
                    Vector q1 = db.getRandomVector(table1, random);
                    Result dists = new Result().loadDistancesBetween(q1, table1, db);
                    double initial_r = dists.distanceToKNearest(k);
                    double target_r = 1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0);
                    double scalingFactor = target_r / initial_r;
                    Progress.updateBar(++pg);

                    // process vectors
                    NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        v.multiply(scalingFactor);
                        v = nd.transform(v);
                        return v.dataString();
                    }, table1);
                    Progress.updateBar(++pg);

                    // update q to transformed version
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table1);
                    double s = NashDevice.getDistortionFactor(d, dPrime, target_r, 0.5, new Random(SEED));

                    // initiate AIMN and populate
                    AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(false);
                    aimn.populateFromDB(table1);
                    double r = aimn.getR();
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    Progress.updateBar(++pg);

                    // calculate distances
                    Result distances = new Result().loadDistancesBetween(q2, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(distances, queryList, r, c * r);
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                // write result to file
                writer.write(String.format(Locale.US, "%d,%s,%s\n",
                        k, stats.stats(), stats.pr()));
            }

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    // single run
    public static void exp10() throws Exception {
        String name = "accuracy10";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        int dPrime = 500;
        double c = 1.5;
        int k = 100;
        int reps = 1;

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;

        // progress bar
        Progress.newBar("Experiment " + name, reps * 6);
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write(stats.statsHeader() + ", " + stats.prHeader() + "\n");

            for (int i = 0; i < reps; i++) {
                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                Random random = new Random(SEED);

                // find r for k-nearest neighbors and compute scaling factor
                Vector q1 = db.getRandomVector(table1, random);
                Result dists = new Result().loadDistancesBetween(q1, table1, db);
                double initial_r = dists.distanceToKNearest(k);
                Progress.printAbove("i_r: " + initial_r);
                double target_r = 1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0);
                Progress.printAbove("t_r: " + target_r);
                double scalingFactor = target_r / initial_r;
                Progress.printAbove("scale: " + scalingFactor);
                Progress.updateBar(++pg);

                // process vectors
                NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    v.multiply(scalingFactor);
                    v = nd.transform(v);
                    return v.dataString();
                }, table1);
                Progress.updateBar(++pg);

                double s = 0.9525;

                // update q to transformed version
                Vector q2 = db.getVectorByLabel(q1.getLabel(), table1);

                // initiate AIMN and populate
                AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table1);
                double r = aimn.getR() * s;
                Progress.updateBar(++pg);

                // ensure that test for each theta is the same
                random = new Random(SEED);

                // results
                aimn.queryFast(q2);
                Set<String> queryList = new HashSet<>(aimn.queryList());
                Progress.updateBar(++pg);

                // calculate distances
                Result distances = new Result().loadDistancesBetween(q2, table1, db);

                // write results
                Progress.newStatus("writing results...");
                stats.update(distances, queryList, r, c * r);
                Progress.clearStatus();
                Progress.updateBar(++pg);
            }
            // write result to file
            writer.write(String.format(Locale.US, "%s,%s\n",
                    stats.stats(), stats.pr()));

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial r for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    public static void exp11() throws Exception {
        String name = "accuracy11";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int d = 300;
        int dPrime = 300;
        int k = 5000;
        int reps = 1;

        double[] cValues = new double[] { 1.01, 1.05, 1.1, 1.15, 1.2, 1.25, 1.3, 1.35, 1.4, 1.45, 1.5 };
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;

        // progress bar
        Progress.newBar("Experiment " + name, cValues.length * (reps * 6));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("c," + stats.statsHeader() + ", " + stats.prHeader() + "\n");

            for (double c : cValues) {
                stats.reset();
                Random random = new Random(SEED);

                for (int i = 0; i < reps; i++) {
                    // load vectors to DB
                    String table1 = "vectors1";
                    db.loadVectorsIntoDB(table1, filepathSource, n, d);
                    Progress.updateBar(++pg);

                    // find r for k-nearest neighbors and compute scaling factor
                    Vector q1 = db.getRandomVector(table1, random);
                    Result dists = new Result().loadDistancesBetween(q1, table1, db);
                    double initial_cr = dists.distanceToKNearest(k);
                    double target_cr = c * (1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0));
                    double scalingFactor = target_cr / initial_cr;
                    Progress.updateBar(++pg);

                    // process vectors
                    NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        v.multiply(scalingFactor);
                        v = nd.transform(v);
                        return v.dataString();
                    }, table1);
                    Progress.updateBar(++pg);

                    double s = NashDevice.getDistortionFactor(d, dPrime, target_cr, 1.0, new Random(SEED));

                    // update q to transformed version
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table1);
                    // initiate AIMN and populate
                    AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(false);
                    aimn.populateFromDB(table1);
                    double r = aimn.getR() * s;
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    Progress.updateBar(++pg);

                    // calculate distances
                    Result distances = new Result().loadDistancesBetween(q2, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(distances, queryList, r, c * r);
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                // write result to file
                writer.write(String.format(Locale.US, "%.3f,%s,%s\n",
                        c, stats.stats(), stats.pr()));
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial cr for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    // optimize r range
    public static void exp100() throws Exception {
        String name = "accuracy100";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int dPrime = 300;
        double c = 1.05;
        double s = 1.0;
        int k = 1000;
        int d = 300;
        int reps = 1;

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;

        // progress bar
        Progress.newBar("Experiment " + name, reps * 6);
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write(stats.statsHeader() + ", " + stats.prHeader() + "\n");

            for (int i = 0; i < reps; i++) {
                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                Random random = new Random(SEED);

                // find r for k-nearest neighbors and compute scaling factor
                Vector q1 = db.getRandomVector(table1, random);
                Result dists = new Result().loadDistancesBetween(q1, table1, db);
                double initial_r = dists.distanceToKNearest(k);
                Progress.printAbove("i_r: " + initial_r);
                double target_r = 1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0);
                Progress.printAbove("t_r: " + target_r);
                double scalingFactor = target_r / initial_r;
                Progress.printAbove("scale: " + scalingFactor);
                Progress.updateBar(++pg);

                // process vectors
                NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    v.multiply(scalingFactor);
                    v = nd.transform(v);
                    return v.dataString();
                }, table1);
                Progress.updateBar(++pg);

                // update q to transformed version
                Vector q2 = db.getVectorByLabel(q1.getLabel(), table1);

                // initiate AIMN and populate
                AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table1);
                double r = aimn.getR() * s;
                Progress.updateBar(++pg);

                // ensure that test for each theta is the same
                random = new Random(SEED);

                // results
                aimn.queryFast(q2);
                Set<String> queryList = new HashSet<>(aimn.queryList());
                Progress.updateBar(++pg);

                // calculate distances
                Result distances = new Result().loadDistancesBetween(q2, table1, db);

                // write results
                Progress.newStatus("writing results...");
                stats.update(distances, queryList, r, c * r);
                Progress.clearStatus();
                Progress.updateBar(++pg);
            }
            // write result to file
            writer.write(String.format(Locale.US, "%s,%s\n",
                    stats.stats(), stats.pr()));

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial r for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    // optimize cr range
    public static void exp101() throws Exception {
        String name = "accuracy101";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_00;
        int dPrime = 300;
        double c = 1.05;
        double s = 1.0;
        int k = 1000;

        int d = 300;
        int reps = 1;

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;

        // progress bar
        Progress.newBar("Experiment " + name, reps * 6);
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write(stats.statsHeader() + ", " + stats.prHeader() + "\n");

            for (int i = 0; i < reps; i++) {
                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                Random random = new Random(SEED);

                // find r for k-nearest neighbors and compute scaling factor
                Vector q1 = db.getRandomVector(table1, random);
                Result dists = new Result().loadDistancesBetween(q1, table1, db);
                double initial_cr = dists.distanceToKNearest(k);
                double target_cr = c * (1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0));
                double scalingFactor = target_cr / initial_cr;
                Progress.updateBar(++pg);

                // process vectors
                NashDevice nd = new NashDevice(d, dPrime, new Random(SEED));
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    v.multiply(scalingFactor);
                    v = nd.transform(v);
                    return v.dataString();
                }, table1);
                Progress.updateBar(++pg);

                // update q to transformed version
                Vector q2 = db.getVectorByLabel(q1.getLabel(), table1);

                // initiate AIMN and populate
                AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table1);
                double r = aimn.getR() * s;
                Progress.updateBar(++pg);

                // ensure that test for each theta is the same
                random = new Random(SEED);

                // results
                aimn.queryFast(q2);
                Set<String> queryList = new HashSet<>(aimn.queryList());
                Progress.updateBar(++pg);

                // calculate distances
                Result distances = new Result().loadDistancesBetween(q2, table1, db);

                // write results
                Progress.newStatus("writing results...");
                stats.update(distances, queryList, r, c * r);
                Progress.clearStatus();
                Progress.updateBar(++pg);
            }
            // write result to file
            writer.write(String.format(Locale.US, "%s,%s\n",
                    stats.stats(), stats.pr()));

            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial cr for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }
}