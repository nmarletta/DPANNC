package dpannc.EXP;

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
        exp2();
        // exp3();
        // exp4();
    }

    // Difference in distance after Nash Device tranformation
    public static void exp1() throws Exception {
        DB db = new DB("dpannc");

        String name = "diff_new";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance from q (in space) / nash device diff\n"); // title
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
                    diff.add(""+i, q1.distance(v1) - q2.distance(v2));
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

    // Distances after Nash Device tranformation
    public static void exp2() throws Exception {
        DB db = new DB("dpannc");

        String name = "dist_trans";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance / transformed distance\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("distance pre transformation, distance post transformation\n"); // coulumns

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
                    transformedDists.add(""+i, q2.distance(v2));
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

    // Difference in distance after Nash Device tranformation
    // For vectors IN SPACE
    public static void exp3() throws Exception {
        String name = "change_inspace";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance from q (in space) / nash transformation change\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("dist,median,mean\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            double mag = 1; // magnitude of initial vector
            Vector q = new Vector(d).randomGaussian(random).setMagnitude(mag);

            // generate dataset of vectors
            Path filepathSource = Paths.get("resources/generated", d + "D_" + n + ".txt");
            double min = 0.1;
            double max = 1.9;
            double inc = 0.05;
            for (double dist = min; dist < max; dist += inc) {
                DataGenerator.atDistanceInSpace(filepathSource, q, n, dist, random);

                // load vectors into database - two identical tables
                DB db = new DB("dpannc");
                String table1 = "v1";
                String table2 = "v2";
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                db.loadVectorsIntoDB(table2, filepathSource, n, d);

                // apply Nash Transform on one table
                NashDevice nd = new NashDevice(d, d, SEED, random);
                db.applyNashTransform(nd, table2);

                // calculate results
                Result dists1 = new Result().loadDistancesBetween(q, table1, db, false);
                Result dists2 = new Result().loadDistancesBetween(q, table2, db, false);
                Result change = dists1.changeBetween(dists2);

                // write result to file
                writer.write(dist + "," + change.median() + "," + change.mean() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of initial vector: " + mag + "\n");

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    // Difference in distance after Nash Device tranformation
    // For vectors IN SPACE
    public static void exp4() throws Exception {
        DB db = new DB("dpannc");
        String name = "diff_both";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance from q / nash device diff (MSD)\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("dist,onSphere,inSpace\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            double mag = 1; // magnitude of initial vector
            Vector q = new Vector(d).randomGaussian(random).setMagnitude(mag);

            //
            Path filepathSource = Paths.get("resources/generated", d + "D_" + n + ".txt");
            double min = 0.001;
            double max = 1.9; // mag * 2 (diameter of sphere)
            double inc = 0.05;
            for (double dist = min; dist < max; dist += inc) {
                // ON SPHERE
                // create vectors
                DataGenerator.atDistanceOnSphere(filepathSource, q, n, dist, random);
                String tblOnSphere1 = "onSphere1";
                String tblOnSphere2 = "onSphere2";
                // load into database
                db.loadVectorsIntoDB(tblOnSphere1, filepathSource, n, d);
                db.loadVectorsIntoDB(tblOnSphere2, filepathSource, n, d);

                // IN SPACE
                // create vectors
                DataGenerator.atDistanceInSpace(filepathSource, q, n, dist, random);
                String tblInSpace1 = "inSpace1";
                String tblInSpace2 = "inSpace2";
                // load into database
                db.loadVectorsIntoDB(tblInSpace1, filepathSource, n, d);
                db.loadVectorsIntoDB(tblInSpace2, filepathSource, n, d);

                // apply Nash Transform
                NashDevice nd = new NashDevice(d, d, 1, random);
                db.applyNashTransform(nd, tblOnSphere2);
                db.applyNashTransform(nd, tblInSpace2);

                // calculate results
                Result distsOnSphere1 = new Result().loadDistancesBetween(q, tblOnSphere1, db, false);
                Result distsOnSphere2 = new Result().loadDistancesBetween(q, tblOnSphere2, db, false);
                Result diffOnSphere = distsOnSphere1.diffBetween(distsOnSphere2);

                Result distsInSpace1 = new Result().loadDistancesBetween(q, tblInSpace1, db, false);
                Result distsInSpace2 = new Result().loadDistancesBetween(q, tblInSpace2, db, false);
                Result diffInSpace = distsInSpace1.diffBetween(distsInSpace2);

                // write result to file
                writer.write(dist + "," + diffOnSphere.msd() + "," + diffInSpace.msd() + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            writer.write("# Magnitude of initial vector: " + mag + "\n");

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

    public static void exp5() throws Exception {
        DB db = new DB("dpannc");
        String name = "closest";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis

            writer.write("dist,pre,post\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            double mag = 1; // magnitude of initial vector
            // Vector q = new Vector(d).randomGaussian(random).setMagnitude(mag);

            //
            Path filepathSource = Paths.get("resources/fasttext", "dk-300d.txt");
            double min = 0.001;
            double max = 1.9; // mag * 2 (diameter of sphere)
            double inc = 0.05;
            for (double dist = min; dist < max; dist += inc) {
                // ON SPHERE
                String table1 = "table1";
                String table2 = "table2";
                // load into database
                db.loadVectorsIntoDB(table1, filepathSource, n, d);
                db.loadVectorsIntoDB(table2, filepathSource, n, d);
                Vector q = db.getRandomVector(table1, random);

                // apply Nash Transform
                NashDevice nd = new NashDevice(d, d, 1, random);
                db.applyNashTransform(nd, table2);

                // calculate results
                Result distsPre = new Result().loadDistancesBetween(q, table1, db, false);
                Result distsPost = new Result().loadDistancesBetween(q, table2, db, false);

                double res1 = distsPre.amountLessThan(dist);
                double res2 = distsPost.amountLessThan(dist);
                double diff = res1 - res2;
                // write result to file
                writer.write(dist + "," + res1 + "," + res2 + "\n");
            }
            // metadata
            writer.write("# SEED=" + SEED + ", d=" + d + ", n=" + n + "\n");
            // writer.write("# Magnitude of initial vector: " + mag + "\n");

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }
}
