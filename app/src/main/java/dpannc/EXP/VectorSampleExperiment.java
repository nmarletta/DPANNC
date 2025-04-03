package dpannc.EXP;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import dpannc.Vector;
import dpannc.database.DB;

public class VectorSampleExperiment {
    public static void main(String[] args) throws Exception {
        exp1();
    }
    
    public static void exp1() throws Exception {
        DB db = new DB("dpannc");

        String name = "dist_dev";
        Path filepathTarget = Paths.get("results/vector", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("distance from q / mean standard deviation\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2\n"); // columns on y-axis
            writer.write("dist,onSphere,inSpace\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            Vector q = new Vector(d).randomGaussian(random);

            // generate dataset of vectors
            Path filepathSource = Paths.get("resources/generated", d + "D_" + n + ".txt");
            double min = 0.001;
            double max = 1.9;
            double inc = 0.05;
            for (double dist = min; dist < max; dist += inc) {
                // ON SPHERE
                DataGenerator.atDistanceOnSphere(filepathSource, q, n, dist, random);
                String tblOnSphere = "onSphere";
                db.loadVectorsIntoDB(tblOnSphere, filepathSource, n, d);

                // IN SPACE
                DataGenerator.atDistanceOnSphere(filepathSource, q, n, dist, random);
                String tblInSpace = "inSpace";
                db.loadVectorsIntoDB(tblInSpace, filepathSource, n, d);

                // calculate results
                Result onSphereRes = new Result(q, tblOnSphere, db, false);
                Result inSpaceRes = new Result(q, tblInSpace, db, false);

                // write result to file
                writer.write(dist + "," + onSphereRes.msd() + "," + inSpaceRes.msd() + "\n"); 
            }

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }
}
