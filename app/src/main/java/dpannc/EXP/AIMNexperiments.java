package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import dpannc.NashDevice;
import dpannc.Progress;
import dpannc.AIMN.AIMN;
import dpannc.database.DB;
import dpannc.Vector;

public class AIMNexperiments {

    public static void main(String[] args) throws Exception {
        // expAIMN();
    }

    public static void exp1() throws Exception {
        String name = "AIMN_check";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 100_000;
        int d = 300;
        int reps = 10;

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double c = 1.5;

        Progress.newBar("Experiment " + name, (3 + reps));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17\n"); // columns on y-axis
            writer.write(
                    "inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer, total\n");

            // load vectors to DB
            String table = "vectors";
            db.loadVectorsIntoDB(table, filepathSource, n, d);
            Progress.updateBar(++pg);

            // transform vectors
            NashDevice nd = new NashDevice(d, d, random);
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                v = nd.transform(v);
                // v.normalize();
                return v.dataString();
            }, table);
            Progress.updateBar(++pg);

            // initiate AIMN and populate
            AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta, db);
            Progress.printAbove(aimn.getSettingsString());
            aimn.DP(false);
            aimn.populateFromDB(table, db);
            Progress.updateBar(++pg);

            double inner_brute, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal,
                    inner_recallTotal;
            inner_brute = inner_count = inner_TP = inner_FN = inner_FP = inner_f1Total = inner_precisionTotal = inner_recallTotal = 0;
            double fuzzy_brute, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal,
                    fuzzy_recallTotal;
            fuzzy_brute = fuzzy_count = fuzzy_TP = fuzzy_FN = fuzzy_FP = fuzzy_f1Total = fuzzy_precisionTotal = fuzzy_recallTotal = 0;
            double outer_count = 0;
            double total_count = 0;

            // experiment
            for (int i = 0; i < reps; i++) {
                // choose and run query
                Vector q = db.getRandomVector(table, random);
                int count = aimn.query(q);
                List<String> queryList = aimn.queryList();

                // calculate all distances
                Result AIMNres = new Result().loadDistancesBetween(q, queryList, table, db);
                Result BRUTEres = new Result().loadDistancesBetween(q, table, db);

                double r = aimn.getR();

                // INNER REGION results
                Set<String> A_inner = AIMNres.lessThan(r);
                Set<String> B_inner = BRUTEres.lessThan(r);

                Set<String> inner_intersectionSet = new HashSet<String>(A_inner);
                inner_intersectionSet.retainAll(B_inner);
                double inner_intersection = inner_intersectionSet.size();

                // Set<String> inner_unionSet = new HashSet<String>(A_inner);
                // inner_unionSet.addAll(B_inner);
                // double inner_union = inner_unionSet.size();

                double inner_precision = (B_inner.size() == 0) ? 0 : (double) inner_intersection / B_inner.size(); // TP/(TP+FP)
                double inner_recall = (A_inner.size() == 0) ? 0 : (double) inner_intersection / A_inner.size(); // TP/(TP+FN)
                double inner_f1 = (inner_precision + inner_recall == 0) ? 0
                        : 2 * inner_precision * inner_recall / (inner_precision + inner_recall);

                inner_brute += (double) B_inner.size() / reps;
                inner_count += (double) A_inner.size() / reps;
                inner_TP += inner_intersection / reps;
                inner_FN += ((double) A_inner.size() - inner_intersection) / reps;
                inner_FP += ((double) B_inner.size() - inner_intersection) / reps;
                inner_f1Total += inner_f1 / reps;
                inner_precisionTotal += inner_precision / reps;
                inner_recallTotal += inner_recall / reps;

                // FUZZY REGION results
                Set<String> A_fuzzy = AIMNres.within(r, c * r);
                Set<String> B_fuzzy = BRUTEres.within(r, c * r);

                Set<String> fuzzy_intersectionSet = new HashSet<String>(A_fuzzy);
                fuzzy_intersectionSet.retainAll(B_fuzzy);
                double fuzzy_intersection = fuzzy_intersectionSet.size();

                // Set<String> fuzzy_unionSet = new HashSet<String>(A_fuzzy);
                // fuzzy_unionSet.addAll(B_fuzzy);
                // double fuzzy_union = fuzzy_unionSet.size();

                double fuzzy_precision = (B_fuzzy.size() == 0) ? 0 : (double) fuzzy_intersection / B_fuzzy.size(); // TP
                                                                                                                   // /
                                                                                                                   // (TP
                                                                                                                   // +
                                                                                                                   // FP)
                double fuzzy_recall = (A_fuzzy.size() == 0) ? 0 : (double) fuzzy_intersection / A_fuzzy.size(); // TP /
                                                                                                                // (TP +
                                                                                                                // FN)
                double fuzzy_f1 = (fuzzy_precision + fuzzy_recall == 0) ? 0
                        : 2 * fuzzy_precision * fuzzy_recall / (fuzzy_precision + fuzzy_recall);

                fuzzy_brute += (double) B_fuzzy.size() / reps;
                fuzzy_count += (double) A_fuzzy.size() / reps;
                fuzzy_TP += fuzzy_intersection / reps;
                fuzzy_FN += ((double) A_fuzzy.size() - fuzzy_intersection) / reps;
                fuzzy_FP += ((double) B_fuzzy.size() - fuzzy_intersection) / reps;
                fuzzy_f1Total += fuzzy_f1 / reps;
                fuzzy_precisionTotal += fuzzy_precision / reps;
                fuzzy_recallTotal += fuzzy_recall / reps;

                outer_count = (AIMNres.size() - A_inner.size() - A_fuzzy.size()) / reps;
                total_count = AIMNres.size() / reps;

                Progress.updateBar(++pg);
            }

            // write result to file
            writer.write(String.format(Locale.US,
                    "%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f\n",
                    inner_brute, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal,
                    inner_recallTotal,
                    fuzzy_brute, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP,
                    fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer_count, total_count));

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    public static void exp2() throws Exception {
        String name = "AIMN_c-values";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 100_000;
        int d = 300;
        int reps = 10;
        double[] cValues = new double[] { 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0 };

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;

        Progress.newBar("Experiment " + name, (3 + reps * cValues.length));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18\n"); // columns on y-axis
            writer.write(
                    "c, inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer, total\n");

            // load vectors to DB
            String table = "vectors";
            db.loadVectorsIntoDB(table, filepathSource, n, d);
            Progress.updateBar(++pg);

            // transform vectors
            NashDevice nd = new NashDevice(d, d, random);
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                v = nd.transform(v);
                // v.normalize();
                return v.dataString();
            }, table);
            Progress.updateBar(++pg);

            double inner_brute, inner_count, inner_TP, inner_FN, inner_FP, 
                    inner_f1Total, inner_precisionTotal, inner_recallTotal;
            double fuzzy_brute, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, 
                    fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal;

            for (double c : cValues) {
                // initiate AIMN and populate
                AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table, db);
                Progress.updateBar(++pg);

                inner_brute = inner_count = inner_TP = inner_FN = inner_FP = 
                inner_f1Total = inner_precisionTotal = inner_recallTotal = 0;
                fuzzy_brute = fuzzy_count = fuzzy_TP = fuzzy_FN = fuzzy_FP = 
                fuzzy_f1Total = fuzzy_precisionTotal = fuzzy_recallTotal = 0;
                double outer_count = 0;
                double total_count = 0;

                // experiment
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Vector q = db.getRandomVector(table, random);
                    int count = aimn.query(q);
                    List<String> queryList = aimn.queryList();

                    // calculate all distances
                    Result AIMNres = new Result().loadDistancesBetween(q, queryList, table, db);
                    Result BRUTEres = new Result().loadDistancesBetween(q, table, db);

                    double r = aimn.getR();

                    // INNER REGION results
                    Set<String> A_inner = AIMNres.lessThan(r);
                    Set<String> B_inner = BRUTEres.lessThan(r);

                    Set<String> inner_intersectionSet = new HashSet<String>(A_inner);
                    inner_intersectionSet.retainAll(B_inner);
                    double inner_intersection = inner_intersectionSet.size();

                    // Set<String> inner_unionSet = new HashSet<String>(A_inner);
                    // inner_unionSet.addAll(B_inner);
                    // double inner_union = inner_unionSet.size();

                    // TP/(TP+FP)
                    double inner_precision = (B_inner.size() == 0) ? 0 : (double) inner_intersection / B_inner.size();
                    // TP/(TP+FN)
                    double inner_recall = (A_inner.size() == 0) ? 0 : (double) inner_intersection / A_inner.size();
                    double inner_f1 = (inner_precision + inner_recall == 0) ? 0
                            : 2 * inner_precision * inner_recall / (inner_precision + inner_recall);

                    inner_brute += (double) B_inner.size() / reps;
                    inner_count += (double) A_inner.size() / reps;
                    inner_TP += inner_intersection / reps;
                    inner_FN += ((double) A_inner.size() - inner_intersection) / reps;
                    inner_FP += ((double) B_inner.size() - inner_intersection) / reps;
                    inner_f1Total += inner_f1 / reps;
                    inner_precisionTotal += inner_precision / reps;
                    inner_recallTotal += inner_recall / reps;

                    // FUZZY REGION results
                    Set<String> A_fuzzy = AIMNres.within(r, c * r);
                    Set<String> B_fuzzy = BRUTEres.within(r, c * r);

                    Set<String> fuzzy_intersectionSet = new HashSet<String>(A_fuzzy);
                    fuzzy_intersectionSet.retainAll(B_fuzzy);
                    double fuzzy_intersection = fuzzy_intersectionSet.size();

                    // Set<String> fuzzy_unionSet = new HashSet<String>(A_fuzzy);
                    // fuzzy_unionSet.addAll(B_fuzzy);
                    // double fuzzy_union = fuzzy_unionSet.size();

                    double fuzzy_precision = (B_fuzzy.size() == 0) ? 0 : (double) fuzzy_intersection / B_fuzzy.size();
                    double fuzzy_recall = (A_fuzzy.size() == 0) ? 0 : (double) fuzzy_intersection / A_fuzzy.size();
                    double fuzzy_f1 = (fuzzy_precision + fuzzy_recall == 0) ? 0
                            : 2 * fuzzy_precision * fuzzy_recall / (fuzzy_precision + fuzzy_recall);

                    fuzzy_brute += (double) B_fuzzy.size() / reps;
                    fuzzy_count += (double) A_fuzzy.size() / reps;
                    fuzzy_TP += fuzzy_intersection / reps;
                    fuzzy_FN += ((double) A_fuzzy.size() - fuzzy_intersection) / reps;
                    fuzzy_FP += ((double) B_fuzzy.size() - fuzzy_intersection) / reps;
                    fuzzy_f1Total += fuzzy_f1 / reps;
                    fuzzy_precisionTotal += fuzzy_precision / reps;
                    fuzzy_recallTotal += fuzzy_recall / reps;

                    outer_count = (AIMNres.size() - A_inner.size() - A_fuzzy.size()) / reps;
                    total_count = AIMNres.size() / reps;

                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US,
                        "%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f\n",
                        c, inner_brute, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal,
                        inner_recallTotal,
                        fuzzy_brute, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP,
                        fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer_count, total_count));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }
}
