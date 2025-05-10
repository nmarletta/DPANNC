package dpannc.AIMN;

import java.util.*;

public class CartesianProduct {
    public static void main(String[] args) {
        List<Set<Integer>> sets = List.of(
            Set.of(1),
            Set.of(2, 3),
            Set.of(4, 5),
            Set.of(6, 7)
        );
        List<String> product = CartesianProduct.cp(sets);
        product.forEach(System.out::println);
    }

    public static List<String> cp(List<Set<Integer>> sets) {
        List<String> result = new ArrayList<>();

        if (sets == null || sets.isEmpty()) return result;

        // Convert sets to list for index access
        List<List<Integer>> lists = new ArrayList<>();
        for (Set<Integer> set : sets) {
            if (set.isEmpty()) return result; // empty set → empty product
            lists.add(new ArrayList<>(set));
        }

        int[] indices = new int[lists.size()];

        while (true) {
            // Build one combination
            StringBuilder sb = new StringBuilder("R");
            for (int i = 0; i < indices.length; i++) {
                sb.append(":").append(lists.get(i).get(indices[i]));
            }
            result.add(sb.toString());

            // Increment indices
            int k = indices.length - 1;
            while (k >= 0) {
                indices[k]++;
                if (indices[k] < lists.get(k).size()) break;
                indices[k] = 0;
                k--;
            }
            if (k < 0) break; // done
        }

        return result;
    }
}
