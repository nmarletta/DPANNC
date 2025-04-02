package dpannc.EXP;

import java.nio.file.*;
import java.util.List;
import java.util.Random;

import dpannc.NashDevice;
import dpannc.AIMN.AIMN;
import dpannc.HY.HY;
import dpannc.database.DB;
import dpannc.Vector;
    
public class Experiments {
    
    public static void main(String[] args) throws Exception {
        // expHY();
        expAIMN();
    }

    public static void expHY() throws Exception {
        int SEED = 100;
        Random random = new Random(SEED);
        int n = 5000;
        int d = 4;
        
        Path filepath = Paths.get("resources", "generated", d + "D_" + n + ".txt");
        DataGenerator.generateRandom(filepath, d, n, 10, random);

        // Path filepath = Paths.get("resources", "fasttext", "dk-300d.txt");

        DB db = new DB("dpannc");
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
        hy.populateFromDB(n, d, db);
        
        Vector q = new Vector(d).random(random, 10);

        System.out.println("r-a: " + (r-a));
        System.out.println("  r: " + r);
        System.out.println("r+a: " + (r+a));
        System.out.println("---");
        int count = hy.query(q, a, r);
        // hy.print();

        List<String> queryList = hy.queryList();
        Result HYres = new Result(q, queryList, table, db, false);
        Result BRUTEres = new Result(q, table, db, false);

        System.out.println("  QUERY: " + count);
        int A1 = HYres.amountWithin(r-a);
        int A2 = HYres.amountWithin(r-a, r+a);
        int A3 = HYres.amountOutside(r+a);

        int B1 = BRUTEres.amountWithin(r-a);
        int B2 = BRUTEres.amountWithin(r-a, r+a);
        int B3 = BRUTEres.amountOutside(r+a);

        System.out.println(" inside: " + A1 + " / " + B1);
        System.out.println("  fuzzy: " + A2 + " / " + B2);
        System.out.println("outside: " + A3 + " / " + B3);
    }

    public static void expAIMN() throws Exception {
        int SEED = 100;
        Random random = new Random(SEED);
        int n = 1000;
        int d = 300;
        
        // Path filepath = Paths.get("resources", "generated", d + "D_" + n + ".txt");
        // DataGenerator.generateRandom(filepath, d, n, 10, random);

        Path filepath = Paths.get("resources", "fasttext", "dk-300d.txt");
        DB db = new DB("dpannc");
        String table = "vectors";
        db.loadVectorsIntoDB(table, filepath, n, d);

        // NashDevice nd = new NashDevice(d, d-1, SEED, random);
        // db.applyNashTransform(nd);
        // d--;
        
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double c = 1.9;

        Vector q = new Vector(d).random(random, 10);

        AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta);
        aimn.populateFromDB(table, db);
        double r = aimn.getR();
        System.out.println("r: " + r);
        System.out.println("cr: " + c*r);
        System.out.println("---");
        int count = aimn.query(q);

        List<String> queryList = aimn.queryList();
        Result AIMNres = new Result(q, queryList, table, db, true);
        Result BRUTEres = new Result(q, table, db, true);

        System.out.println("  QUERY: " + count);
        int A1 = AIMNres.amountWithin(r);
        int A2 = AIMNres.amountWithin(r, c*r);
        int A3 = AIMNres.amountOutside(c*r);

        int B1 = BRUTEres.amountWithin(r);
        int B2 = BRUTEres.amountWithin(r, c*r);
        int B3 = BRUTEres.amountOutside(c*r);

        System.out.println(" inside: " + A1 + " / " + B1);
        System.out.println("  fuzzy: " + A2 + " / " + B2);
        System.out.println("outside: " + A3 + " / " + B3);
    }
}
