package dpannc.EXP;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import dpannc.DistMapper;
import dpannc.NashDevice;
import dpannc.Progress;
import dpannc.Vector;
import dpannc.database.DB;

public class NashDeviceExperiment {

    public static void main(String[] args) throws Exception {
        // exp1();
        // exp2();
        // exp3();
        // exp4();
        // exp5();
        // exp6();
        // exp7();
        // exp12();
        exp14();
        // distMap();
    }

    /*
     * ***********************
     * 1 * DISTANCE DIFFERENCE
     * ***********************
     */
    public static void exp1() throws Exception {
        String name = "nash1";

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 100; // sample size
        int d = 300; // dimensions

        double min = 0.001;
        double max = 5;
        double inc = 0.1;

        NashDevice nd = new NashDevice(d, d, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance from qq / difference after transformation\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("dist,median,stddev\n"); // coulumns

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
                writer.write(dist + "," + diff.median() + "," + diff.stddev() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of vectors after transformation: " + 1 + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * ***************************
     * 2 * DISTANCE TRANSFORMATION
     * ***************************
     * description....
     */
    public static void exp2() throws Exception {
        String name = "nash2";

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 100; // sample size
        int d = 300; // dimensions
        int dPrime = 600;

        double min = 0.001;
        double max = 5;
        double inc = 0.1;

        NashDevice nd = new NashDevice(d, dPrime, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed distance\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("initial distance, median transformed distance\n"); // coulumns

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
                writer.write(dist + "," + transformedDists.median() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of vectors after transformation: " + 1 + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * ***********************
     * 1 * DISTANCE DIFFERENCE
     * ***********************
     */
    public static void exp3() throws Exception {
        String name = "nash3";

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 100; // sample size
        int d = 300; // dimensions
        double dist = 0.1;

        double min = 0.1;
        double max = 5;
        double inc = 0.1;

        NashDevice nd = new NashDevice(d, d, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial magnitude of q / difference after transformation\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("magnitude,median,stddev\n"); // coulumns

            for (double mag = min; mag < max; mag += inc) {
                Result diff = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random).setMagnitude(mag);
                    Vector v1 = q1.sampleWithDistance(dist, random);

                    // apply Nash Transform
                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    // save distances before and after transformation
                    diff.add("" + i, q1.distance(v1) - q2.distance(v2));
                }

                // write result to file
                writer.write(mag + "," + diff.median() + "," + diff.stddev() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of vectors after transformation: " + 1 + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * ***************************
     * 2 * DISTANCE TRANSFORMATION
     * ***************************
     * ligegyldig
     */
    public static void exp4() throws Exception {
        String name = "nash4";

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 100; // sample size
        int d = 300; // dimensions
        double dist = 0.1;

        double min = 0.1;
        double max = 5;
        double inc = 0.1;

        NashDevice nd = new NashDevice(d, d, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed distance\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("initial magnitude, median transformed distance\n"); // coulumns

            for (double mag = min; mag < max; mag += inc) {
                Result transformedDists = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random).setMagnitude(mag);
                    Vector v1 = q1.sampleWithDistance(dist, random);

                    // apply Nash Transform
                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    // save distances after transformation
                    transformedDists.add("" + i, q2.distance(v2));
                }

                // write result to file
                writer.write(dist + "," + transformedDists.median() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of vectors after transformation: " + 1 + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * *********************
     * 5 - PRECISION DIMENSIONALITY REDUCTION - fasttext/english_2M_300D
     * *********************
     */
    public static void exp5() throws Exception {
        String name = "nash5";
        DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 60_000; // sample size
        int d = 300; // dimensions
        int reps = 10;
        double r = 1.0;

        int[] d_ = new int[] { 10, 20, 50, 100, 150, 200, 250 };

        Progress.newBar("Precision in dimensionality reduction", 1 + d_.length * 2 + d_.length);
        int pg = 0;

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8\n"); // columns on y-axis
            writer.write("r, rPrime, brute, trueFound, notFound, falseFound, precisionTotal, recallTotal, f1Total\n"); // coulumns

            // load into database
            Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
            String table1 = "table1";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            Progress.updateBar(++pg);

            for (int j = 0; j < d_.length; j++) {
                String table2 = "table2";
                db.loadVectorsIntoDB(table2, filepathSource, n, d);
                Progress.updateBar(++pg);

                // scale vectors and apply Nash transform
                NashDevice nd = new NashDevice(d, d_[j], random);
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    v = nd.transform(v);
                    return v.dataString();
                }, table2);
                Progress.updateBar(++pg);

                // double rPrime = DistMapper.getP95(r);
                double rPrime = DistMapper.getMedian(r);

                double brute = 0;
                double trueFound = 0;
                double notFound = 0;
                double falseFound = 0;
                double precisionTotal = 0;
                double recallTotal = 0;
                double f1Total = 0;
                for (int i = 0; i < reps; i++) {
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);
                    // calculate results
                    Result distsPre = new Result().loadDistancesBetween(q1, table1, db);
                    Result distsPost = new Result().loadDistancesBetween(q2, table2, db);
                    Set<String> A = distsPre.lessThan(r);
                    Set<String> B = distsPost.lessThan(rPrime);

                    Set<String> intersectionSet = new HashSet<String>(A);
                    intersectionSet.retainAll(B);
                    double intersection = intersectionSet.size();

                    // Set<String> unionSet = new HashSet<String>(A);
                    // unionSet.addAll(B);
                    // double union = unionSet.size();

                    double precision = (B.size() == 0) ? 0 : (double) intersection / B.size(); // TP / (TP + FP)
                    double recall = (A.size() == 0) ? 0 : (double) intersection / A.size(); // TP / (TP + FN)
                    double f1 = (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);

                    f1Total += f1 / reps;
                    precisionTotal += precision / reps;
                    recallTotal += recall / reps;
                    brute += A.size() / reps;
                    trueFound += intersection / reps;
                    notFound += (A.size() - intersection) / reps;
                    falseFound += (B.size() - intersection) / reps;
                }
                // write result to file
                writer.write(String.format(Locale.US, "%d,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f\n",
                        d_[j], r, rPrime, brute, trueFound, notFound, falseFound, precisionTotal, recallTotal,
                        f1Total));
                Progress.updateBar(++pg);

            }

            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Average taken over: " + reps + " repetions\n");
            writer.write("# 95th percentile\n");
            writer.write("# Data: " + filepathSource.toString() + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * *********************
     * 6 - PRECISION - fasttext/english_2M_300D
     * *********************
     */
    public static void exp6() throws Exception {
        String name = "nash6";
        DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 10_000; // sample size
        int d = 784; // dimensions
        int reps = 1;

        // load into database
        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt");
        String table1 = "table1";
        String table2 = "table2";
        db.loadVectorsIntoDB(table1, filepathSource, n, d);
        db.loadVectorsIntoDB(table2, filepathSource, n, d);

        // apply Nash Transform
        NashDevice nd = new NashDevice(d, d, random);
        db.applyTransformation(data -> {
            Vector v = Vector.fromString(".", data);
            // v.multiply(scale);
            v = nd.transform(v);
            return v.dataString();
        }, table2);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8\n"); // columns on y-axis

            writer.write("r, rPrime, brute, trueFound, notFound, falseFound, precisionTotal, recallTotal, f1Total\n"); // coulumns

            for (double r = 0.1; r < 2.05; r += 0.1) {
                // double rPrime = DistMapper.getP95(r);
                double rPrime = DistMapper.getMedian(r);

                double brute = 0;
                double trueFound = 0;
                double notFound = 0;
                double falseFound = 0;
                double precisionTotal = 0;
                double recallTotal = 0;
                double f1Total = 0;
                for (int i = 0; i < reps; i++) {
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = nd.transform(q1);
                    // calculate results
                    Result distsPre = new Result().loadDistancesBetween(q1, table1, db);
                    Result distsPost = new Result().loadDistancesBetween(q2, table2, db);
                    Set<String> A = distsPre.lessThan(r);
                    Set<String> B = distsPost.lessThan(rPrime);
                    Set<String> intersectionSet = new HashSet<String>(A);
                    intersectionSet.retainAll(B);
                    Set<String> unionSet = new HashSet<String>(A);
                    unionSet.addAll(B);
                    double intersection = intersectionSet.size();
                    // double union = unionSet.size();
                    double precision = (B.size() == 0) ? 0 : (double) intersection / B.size(); // TP / (TP + FP)
                    double recall = (A.size() == 0) ? 0 : (double) intersection / A.size(); // TP / (TP + FN)
                    double f1 = (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);
                    f1Total += f1 / reps;
                    precisionTotal += precision / reps;
                    recallTotal += recall / reps;
                    brute += A.size() / reps;
                    trueFound += intersection / reps;
                    notFound += (A.size() - intersection) / reps;
                    falseFound += (B.size() - intersection) / reps;
                }
                System.out.println("r: " + r);
                // write result to file
                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f\n",
                        r, rPrime, brute, trueFound, notFound, falseFound, precisionTotal, recallTotal, f1Total));

            }

            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Average taken over: " + reps + " repetions\n");
            writer.write("# 95th percentile\n");
            writer.write("# Data: " + filepathSource.toString() + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * *********************
     * 7 - PRECISION - fashionMNIST/fashionMNIST_60K_784D
     * *********************
     */
    public static void exp7() throws Exception {
        String name = "nash7";
        DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 60_000; // sample size
        int d = 784; // dimensions
        int reps = 10;
        double scale = 1.0 / 3100.0;

        // load into database
        Path filepathSource = Paths.get("app/resources/fashionMNIST/fashionMNIST_60K_784D.txt").toAbsolutePath();
        String table1 = "table1";
        String table2 = "table2";
        db.loadVectorsIntoDB(table1, filepathSource, n, d);
        db.loadVectorsIntoDB(table2, filepathSource, n, d);

        // scale vectors and apply Nash transform
        NashDevice nd = new NashDevice(d, d, random);
        db.applyTransformation(data -> {
            Vector v = Vector.fromString(".", data);
            v.multiply(scale);
            v = nd.transform(v);
            return v.dataString();
        }, table2);

        db.applyTransformation(data -> {
            Vector v = Vector.fromString(".", data);
            v.multiply(scale);
            return v.dataString();
        }, table1);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8\n"); // columns on y-axis
            writer.write("r, rPrime, brute, trueFound, notFound, falseFound, precisionTotal, recallTotal, f1Total\n"); // coulumns

            for (double r = 0.1; r < 1.05; r += 0.1) {
                // double rPrime = DistMapper.getP95(r);
                double rPrime = DistMapper.getMedian(r);

                double brute = 0;
                double trueFound = 0;
                double notFound = 0;
                double falseFound = 0;
                double precisionTotal = 0;
                double recallTotal = 0;
                double f1Total = 0;
                for (int i = 0; i < reps; i++) {
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);
                    // calculate results
                    Result distsPre = new Result().loadDistancesBetween(q1, table1, db);
                    Result distsPost = new Result().loadDistancesBetween(q2, table2, db);
                    Set<String> A = distsPre.lessThan(r);
                    Set<String> B = distsPost.lessThan(rPrime);

                    Set<String> intersectionSet = new HashSet<String>(A);
                    intersectionSet.retainAll(B);
                    double intersection = intersectionSet.size();

                    // Set<String> unionSet = new HashSet<String>(A);
                    // unionSet.addAll(B);
                    // double union = unionSet.size();

                    double precision = (B.size() == 0) ? 0 : (double) intersection / B.size(); // TP / (TP + FP)
                    double recall = (A.size() == 0) ? 0 : (double) intersection / A.size(); // TP / (TP + FN)
                    double f1 = (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);

                    f1Total += f1 / reps;
                    precisionTotal += precision / reps;
                    recallTotal += recall / reps;
                    brute += A.size() / reps;
                    trueFound += intersection / reps;
                    notFound += (A.size() - intersection) / reps;
                    falseFound += (B.size() - intersection) / reps;
                }
                System.out.println("r: " + r);
                // write result to file
                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f\n",
                        r, rPrime, brute, trueFound, notFound, falseFound, precisionTotal, recallTotal, f1Total));

            }

            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Average taken over: " + reps + " repetions\n");
            writer.write("# 95th percentile\n");
            writer.write("# Data: " + filepathSource.toString() + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * *********************
     * 7 * K-NEAREST - fashionMNIST/fashionMNIST_60K_784D
     * *********************
     */
    public static void exp8() throws Exception {
        String name = "nash8";
        DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 1_000; // sample size
        int d = 784; // dimensions
        int rep = 5;
        double scale = 1.0 / 3100.0;

        // load into database
        // Path filepathSource = Paths.get("app/resources/fasttext/dk-300d.txt");
        Path filepathSource = Paths.get("app/resources/fashionMNIST/fashionMNIST_60K_784D.txt").toAbsolutePath();
        String table1 = "table1";
        String table2 = "table2";
        db.loadVectorsIntoDB(table1, filepathSource, n, d);
        db.loadVectorsIntoDB(table2, filepathSource, n, d);

        // apply Nash Transform
        NashDevice nd = new NashDevice(d, d, random);
        db.applyTransformation(data -> {
            Vector v = Vector.fromString(".", data);
            v.multiply(scale);
            v = nd.transform(v);
            return v.dataString();
        }, table2);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("k-nearest neighbours / radius\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("neibours,initialRadius,transformedRadius\n"); // coulumns

            for (int k = 500; k <= 2000; k += 500) {
                Result r1 = new Result();
                Result r2 = new Result();
                for (int i = 0; i < rep; i++) {
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);
                    // calculate distances
                    Result dists1 = new Result().loadDistancesBetween(q1, table1, db);
                    Result dists2 = new Result().loadDistancesBetween(q2, table2, db);
                    r1.add("" + i, dists1.distanceToKNearest(k));
                    r2.add("" + i, dists2.distanceToKNearest(k));
                }
                System.out.println("k: " + k);
                System.out.println(k + " " + r1.mean() + " " + r2.mean());
                // write result to file
                writer.write(String.format(Locale.US, "%d,%.5f,%.5f\n",
                        k, r1.mean(), r2.mean()));
            }

            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Average taken over: " + rep + " repetions\n");
            writer.write("# Data: " + filepathSource.toString() + "\n");
            System.out.println("Written to file: " + filepathTarget.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * *********************
     * 9 * DIST-MAP
     * *********************
     */
    public static void exp9() throws Exception {
        String name = "nash9";
        // DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 1000; // sample size
        int d = 784;

        double min = 1.1;
        double max = 1.4;
        double inc = 0.02;

        NashDevice nd = new NashDevice(d, d, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed distance distribution\n");
            writer.write("0\n");
            writer.write("1,2,3,4\n");
            writer.write("initial,median,mean,p5,p95\n");

            for (double dist = min; dist < max; dist += inc) {
                Result transformedDists = new Result();

                for (int i = 0; i < n; i++) {
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleWithDistance(dist, random);

                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    transformedDists.add("" + i, q2.distance(v2));
                }

                // collect stats
                double median = transformedDists.median();
                double mean = transformedDists.mean();
                double p5 = transformedDists.percentile(0.05);
                double p95 = transformedDists.percentile(0.95);

                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f,%.5f\n",
                        dist, median, mean, p5, p95));
            }

            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * *********************
     * 10 * Error bounds
     * *********************
     */
    public static void exp10() throws Exception {
        String name = "nash10";
        // DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 10;
        int d = 300;
        int D = d / 2;
        // double gamma = Math.sqrt(Math.log(n)/d);
        double gamma = 0.1;

        double min = 0;
        double max = 2 * Math.sqrt(gamma);
        double inc = (max - min) / 50;

        NashDevice nd = new NashDevice(d, d, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance / bounds\n");
            writer.write("0\n");
            writer.write("1,2,3,4,5,6\n");
            writer.write("distance,res1,res2,res3,bound1,bound2,bound3\n");

            for (double dist = min; dist < max; dist += inc) {
                double result1 = 0;
                double result2 = 0;
                double result3 = 0;

                int reps = 1000;
                for (int i = 0; i < reps; i++) {
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleWithDistance(dist, random);

                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    double tDist = q2.distance(v2);

                    if (!(tDist * tDist <= (1 + gamma) * dist * dist)) {
                        result1 += 1.0;
                    }

                    if (dist <= Math.sqrt(gamma) && !(tDist * tDist >= (1 - gamma) * dist * dist)) {
                        result2 += 1.0;
                    }

                    if (dist >= Math.sqrt(gamma) && !(tDist * tDist >= gamma / 2)) {
                        result3 += 1.0;
                    }
                }

                // collect stats
                double res1 = 1.0 / (double) reps * result1;
                double res2 = 1.0 / (double) reps * result2;
                double res3 = 1.0 / (double) reps * result3;
                double bound1 = Math.exp(-(D * gamma * gamma) / 6.0);
                double bound2 = Math.exp(-((3 * D * Math.pow(gamma, 2)) / 128.0));
                double bound3 = Math.exp(-(D * gamma / 128.0));
                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f\n",
                        dist, res1, res2, res3, bound1, bound2, bound3));
            }

            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# gamma = " + gamma + "\n");
            writer.write("# sqrt(gamma) = " + Math.sqrt(gamma) + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    /*
     * *********************
     * 11 * Error bounds
     * *********************
     */
    public static void exp11() throws Exception {
        String name = "nash11";
        // DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 10;
        int d = 300;
        int D = d / 2;
        // double gamma = Math.sqrt(Math.log(n)/d);

        NashDevice nd = new NashDevice(d, d, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance / bounds\n");
            writer.write("0\n");
            writer.write("1,2\n");
            writer.write("distance,failNear,failFar\n");

            double gamma = 0.4;
            double r = 0.4; // pick an r within sqrt(gamma)
            double c = 1.3; // pick a c > 1

            double min = 0;
            double max = 2;
            double inc = (max - min) / 50;

            int reps = 1000;
            double failNear = 0;
            double failFar = 0;
            for (double dist = min; dist < max; dist += inc) {
                for (int i = 0; i < reps; i++) {
                    // NEAR test
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleWithDistance(dist, random);
                    double distA = q1.distance(v1);

                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);
                    double tDistA = q2.distance(v2);

                    if (tDistA > (1 + gamma) * distA) {
                        failNear += 1;
                    }

                    // FAR test
                    Vector v3 = q1.sampleWithDistance(c * dist, random);
                    Vector v3t = nd.transform(v3);
                    double tDistB = q2.distance(v3t);
                    if (tDistB < (1 - gamma) * c * distA) {
                        failFar += 1;
                    }
                }
                writer.write(String.format(Locale.US, "%.3f,%.5f,%.5f\n",
                        dist, failNear / reps, failFar / reps));
            }
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# gamma = " + gamma + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    // HEATMAP
    // gamma/distance/fails
    public static void exp12() throws Exception {
        String name = "nash12";
        // DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 10;
        int d = 300;
        int D = d / 2;
        // double gamma = Math.sqrt(Math.log(n)/d);

        NashDevice nd = new NashDevice(d, d, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            // writer.write("distance / bounds\n");
            // writer.write("0\n");
            // writer.write("1\n");
            // writer.write("distance,gamma,fails\n");

            double c = 1.3; // pick a c > 1

            double gmin = 0;
            double gmax = 0.5;
            double ginc = (gmax - gmin) / 20;

            int reps = 1000;

            double rmin = 0.0;
            double rmax = 1.0;
            double rinc = (rmax - rmin) / 20;
            writer.write("-----");
            for (double r = rmin; r <= rmax; r += rinc) {
                writer.write(String.format(Locale.US, ",%.3f", r));
            }
            writer.write("\n");
            for (double gamma = gmin; gamma <= gmax; gamma += ginc) {
                writer.write(String.format(Locale.US, "%.3f",
                        gamma));
                for (double r = rmin; r <= rmax; r += rinc) {
                    double fails = 0;
                    for (int i = 0; i < reps; i++) {
                        // NEAR test
                        Vector q1 = new Vector(d).randomGaussian(random);
                        Vector v1 = q1.sampleWithDistance(r, random);
                        double dist = q1.distance(v1);

                        Vector q2 = nd.transform(q1);
                        Vector v2 = nd.transform(v1);
                        double tDist = q2.distance(v2);

                        if (tDist > (1 + gamma) * dist || tDist < (1 - gamma) * dist) {
                            fails += 1;
                        }

                    }
                    writer.write(String.format(Locale.US, ",%.3f",
                            1.0 / (double) reps * fails));
                }
                writer.write("\n");
            }
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    // HEATMAP
    // gamma/distance/fails
    public static void exp13() throws Exception {
        String name = "nash13";
        // DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 10;
        // double gamma = Math.sqrt(Math.log(n)/d);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            // writer.write("distance / bounds\n");
            // writer.write("0\n");
            // writer.write("1\n");
            // writer.write("distance,gamma,fails\n");

            double c = 1.3; // pick a c > 1

            double gmin = 0;
            double gmax = 0.5;
            double ginc = (gmax - gmin) / 20;

            int reps = 1000;

            int dmin = 0;
            int dmax = 500;
            int dinc = (dmax - dmin) / 20;
            writer.write("-----");
            for (int d = dmin; d <= dmax; d += dinc) {
                writer.write(String.format(Locale.US, ",%d", d));
            }
            writer.write("\n");
            for (double gamma = gmin; gamma <= gmax; gamma += ginc) {
                writer.write(String.format(Locale.US, "%.3f",
                        gamma));
                for (int d = dmin; d <= dmax; d += dinc) {
                    double fails = 0;
                    for (int i = 0; i < reps; i++) {
                        // NEAR test
                        Vector q1 = new Vector(d).randomGaussian(random);
                        Vector v1 = q1.sampleWithDistance(Math.sqrt(gamma / 2.0), random);
                        double dist = q1.distance(v1);

                        NashDevice nd = new NashDevice(d, d, random);
                        Vector q2 = nd.transform(q1);
                        Vector v2 = nd.transform(v1);
                        double tDist = q2.distance(v2);

                        if (tDist > (1 + gamma) * dist || tDist < (1 - gamma) * dist) {
                            fails += 1;
                        }
                    }
                    writer.write(String.format(Locale.US, ",%.3f",
                            1.0 / (double) reps * fails));
                }
                writer.write("\n");
            }
            writer.write("# SEED=" + SEED + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    // precision - relation between d and d'
    public static void exp14() throws Exception {
        String name = "nash14";
        // DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 10;
        // double gamma = Math.sqrt(Math.log(n)/d);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("d / d'\n");
            writer.write("0\n");
            writer.write("1\n");
            writer.write("distance,gamma,fails\n");

            double r = 0.8;

            int reps = 1000;

            int dmin = 5;
            int dmax = 500;
            int dinc = (dmax - dmin) / 20;

            int Dmin = 5;
            int Dmax = 500;
            int Dinc = (Dmax - Dmin) / 20;

            double p = 0.99; // success threshold
            double eps = 1e-3; // search precision
            double gammaMax = 1.0;

            Progress.newBar("Experiment " + name,
                    ((int) (1 + (dmax - dmin) / dinc) * (int) (1 + (Dmax - Dmin) / Dinc)));
            int pg = 0;

            writer.write("-----");
            for (int D = Dmin; D <= Dmax; D += Dinc) {
                writer.write(String.format(Locale.US, ",%d", D));
            }
            writer.write("\n");
            for (int d = dmin; d <= dmax; d += dinc) {
                writer.write(String.format(Locale.US, "%d", d));
                for (int D = Dmin; D <= Dmax; D += Dinc) {
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
                    writer.write(String.format(Locale.US, ",%.4f", bestGamma));
                    Progress.updateBar(++pg);
                }
                writer.write("\n");
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# reps=" + reps + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    // precision - relation between r and d'
    public static void exp15() throws Exception {
        String name = "nash15";
        // DB db = new DB("DB/AIMN_" + name, true);

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 10;
        // double gamma = Math.sqrt(Math.log(n)/d);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("d / d'\n");
            writer.write("0\n");
            writer.write("1\n");
            writer.write("d',r, gamma\n");

            int reps = 1000;

            int dmin = 150;
            int dmax = 2000;
            int dinc = (dmax - dmin) / 30;

            double rmin = 0.01;
            double rmax = 1.0;
            double rinc = (rmax - rmin) / 30;

            double p = 0.99; // success threshold
            double eps = 1e-3; // search precision
            double gammaMax = 1.0;

            Progress.newBar("Experiment " + name,
                    ((int) (1 + (dmax - dmin) / dinc) * (int) (1 + (rmax - rmin) / rinc)));
            int pg = 0;

            writer.write("-----");
            for (double r = rmin; r <= rmax; r += rinc) {
                writer.write(String.format(Locale.US, ",%.3f", r));
            }
            writer.write("\n");
            for (int d = dmin; d <= dmax; d += dinc) {
                writer.write(String.format(Locale.US, "%d", d));
                for (double r = rmin; r <= rmax; r += rinc) {
                    double low = 0.0;
                    double high = gammaMax;
                    double bestGamma = -1;

                    while (high - low > eps) {
                        double mid = (low + high) / 2.0;
                        int fails = 0;

                        for (int i = 0; i < reps; i++) {
                            Vector q1 = new Vector(30).randomGaussian(random);
                            Vector v1 = q1.sampleWithDistance(Math.sqrt(r), random);
                            double dist = q1.distance(v1);

                            NashDevice nd = new NashDevice(30, d, random);
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
                    writer.write(String.format(Locale.US, ",%.4f", bestGamma));
                    Progress.updateBar(++pg);
                }
                writer.write("\n");
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# reps=" + reps + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // points jumping between regions
    public static void exp16() throws Exception {
        String name = "nash16";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 200_000;
        int d = 300;
        int dPrime = 2000;
        int reps = 10;
        double c = 1.1;

        double min = 0.4;
        double max = 1.0;
        double inc = (max - min) / 10;

        Progress.newBar("Experiment " + name, (3 + reps * (int) (1 + (max - min) / inc)));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
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
                v = nd.transform(v);
                return v.dataString();
            }, table1);
            Progress.updateBar(++pg);

            // results
            for (double r = min; r <= max; r += inc) {
                // Stats inner = new Stats();
                // Stats fuzzy = new Stats();
                // Stats outer = new Stats();
                double total_count = 0;
                double i2f = 0, f2i = 0, f2o = 0, o2f = 0, i2o = 0, o2i = 0;
                double A_inner_total = 0, A_fuzzy_total = 0, A_outer_total = 0;
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
                    A_inner_total += A_inner.size() / reps;
                    A_fuzzy_total += A_fuzzy.size() / reps;
                    A_outer_total += A_outer.size() / reps;

                    Set<String> i_to_f = new HashSet<>(A_inner);
                    i_to_f.retainAll(B_fuzzy);

                    Set<String> f_to_i = new HashSet<>(A_fuzzy);
                    f_to_i.retainAll(B_inner);

                    Set<String> f_to_o = new HashSet<>(A_fuzzy);
                    f_to_o.retainAll(B_outer);

                    Set<String> o_to_f = new HashSet<>(A_outer);
                    o_to_f.retainAll(B_fuzzy);

                    Set<String> i_to_o = new HashSet<>(A_inner);
                    i_to_o.retainAll(B_outer);

                    Set<String> o_to_i = new HashSet<>(A_outer);
                    o_to_i.retainAll(B_inner);

                    i2f += i_to_f.size() / reps;
                    f2i += f_to_i.size() / reps;
                    f2o += f_to_o.size() / reps;
                    o2f += o_to_f.size() / reps;
                    i2o += i_to_o.size() / reps;
                    o2i += o_to_i.size() / reps;
                    total_count += Ares.size() / reps;

                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                i2f = B_inner_total == 0 ? 0 : i2f / B_inner_total;
                f2i = B_fuzzy_total == 0 ? 0 : f2i / B_fuzzy_total;
                f2o = B_fuzzy_total == 0 ? 0 : f2o / B_fuzzy_total;
                o2f = B_outer_total == 0 ? 0 : o2f / B_outer_total;
                i2o = B_inner_total == 0 ? 0 : i2o / B_inner_total;
                o2i = B_outer_total == 0 ? 0 : o2i / B_outer_total;
                writer.write(String.format(Locale.US,
                        "%.3f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.1f,%.1f,%.1f\n",
                        r, i2f, i2o, f2i, f2o, o2i, o2f, B_inner_total, B_fuzzy_total, B_outer_total));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Progress.printAbove(e.getMessage());
        }
        // Progress.end();
    }

    // points jumping between regions
    public static void exp17() throws Exception {
        String name = "nash17";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 200_000;
        int d = 300;
        int reps = 10;
        double c = 1.1;
        double r = 1.0 / Math.pow(Math.log(n) / Math.log(10), 1.0 / 8.0);
        int min = d;
        int max = 2000;
        int inc = (max - min) / 10;

        Progress.newBar("Experiment " + name, 1 + (3 + reps * ((max - min) / inc) + 2 * ((max - min) / inc)));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("d' / region transitions\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15\n"); // columns on y-axis
            writer.write(
                    "d', i2f, i2fp, i2o, i2op, f2i, f2ip, f2o, f2op, o2i, o2ip, o2f, o2fp, inner_count, fuzzy_count, outer_count\n");

            // load vectors to DB
            String table1 = "vectors1";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            Progress.updateBar(++pg);

            // results
            for (int dPrime = min; dPrime <= max; dPrime += inc) {
                String table2 = "vectors2";
                db.loadVectorsIntoDB(table2, filepathSource, n, d);
                Progress.updateBar(++pg);

                // transform vectors
                random = new Random(SEED);
                NashDevice nd = new NashDevice(d, dPrime, random);
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    v = nd.transform(v);
                    return v.dataString();
                }, table2);
                Progress.updateBar(++pg);

                double total_count = 0;
                double i2f = 0, f2i = 0, f2o = 0, o2f = 0, i2o = 0, o2i = 0;
                double B_inner_total = 0, B_fuzzy_total = 0, B_outer_total = 0;

                random = new Random(SEED);
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

                    Set<String> i_to_f = new HashSet<>(A_inner);
                    i_to_f.retainAll(B_fuzzy);

                    Set<String> f_to_i = new HashSet<>(A_fuzzy);
                    f_to_i.retainAll(B_inner);

                    Set<String> f_to_o = new HashSet<>(A_fuzzy);
                    f_to_o.retainAll(B_outer);

                    Set<String> o_to_f = new HashSet<>(A_outer);
                    o_to_f.retainAll(B_fuzzy);

                    Set<String> i_to_o = new HashSet<>(A_inner);
                    i_to_o.retainAll(B_outer);

                    Set<String> o_to_i = new HashSet<>(A_outer);
                    o_to_i.retainAll(B_inner);

                    i2f += i_to_f.size() / reps;
                    f2i += f_to_i.size() / reps;
                    f2o += f_to_o.size() / reps;
                    o2f += o_to_f.size() / reps;
                    i2o += i_to_o.size() / reps;
                    o2i += o_to_i.size() / reps;
                    total_count += Ares.size() / reps;

                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }
                
                double i2fp = B_inner_total == 0 ? 0 : i2f / B_inner_total;
                double f2ip = B_fuzzy_total == 0 ? 0 : f2i / B_fuzzy_total;
                double f2op = B_fuzzy_total == 0 ? 0 : f2o / B_fuzzy_total;
                double o2fp = B_outer_total == 0 ? 0 : o2f / B_outer_total;
                double i2op = B_inner_total == 0 ? 0 : i2o / B_inner_total;
                double o2ip = B_outer_total == 0 ? 0 : o2i / B_outer_total;
                writer.write(String.format(Locale.US,
                        "%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.1f,%.1f,%.1f\n",
                        dPrime, i2f, i2fp, i2o, i2op, f2i, f2ip, f2o, f2op, o2i, o2ip, o2f, o2fp, B_inner_total, B_fuzzy_total, B_outer_total));
            }
            writer.write("# SEED=" + SEED + "\n");
            writer.write("# reps=" + reps + "\n");
            writer.write("# n=" + n + "\n");
            writer.write("# c=" + c + "\n");
            writer.write("# r=" + r + "\n");
            writer.write("# d=" + d + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            Progress.printAbove(e.getMessage());
        }
        // Progress.end();
    }
}