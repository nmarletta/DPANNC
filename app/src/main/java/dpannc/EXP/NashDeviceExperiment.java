package dpannc.EXP;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import dpannc.DataGenerator;
import dpannc.NashDevice;
import dpannc.Vector;
import dpannc.HY.HY;
import dpannc.database.DB;

public class NashDeviceExperiment {
    public static void main(String[] args) throws Exception {
        expND();
    }

    public static void expND() throws Exception {
        int SEED = 10;
        Random random = new Random(SEED);
        int n = 10000;
        int d = 300;

        // Path filepath = Paths.get("resources", "generated", d + "D_" + n + ".txt");
        // DataGenerator.generateRandom(filepath, d, n, 10, random);

        Path filepath = Paths.get("resources", "fasttext", "dk-300d.txt");

        DB db = new DB("dpannc");

        String table1 = "v1";
        db.loadVectorsIntoDB(table1, filepath, n, d);

        String table2 = "v2";
        db.loadVectorsIntoDB(table2, filepath, n, d);

        NashDevice nd = new NashDevice(d, d, SEED, random);
        db.applyNashTransform(nd, table2);

        int N = 1;
        double meanSum = 0;
        double medianSum = 0;
        double msdSum = 0;
        double madSum = 0;

        for (int i = 0; i < N; i++) {
            Vector q = db.getRandomVector(table1, random);
            Result dists1 = new Result(q, table1, db, false);
            Result dists2 = new Result(q, table2, db, false);
            Result change = dists1.changeBetween(dists2);
            meanSum += change.mean();
            medianSum += change.median();
            msdSum += change.msd();
            madSum += change.mad();
            db.loadResultIntoDB("change", change);
        }
        System.out.println("mean: " + meanSum/N);
        System.out.println("median: " + medianSum/N);
        System.out.println("msd: " + msdSum/N);
        System.out.println("mad: " + madSum/N);
    }
}
