package dpannc.EXP;

import java.util.*;

class Stats {
    double actual = 0, count = 0, TP = 0, FN = 0, FP = 0;
    double f1Total = 0, precisionTotal = 0, recallTotal = 0;
    int reps = 0;

    // B = expected (brute force), A = actual (algorithm query set)
    void update(Set<String> B, Set<String> A) {
        Set<String> intersection = new HashSet<>(B);
        intersection.retainAll(A);

        double tp = intersection.size();
        double fn = B.size() - tp;
        double fp = A.size() - tp;

        double precision = (tp + fp == 0) ? 0 : tp / (tp + fp);
        double recall = (tp + fn == 0) ? 0 : tp / (tp + fn);
        double f1 = (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);

        actual += B.size();
        count += A.size();
        TP += tp;
        FN += fn;
        FP += fp;
        precisionTotal += precision;
        recallTotal += recall;
        f1Total += f1;
        reps++;
    }

    
    String toCSV() {
        return String.format(Locale.US, "%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f",
            actual / reps, count / reps, TP / reps, FN / reps, FP / reps,
            f1Total / reps, precisionTotal / reps, recallTotal / reps);
    }

    String TP() {
        return String.format(Locale.US, "%.1f", TP / reps);
    }
    String FN() {
        return String.format(Locale.US, "%.1f", FN / reps);
    }
    String FP() {
        return String.format(Locale.US, "%.1f", FP / reps);
    }
    String precision() {
        return String.format(Locale.US, "%.1f", precisionTotal / reps);
    }
    String recall() {
        return String.format(Locale.US, "%.1f", recallTotal / reps);
    }
    String f1() {
        return String.format(Locale.US, "%.1f", f1Total / reps);
    }
}

