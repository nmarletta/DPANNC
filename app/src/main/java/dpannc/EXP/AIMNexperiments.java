package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import dpannc.NashDevice;
import dpannc.AIMN.AIMN;
import dpannc.database.DB;
import dpannc.Vector;

public class AIMNexperiments {
    static DB db = new DB("dpannc");

    public static void main(String[] args) throws Exception {
        expAIMN();
    }

    public static void expAIMN() throws Exception {
        String name = "foundAIMN";

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 100_000;
        int d = 300;
        int reps = 1;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt");
        String table = "vectors";
        db.loadVectorsIntoDB(table, filepathSource, n, d);

        NashDevice nd = new NashDevice(d, d, random);
        db.applyTransformation(data -> {
            Vector v = Vector.fromString(".", data);
            // v.multiply(scale);
            v = nd.transform(v);
            return v.dataString();
        }, table);

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double r = 0.667;
        double c = 1.2;

        AIMN aimn = new AIMN(n, d, r, c, sensitivity, epsilon, delta);
        aimn.DP(false);
        aimn.populateFromDB(table, db);

        double L_f1Total, L_precisionTotal, L_recallTotal, L_brute, L_TP, L_FN, L_FP;
        L_f1Total = L_precisionTotal = L_recallTotal = L_brute = L_TP = L_FN = L_FP = 0;
        double M_f1Total, M_precisionTotal, M_recallTotal, M_brute, M_TP, M_FN, M_FP;
        M_f1Total = M_precisionTotal = M_recallTotal = M_brute = M_TP = M_FN = M_FP = 0;

        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13\n"); // columns on y-axis
            writer.write("L_brute, L_TP, L_FN, L_FP, L_f1Total, L_precisionTotal, L_recallTotal, M_brute, M_TP, M_FN, M_FP, M_f1Total, M_precisionTotal, M_recallTotal\n");

            for (int i = 0; i < reps; i++) {
                Vector q = db.getRandomVector(table, random);
                aimn.query(q);
                List<String> queryList = aimn.queryList();
                Result AIMNres = new Result().loadDistancesBetween(q, queryList, table, db); // true
                Result BRUTEres = new Result().loadDistancesBetween(q, table, db); // true

                // INNER REGION results
                Set<String> A_L = AIMNres.lessThan(r);
                Set<String> B_L = BRUTEres.lessThan(r);

                Set<String> L_intersectionSet = new HashSet<String>(A_L);
                L_intersectionSet.retainAll(B_L);
                double L_intersection = L_intersectionSet.size();

                Set<String> L_unionSet = new HashSet<String>(A_L);
                L_unionSet.addAll(B_L);
                double L_union = L_unionSet.size();

                double L_precision = (B_L.size() == 0) ? 0 : (double) L_intersection / B_L.size(); // TP / (TP + FP)
                double L_recall = (A_L.size() == 0) ? 0 : (double) L_intersection / A_L.size(); // TP / (TP + FN)
                double L_f1 = (L_precision + L_recall == 0) ? 0 : 2 * L_precision * L_recall / (L_precision + L_recall);

                L_f1Total += L_f1 / reps;
                L_precisionTotal += L_precision / reps;
                L_recallTotal += L_recall / reps;
                L_brute += A_L.size() / reps;
                L_TP += L_intersection / reps;
                L_FN += (A_L.size() - L_intersection) / reps;
                L_FP += (B_L.size() - L_intersection) / reps;

                // FUZZY REGION results
                Set<String> A_M = AIMNres.within(r, c * r);
                Set<String> B_M = BRUTEres.within(r, c * r);

                Set<String> M_intersectionSet = new HashSet<String>(A_M);
                M_intersectionSet.retainAll(B_M);
                double M_intersection = M_intersectionSet.size();

                Set<String> M_unionSet = new HashSet<String>(A_M);
                M_unionSet.addAll(B_M);
                double M_union = M_unionSet.size();

                double M_precision = (B_M.size() == 0) ? 0 : (double) M_intersection / B_M.size(); // TP / (TP + FP)
                double M_recall = (A_M.size() == 0) ? 0 : (double) M_intersection / A_M.size(); // TP / (TP + FN)
                double M_f1 = (M_precision + M_recall == 0) ? 0 : 2 * M_precision * M_recall / (M_precision + M_recall);

                M_f1Total += M_f1 / reps;
                M_precisionTotal += M_precision / reps;
                M_recallTotal += M_recall / reps;
                M_brute += A_M.size() / reps;
                M_TP += M_intersection / reps;
                M_FN += (A_M.size() - M_intersection) / reps;
                M_FP += (B_M.size() - M_intersection) / reps;

            }
            System.out.println("r: " + r);

            // write result to file
            writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f\n",
                    L_brute, L_TP, L_FN, L_FP, L_f1Total, L_precisionTotal, L_recallTotal, M_brute, M_TP, M_FN, M_FP,
                    M_f1Total, M_precisionTotal, M_recallTotal));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
