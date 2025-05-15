package dpannc.EXP;

import java.util.*;

class Stats {

    double missedInner = 0.0;
    double includedInner = 0.0;
    double missedFuzzy = 0.0;
    double includedFuzzy = 0.0;
    double includedFar = 0.0;
    
    double total = 0.0;

    int queries = 0;

    // B = expected (brute force), A = actual (algorithm query set)
    void update(Result data, Set<String> query, double r, double cr) {

        Set<String> inner = data.lessThan(r);
        Set<String> fuzzy = data.within(r, cr);
        Set<String> outer = data.greaterThan(cr);

        Set<String> missedInnerSet = new HashSet<>(inner);
        missedInnerSet.removeAll(query);
        missedInner += missedInnerSet.size();

        Set<String> includedInnerSet = new HashSet<>(inner);
        includedInnerSet.retainAll(query);
        includedInner += includedInnerSet.size();

        Set<String> missedFuzzySet = new HashSet<>(fuzzy);
        missedFuzzySet.removeAll(query);
        missedFuzzy += missedFuzzySet.size();

        Set<String> includedFuzzySet = new HashSet<>(fuzzy);
        includedFuzzySet.retainAll(query);
        includedFuzzy += includedFuzzySet.size();

        Set<String> includedFarSet = new HashSet<>(outer);
        includedFarSet.retainAll(query);
        includedFar += includedFarSet.size();

        

        total += query.size();

        queries++;
    }

    String stats() {
        return String.format(Locale.US, "%.1f, %.1f, %.1f, %.1f, %.1f, %.1f",
                missedInner / queries, includedInner / queries, missedFuzzy / queries, includedFuzzy / queries, includedFar / queries, total / queries);
    }

    String statsHeader() {
        return "missedInner, includedInner, missedFuzzy, includedFuzzy, includedFar, total";
    }

    String pr() {
        double fuzzyCoverage = (includedFuzzy + includedFar) == 0 ? 1.0 : includedFuzzy / (includedFuzzy + includedFar);
        double precision = (total + missedInner + missedFuzzy) == 0 ? 1.0 : (includedInner + includedFuzzy) / (total + missedInner + missedFuzzy);
        double recall = (missedInner + includedInner) == 0 ? 1.0 : includedInner / (missedInner + includedInner);
        return String.format(Locale.US, "%.3f, %.3f, %.3f", 
        precision, recall);
    }

    String prHeader() {
        return "fuzzycoverage, precision, recall";
    }
}