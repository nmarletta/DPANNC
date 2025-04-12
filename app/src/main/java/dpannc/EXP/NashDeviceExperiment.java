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
import dpannc.Vector;
import dpannc.database.DB;

public class NashDeviceExperiment {
    static DB db = new DB("dpannc");

    public static void main(String[] args) throws Exception {
        // exp1();
        // exp2();
        // exp3();
        // exp4();
        // exp5();
        // exp6();
        exp7();
        // exp8();
        // distMap();
    }

    /* 
     * ***********************
     * 1 * DISTANCE DIFFERENCE
     * ***********************
     */ 
    public static void exp1() throws Exception {
        String name = "dist_diff";

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
        String name = "dist_trans";

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
     * *********************
     * 6 - PRECISION - fasttext/english_2M_300D
     * *********************
     */ 
    public static void exp6() throws Exception {
        String name = "found-mnist";
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
        db.applyNashTransform(nd, table2);
        // db.applyTransformation(data -> {
        // return nd.transform(Vector.fromString(".", data)).dataString();
        // }, table2);


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
        String name = "found-mnist";

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 60_000; // sample size
        int d = 784; // dimensions
        int reps = 10;
        double scale = 1.0 / 3100.0;

        // load into database
        Path filepathSource = Paths.get("app/resources/fashionMNIST/fashionMNIST_60K_784D.txt");
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
        String name = "rval2";

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 60_000; // sample size
        int d = 784; // dimensions
        int rep = 5;
        double scale = 1.0 / 3100.0;

        // load into database
        // Path filepathSource = Paths.get("app/resources/fasttext/dk-300d.txt");
        Path filepathSource = Paths.get("app/resources/fashionMNIST/fashionMNIST_60K_784D.txt");
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
     * 8 * DIST-MAP
     * *********************
     */
    public static void distMap() throws Exception {
        String name = "dist_map";

        // settings
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 100; // sample size
        int d = 300;

        double min = 0.001;
        double max = 5;
        double inc = 0.05;

        NashDevice nd = new NashDevice(d, d, random);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed distance distribution\n");
            writer.write("0\n");
            writer.write("1,2,3,4,5\n");
            writer.write("initial,median,mean,stddev,p5,p95\n");

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
                double msd = transformedDists.stddev();
                double p5 = transformedDists.percentile(0.05);
                double p95 = transformedDists.percentile(0.95);

                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f,%.5f,%.5f\n",
                        dist, median, mean, msd, p5, p95));
            }

            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }
}