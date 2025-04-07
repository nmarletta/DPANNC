package dpannc.EXP;

import java.io.BufferedReader;
import java.io.FileReader;
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
        // exp1();
        // exp2();
        // exp3();
        // exp4();
        // exp5();
        distMap();
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

            writer.write("dist,median,MSD\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            double sigma = 1; // magnitude of vectors after transformation

            double min = 0.001;
            double max = 5;
            double inc = 0.1;

            NashDevice nd = new NashDevice(d, d, 1, random);

            for (double dist = min; dist < max; dist += inc) {
                Result diff = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleInSpace(dist, random);

                    // apply Nash Transform
                    Vector q2 = nd.transform(q1);
                    Vector v2 = nd.transform(v1);

                    // save distances before and after transformation
                    diff.add("" + i, q1.distance(v1) - q2.distance(v2));
                }

                // write result to file
                writer.write(dist + "," + diff.median() + "," + diff.msd() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of vectors after transformation: " + sigma + "\n");

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
            double sigma = 1; // magnitude of vectors after transformation

            double min = 0.001;
            double max = 5;
            double inc = 0.1;

            NashDevice nd = new NashDevice(d, d, 1, random);

            for (double dist = min; dist < max; dist += inc) {
                Result transformedDists = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleInSpace(dist, random);

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
            writer.write("# Magnitude of vectors after transformation: " + sigma + "\n");

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

            writer.write("dist,median,MSD\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            double sigma = 1; // magnitude of vectors after transformation

            double min = 0.001;
            double max = 10;
            double inc = 0.1;

            NashDevice nd = new NashDevice(d, d, 1, random);

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
                writer.write(dot + "," + diff.median() + "," + diff.msd() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of vectors after transformation: " + sigma + "\n");

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
            double sigma = 1; // magnitude of vectors after transformation

            double min = 0.1;
            double max = 10;
            double inc = 1;

            NashDevice nd = new NashDevice(d, d, 1, random);

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
            writer.write("# Magnitude of vectors after transformation: " + sigma + "\n");

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    public static void exp5() throws Exception {
        DB db = new DB("dpannc");
        String name = "found";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance from q / no. found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("dist,pre,post\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100000; // sample size
            int d = 300; // dimensions
            int rep = 1;

            // load into database
            Path filepathSource = Paths.get("resources/fasttext", "dk-300d.txt");
            String table1 = "table1";
            String table2 = "table2";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            db.loadVectorsIntoDB(table2, filepathSource, n, d);

            // apply Nash Transform
            NashDevice nd = new NashDevice(d, d, 1, random);
            db.applyNashTransform(nd, table2);


                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.trim().split(",");
                    double preDist = Double.parseDouble(tokens[0]);
                    double postDist = Double.parseDouble(tokens[1]);

                    double avg1 = 0;
                    double avg2 = 0;
                    for (int i = 0; i < rep; i++) {
                        Vector q = db.getRandomVector(table1, random);
                        // calculate results
                        Result distsPre = new Result().loadDistancesBetween(q, table1, db);
                        Result distsPost = new Result().loadDistancesBetween(q, table2, db);
                        double res1 = distsPre.amountLessThan(preDist);
                        double res2 = distsPost.amountLessThan(postDist);
                        Set<String> neighborsBefore = distsPre.getIDsWithinRadius(r);
                        Set<String> neighborsAfter = distsPost.getIDsWithinRadius(r);
                        double jaccard = intersection(neighborsBefore, neighborsAfter).size() / union(...).size();

                        avg1 += res1 / rep;
                        avg2 += res2 / rep;
                    }
                    System.out.println(preDist);
                    // double diff = res1 - res2;
                    // write result to file
                    writer.write(preDist + "," + avg1 + "," + avg2 + "\n");
                }
            
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Average taken over: " + rep + " repetions\n");
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
            writer.write("initial distance / transformed distance\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("initial distance, median transformed distance\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            double sigma = 1; // magnitude of vectors after transformation

            double min = 0.001;
            double max = 5;
            double inc = 0.1;

            NashDevice nd = new NashDevice(d, d, 1, random);

            for (double dist = min; dist < max; dist += inc) {
                Result transformedDists = new Result();

                for (int i = 0; i < n; i++) {
                    // generate vectors
                    Vector q1 = new Vector(d).randomGaussian(random);
                    Vector v1 = q1.sampleInSpace(dist, random);

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
            writer.write("# Magnitude of vectors after transformation: " + sigma + "\n");

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }
}