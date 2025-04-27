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
import dpannc.Vector;
import dpannc.AIMN.AIMN;
import dpannc.database.DB;

public class AIMNexperiments {

    public static void main(String[] args) throws Exception {
        // expAIMN();
        exp7();
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
        double c = 1.1;

        Progress.newBar("Experiment " + name, (3 + reps));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25\n"); // columns on y-axis
            writer.write(
                    "r_i, inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer_actual, outer_count, outer_TP, outer_FN, outer_FP, outer_f1Total, outer_precisionTotal, outer_recallTotal, total\n");

            // load vectors to DB
            String table = "vectors";
            db.loadVectorsIntoDB(table, filepathSource, n, d);
            Progress.updateBar(++pg);

            // transform vectors
            NashDevice nd = new NashDevice(d, d, random);
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                v = nd.transform(v);
                return v.dataString();
            }, table);
            Progress.updateBar(++pg);

            // initiate AIMN and populate
            AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta, db);
            Progress.printAbove(aimn.getSettingsString());
            aimn.DP(false);
            aimn.populateFromDB(table, db);
            double r = aimn.getR();
            Progress.updateBar(++pg);

            Stats inner = new Stats();
            Stats fuzzy = new Stats();
            Stats outer = new Stats();
            double total_count = 0;

            // results
            for (int i = 0; i < reps; i++) {
                // choose and run query
                Progress.newStatus("Querying...");
                Vector q = db.getRandomVector(table, random);
                aimn.query(q);
                List<String> queryList = aimn.queryList();
                Progress.clearStatus();

                Progress.newStatus("writing results...");

                // calculate all distances
                Result AIMNres = new Result().loadDistancesBetween(q, queryList, table, db);
                Result BRUTEres = new Result().loadDistancesBetween(q, table, db);

                // INNER REGION results
                Set<String> A_inner = AIMNres.lessThan(r);
                Set<String> B_inner = BRUTEres.lessThan(r);
                inner.update(B_inner, A_inner);

                // FUZZY REGION results
                Set<String> A_fuzzy = AIMNres.within(r, c * r);
                Set<String> B_fuzzy = BRUTEres.within(r, c * r);
                fuzzy.update(B_fuzzy, A_fuzzy);

                // OUTER REGION results
                Set<String> A_outer = AIMNres.greaterThan(c * r);
                Set<String> B_outer = BRUTEres.greaterThan(c * r);
                outer.update(B_outer, A_outer);

                total_count += AIMNres.size() / reps;

                Progress.clearStatus();
                Progress.updateBar(++pg);
            }

            // write result to file
            writer.write(String.format(Locale.US, "%.2f,%s,%s,%s,%.1f\n",
                    r, inner.toCSV(), fuzzy.toCSV(), outer.toCSV(), total_count));

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
        int reps = 10;
        double[] cValues = new double[] { 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0 };

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;

        Progress.newBar("Experiment " + name, (2 + reps * cValues.length));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25\n"); // columns on y-axis
            writer.write(
                    "c, inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer_actual, outer_count, outer_TP, outer_FN, outer_FP, outer_f1Total, outer_precisionTotal, outer_recallTotal, total\n");

            // load vectors to DB
            String table = "vectors";
            db.loadVectorsIntoDB(table, filepathSource, n, d);
            Progress.updateBar(++pg);

            // transform vectors
            NashDevice nd = new NashDevice(d, d, random);
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                v = nd.transform(v);
                return v.dataString();
            }, table);
            Progress.updateBar(++pg);

            for (double c : cValues) {
                random = new Random(SEED);

                // initiate AIMN and populate
                AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta, db);
                Progress.printAbove(aimn.getSettingsString());
                aimn.DP(false);
                aimn.populateFromDB(table, db);
                double r = aimn.getR();
                Progress.updateBar(++pg);

                Stats inner = new Stats();
                Stats fuzzy = new Stats();
                Stats outer = new Stats();
                double total_count = 0;

                // results
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Progress.newStatus("Querying...");
                    Vector q = db.getRandomVector(table, random);
                    aimn.query(q);
                    List<String> queryList = aimn.queryList();
                    Progress.clearStatus();

                    // calculate all distances
                    Result AIMNres = new Result().loadDistancesBetween(q, queryList, table, db);
                    Result BRUTEres = new Result().loadDistancesBetween(q, table, db);

                    Progress.newStatus("writing results...");

                    // INNER REGION results
                    Set<String> A_inner = AIMNres.lessThan(r);
                    Set<String> B_inner = BRUTEres.lessThan(r);
                    inner.update(B_inner, A_inner);

                    // FUZZY REGION results
                    Set<String> A_fuzzy = AIMNres.within(r, c * r);
                    Set<String> B_fuzzy = BRUTEres.within(r, c * r);
                    fuzzy.update(B_fuzzy, A_fuzzy);

                    // OUTER REGION results
                    Set<String> A_outer = AIMNres.greaterThan(c * r);
                    Set<String> B_outer = BRUTEres.greaterThan(c * r);
                    outer.update(B_outer, A_outer);

                    total_count += AIMNres.size() / reps;

                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.1f,%s,%s,%s,%.1f\n",
                        c, inner.toCSV(), fuzzy.toCSV(), outer.toCSV(), total_count));
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
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25\n"); // columns on y-axis
            writer.write(
                    "r_i, inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer_actual, outer_count, outer_TP, outer_FN, outer_FP, outer_f1Total, outer_precisionTotal, outer_recallTotal, total\n");

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

                // find transformed r and scaling factor
                double r_aimn = aimn.getR(); // the fixed radius that we should fit the data to
                double r_target = DistMapper.getMedianRev(r_aimn); // the desired radius for pre nash transformation
                double scale = r_target / r_i; // scaling factor

                // transform vectors
                NashDevice nd = new NashDevice(d, d, random);
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    v.multiply(scale); // scaling first
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

                // results
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Progress.newStatus("Querying...");
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);
                    aimn.query(q2);
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

                    total_count += AIMNres.size() / reps;

                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.1f,%s,%s,%s,%.1f\n",
                        r_i, inner.toCSV(), fuzzy.toCSV(), outer.toCSV(), total_count));
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

        // Path filepathSource =
        // Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathSource = Paths.get("app/resources/fashionMNIST/fashionMNIST_60K_784D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25\n"); // columns on y-axis
            writer.write(
                    "r_i, inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer_actual, outer_count, outer_TP, outer_FN, outer_FP, outer_f1Total, outer_precisionTotal, outer_recallTotal, total\n");

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

                // find transformed r and scaling factor
                double r_aimn = aimn.getR(); // the fixed radius that we should fit the data to
                double r_target = DistMapper.getMedianRev(r_aimn); // the desired radius for pre nash transformation
                double scale = r_target / r_i; // scaling factor

                NashDevice nd = new NashDevice(d, d, random);
                db.applyTransformation(data -> {
                    Vector v = Vector.fromString(".", data);
                    v.multiply(scale); // scaling first
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

                // results
                for (int i = 0; i < reps; i++) {
                    // choose and run query
                    Progress.newStatus("Querying...");
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);
                    aimn.query(q2);
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

                    total_count += AIMNres.size() / reps;

                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.1f,%s,%s,%s,%.1f\n",
                        r_i, inner.toCSV(), fuzzy.toCSV(), outer.toCSV(), total_count));
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

    public static void exp5() throws Exception {
        String name = "aimn5";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 100_000;
        int d = 300;
        int reps = 10;

        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        double c = 1.1;

        Progress.newBar("Experiment " + name, 2 * (3 + reps));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25\n"); // columns on y-axis
            writer.write(
                    "nash?,r, inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer_actual, outer_count, outer_TP, outer_FN, outer_FP, outer_f1Total, outer_precisionTotal, outer_recallTotal, total\n");

            // load vectors to DB
            String table = "vectors";
            db.loadVectorsIntoDB(table, filepathSource, n, d);
            Progress.updateBar(++pg);

            // transform vectors
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                v.normalize();
                return v.dataString();
            }, table);
            Progress.updateBar(++pg);

            /*
             * WITHOUT NASH
             */
            // initiate AIMN and populate
            AIMN aimn = new AIMN(n, d, c, sensitivity, epsilon, delta, db);
            Progress.printAbove(aimn.getSettingsString());
            aimn.DP(false);
            aimn.populateFromDB(table, db);
            double r = aimn.getR();
            Progress.updateBar(++pg);

            Stats inner = new Stats();
            Stats fuzzy = new Stats();
            Stats outer = new Stats();
            double total_count = 0;

            // results
            for (int i = 0; i < reps; i++) {
                // choose and run query
                Progress.newStatus("Querying...");
                Vector q = db.getRandomVector(table, random);
                aimn.query(q);
                List<String> queryList = aimn.queryList();
                Progress.clearStatus();

                // calculate all distances
                Result AIMNres = new Result().loadDistancesBetween(q, queryList, table, db);
                Result BRUTEres = new Result().loadDistancesBetween(q, table, db);

                Progress.newStatus("writing results...");

                // INNER REGION results
                Set<String> A_inner = AIMNres.lessThan(r);
                Set<String> B_inner = BRUTEres.lessThan(r);
                inner.update(B_inner, A_inner);

                // FUZZY REGION results
                Set<String> A_fuzzy = AIMNres.within(r, c * r);
                Set<String> B_fuzzy = BRUTEres.within(r, c * r);
                fuzzy.update(B_fuzzy, A_fuzzy);

                // OUTER REGION results
                Set<String> A_outer = AIMNres.greaterThan(c * r);
                Set<String> B_outer = BRUTEres.greaterThan(c * r);
                outer.update(B_outer, A_outer);

                total_count += AIMNres.size() / reps;

                Progress.clearStatus();
                Progress.updateBar(++pg);
            }

            /*
             * WITH NASH
             */
            // write result to file
            writer.write(String.format(Locale.US, "%s,%.2f,%s,%s,%s,%.1f\n",
                    "with nash", r, inner.toCSV(), fuzzy.toCSV(), outer.toCSV(), total_count));

            // transform vectors
            NashDevice nd = new NashDevice(d, d, random);
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                v = nd.transform(v);
                return v.dataString();
            }, table);
            Progress.updateBar(++pg);

            // initiate AIMN and populate
            aimn = new AIMN(n, d, c, sensitivity, epsilon, delta, db);
            Progress.printAbove(aimn.getSettingsString());
            aimn.DP(false);
            aimn.populateFromDB(table, db);
            r = aimn.getR();
            Progress.updateBar(++pg);

            inner = new Stats();
            fuzzy = new Stats();
            outer = new Stats();
            total_count = 0;

            // results
            for (int i = 0; i < reps; i++) {
                // choose and run query
                Progress.newStatus("Querying...");
                Vector q = db.getRandomVector(table, random);
                aimn.query(q);
                List<String> queryList = aimn.queryList();
                Progress.clearStatus();

                // calculate all distances
                Result AIMNres = new Result().loadDistancesBetween(q, queryList, table, db);
                Result BRUTEres = new Result().loadDistancesBetween(q, table, db);

                Progress.newStatus("writing results...");

                // INNER REGION results
                Set<String> A_inner = AIMNres.lessThan(r);
                Set<String> B_inner = BRUTEres.lessThan(r);
                inner.update(B_inner, A_inner);

                // FUZZY REGION results
                Set<String> A_fuzzy = AIMNres.within(r, c * r);
                Set<String> B_fuzzy = BRUTEres.within(r, c * r);
                fuzzy.update(B_fuzzy, A_fuzzy);

                // OUTER REGION results
                Set<String> A_outer = AIMNres.greaterThan(c * r);
                Set<String> B_outer = BRUTEres.greaterThan(c * r);
                outer.update(B_outer, A_outer);

                total_count += AIMNres.size() / reps;

                Progress.clearStatus();
                Progress.updateBar(++pg);
            }

            // write result to file
            writer.write(String.format(Locale.US, "%s,%.2f,%s,%s,%s,%.1f\n",
                    "with nash", r, inner.toCSV(), fuzzy.toCSV(), outer.toCSV(), total_count));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    public static void exp6() throws Exception {
        String name = "aimn6";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 100_000;
        int d = 300;
        int dPrime = 300;
        int reps = 1;
        double c = 1.1;

        double min = 0.5;
        double max = 2.0;
        double inc = (max - min) / 20;

        Progress.newBar("Experiment " + name, (3 + reps * (int) ((max - min) / inc) + 1));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25\n"); // columns on y-axis
            writer.write(
                    "r, inner_actual, inner_count, inner_TP, inner_FN, inner_FP, inner_f1Total, inner_precisionTotal, inner_recallTotal, fuzzy_actual, fuzzy_count, fuzzy_TP, fuzzy_FN, fuzzy_FP, fuzzy_f1Total, fuzzy_precisionTotal, fuzzy_recallTotal, outer_actual, outer_count, outer_TP, outer_FN, outer_FP, outer_f1Total, outer_precisionTotal, outer_recallTotal, total\n");

            // load vectors to DB
            String table1 = "vectors1";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            Progress.updateBar(++pg);
            String table2 = "vectors2";
            db.loadVectorsIntoDB(table2, filepathSource, n, d);
            Progress.updateBar(++pg);

            // transform vectors
            NashDevice nd = new NashDevice(d, dPrime, random);
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                v = nd.transform(v);
                return v.dataString();
            }, table1);
            Progress.updateBar(++pg);

            // results
            for (double r = min; r <= max; r += inc) {
                Stats inner = new Stats();
                Stats fuzzy = new Stats();
                Stats outer = new Stats();
                double total_count = 0;

                for (int i = 0; i < reps; i++) {
                    // choose query point
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);

                    // calculate all distances
                    Result Ares = new Result().loadDistancesBetween(q1, table1, db);
                    Result Bres = new Result().loadDistancesBetween(q2, table2, db);

                    Progress.newStatus("writing results...");

                    // INNER REGION results
                    Set<String> A_inner = Ares.lessThan(r);
                    Set<String> B_inner = Bres.lessThan(r);
                    inner.update(B_inner, A_inner);

                    // FUZZY REGION results
                    Set<String> A_fuzzy = Ares.within(r, c * r);
                    Set<String> B_fuzzy = Bres.within(r, c * r);
                    fuzzy.update(B_fuzzy, A_fuzzy);

                    // OUTER REGION results
                    Set<String> A_outer = Ares.greaterThan(c * r);
                    Set<String> B_outer = Bres.greaterThan(c * r);
                    outer.update(B_outer, A_outer);

                    total_count += Ares.size() / reps;

                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                    A_inner.retainAll(B_outer);
                    B_inner.retainAll(A_outer);
                    Progress.printAbove("i->o: " + A_inner.size() + ", o->i: " + B_inner.size());
                }

                // write result to file
                writer.write(String.format(Locale.US, "%.2f,%s,%s,%s,%.1f\n",
                        r, inner.toCSV(), fuzzy.toCSV(), outer.toCSV(), total_count));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Progress.end();
    }

    // points jumping between regions
    public static void exp7() throws Exception {
        String name = "aimn7";
        DB db = new DB("DB/AIMN_" + name, true);

        int SEED = 100;
        Random random = new Random(SEED);
        int n = 100_000;
        int d = 150;
        int dPrime = 500;
        int reps = 10;
        double c = 1.1;

        double min = 0.01;
        double max = 0.9;
        double inc = (max - min) / 20;

        Progress.newBar("Experiment " + name, (3 + reps * (int) ((max - min) / inc) + 1));
        int pg = 0;

        Path filepathSource = Paths.get("app/resources/fasttext/english_2M_300D.txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/results/AIMN/" + name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance from q / found vectors\n"); // title
            writer.write("0\n"); // coulmns on x-axis
            writer.write("1,2,3,4,5,6,7\n"); // columns on y-axis
            writer.write(
                    "r, i2f, f2i, f2o, o2f, i2o, o2i, total_count\n");

            // load vectors to DB
            String table1 = "vectors1";
            db.loadVectorsIntoDB(table1, filepathSource, n, d);
            Progress.updateBar(++pg);
            String table2 = "vectors2";
            db.loadVectorsIntoDB(table2, filepathSource, n, d);
            Progress.updateBar(++pg);

            // transform vectors
            NashDevice nd = new NashDevice(d, dPrime, random);
            db.applyTransformation(data -> {
                Vector v = Vector.fromString(".", data);
                v = nd.transform(v);
                return v.dataString();
            }, table1);
            Progress.updateBar(++pg);

            // results
            for (double r = min; r <= max; r += inc) {
                // Stats inner = new Stats();
                // Stats fuzzy = new Stats();
                // Stats outer = new Stats();
                double total_count = 0;
                double i2f = 0, f2i = 0, f2o = 0, o2f = 0, i2o = 0, o2i = 0;
                double A_inner_total = 0, A_fuzzy_total = 0, A_outer_total = 0;
                for (int i = 0; i < reps; i++) {
                    // choose query point
                    Vector q1 = db.getRandomVector(table1, random);
                    Vector q2 = db.getVectorByLabel(q1.getLabel(), table2);

                    // calculate all distances
                    Result Ares = new Result().loadDistancesBetween(q1, table1, db);
                    Result Bres = new Result().loadDistancesBetween(q2, table2, db);

                    Progress.newStatus("writing results...");

                    // INNER REGION results
                    Set<String> A_inner = Ares.lessThan(r);
                    Set<String> B_inner = Bres.lessThan(r);
                    // inner.update(B_inner, A_inner);

                    // FUZZY REGION results
                    Set<String> A_fuzzy = Ares.within(r, c * r);
                    Set<String> B_fuzzy = Bres.within(r, c * r);
                    // fuzzy.update(B_fuzzy, A_fuzzy);

                    // OUTER REGION results
                    Set<String> A_outer = Ares.greaterThan(c * r);
                    Set<String> B_outer = Bres.greaterThan(c * r);
                    // outer.update(B_outer, A_outer);

                    A_inner_total += A_inner.size() / reps;
                    A_fuzzy_total += A_fuzzy.size() / reps;
                    A_outer_total += A_outer.size() / reps;

                    Set<String> i_to_f = new HashSet<>(A_inner);
                    i_to_f.retainAll(B_fuzzy);

                    Set<String> f_to_i = new HashSet<>(A_fuzzy);
                    f_to_i.retainAll(B_inner);

                    Set<String> f_to_o = new HashSet<>(A_fuzzy);
                    f_to_o.retainAll(B_outer);

                    Set<String> o_to_f = new HashSet<>(A_outer);
                    o_to_f.retainAll(B_fuzzy);

                    Set<String> i_to_o = new HashSet<>(A_inner);
                    i_to_o.retainAll(B_outer);

                    Set<String> o_to_i = new HashSet<>(A_outer);
                    o_to_i.retainAll(B_inner);

                    i2f += i_to_f.size() / reps;
                    f2i += f_to_i.size() / reps;
                    f2o += f_to_o.size() / reps;
                    o2f += o_to_f.size() / reps;
                    i2o += i_to_o.size() / reps;
                    o2i += o_to_i.size() / reps;
                    total_count += Ares.size() / reps;

                    Progress.clearStatus();
                    Progress.updateBar(++pg);
                }

                i2f = A_inner_total == 0 ? 0 : i2f / A_inner_total;
                f2i = A_fuzzy_total == 0 ? 0 : f2i / A_fuzzy_total;
                f2o = A_fuzzy_total == 0 ? 0 : f2o / A_fuzzy_total;
                o2f = A_outer_total == 0 ? 0 : o2f / A_outer_total;
                i2o = A_inner_total == 0 ? 0 : i2o / A_inner_total;
                o2i = A_outer_total == 0 ? 0 : o2i / A_outer_total;
                writer.write(String.format(Locale.US,
                        "%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.0f,%.0f,%.0f\n", 
                        r, i2f, i2o, f2i, f2o, o2i, o2f, A_inner_total, A_fuzzy_total, A_outer_total));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Progress.end();
    }
}