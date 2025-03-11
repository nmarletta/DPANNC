package dpannc;

import java.util.*;

public class VectorComparators {
    // Compare by label alphabetically
    public static final Comparator<Vector> LABEL = Comparator.comparing(Vector::getLabel);

    // Compare using dot product with a given reference vector
    public static Comparator<Vector> byDot(Vector reference) {
        return Comparator.comparing(v -> v.dot(reference));
    }

    // Compare using dot product with a given reference vector
    public static Comparator<Vector> byDist(Vector reference) {
        return Comparator.comparing(v -> v.distance(reference));
    }
}