package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;

import dpannc.Progress;
import dpannc.Vector;
import dpannc.database.DB;

public class DataExperiment {
    public static void main(String[] args) {
        exp1();
    }
    public static void exp1() {
        String name = "DATA_K";
        DB db = new DB("DB" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 400_000;
        int k = 1_000;

        int[] dimensions = new int[] { 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 100, 150, 200,
            250 };
        int reps = 1;

        Progress.newBar("Experiment " + name, dimensions.length + reps * dimensions.length);
        int pg = 0;

        Path filepathTarget = Paths.get("app/results/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis
            writer.write("\n");

            String table = "vectors";
            for (int d : dimensions) {
                Path filepathSource = Paths.get("app/resources/fasttext/truncated/trun_english_500K_" + d + "D.txt").toAbsolutePath();
                db.loadVectorsIntoDB(table, filepathSource, n, d);
                Progress.updateBar(++pg);

                double dist = 0;
                for (int i = 0; i < reps; i++) {
                    Vector q = db.getRandomVector(table, random);
                    Result result = new Result().loadDistancesBetween(q, table, db);
                    dist += result.distanceToKNearest(k) / reps;
                    Progress.updateBar(++pg);
                }
                writer.write(String.format(Locale.US, "%d,%.5f\n", d, dist));
            }
            // metadata
            writer.write("# Data: fasttext/trun_english_500K_\n");
            writer.write("# SEED=" + SEED + ", dataset size=" + n + ", dist for k neighbors: K=" + k + "\n");
            writer.write("# Average taken over: " + reps + " repetions\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }
}
