package dpannc.EXP;

import java.util.*;

class Stats {

    double innerMissed = 0.0;
    double innerTrue = 0.0;
    double includedFar = 0.0;
    double includedFuzzy = 0.0;
    double total = 0.0;

    int queries = 0;

    // B = expected (brute force), A = actual (algorithm query set)
    void update(Result data, Set<String> query, double r, double cr) {

        Set<String> inner = data.lessThan(r);
        Set<String> fuzzy = data.within(r, cr);
        Set<String> outer = data.greaterThan(cr);

        // false negative
        // missed near points
        Set<String> innerMissedSet = new HashSet<>(inner);
        innerMissedSet.removeAll(query);
        innerMissed += innerMissedSet.size();

        // true positive
        // found near points
        Set<String> innerTrueSet = new HashSet<>(inner);
        innerTrueSet.retainAll(query);
        innerTrue += innerTrueSet.size();

        // false positive
        // included far points
        Set<String> includedFarSet = new HashSet<>(outer);
        includedFarSet.retainAll(query);
        includedFar += includedFarSet.size();

        // fuzzy positive
        Set<String> includedFuzzySet = new HashSet<>(fuzzy);
        includedFuzzySet.retainAll(query);
        includedFuzzy += includedFuzzySet.size();

        total += query.size();

        queries++;
    }

    String stats() {
        return String.format(Locale.US, "%.1f,%.1f,%.1f,%.1f,%.1f",
                innerMissed / queries, innerTrue / queries, includedFuzzy / queries, includedFar / queries, total / queries);
    }

    String statsHeader() {
        return "innerMissed, innerTrue, includedFuzzy, includedFar, total";
    }

    String pr() {
        double TP = innerTrue;
        double FN = innerMissed;
        double FP = includedFuzzy + includedFar;
        double precision = (TP + FP) == 0 ? 1.0 : TP / (TP + FP);
        double recall = (TP + FN) == 0 ? 1.0 : TP / (TP + FN);
        return String.format(Locale.US, "%.3f, %.3f", 
        precision, recall);
    }

    String prHeader() {
        return "precision, recall";
    }
}
