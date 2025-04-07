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
    public static void main(String[] args) throws Exception {
        // exp1();
        // exp2();
        // exp3();
        // exp4();
        // exp5();
        exp6();
        exp7();
        // distMap();

    }

    // Difference in DISTANCE after Nash Device tranformation
    public static void exp1() throws Exception {
        DB db = new DB("dpannc");

        String name = "dist_diff";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance from q / difference after transformation\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("dist,median,stddev\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions

            double min = 0.001;
            double max = 5;
            double inc = 0.1;

            NashDevice nd = new NashDevice(d, d, random);

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

    // DISTANCES after Nash Device tranformation
    public static void exp2() throws Exception {
        DB db = new DB("dpannc");

        String name = "dist_trans";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed distance\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("initial distance, median transformed distance\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions

            double min = 0.001;
            double max = 5;
            double inc = 0.1;

            NashDevice nd = new NashDevice(d, d, random);

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

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    // Difference in DOT-PRODUCT after Nash Device tranformation
    public static void exp3() throws Exception {
        DB db = new DB("dpannc");

        String name = "dot_diff";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("dot-product / difference after transformation\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("dist,median,stddev\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions

            double min = -5.0;
            double max = 5.0;
            double inc = 0.05;

            NashDevice nd = new NashDevice(d, d, random);

            for (double dot = min; dot < max; dot += inc) {
                Result diff = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleWithDot(dot, random);

                    // apply Nash Transform
                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    // save distances before and after transformation
                    diff.add("" + i, q1.dot(v1) - q2.dot(v2));
                }

                // write result to file
                writer.write(dot + "," + diff.median() + "," + diff.stddev() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of vectors after transformation: " + 1 + "\n");

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    // DOT-PRODUCT after Nash Device tranformation
    public static void exp4() throws Exception {
        DB db = new DB("dpannc");

        String name = "dot_trans";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial dot-product / transformed dot-product\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("initial dot-product, mean transformed dot-product\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            double mag = 1; // magnitude of vectors after transformation

            double min = -5.0;
            double max = 5.0;
            double inc = 0.05;

            NashDevice nd = new NashDevice(d, d, random);

            for (double dot = min; dot < max; dot += inc) {
                Result transformedDots = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleWithDot(dot, random);

                    // apply Nash Transform
                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    // save distances after transformation
                    transformedDots.add("" + i, q2.dot(v2));
                }

                // write result to file
                writer.write(dot + "," + transformedDots.mean() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of vectors after transformation: " + 1 + "\n");

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    // DISTANCE to DOT-PRODUCT after Nash Device tranformation
    public static void exp5() throws Exception {
        DB db = new DB("dpannc");

        String name = "distdot_trans";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed dot-product\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("initial distance, mean transformed dot-product\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            double mag = 1; // magnitude of vectors after transformation

            double min = 0.1;
            double max = 5;
            double inc = 0.05;

            NashDevice nd = new NashDevice(d, d, random);

            for (double dot = min; dot < max; dot += inc) {
                Result transformedDots = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleWithDistance(dot, random);

                    // apply Nash Transform
                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    // save distances after transformation
                    transformedDots.add("" + i, q2.dot(v2));
                }

                // write result to file
                writer.write(dot + "," + transformedDots.mean() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of vectors after transformation: " + 1 + "\n");

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    public static void exp6() throws Exception {
        DB db = new DB("dpannc");
        String name = "found";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3\n"); // columns on y-axis

            writer.write("r,precision,recall,f1\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 200_000; // sample size
            int d = 300; // dimensions
            int reps = 50;

            // load into database
            Path filepathSource = Paths.get("resources/fasttext", "dk-300d.txt");
            String table1 = "table1";
            String table2 = "table2";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            db.loadVectorsIntoDB(table2, filepathSource, n, d);

            // apply Nash Transform
            NashDevice nd = new NashDevice(d, d, random);
            db.applyNashTransform(nd, table2);

            for (double r = 0.1; r < 1.99; r += 0.02) {
                // double rPrime = DistMapper.getP95(r);
                double rPrime = DistMapper.getMedian(r);

                double precision = 0;
                double recall = 0;
                double f1 = 0;
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
                    // Set<String> unionSet = new HashSet<String>(A);
                    // unionSet.addAll(B);
                    double intersection = intersectionSet.size();
                    // double union = unionSet.size();
                    // true positive / (true positive + false positive)
                    precision += (B.size() == 0) ? 0 : (double) intersection / B.size(); 
                    // true positive / (true positive + false positive)
                    recall += (A.size() == 0) ? 0 : (double) intersection / A.size(); 
                    f1 += (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);
                }
                System.out.println("1: " + r);
                // write result to file
                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f\n",
                r, precision/reps, recall/reps, f1/reps));
            }

            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Average taken over: " + reps + " repetions\n");
            writer.write("# Data: " + filepathSource.toString() + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    public static void exp7() throws Exception {
        DB db = new DB("dpannc");
        String name = "found2";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance from q / jaccard simmilarity\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("r,meanJaccard\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 200_000; // sample size
            int d = 300; // dimensions
            int rep = 50;

            // load into database
            Path filepathSource = Paths.get("resources/fasttext", "dk-300d.txt");
            String table1 = "table1";
            String table2 = "table2";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            db.loadVectorsIntoDB(table2, filepathSource, n, d);

            // apply Nash Transform
            NashDevice nd = new NashDevice(d, d, random);
            db.applyNashTransform(nd, table2);

            for (double r = 0.1; r < 1.99; r += 0.02) {

                double rPrime = DistMapper.getP95(r);

                Result jacRes = new Result();
                Result intRes = new Result();
                Result uniRes = new Result();
                for (int i = 0; i < rep; i++) {
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = nd.transform(q1);
                    // calculate distances
                    Result distsPre = new Result().loadDistancesBetween(q1, table1, db);
                    Result distsPost = new Result().loadDistancesBetween(q2, table2, db);
                    Set<String> resultBefore = distsPre.lessThan(r);
                    Set<String> resultAfter = distsPost.lessThan(rPrime);
                    //
                    Set<String> intersectionSet = new HashSet<String>(resultBefore);
                    intersectionSet.retainAll(resultAfter);
                    Set<String> unionSet = new HashSet<String>(resultBefore);
                    unionSet.addAll(resultAfter);
                    double intersection = intersectionSet.size();
                    double union = unionSet.size();
                    double jaccard = intersection == 0 || union == 0 ? 0 : intersection / union;
                    intRes.add("" + i, intersection);
                    uniRes.add("" + i, union);
                    jacRes.add("" + i, jaccard);
                }
                System.out.println("2: " + r);
                // write result to file
                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f\n",
                r, intRes.mean(), uniRes.mean(), jacRes.mean()));
            }

            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Average taken over: " + rep + " repetions\n");
            writer.write("# 95 percentile\n");
            writer.write("# Data: " + filepathSource.toString() + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    // DISTANCES after Nash Device tranformation
    public static void distMap() throws Exception {
        DB db = new DB("dpannc");

        String name = "dist_map";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed distance distribution\n");
            writer.write("0\n");
            writer.write("1,2,3,4,5\n"); // median, mean, stddev, p5, p95
            writer.write("initial,median,mean,stddev,p5,p95\n");

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300;

            double min = 0.001;
            double max = 5;
            double inc = 0.05;

            NashDevice nd = new NashDevice(d, d, random);

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
                double p5 = transformedDists.quantile(0.05);
                double p95 = transformedDists.quantile(0.95);

                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f,%.5f,%.5f\n",
                        dist, median, mean, msd, p5, p95));
            }

            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

}