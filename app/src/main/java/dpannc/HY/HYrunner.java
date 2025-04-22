package dpannc.HY;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import dpannc.Progress;
import dpannc.Vector;
import dpannc.database.DB;

public class HYrunner {
    public static void main(String[] args) throws Exception {
        String name = "HY1";
        DB db = new DB("DB/HY_" + name, true);
        int SEED = 100;
        Random random = new Random(SEED);
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int n = 1000;
        int d = 3;
        double scale = 100;

        Path filepathSource = Paths.get("app/resources/fasttext/truncated/trun_english_500K_" + d + "D.txt").toAbsolutePath();
        String table = "vectors";
        db.loadVectorsIntoDB(table, filepathSource, n, d);
        db.applyTransformation(data -> {
            Vector v = Vector.fromString(data);
            v.multiply(scale);
            return v.dataString();
        }, table);

        HY hy = new HY(sensitivity, epsilon, delta, db);
        hy.populateFromDB(n, d, table, db);
        hy.print();

        // Vector q = new Vector(new double[] {0, 0, 0});
        Vector q = new Vector(3).random(random, 10);
        int result = hy.query(q, 0.05, 3.0);
        System.out.println("result: " + result);
    }
}
