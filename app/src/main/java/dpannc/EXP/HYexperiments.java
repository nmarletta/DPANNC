package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.*;
import java.util.*;

import dpannc.Progress;
import dpannc.Vector;
import dpannc.HY.HY;
import dpannc.database.DB;

public class HYexperiments {

    public static void exp1() throws Exception {
        String name = "HY1";
        DB db = new DB("DB/HY_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 200_000;

        int reps = 10;
        int[] dimensions = new int[] { 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 100, 150, 200,
                250 };

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double scale = 100;

        double alpha = 0.1 * scale;
        double radius = 0.5 * scale;

        Progress.newBar("Experiment " + name, (3 + 3 * reps) * dimensions.length);
        int pg = 0;

        Path filepathTarget = Paths.get("app/results/HY/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18\n"); // columns on y-axis
            writer.write(
                    "inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer, total\n");

            double inner_brute_total, inner_count_total, inner_TP_total, inner_FN_total, inner_FP_total,
                    inner_f1_total, inner_precision_total, inner_recall_total;
            double fuzzy_brute_total, fuzzy_count_total, fuzzy_TP_total, fuzzy_FN_total, fuzzy_FP_total,
                    fuzzy_f1_total, fuzzy_precision_total, fuzzy_recall_total;

            for (int d : dimensions) {
                Path filepathSource = Paths.get("app/resources/fasttext/truncated/trun_english_500K_" + d + "D.txt")
                        .toAbsolutePath();
                String table = "vectors";
                db.loadVectorsIntoDB(table, filepathSource, n, d);
                Progress.updateBar(++pg);

                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(data);
                    v.multiply(scale);
                    return v.dataString();
                }, table);
                Progress.updateBar(++pg);

                // initiate HY
                HY hy = new HY(sensitivity, epsilon, delta, db);
                // Progress.printAbove(hy.getSettingsString());
                hy.DP(false);

                // populate HY
                hy.populateFromDB(n, d, table, db);
                Progress.updateBar(++pg);

                inner_brute_total = inner_count_total = inner_TP_total = inner_FN_total = inner_FP_total = inner_f1_total = inner_precision_total = inner_recall_total = 0;
                fuzzy_brute_total = fuzzy_count_total = fuzzy_TP_total = fuzzy_FN_total = fuzzy_FP_total = fuzzy_f1_total = fuzzy_precision_total = fuzzy_recall_total = 0;
                double outer_count = 0;
                double total_count = 0;

                // experiment
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Progress.newStatus("Querying...");
                    Vector q = db.getRandomVector(table, random);
                    int count = hy.query(q, alpha, radius);
                    List<String> queryList = hy.queryList();
                    Progress.clearStatus();
                    Progress.updateBar(++pg);

                    // calculate all distances
                    Result BRUTEres = new Result().loadDistancesBetween(q, table, db);
                    Result HYres = new Result().loadDistancesBetween(q, queryList, table, db);
                    Progress.updateBar(++pg);

                    Progress.newStatus("writing results...");

                    // INNER REGION results
                    Set<String> B_inner = BRUTEres.lessThan(radius - alpha); // initial r for raw dataset
                    Set<String> A_inner = HYres.lessThan(radius - alpha); // HYs r for transformed dataset

                    Set<String> inner_intersectionSet = new HashSet<String>(A_inner);
                    inner_intersectionSet.retainAll(B_inner);

                    double inner_brute = (double) B_inner.size();
                    double inner_count = (double) A_inner.size();
                    double inner_TP = inner_intersectionSet.size();
                    double inner_FP = (double) A_inner.size() - inner_TP;
                    double inner_FN = (double) B_inner.size() - inner_TP;
                    double inner_precision = (inner_TP + inner_FP == 0) ? 0 : inner_TP / (inner_TP + inner_FP);
                    double inner_recall = (inner_TP + inner_FN == 0) ? 0 : inner_TP / (inner_TP + inner_FN);
                    double inner_f1 = (inner_precision + inner_recall == 0) ? 0
                            : 2 * inner_precision * inner_recall / (inner_precision + inner_recall);

                    inner_brute_total += inner_brute / reps;
                    inner_count_total += inner_count / reps;
                    inner_TP_total += inner_TP / reps;
                    inner_FP_total += inner_FP / reps;
                    inner_FN_total += inner_FN / reps;
                    inner_f1_total += inner_f1 / reps;
                    inner_precision_total += inner_precision / reps;
                    inner_recall_total += inner_recall / reps;

                    // FUZZY REGION results
                    Set<String> B_fuzzy = BRUTEres.within(radius - alpha, alpha + radius);
                    Set<String> A_fuzzy = HYres.within(radius - alpha, alpha + radius);

                    Set<String> fuzzy_intersectionSet = new HashSet<String>(A_fuzzy);
                    fuzzy_intersectionSet.retainAll(B_fuzzy);

                    double fuzzy_brute = (double) B_fuzzy.size();
                    double fuzzy_count = (double) A_fuzzy.size();
                    double fuzzy_TP = fuzzy_intersectionSet.size();
                    double fuzzy_FP = (double) A_fuzzy.size() - fuzzy_TP;
                    double fuzzy_FN = (double) B_fuzzy.size() - fuzzy_TP;
                    double fuzzy_precision = (fuzzy_TP + fuzzy_FP == 0) ? 0 : fuzzy_TP / (fuzzy_TP + fuzzy_FP);
                    double fuzzy_recall = (fuzzy_TP + fuzzy_FN == 0) ? 0 : fuzzy_TP / (fuzzy_TP + fuzzy_FN);
                    double fuzzy_f1 = (fuzzy_precision + fuzzy_recall == 0) ? 0
                            : 2 * fuzzy_precision * fuzzy_recall / (fuzzy_precision + fuzzy_recall);

                    fuzzy_brute_total += fuzzy_brute / reps;
                    fuzzy_count_total += fuzzy_count / reps;
                    fuzzy_TP_total += fuzzy_TP / reps;
                    fuzzy_FP_total += fuzzy_FP / reps;
                    fuzzy_FN_total += fuzzy_FN / reps;
                    fuzzy_f1_total += fuzzy_f1 / reps;
                    fuzzy_precision_total += fuzzy_precision / reps;
                    fuzzy_recall_total += fuzzy_recall / reps;

                    outer_count = ((double) HYres.size() - A_inner.size() - A_fuzzy.size()) / reps;
                    total_count = (double) HYres.size() / reps;
                    // if (count != HYres.size())
                    // Progress.printAbove(count + " :: " + HYres.size());
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US,
                        "%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f\n",
                        inner_brute_total, inner_count_total, inner_TP_total, inner_FN_total, inner_FP_total,
                        inner_f1_total, inner_precision_total, inner_recall_total,
                        fuzzy_brute_total, fuzzy_count_total, fuzzy_TP_total, fuzzy_FN_total, fuzzy_FP_total,
                        fuzzy_f1_total, fuzzy_precision_total, fuzzy_recall_total,
                        outer_count, total_count));
            }
            // metadata
            writer.write("# Data: fasttext/english_2M_\n");
            writer.write("# SEED=" + SEED + ", dataset size=" + n + "\n");
            writer.write("# Average taken over: " + reps + " repetions\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    public static void exp2() throws Exception {
        String name = "HY2";
        DB db = new DB("DB/HY_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 500_000;
        int d = 5;

        double min = 1;
        double max = 20;
        double inc = 1;

        int reps = 5;

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double scale = 100;

        Progress.newBar("Experiment " + name, (3 + 3 * reps) * (int) ((max - min) / inc));
        int pg = 0;

        Path filepathTarget = Paths.get("app/results/HY/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18\n"); // columns on y-axis
            writer.write(
                    "inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer, total\n");

            double inner_brute_total, inner_count_total, inner_TP_total, inner_FN_total, inner_FP_total,
                    inner_f1_total, inner_precision_total, inner_recall_total;
            double fuzzy_brute_total, fuzzy_count_total, fuzzy_TP_total, fuzzy_FN_total, fuzzy_FP_total,
                    fuzzy_f1_total, fuzzy_precision_total, fuzzy_recall_total;

            for (double r = min; r <= max; r += inc) {
                Path filepathSource = Paths.get("app/resources/fasttext/truncated/trun_english_500K_" + d + "D.txt")
                        .toAbsolutePath();
                String table = "vectors";
                db.loadVectorsIntoDB(table, filepathSource, n, d);
                Progress.updateBar(++pg);

                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(data);
                    v.multiply(scale);
                    return v.dataString();
                }, table);
                Progress.updateBar(++pg);

                // initiate HY
                HY hy = new HY(sensitivity, epsilon, delta, db);
                // Progress.printAbove(hy.getSettingsString());
                hy.DP(false);

                // populate HY
                hy.populateFromDB(n, d, table, db);
                Progress.updateBar(++pg);

                inner_brute_total = inner_count_total = inner_TP_total = inner_FN_total = inner_FP_total = inner_f1_total = inner_precision_total = inner_recall_total = 0;
                fuzzy_brute_total = fuzzy_count_total = fuzzy_TP_total = fuzzy_FN_total = fuzzy_FP_total = fuzzy_f1_total = fuzzy_precision_total = fuzzy_recall_total = 0;
                double outer_count = 0;
                double total_count = 0;

                // experiment
                for (int i = 0; i < reps; i++) {
                    double alpha = 0.05;
                    double radius = r;

                    // choose and run query
                    Progress.newStatus("Querying...");
                    Vector q = db.getRandomVector(table, random);
                    int count = hy.query(q, alpha, radius);
                    List<String> queryList = hy.queryList();
                    Progress.clearStatus();
                    Progress.updateBar(++pg);

                    // calculate all distances
                    Result BRUTEres = new Result().loadDistancesBetween(q, table, db);
                    Result HYres = new Result().loadDistancesBetween(q, queryList, table, db);
                    Progress.updateBar(++pg);

                    Progress.newStatus("writing results...");

                    // INNER REGION results
                    Set<String> B_inner = BRUTEres.lessThan(radius - alpha); // initial r for raw dataset
                    Set<String> A_inner = HYres.lessThan(radius - alpha); // HYs r for transformed dataset

                    Set<String> inner_intersectionSet = new HashSet<String>(A_inner);
                    inner_intersectionSet.retainAll(B_inner);

                    double inner_brute = (double) B_inner.size();
                    double inner_count = (double) A_inner.size();
                    double inner_TP = inner_intersectionSet.size();
                    double inner_FP = (double) A_inner.size() - inner_TP;
                    double inner_FN = (double) B_inner.size() - inner_TP;
                    double inner_precision = (inner_TP + inner_FP == 0) ? 0 : inner_TP / (inner_TP + inner_FP);
                    double inner_recall = (inner_TP + inner_FN == 0) ? 0 : inner_TP / (inner_TP + inner_FN);
                    double inner_f1 = (inner_precision + inner_recall == 0) ? 0
                            : 2 * inner_precision * inner_recall / (inner_precision + inner_recall);

                    inner_brute_total += inner_brute / reps;
                    inner_count_total += inner_count / reps;
                    inner_TP_total += inner_TP / reps;
                    inner_FP_total += inner_FP / reps;
                    inner_FN_total += inner_FN / reps;
                    inner_f1_total += inner_f1 / reps;
                    inner_precision_total += inner_precision / reps;
                    inner_recall_total += inner_recall / reps;

                    // FUZZY REGION results
                    Set<String> B_fuzzy = BRUTEres.within(radius - alpha, alpha + radius);
                    Set<String> A_fuzzy = HYres.within(radius - alpha, alpha + radius);

                    Set<String> fuzzy_intersectionSet = new HashSet<String>(A_fuzzy);
                    fuzzy_intersectionSet.retainAll(B_fuzzy);

                    double fuzzy_brute = (double) B_fuzzy.size();
                    double fuzzy_count = (double) A_fuzzy.size();
                    double fuzzy_TP = fuzzy_intersectionSet.size();
                    double fuzzy_FP = (double) A_fuzzy.size() - fuzzy_TP;
                    double fuzzy_FN = (double) B_fuzzy.size() - fuzzy_TP;
                    double fuzzy_precision = (fuzzy_TP + fuzzy_FP == 0) ? 0 : fuzzy_TP / (fuzzy_TP + fuzzy_FP);
                    double fuzzy_recall = (fuzzy_TP + fuzzy_FN == 0) ? 0 : fuzzy_TP / (fuzzy_TP + fuzzy_FN);
                    double fuzzy_f1 = (fuzzy_precision + fuzzy_recall == 0) ? 0
                            : 2 * fuzzy_precision * fuzzy_recall / (fuzzy_precision + fuzzy_recall);

                    fuzzy_brute_total += fuzzy_brute / reps;
                    fuzzy_count_total += fuzzy_count / reps;
                    fuzzy_TP_total += fuzzy_TP / reps;
                    fuzzy_FP_total += fuzzy_FP / reps;
                    fuzzy_FN_total += fuzzy_FN / reps;
                    fuzzy_f1_total += fuzzy_f1 / reps;
                    fuzzy_precision_total += fuzzy_precision / reps;
                    fuzzy_recall_total += fuzzy_recall / reps;

                    outer_count = ((double) HYres.size() - A_inner.size() - A_fuzzy.size()) / reps;
                    total_count = (double) HYres.size() / reps;
                    if (count != HYres.size())
                    Progress.printAbove(count + " :: " + HYres.size());
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US,
                        "%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f\n",
                        inner_brute_total, inner_count_total, inner_TP_total, inner_FN_total, inner_FP_total,
                        inner_f1_total, inner_precision_total, inner_recall_total,
                        fuzzy_brute_total, fuzzy_count_total, fuzzy_TP_total, fuzzy_FN_total, fuzzy_FP_total,
                        fuzzy_f1_total, fuzzy_precision_total, fuzzy_recall_total,
                        outer_count, total_count));
            }
            // metadata
            writer.write("# Data: fasttext/english_2M_\n");
            writer.write("# SEED=" + SEED + ", dataset size=" + n + "\n");
            writer.write("# Average taken over: " + reps + " repetions\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }
}
