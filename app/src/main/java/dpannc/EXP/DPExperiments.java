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

public class DPExperiments {

    // epsilon
    public static void exp1() throws Exception {
        String name = "dp1";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int dPrime = 300;
        double c = 1.5;
        double s = 1.0;
        int k = 1000;
        int d = 300;
        int reps = 10;

        double sensitivity = 1.0;
        double delta = 1.0/n;
        double[] epsilonValues = new double[] { 1.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0 };
        // progress bar
        Progress.newBar("Experiment " + name, epsilonValues.length * (reps * 5));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("epsilon, threshold, " + stats.statsHeader()
                    + ", noisyCount, totalBuckets, emptyBuckets\n");

            for (double epsilon : epsilonValues) {
                stats.reset();

                double threshold = 0;
                double rawCount = 0;
                double noisyCount = 0;
                double buckets = 0;
                double emptyBuckets = 0;

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
                    double target_r = (1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0));
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
                    aimn.DP(true);
                    aimn.populateFromDB(table1);
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());

                    // calculate distances
                    double r = aimn.getR();
                    Result distances = new Result().loadDistancesBetween(q2, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(distances, queryList, r, c * r);
                    threshold += aimn.noiseThreshold() / reps;
                    rawCount += aimn.getCount() / reps;
                    noisyCount += aimn.getNoisyCount() / reps;
                    buckets += aimn.buckets() / reps;
                    emptyBuckets += aimn.emptyBuckets() / reps;

                    if (rawCount == 0 || noisyCount == 0) {
                        Progress.printAbove("0");
                    } else {
                        Progress.printAbove("raw: " + rawCount + ", noisy: " + noisyCount);
                    }
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                // write result to file
                writer.write(String.format(Locale.US, "%.5f, %.3f, %s, %.0f, %.0f, %.0f\n",
                        epsilon, threshold, stats.stats(), noisyCount, buckets, emptyBuckets));
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# s: " + s + "\n");
            writer.write("# delta: " + delta + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial cr for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

public static void exp2() throws Exception {
        String name = "dp2";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        int dPrime = 300;
        double s = 1.0;
        int k = 1000;
        int d = 300;
        int reps = 10;

        double sensitivity = 1.0;
        double epsilon = 10.0;
        double delta = 1.0/n;
        double[] cValues = new double[] { 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0 };
        // progress bar
        Progress.newBar("Experiment " + name, cValues.length * (reps * 5));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("c, threshold, " + stats.statsHeader()
                    + ", noisyCount, totalBuckets, emptyBuckets\n");

            for (double c : cValues) {
                stats.reset();

                double threshold = 0;
                double rawCount = 0;
                double noisyCount = 0;
                double buckets = 0;
                double emptyBuckets = 0;

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
                    double target_r = (1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0));
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
                    aimn.DP(true);
                    aimn.populateFromDB(table1);
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());

                    // calculate distances
                    double r = aimn.getR();
                    Result distances = new Result().loadDistancesBetween(q2, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(distances, queryList, r, c * r);
                    threshold += aimn.noiseThreshold() / reps;
                    rawCount += aimn.getCount() / reps;
                    noisyCount += aimn.getNoisyCount() / reps;
                    buckets += aimn.buckets() / reps;
                    emptyBuckets += aimn.emptyBuckets() / reps;

                    if (rawCount == 0 || noisyCount == 0) {
                        Progress.printAbove("0");
                    } else {
                        Progress.printAbove("raw: " + rawCount + ", noisy: " + noisyCount);
                    }
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                // write result to file
                writer.write(String.format(Locale.US, "%.5f, %.3f, %s, %.0f, %.0f, %.0f\n",
                        c, threshold, stats.stats(), noisyCount, buckets, emptyBuckets));
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# d': " + dPrime + "\n");
            writer.write("# s: " + s + "\n");
            writer.write("# epsilon: " + epsilon + "\n");
            writer.write("# delta: " + delta + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial cr for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    public static void exp3() throws Exception {
        String name = "dp3";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;

        // settings
        int n = 100_000;
        double c = 1.5;
        double s = 1.0;
        int k = 1000;
        int d = 300;
        int reps = 10;

        double sensitivity = 1.0;
        double epsilon = 10.0;
        double delta = 1.0/n;
        int[] dPrimeValues = new int[] { 400, 500 };
        // int[] dPrimeValues = new int[] { 5, 10, 15, 20, 25, 50, 100, 200, 300 };
        // progress bar
        Progress.newBar("Experiment " + name, dPrimeValues.length * (reps * 5));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D_shuffled.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            Stats stats = new Stats();

            // CSV header
            writer.write("d', threshold, " + stats.statsHeader()
                    + ", noisyCount, totalBuckets, emptyBuckets\n");

            for (int dPrime : dPrimeValues) {
                stats.reset();

                double threshold = 0;
                double rawCount = 0;
                double noisyCount = 0;
                double buckets = 0;
                double emptyBuckets = 0;

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
                    double target_r = (1.0 / Math.pow((Math.log(n) / Math.log(10)), 1.0 / 8.0));
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
                    aimn.DP(true);
                    aimn.populateFromDB(table1);
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());

                    // calculate distances
                    double r = aimn.getR();
                    Result distances = new Result().loadDistancesBetween(q2, table1, db);

                    // write results
                    Progress.newStatus("writing results...");
                    stats.update(distances, queryList, r, c * r);
                    threshold += aimn.noiseThreshold() / reps;
                    rawCount += aimn.getCount() / reps;
                    noisyCount += aimn.getNoisyCount() / reps;
                    buckets += aimn.buckets() / reps;
                    emptyBuckets += aimn.emptyBuckets() / reps;

                    if (rawCount == 0 || noisyCount == 0) {
                        Progress.printAbove("0");
                    } else {
                        Progress.printAbove("raw: " + rawCount + ", noisy: " + noisyCount);
                    }
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                // write result to file
                writer.write(String.format(Locale.US, "%d, %.3f, %s, %.0f, %.0f, %.0f\n",
                        dPrime, threshold, stats.stats(), noisyCount, buckets, emptyBuckets));
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n: " + n + "\n");
            writer.write("# d: " + d + "\n");
            writer.write("# c: " + c + "\n");
            writer.write("# s: " + s + "\n");
            writer.write("# epsilon: " + epsilon + "\n");
            writer.write("# delta: " + delta + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# Initial cr for " + k + "-nearest neighbors\n");
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }
    
}