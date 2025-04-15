package dpannc.EXP;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import dpannc.Vector;
import dpannc.HY.HY;
import dpannc.database.DB;

public class HYexperiments {
    

    public static void expHY() throws Exception {
        String name = "expHY";
        DB db = new DB("DB/HY_" + name, true);
        
        int SEED = 100;
        Random random = new Random(SEED);
        int n = 5000;
        int d = 4;

        Path filepath = Paths.get("resources", "generated", d + "D_" + n + ".txt");
        DataGenerator.generateRandom(filepath, d, n, 10, random);

        // Path filepath = Paths.get("resources", "fasttext", "dk-300d.txt");

        String table = "vectors";
        db.loadVectorsIntoDB(table, filepath, n, d);

        // NashDevice nd = new NashDevice(d, d-1, SEED, random);
        // db.applyNashTransform(nd);
        // d--;

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double r = 3;
        double a = 0.01;

        HY hy = new HY(sensitivity, epsilon, delta);
        hy.populateFromDB(n, d, table, db);

        Vector q = new Vector(d).random(random, 10);

        System.out.println("r-a: " + (r - a));
        System.out.println("  r: " + r);
        System.out.println("r+a: " + (r + a));
        System.out.println("---");
        int count = hy.query(q, a, r);
        // hy.print();

        List<String> queryList = hy.queryList();
        Result HYres = new Result().loadDistancesBetween(q, queryList, table, db);
        Result BRUTEres = new Result().loadDistancesBetween(q, table, db);

        System.out.println("  QUERY: " + count);
        int A1 = HYres.amountLessThan(r - a);
        int A2 = HYres.amountWithin(r - a, r + a);
        int A3 = HYres.amountGreaterThan(r + a);

        int B1 = BRUTEres.amountLessThan(r - a);
        int B2 = BRUTEres.amountWithin(r - a, r + a);
        int B3 = BRUTEres.amountGreaterThan(r + a);

        System.out.println(" inside: " + A1 + " / " + B1);
        System.out.println("  fuzzy: " + A2 + " / " + B2);
        System.out.println("outside: " + A3 + " / " + B3);
    }
}
