package dpannc.EXP;

import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    public static void expAIMN() throws Exception {
        String name = "foundbg";
        Path filepathTarget = Paths.get("../results/nash", name + ".csv");
        Files.createDirectories(filepathTarget.getParent());

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 100_000;
        int d = 300;

        // Path filepathSource = Paths.get(System.getProperty("user.dir"), "../resources", "fasttext", "dk-300d.txt");
        Path filepathSource = Paths.get("resources", "fasttext", "dk-300d.txt");
        DB db = new DB("dpanncAIMN");
        String table = "vectors";
        db.loadVectorsIntoDB(table, filepathSource, n, d);
        NashDevice nd = new NashDevice(d, d, random);
        db.applyNashTransform(nd, table);

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double r = 0.667;
        double c = 1.2;

        AIMN aimn = new AIMN(n, d, r, c, sensitivity, epsilon, delta);
        aimn.DP(false);
        aimn.populateFromDB(table, db);

        Vector q = db.getRandomVector(table, random);
        int count = aimn.query(q);
        System.out.println("count: " + count);

        List<String> queryList = aimn.queryList();
        Result AIMNres = new Result().loadDistancesBetween(q, queryList, table, db); // true
        Result BRUTEres = new Result().loadDistancesBetween(q, table, db); // true

        Set<String> A_L = AIMNres.lessThan(r);
        Set<String> A_M = AIMNres.within(r, c*r);
        // Set<String> A_H = AIMNres.greaterThan(c*r);

        Set<String> B_L = BRUTEres.lessThan(r);
        Set<String> B_M = BRUTEres.within(r, c*r);
        // Set<String> B_H = BRUTEres.greaterThan(c*r);
        
        Set<String> L_intersectionSet = new HashSet<String>(A_L);
        L_intersectionSet.retainAll(B_L);
        Set<String> L_unionSet = new HashSet<String>(A_L);
        L_unionSet.addAll(B_L);
        double L_intersection = L_intersectionSet.size();
        double L_union = L_unionSet.size();
        double L_jaccard = L_intersection == 0 || L_union == 0 ? 0 : L_intersection / L_union;
        System.out.println(" inside: " + L_jaccard + " - " + A_L.size() + " / " + B_L.size());

        Set<String> M_intersectionSet = new HashSet<String>(A_M);
        M_intersectionSet.retainAll(B_M);
        Set<String> M_unionSet = new HashSet<String>(A_M);
        M_unionSet.addAll(B_M);
        double M_intersection = M_intersectionSet.size();
        double M_union = M_unionSet.size();
        double M_jaccard = M_intersection == 0 || M_union == 0 ? 0 : M_intersection / M_union;
        System.out.println(" inside: " + M_jaccard + " - " + A_M.size() + " / " + B_M.size());

        // Set<String> H_intersectionSet = new HashSet<String>(A_H);
        // H_intersectionSet.retainAll(B_H);
        // Set<String> H_unionSet = new HashSet<String>(A_H);
        // H_unionSet.addAll(B_H);
        // double H_intersection = H_intersectionSet.size();
        // double H_union = H_unionSet.size();
        // double H_jaccard = H_intersection == 0 || H_union == 0 ? 0 : H_intersection / H_union;
        // System.out.println(" inside: " + H_jaccard + " - " + A_H.size() + " / " + B_H.size());
    }
}
