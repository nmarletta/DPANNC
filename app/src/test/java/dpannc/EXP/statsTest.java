// package dpannc.EXP;

// import org.junit.jupiter.api.Test;
// import java.util.*;
// import static org.junit.jupiter.api.Assertions.*;

// class StatsTest {

//     @Test
//     void testUpdateWithPerfectMatch() {
//         Stats stats = new Stats();
//         Set<String> B = new HashSet<>(Arrays.asList("a", "b", "c"));
//         Set<String> A = new HashSet<>(Arrays.asList("a", "b", "c"));

//         stats.update(B, A);

//         assertEquals(3.0, stats.actual);
//         assertEquals(3.0, stats.count);
//         assertEquals(3.0, stats.TP);
//         assertEquals(0.0, stats.FN);
//         assertEquals(0.0, stats.FP);
//         assertEquals(1.0, stats.precisionTotal);
//         assertEquals(1.0, stats.recallTotal);
//         assertEquals(1.0, stats.f1Total);
//         assertEquals(1, stats.reps);
//     }

//     @Test
//     void testUpdateWithNoOverlap() {
//         Stats stats = new Stats();
//         Set<String> B = new HashSet<>(Arrays.asList("a", "b", "c"));
//         Set<String> A = new HashSet<>(Arrays.asList("x", "y", "z"));

//         stats.update(B, A);

//         assertEquals(3.0, stats.actual);
//         assertEquals(3.0, stats.count);
//         assertEquals(0.0, stats.TP);
//         assertEquals(3.0, stats.FN);
//         assertEquals(3.0, stats.FP);
//         assertEquals(0.0, stats.precisionTotal);
//         assertEquals(0.0, stats.recallTotal);
//         assertEquals(0.0, stats.f1Total);
//         assertEquals(1, stats.reps);
//     }

//     @Test
//     void testUpdateWithPartialOverlap() {
//         Stats stats = new Stats();
//         Set<String> B = new HashSet<>(Arrays.asList("a", "b", "c"));
//         Set<String> A = new HashSet<>(Arrays.asList("b", "c", "d"));

//         stats.update(B, A);

//         assertEquals(3.0, stats.actual);
//         assertEquals(3.0, stats.count);
//         assertEquals(2.0, stats.TP);
//         assertEquals(1.0, stats.FN);
//         assertEquals(1.0, stats.FP);
//         assertEquals(2.0 / 3.0, stats.precisionTotal);
//         assertEquals(2.0 / 3.0, stats.recallTotal);
//         assertEquals(2 * (2.0 / 3.0) * (2.0 / 3.0) / (4.0 / 3.0), stats.f1Total);
//         assertEquals(1, stats.reps);
//     }

//     @Test
//     void testToCSV() {
//         Stats stats = new Stats();

//         stats.update(Set.of("a", "b"), Set.of("b", "c")); // 1 TP, 1 FN, 1 FP
//         stats.update(Set.of("x"), Set.of("x"));           // 1 TP, 0 FN, 0 FP

//         String csv = stats.toCSV();
//         String[] values = csv.split(",");

//         assertEquals(8, values.length);
//         assertDoesNotThrow(() -> Double.parseDouble(values[0])); // average actual
//         assertDoesNotThrow(() -> Double.parseDouble(values[1])); // average count
//         assertDoesNotThrow(() -> Double.parseDouble(values[2])); // avg TP
//         assertDoesNotThrow(() -> Double.parseDouble(values[3])); // avg FN
//         assertDoesNotThrow(() -> Double.parseDouble(values[4])); // avg FP
//         assertDoesNotThrow(() -> Double.parseDouble(values[5])); // avg F1
//         assertDoesNotThrow(() -> Double.parseDouble(values[6])); // avg precision
//         assertDoesNotThrow(() -> Double.parseDouble(values[7])); // avg recall
//     }

//     @Test
//     void testEmptySets() {
//         Stats stats = new Stats();
//         stats.update(Collections.emptySet(), Collections.emptySet());

//         assertEquals(0.0, stats.actual);
//         assertEquals(0.0, stats.count);
//         assertEquals(0.0, stats.TP);
//         assertEquals(0.0, stats.FN);
//         assertEquals(0.0, stats.FP);
//         assertEquals(0.0, stats.precisionTotal);
//         assertEquals(0.0, stats.recallTotal);
//         assertEquals(0.0, stats.f1Total);
//         assertEquals(1, stats.reps);
//     }
// }
