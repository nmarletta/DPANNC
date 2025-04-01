package dpannc.EXP;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import dpannc.DataGenerator;
import dpannc.AIMN.AIMN;
import dpannc.database.DB;
import dpannc.Vector;
    
public class Experiments {
    
    public static void main(String[] args) throws Exception {
        int SEED = 100;
        Random random = new Random(SEED);

        int n = 10000;
        int d = 10;
        
        Path filepath = Paths.get("resources", "generated", d + "D_" + n + ".txt");
        DataGenerator.generateRandom(filepath, d, n, 10, random);
        DB db = new DB("dpannc");
        db.loadVectorsIntoDB(filepath, n, d);

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double c = 1.5;

        Vector q = new Vector(d).random(random, 10).normalize();

        AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta);
        aimn.populateFromDB(db);
        double R = aimn.getR();
        double C = aimn.getC();
        int count = aimn.query(q);
        System.out.println(count);
        List<String> query = aimn.queryList();
        Result res = new Result(q, query, db);
        int count2 = res.amountWithin(C*R);
        
        System.out.println("AIMN: within cr(" + C*R + "): " + count2);
        Result brute = new Result(q, db);
        int count3 = brute.amountWithin(C*R);
        System.out.println("BRUTE: within r(" + C*R + "): " + count3);
    }
}
