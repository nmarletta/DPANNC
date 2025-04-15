package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import dpannc.Vector;
import dpannc.database.DB;
import dpannc.database.DBiterator;

public class DataProperties {
    public static void main(String[] args) throws Exception {
        magnitudeDistribution();
    }
    public static void magnitudeDistribution() throws Exception {
        String name = "magDis";
        DB db = new DB("DB/AIMN_" + name, true);

        Path filepathTarget = Paths.get("app/results/", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("magnitude / no. points\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("magnitude,points\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 2_000_000; // sample size
            int d = 300; // dimensions

            // load into database
            // String nameSource = "fashionMNIST_60K_784D";
            // Path filepathSource = Paths.get("app/resources/fashionMNIST/" +  nameSource + ".txt");
            String nameSource = "english_2M_300D";
            Path filepathSource = Paths.get("app/resources/fasttext/" +  nameSource + ".txt");
            String table1 = "table1";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);

            DBiterator it = db.iterator(table1);
            double max = 0;
            double avg = 0;
            while (it.hasNext()) {
                Vector v = it.next();
                double mag = v.magnitude();
                avg += mag / n;
                if (mag > max) max = mag;
            }
            System.out.println("max: " + max);
            System.out.println("avg: " + avg);
        } catch (

        Exception e) {
            e.printStackTrace();
        }
    }
}
