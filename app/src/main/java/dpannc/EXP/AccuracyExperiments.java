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
        String name = "accuracy1a";
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
                // v.normalize();
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
    public static void exp2() throws Exception {
        String name = "accuracy2";
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
        double[] sValues = new double[] { 0.87, 0.88, 0.885, 0.89, 0.895, 0.9, 0.905, 0.91, 0.92, 0.93, 0.94, 0.95,
                1.0 };

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

    // for increasing dataset size n
    public static void exp3() throws Exception {
        String name = "accuracy3";
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
        int[] nValues = new int[] { 100_000, 200_000, 300_000, 400_000, 500_000, 600_000, 700_000, 800_000, 900_000,
                1_000_000 };

        // progress bar
        Progress.newBar("Experiment " + name, nValues.length * (3 + reps * 2));
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

                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);

                Progress.updateBar(pg);

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
                AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
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
                    Vector q1 = db.getVectorByLabel("overman", table1);
                    // Vector q1 = Vector.fromString("tigrinus", "-0.0381 -0.0317 -0.0227 -0.0611
                    // 0.0522 -0.0997 0.0037 0.0048 -0.0262 -0.0105 -0.0693 0.0673 -0.0117 0.0118
                    // 0.0336 -0.127 -0.0136 -0.046 0.0682 0.0772 -0.0131 -0.0533 0.0321 0.0843
                    // -0.0061 -0.0307 0.0431 0.0491 0.0092 0.222 0.0409 0.032 -8.0E-4 0.0788 0.0248
                    // 0.0145 -0.0072 0.0476 0.0521 0.0214 0.0946 0.0902 -0.0152 -0.0406 0.1325
                    // -0.0732 0.0181 0.0217 -0.0708 0.0938 -0.0964 -0.0391 -0.06 -0.08 0.0021
                    // -0.0225 0.0202 0.0195 0.0026 0.0101 -0.0434 0.0467 0.0302 0.067 -0.056
                    // -0.0634 0.0411 -0.0631 -0.1085 -0.0481 0.096 0.0313 0.0344 -0.0358 0.0027
                    // 0.0455 0.0613 -0.0345 -0.0343 -0.0019 -0.0568 -0.0681 -0.0335 -0.1245 -0.0079
                    // -0.01 -0.0114 0.0621 -4.0E-4 -0.0727 0.0053 -0.0804 -0.1059 -0.0967 -0.0022
                    // 0.0019 -0.0194 -0.0684 0.0254 0.0732 -0.0457 0.0074 -0.1146 -0.0197 1.0E-4
                    // 0.0182 -0.0478 0.0403 0.0152 -0.1219 -0.0369 0.0165 -3.0E-4 -0.0195 0.0047
                    // 0.0658 0.0611 0.1296 0.0026 -0.0084 -0.0239 0.0624 0.0113 -0.0343 0.0668
                    // -0.0896 0.0196 0.0535 -0.0558 -0.0764 0.0047 -0.0195 0.0398 0.0495 -0.0434
                    // -0.0189 -0.0838 0.0116 0.0516 -0.0375 0.1632 0.0102 -0.0218 0.0345 -0.0111
                    // -0.0417 -0.1574 0.0663 -0.0495 0.0108 -0.0199 0.0246 -0.0901 -0.0216 -0.0814
                    // -0.0266 -0.0716 -0.0474 0.0565 -0.0636 0.0068 0.0727 0.1 0.092 -0.0062
                    // -0.0363 0.1033 -0.0015 -0.0232 -0.012 -0.0464 0.0756 -0.0374 -0.0187 -0.0334
                    // -0.0356 -0.002 -0.0169 -0.0104 0.0328 0.0195 0.0147 0.1209 -0.2176 0.0281
                    // 0.0574 0.0701 0.0591 -0.0092 0.0488 0.0188 -0.0709 -0.0471 0.0163 -0.068
                    // -0.0499 0.0321 0.0229 0.0152 -0.036 0.0272 -0.0062 -0.0043 0.0615 -0.0525
                    // -0.0585 0.021 0.0432 0.0621 -0.0543 -0.0593 0.0686 0.0149 0.0493 -0.004
                    // -0.0075 -0.0302 -5.0E-4 0.0349 0.0227 0.0348 0.0012 0.0038 0.1392 -0.0437
                    // -0.0412 -0.0944 0.0814 -0.0037 0.0048 0.0391 -0.0434 -0.0113 0.0043 -0.008
                    // 0.015 -0.0506 0.0263 0.015 0.0044 -0.002 0.0276 -0.0286 -0.0542 0.072 0.0166
                    // -0.014 0.0252 0.0286 0.0417 -0.0414 0.0356 -0.0669 0.0116 0.0037 0.1004
                    // -0.0081 0.0544 0.0717 -0.0709 -0.0599 -0.0927 -0.0797 0.0275 -0.0398 -0.054
                    // -0.022 -0.0442 -0.0588 0.0108 -0.1006 0.0031 0.0962 -0.0307 0.0103 0.0877
                    // -0.0371 0.0105 -0.0543 -0.0553 0.0307 0.019 -0.0386 0.049 0.0493 0.0098
                    // 0.0253 0.0915 -0.001 -0.0395 0.0361 -0.0411 0.0917 -0.0021 0.0537 0.0038
                    // 0.0203 0.0702 -0.0783 0.0388");

                    aimn.queryFast(q1);
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

    public static void exp4a() throws Exception {
        String name = "accuracy4a";
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

                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                for (int i = 0; i < reps; i++) {
                    String table2 = "vectors2";
                    db.copyTable(table1, table2);
                    Progress.updateBar(++pg);

                    NashDevice nd = new NashDevice(d, dPrime);
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        v = nd.transform(v);
                        return v.dataString();
                    }, table2);
                    Progress.updateBar(++pg);

                    // find r for k-nearest neighbors and compute scaling factor
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);

                    // initiate AIMN and populate
                    AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(false);
                    aimn.populateFromDB(table2);
                    double r = aimn.getR();
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    Progress.updateBar(++pg);

                    // write results
                    Result distances = new Result().loadDistancesBetween(q2, table2, db);
                    stats.update(distances, queryList, r, c * r);
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
            writer.write("# datafile: " + filepathSource + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    public static void exp4b() throws Exception {
        String name = "accuracy4b";
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

                // load vectors to DB
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                for (int i = 0; i < reps; i++) {
                    String table2 = "vectors2";
                    db.copyTable(table1, table2);
                    Progress.updateBar(++pg);

                    NashDevice nd = new NashDevice(d, dPrime);
                    db.applyTransformation(data -> {
                        Vector v = Vector.fromString(".", data);
                        v = nd.transform(v);
                        return v.dataString();
                    }, table2);
                    Progress.updateBar(++pg);

                    // find r for k-nearest neighbors and compute scaling factor
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);

                    // initiate AIMN and populate
                    AIMN aimn = new AIMN(n, dPrime, s, c, sensitivity, epsilon, delta, db);
                    Progress.printAbove(aimn.getSettingsString());
                    aimn.DP(false);
                    aimn.populateFromDB(table2);
                    double r = aimn.getR();
                    Progress.updateBar(++pg);

                    // results
                    aimn.queryFast(q2);
                    Set<String> queryList = new HashSet<>(aimn.queryList());
                    Progress.updateBar(++pg);

                    // write results
                    Result distances = new Result().loadDistancesBetween(q1, table1, db);
                    stats.update(distances, queryList, r, c * r);
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
}