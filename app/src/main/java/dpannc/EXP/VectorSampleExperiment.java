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
            writer.write("1\n"); // columns on y-axis
            writer.write("dist,sampled dist MSD\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 100; // sample size
            int d = 300; // dimensions
            int rep = 100;

            // generate dataset of vectors
            Path filepathSource = Paths.get("resources/generated", d + "D_" + n + ".txt");
            double min = 0.01;
            double max = 10;
            double inc = 0.5;
            for (double dist = min; dist < max; dist += inc) {
                Result result = new Result();
                for (int i = 0; i < rep; i++) {
                    Vector v = new Vector(d).randomGaussian(random);
                    Vector w = v.sampleWithDistance(dist, random);
                    result.add("" + i, v.distance(w));
                }

                // write result to file
                writer.write(dist + "," + result.stddev() + "\n"); 
            }

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }
}
