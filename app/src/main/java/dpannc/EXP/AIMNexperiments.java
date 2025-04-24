package dpannc.EXP;

import java.io.FileWriter;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import dpannc.DistMapper;
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
        String name = "aimn1";
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
        String name = "aimn2";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 100_000;
        int d = 300;
        int reps = 1;
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
                random = new Random(SEED);
                // initiate AIMN and populate
                AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table, db);
                Progress.updateBar(++pg);

                inner_brute = inner_count = inner_TP = inner_FN = inner_FP = inner_f1Total = inner_precisionTotal = inner_recallTotal = 0;
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

                    outer_count += (AIMNres.size() - A_inner.size() - A_fuzzy.size()) / reps;
                    total_count += AIMNres.size() / reps;

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

    public static void exp3() throws Exception {
        String name = "aimn3";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 60_000;
        int d = 784;
        double c = 1.5;

        int reps = 5;
        double[] rValues = new double[] { 800.0, 1000.0, 1200.0, 1400.0, 1600.0 };

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;

        Progress.newBar("Experiment " + name, (1 + (3 + reps) * rValues.length));
        int pg = 0;

        // Path filepathSource =
        // Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathSource = Paths.get("app/resources/fashionMNIST/fashionMNIST_60K_784D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18\n"); // columns on y-axis
            writer.write(
                    "r_i, r_aimn, r_target, inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer, total\n");

            double inner_brute_total, inner_count_total, inner_TP_total, inner_FN_total, inner_FP_total,
                    inner_f1_total, inner_precision_total, inner_recall_total;
            double fuzzy_brute_total, fuzzy_count_total, fuzzy_TP_total, fuzzy_FN_total, fuzzy_FP_total,
                    fuzzy_f1_total, fuzzy_precision_total, fuzzy_recall_total;

            String table1 = "Initialvectors";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            Progress.updateBar(++pg);

            for (double r_i : rValues) {
                // initiate AIMN
                AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);

                // load vectors to DB
                String table2 = "TransformedVectors";
                db.loadVectorsIntoDB(table2, filepathSource, n, d);
                Progress.updateBar(++pg);

                // find new r
                double r_aimn = aimn.getR(); // the fixed radius that we should fit the data to
                double r_target = DistMapper.getMedianRev(r_aimn); // the desired radius for pre nash transformation
                double scale = r_target / r_i; // scaling factor
                // Progress.printAbove("(r_n): " + r_target + "=" + r_aimn + "/" +
                // DistMapper.getMedianRev(r_aimn));
                // Progress.printAbove("scale: " + scale + "=" + r_target + "/" + r_i);
                // transform vectors
                NashDevice nd = new NashDevice(d, d, random);
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    v.multiply(scale);
                    Vector w = nd.transform(v);
                    // v.normalize();
                    return w.dataString();
                }, table2);
                Progress.updateBar(++pg);

                // populate AIMN
                aimn.populateFromDB(table2, db);
                Progress.updateBar(++pg);

                inner_brute_total = inner_count_total = inner_TP_total = inner_FN_total = inner_FP_total = inner_f1_total = inner_precision_total = inner_recall_total = 0;
                fuzzy_brute_total = fuzzy_count_total = fuzzy_TP_total = fuzzy_FN_total = fuzzy_FP_total = fuzzy_f1_total = fuzzy_precision_total = fuzzy_recall_total = 0;
                double outer_count = 0;
                double total_count = 0;

                // experiment
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Progress.newStatus("Querying...");
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);
                    int count = aimn.query(q2);
                    List<String> queryList = aimn.queryList();
                    Progress.clearStatus();

                    // calculate all distances
                    Result BRUTEres = new Result().loadDistancesBetween(q1, table1, db);
                    Result AIMNres = new Result().loadDistancesBetween(q2, queryList, table2, db);

                    Progress.newStatus("writing results...");

                    // INNER REGION results
                    Set<String> B_inner = BRUTEres.lessThan(r_i); // initial r for raw dataset
                    Set<String> A_inner = AIMNres.lessThan(r_aimn); // AIMNs r for transformed dataset

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
                    Set<String> B_fuzzy = BRUTEres.within(r_i, c * r_i);
                    Set<String> A_fuzzy = AIMNres.within(r_aimn, c * r_aimn);

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

                    outer_count += ((double) AIMNres.size() - A_inner.size() - A_fuzzy.size()) / reps;
                    total_count += (double) AIMNres.size() / reps;
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US,
                        "%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f\n",
                        r_i, r_aimn, r_target, inner_brute_total, inner_count_total, inner_TP_total, inner_FN_total,
                        inner_FP_total,
                        inner_f1_total, inner_precision_total, inner_recall_total,
                        fuzzy_brute_total, fuzzy_count_total, fuzzy_TP_total, fuzzy_FN_total, fuzzy_FP_total,
                        fuzzy_f1_total, fuzzy_precision_total, fuzzy_recall_total,
                        outer_count, total_count));
            }
            // metadata
            writer.write("# Data: " + filepathSource.toString() + "\n");
            writer.write("# SEED=" + SEED + ", dimensions=" + d + ", dataset size=" + n + "\n");
            writer.write("# Average taken over: " + reps + " repetions\n");
            writer.write("# Nashed value: Median\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    public static void exp4() throws Exception {
        String name = "aimn4";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 60_000;
        int d = 784;
        double c = 1.2;

        int reps = 1;
        double[] rValues = new double[] { 1000.0, 1200.0, 1400.0, 1600.0, 1800.0, 2000.0 };

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;

        Progress.newBar("Experiment " + name, (1 + (3 + reps) * rValues.length));
        int pg = 0;

        // Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathSource = Paths.get("app/resources/fashionMNIST/fashionMNIST_60K_784D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18\n"); // columns on y-axis
            writer.write(
                    "r_i, inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer_actual, outer_count, outer_TP, total\n");


            String table1 = "Initialvectors";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            Progress.updateBar(++pg);

            for (double r_i : rValues) {
                // initiate AIMN
                AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);

                // load vectors to DB
                String table2 = "TransformedVectors";
                db.loadVectorsIntoDB(table2, filepathSource, n, d);
                Progress.updateBar(++pg);

                // find new r
                double r_aimn = aimn.getR(); // the fixed radius that we should fit the data to
                double r_target = DistMapper.getMedianRev(r_aimn); // the desired radius for pre nash transformation
                double scale = r_target / r_i; // scaling factor

                NashDevice nd = new NashDevice(d, d, random);
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    v.multiply(scale);
                    Vector w = nd.transform(v);
                    return w.dataString();
                }, table2);
                Progress.updateBar(++pg);

                // populate AIMN
                aimn.populateFromDB(table2, db);
                Progress.updateBar(++pg);

                Stats inner = new Stats();
                Stats fuzzy = new Stats();
                Stats outer = new Stats();
                double total_count = 0;

                // experiment
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Progress.newStatus("Querying...");
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);
                    total_count = aimn.query(q2) / reps;
                    List<String> queryList = aimn.queryList();
                    Progress.clearStatus();

                    // calculate all distances
                    Result BRUTEres = new Result().loadDistancesBetween(q1, table1, db);
                    Result AIMNres = new Result().loadDistancesBetween(q2, queryList, table2, db);

                    Progress.newStatus("writing results...");

                    // INNER REGION results
                    Set<String> B_inner = BRUTEres.lessThan(r_i); // initial r for raw dataset
                    Set<String> A_inner = AIMNres.lessThan(r_aimn); // AIMNs r for transformed dataset
                    inner.update(B_inner, A_inner);

                    // FUZZY REGION results
                    Set<String> B_fuzzy = BRUTEres.within(r_i, c * r_i);
                    Set<String> A_fuzzy = AIMNres.within(r_aimn, c * r_aimn);
                    fuzzy.update(B_fuzzy, A_fuzzy);

                    // OUTER REGION results
                    Set<String> B_outer = BRUTEres.greaterThan(c * r_i);
                    Set<String> A_outer = AIMNres.greaterThan(c * r_aimn);
                    outer.update(B_outer, A_outer);
                    
                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.1f,%s,%s,%.1f,%.1f,%.1f,%.1f\n",
                        r_i,
                        inner.toCSV(),
                        fuzzy.toCSV(),
                        outer.toCSV(), total_count));
            }

            // metadata
            writer.write("# Data: " + filepathSource.toString() + "\n");
            writer.write("# SEED=" + SEED + ", dimensions=" + d + ", dataset size=" + n + "\n");
            writer.write("# Average taken over: " + reps + " repetions\n");
            writer.write("# Nashed value: Median\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }
}