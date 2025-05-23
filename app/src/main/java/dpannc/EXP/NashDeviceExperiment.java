package dpannc.EXP;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import dpannc.Progress;
import dpannc.Vector;
import dpannc.AIMN.NashDevice;
import dpannc.database.DB;

public class NashDeviceExperiment {

    public static void main(String[] args) throws Exception {
        exp2();
        // exp3();
        // exp4();
        // exp5();
        // exp6();
        // exp7();
        // exp12();
        // exp14();
        // distMap();
    }

    /*
     * ***************
     * *** NASH 01 ***
     * ***************
     * 
     * difference caused by transformation
     * for increasing r
     */
    public static void exp1() throws Exception {
        String name = "nash1";

        // settings
        int SEED = 100;
        Random random = new Random(SEED);
        int n = 1000; // sample size
        int d = 300; // dimensions
        int D = 1000;

        double min = 0.1;
        double max = 2;
        double inc = 0.05;

        NashDevice nd = new NashDevice(d, D, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance / difference after transformation\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("dist,stddev\n"); // coulumns

            for (double dist = min; dist < max; dist += inc) {
                Result diff = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleWithDistance(dist, random);

                    // apply Nash Transform
                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    // save distances before and after transformation
                    diff.add("" + i, q1.distance(v1) - q2.distance(v2));
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.2f,%.5f\n",
                        dist, diff.stddev()));
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", d'=" + D + ", repetitions=" + n + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * ***************
     * *** NASH 02 ***
     * ***************
     * 
     * transformed distance
     * for increasing r
     */
    public static void exp2() throws Exception {
        String name = "nash2";

        // settings
        int SEED = 100;
        Random random = new Random(SEED);
        int n = 1000; // sample size
        int d = 300; // dimensions
        int D = 300;

        double min = 0.1;
        double max = 2.4;
        double inc = 0.05;

        NashDevice nd = new NashDevice(d, D, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed distance\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3\n"); // columns on y-axis

            writer.write("initial distance, 1st percentile, median, 99th percentile\n"); // coulumns

            for (double dist = min; dist < max; dist += inc) {
                Result transformedDists = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleWithDistance(dist, random);

                    // apply Nash Transform
                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    // save distances after transformation
                    transformedDists.add("" + i, q2.distance(v2));
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.2f,%.5f,%.5f,%.5f\n",
                        dist, transformedDists.percentile(0.01), transformedDists.median(), transformedDists.percentile(0.99)));
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", d'=" + D + ", repetitions=" + n + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * ***************
     * *** NASH 02 ***
     * ***************
     * 
     * transformed distance
     * for increasing r
     */
    public static void exp3() throws Exception {
        String name = "nash3";

        // settings
        int SEED = 100;
        Random random = new Random(SEED);
        int n = 1000; // sample size
        int d = 784; // dimensions
        int D = 784;

        double min = 0.1;
        double max = 2;
        double inc = 0.05;

        NashDevice nd = new NashDevice(d, D, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed distance\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("initial distance, 1th percentile median transformed distance, 99th percentile\n"); // coulumns

            for (double dist = min; dist < max; dist += inc) {
                Result transformedDists = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleWithDistance(dist, random);

                    // apply Nash Transform
                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    // save distances after transformation
                    transformedDists.add("" + i, q2.distance(v2));
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.2f,%.5f,%.5f,%.5f\n",
                        dist, transformedDists.percentile(0.01), transformedDists.median(), transformedDists.percentile(0.99)));
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", d'=" + D + ", repetitions=" + n + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * *********************
     * 9 *
     * *********************
     */
    public static void exp9() throws Exception {
        String name = "nash9";

        // settings
        int SEED = 10;
        int d = 300;
        int dPrime = 300;

        double min = 0.1;
        double max = 1.4;
        double inc = 0.05;

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / distortion factor\n");
            writer.write("0\n");
            writer.write("1,2,3,4,5\n");
            writer.write("dist, p1, p25, median, p75, p99\n");

            for (double dist = min; dist < max; dist += inc) {

                // each measures percentile based on 1000 vectors
                double p5 = NashDevice.getDistortionFactor(d, dPrime, dist, 0.01, new Random(SEED));
                double p25 = NashDevice.getDistortionFactor(d, dPrime, dist, 0.25, new Random(SEED));
                double median = NashDevice.getDistortionFactor(d, dPrime, dist, 0.5, new Random(SEED));
                double p75 = NashDevice.getDistortionFactor(d, dPrime, dist, 0.75, new Random(SEED));
                double p95 = NashDevice.getDistortionFactor(d, dPrime, dist, 0.99, new Random(SEED));

                writer.write(String.format(Locale.US, "%.5f, %.5f, %.5f, %.5f, %.5f, %.5f\n",
                        dist, p5, p25, median, p75, p95));
            }

            writer.write("# SEED=" + SEED + ", d=" + d + ", d'=" + dPrime + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * *********************
     * 9 *
     * *********************
     */
    public static void exp10() throws Exception {
        String name = "nash10";

        // settings
        int SEED = 10;
        int d = 300;
        double dist = 0.8;

        int min = 20;
        int max = 800;
        int inc = 40;

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / distortion factor\n");
            writer.write("0\n");
            writer.write("1,2,3,4,5\n");
            writer.write("d', p1, p25, median, p75, p99\n");

            for (int dPrime = min; dPrime < max; dPrime += inc) {
                double p1 = NashDevice.getDistortionFactor(d, dPrime, dist, 0.01, new Random(SEED));
                double p25 = NashDevice.getDistortionFactor(d, dPrime, dist, 0.25, new Random(SEED));
                double median = NashDevice.getDistortionFactor(d, dPrime, dist, 0.5, new Random(SEED));
                double p75 = NashDevice.getDistortionFactor(d, dPrime, dist, 0.75, new Random(SEED));
                double p99 = NashDevice.getDistortionFactor(d, dPrime, dist, 0.99, new Random(SEED));

                writer.write(String.format(Locale.US, "%d, %.5f, %.5f, %.5f, %.5f, %.5f\n",
                        dPrime, p1, p25, median, p75, p99));
            }

            writer.write("# SEED=" + SEED + ", d=" + d + ", dist=" + dist + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * ***************
     * *** NASH 14 ***
     * ***************
     * 
     * Binary search for best gamma value
     * compares d and d'
     */
    public static void exp14() throws Exception {
        String name = "nash14";
        // DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 100_000;
        double r = 1.0 / Math.pow(Math.log(n) / Math.log(10), 1.0 / 8.0);
        int reps = 1000;

        int[] dValues = new int[] { 5, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400,
                425, 450, 475, 500 };
        int[] DValues = dValues;

        // for binary search
        double p = 0.99; // success threshold
        double eps = 1e-3; // search precision
        double gammaMax = 1.0;

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("d / d'\n");
            writer.write("0\n");
            writer.write("1\n");
            writer.write("d,d',gamma\n");

            Progress.newBar("Experiment " + name, (dValues.length));
            int pg = 0;

            writer.write("-----");
            for (int D : DValues) {
                writer.write(String.format(Locale.US, ",%d", D));
            }
            writer.write("\n");
            for (int d : dValues) {
                writer.write(String.format(Locale.US, "%d", d));
                int counter = 0;
                Progress.newStatusBar("d: " + d + ": ", DValues.length);
                for (int D : DValues) {
                    double low = 0.0;
                    double high = gammaMax;
                    double bestGamma = -1;

                    while (high - low > eps) {
                        double mid = (low + high) / 2.0;
                        int fails = 0;

                        for (int i = 0; i < reps; i++) {
                            Vector q1 = new Vector(d).randomGaussian(random);
                            Vector v1 = q1.sampleWithDistance(Math.sqrt(r), random);
                            double dist = q1.distance(v1);

                            NashDevice nd = new NashDevice(d, D, random);
                            Vector q2 = nd.transform(q1);
                            Vector v2 = nd.transform(v1);
                            double tDist = q2.distance(v2);

                            if (tDist > (1 + mid) * dist || tDist < (1 - mid) * dist) {
                                fails++;
                            }
                        }

                        double failureRate = (double) fails / reps;
                        if (failureRate <= 1.0 - p) {
                            // try smaller gamma
                            bestGamma = mid;
                            high = mid;
                        } else {
                            // try larger gamma
                            low = mid;
                        }
                    }
                    Progress.updateStatusBar(++counter);
                    writer.write(String.format(Locale.US, ",%.4f", bestGamma));
                }
                Progress.clearStatus();
                Progress.updateBar(++pg);
                writer.write("\n");
            }
            Progress.end();
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# r=" + r + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * ***************
     * *** NASH 15 ***
     * ***************
     * 
     * Binary search for best gamma value
     * compares d' and r
     */
    public static void exp15() throws Exception {
        String name = "nash15";
        // DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 10;
        Random random = new Random(SEED);

        int reps = 1000;
        int d = 300;

        int[] DValues = new int[] { 5, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400,
                425, 450, 475, 500 };

        double[] rValues = new double[] { 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 1.0,
                1.05, 1.1, 1.15, 1.2 };

        double p = 0.99; // success threshold
        double eps = 1e-3; // search precision
        double gammaMax = 1.0;

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("d / d'\n");
            writer.write("0\n");
            writer.write("1\n");
            writer.write("d',r, gamma\n");

            Progress.newBar("Experiment " + name, (DValues.length));
            int pg = 0;

            writer.write("-----");
            for (double r : rValues) {
                writer.write(String.format(Locale.US, ",%.3f", r));
            }
            writer.write("\n");
            for (int D : DValues) {
                writer.write(String.format(Locale.US, "%d", D));
                int counter = 0;
                Progress.newStatusBar("d': " + D + ": ", rValues.length);
                for (double r : rValues) {
                    double low = 0.0;
                    double high = gammaMax;
                    double bestGamma = -1;

                    while (high - low > eps) {
                        double mid = (low + high) / 2.0;
                        int fails = 0;

                        for (int i = 0; i < reps; i++) {
                            Vector q1 = new Vector(d).randomGaussian(random);
                            Vector v1 = q1.sampleWithDistance(Math.sqrt(r), random);
                            double dist = q1.distance(v1);

                            NashDevice nd = new NashDevice(d, D, random);
                            Vector q2 = nd.transform(q1);
                            Vector v2 = nd.transform(v1);
                            double tDist = q2.distance(v2);

                            if (tDist > (1 + mid) * dist || tDist < (1 - mid) * dist) {
                                fails++;
                            }
                        }

                        double failureRate = (double) fails / reps;
                        if (failureRate <= 1.0 - p) {
                            // gamma is sufficient — try smaller
                            bestGamma = mid;
                            high = mid;
                        } else {
                            // too many failures — need more tolerance
                            low = mid;
                        }

                    }
                    Progress.updateStatusBar(++counter);
                    writer.write(String.format(Locale.US, ",%.4f", bestGamma));
                }
                Progress.clearStatus();
                Progress.updateBar(++pg);
                writer.write("\n");
            }
            Progress.end();
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# initial d=" + d + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * ***************
     * *** NASH 16 ***
     * ***************
     * 
     * Tracks region transitions
     * for increasing r
     */
    public static void exp16() throws Exception {
        String name = "nash16";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);

        int n = 100_000;
        int d = 300;
        int dPrime = 600;
        int reps = 5;
        double c = 1.1;

        double[] rValues = new double[] { 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9 };

        Progress.newBar("Experiment " + name, (3 + reps * rValues.length));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/NASH/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("r / region transitions\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9\n"); // columns on y-axis
            writer.write(
                    "r, i2f, f2i, f2o, o2f, i2o, o2i, inner_count, fuzzy_count, outer_count\n");

            // load vectors to DB
            String table1 = "vectors1";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            Progress.updateBar(++pg);
            String table2 = "vectors2";
            db.loadVectorsIntoDB(table2, filepathSource, n, d);
            Progress.updateBar(++pg);

            // transform vectors
            NashDevice nd = new NashDevice(d, dPrime, random);
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                // v.multiply(1.1);
                v = nd.transform(v);
                return v.dataString();
            }, table1);
            Progress.updateBar(++pg);

            // results
            for (double r : rValues) {
                double i2f = 0, f2i = 0, f2o = 0, o2f = 0, i2o = 0, o2i = 0;
                double B_inner_total = 0, B_fuzzy_total = 0, B_outer_total = 0;
                for (int i = 0; i < reps; i++) {
                    // choose query point
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);

                    // calculate all distances
                    Result Ares = new Result().loadDistancesBetween(q1, table1, db);
                    Result Bres = new Result().loadDistancesBetween(q2, table2, db);

                    Progress.newStatus("writing results...");

                    // INNER REGION results
                    Set<String> A_inner = Ares.lessThan(r);
                    Set<String> B_inner = Bres.lessThan(r);
                    // inner.update(B_inner, A_inner);

                    // FUZZY REGION results
                    Set<String> A_fuzzy = Ares.within(r, c * r);
                    Set<String> B_fuzzy = Bres.within(r, c * r);
                    // fuzzy.update(B_fuzzy, A_fuzzy);

                    // OUTER REGION results
                    Set<String> A_outer = Ares.greaterThan(c * r);
                    Set<String> B_outer = Bres.greaterThan(c * r);
                    // outer.update(B_outer, A_outer);

                    B_inner_total += B_inner.size() / reps;
                    B_fuzzy_total += B_fuzzy.size() / reps;
                    B_outer_total += B_outer.size() / reps;

                    Set<String> i_to_f = new HashSet<>(B_inner);
                    i_to_f.retainAll(A_fuzzy);

                    Set<String> f_to_i = new HashSet<>(B_fuzzy);
                    f_to_i.retainAll(A_inner);

                    Set<String> f_to_o = new HashSet<>(B_fuzzy);
                    f_to_o.retainAll(A_outer);

                    Set<String> o_to_f = new HashSet<>(B_outer);
                    o_to_f.retainAll(A_fuzzy);

                    Set<String> i_to_o = new HashSet<>(B_inner);
                    i_to_o.retainAll(A_outer);

                    Set<String> o_to_i = new HashSet<>(B_outer);
                    o_to_i.retainAll(A_inner);

                    i2f += i_to_f.size() / reps;
                    f2i += f_to_i.size() / reps;
                    f2o += f_to_o.size() / reps;
                    o2f += o_to_f.size() / reps;
                    i2o += i_to_o.size() / reps;
                    o2i += o_to_i.size() / reps;

                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                writer.write(String.format(Locale.US,
                        "%.3f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.1f,%.1f,%.1f\n",
                        r, i2f, i2o, f2i, f2o, o2i, o2f, B_inner_total, B_fuzzy_total, B_outer_total));
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n=" + n + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# d=" + d + "\n");
            writer.write("# d'=" + dPrime + "\n");
            writer.write("# c=" + c + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            Progress.printAbove(e.getMessage());
        }
        Progress.end();
    }

    /*
     * ***************
     * *** NASH 17 ***
     * ***************
     * 
     * Tracks region transitions
     * for increasing d'
     */
    public static void exp17() throws Exception {
        String name = "nash17";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);

        int n = 100_000;
        int d = 300;
        int reps = 5;
        double r = 1.0 / Math.pow(Math.log(n) / Math.log(10), 1.0 / 8.0);
        double c = 1.1;

        int[] dPrimeValues = new int[] { 300, 400, 500, 600, 700, 800, 900, 1000, 1100, 1200, 1300 };

        Progress.newBar("Experiment " + name, 1 + (2 + reps) * dPrimeValues.length);
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/NASH/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("d' / region transitions\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9\n"); // columns on y-axis
            writer.write(
                    "d', i2f, f2i, f2o, o2f, i2o, o2i, inner_count, fuzzy_count, outer_count\n");

            // load vectors to DB
            String table2 = "vectors2";
            db.loadVectorsIntoDB(table2, filepathSource, n, d);
            Progress.updateBar(++pg);

            // results
            for (int dPrime : dPrimeValues) {
                String table1 = "vectors1";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                Progress.updateBar(++pg);

                // transform vectors
                NashDevice nd = new NashDevice(d, dPrime, random);
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    // v.multiply(1.1);
                    v = nd.transform(v);
                    return v.dataString();
                }, table1);
                Progress.updateBar(++pg);

                double i2f = 0, f2i = 0, f2o = 0, o2f = 0, i2o = 0, o2i = 0;
                double B_inner_total = 0, B_fuzzy_total = 0, B_outer_total = 0;
                for (int i = 0; i < reps; i++) {
                    // choose query point
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);

                    // calculate all distances
                    Result Ares = new Result().loadDistancesBetween(q1, table1, db);
                    Result Bres = new Result().loadDistancesBetween(q2, table2, db);

                    Progress.newStatus("writing results...");

                    // INNER REGION results
                    Set<String> A_inner = Ares.lessThan(r);
                    Set<String> B_inner = Bres.lessThan(r);
                    // inner.update(B_inner, A_inner);

                    // FUZZY REGION results
                    Set<String> A_fuzzy = Ares.within(r, c * r);
                    Set<String> B_fuzzy = Bres.within(r, c * r);
                    // fuzzy.update(B_fuzzy, A_fuzzy);

                    // OUTER REGION results
                    Set<String> A_outer = Ares.greaterThan(c * r);
                    Set<String> B_outer = Bres.greaterThan(c * r);
                    // outer.update(B_outer, A_outer);

                    B_inner_total += B_inner.size() / reps;
                    B_fuzzy_total += B_fuzzy.size() / reps;
                    B_outer_total += B_outer.size() / reps;

                    Set<String> i_to_f = new HashSet<>(B_inner);
                    i_to_f.retainAll(A_fuzzy);

                    Set<String> f_to_i = new HashSet<>(B_fuzzy);
                    f_to_i.retainAll(A_inner);

                    Set<String> f_to_o = new HashSet<>(B_fuzzy);
                    f_to_o.retainAll(A_outer);

                    Set<String> o_to_f = new HashSet<>(B_outer);
                    o_to_f.retainAll(A_fuzzy);

                    Set<String> i_to_o = new HashSet<>(B_inner);
                    i_to_o.retainAll(A_outer);

                    Set<String> o_to_i = new HashSet<>(B_outer);
                    o_to_i.retainAll(A_inner);

                    i2f += i_to_f.size() / reps;
                    f2i += f_to_i.size() / reps;
                    f2o += f_to_o.size() / reps;
                    o2f += o_to_f.size() / reps;
                    i2o += i_to_o.size() / reps;
                    o2i += o_to_i.size() / reps;

                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                writer.write(String.format(Locale.US,
                        "%.3f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.1f,%.1f,%.1f\n",
                        dPrime, i2f, i2o, f2i, f2o, o2i, o2f, B_inner_total, B_fuzzy_total, B_outer_total));
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# n=" + n + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# r=" + r + "\n");
            writer.write("# d=" + d + "\n");
            writer.write("# c=" + c + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            Progress.printAbove(e.getMessage());
        }
        Progress.end();
    }
}