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
        DB db = new DB("dpannc");
        String name = "magDis";
        Path filepathTarget = Paths.get("../results", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("magnitude / no. points\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1\n"); // columns on y-axis

            writer.write("magnitude,points\n"); // coulumns

            // settings
            int SEED = 10;
            Random random = new Random(SEED);
            int n = 60_000; // sample size
            int d = 784; // dimensions

            // load into database
            String nameSource = "fashion-mnist_train";
            Path filepathSource = Paths.get("resources/FashionMNIST", nameSource + ".txt");
            String table1 = "table1";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);

            DBiterator it = db.iterator(table1);
            double max = 0;
            while (it.hasNext()) {
                Vector v = it.next();
                double mag = v.magnitude();
                if (mag > max) max = mag;
            }
            System.out.println(max);


            // // divide each component in each vector by something
            // db.applyTransformation(str -> {
            //     String transformed = Arrays.stream(str.split(" "))
            //             .mapToDouble(Double::parseDouble)
            //             .map(v -> v / 2)
            //             .mapToObj(Double::toString)
            //             .collect(Collectors.joining(" "));
            //     return transformed;
            // }, table1);

        } catch (

        Exception e) {
            e.printStackTrace();
        }
    }
}
