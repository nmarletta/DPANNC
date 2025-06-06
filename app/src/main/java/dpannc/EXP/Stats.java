package dpannc.EXP;

import java.util.*;

class Stats {

    double missedInner = 0.0;
    double includedInner = 0.0;
    double missedFuzzy = 0.0;
    double includedFuzzy = 0.0;
    double includedOuter = 0.0;

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

        Set<String> includedOuterSet = new HashSet<>(outer);
        includedOuterSet.retainAll(query);
        includedOuter += includedOuterSet.size();

        total += query.size();

        queries++;
    }

    public void reset() {
        missedInner = 0.0;
        includedInner = 0.0;
        missedFuzzy = 0.0;
        includedFuzzy = 0.0;
        includedOuter = 0.0;

        total = 0.0;

        queries = 0;
    }

    String stats() {
        return String.format(Locale.US, "%.0f, %.0f, %.0f, %.0f, %.0f, %.0f",
                missedInner / queries, includedInner / queries, missedFuzzy / queries, includedFuzzy / queries,
                includedOuter / queries, total / queries);
    }

    String statsHeader() {
        return "missedInner, includedInner, missedFuzzy, includedFuzzy, includedOuter, total";
    }

    String pr() {
        double relevance = (total) == 0 ? 1.0 : includedInner / (total);
        double precision = (total) == 0 ? 1.0 : (includedInner + includedFuzzy) / total;
        double recall = (missedInner + includedInner) == 0 ? 1.0 : includedInner / (missedInner + includedInner);
        return String.format(Locale.US, "%.3f, %.3f, %.3f",
                relevance, precision, recall);
    }

    String prHeader() {
        return "relevance, precision, recall";
    }

}