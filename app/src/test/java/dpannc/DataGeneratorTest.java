// package dpannc;

// import dpannc.Vector;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.io.TempDir;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// class DataGeneratorTest {

//     @Test
//     void testGenerateVectorAtDistance() {
//         Vector v = new Vector(new float[] {1.0f, 2.0f, 3.0f});
//         float distance = 5.0f;
//         Vector generated = DataGenerator.generateVectorAtDistance(v, distance);

//         assertEquals(distance, generated.distance(v), 0.01, "Generated vector should be at the expected distance");
//     }

//     @Test
//     void testGenerateVectorAtDistanceWithZeroDistance() {
//         Vector v = new Vector(new float[] {1.0f, 2.0f, 3.0f});

//         Vector generated = DataGenerator.generateVectorAtDistance(v, 0.0f);

//         assertEquals(0.0f, generated.distance(v), 0.01, "Generated vector should be identical when distance is zero");
//         assertNotSame(v, generated, "Generated vector should be a different instance");
//     }

//     @Test
//     void testGenerateVectorAtDistanceWithNegativeDistance() {
//         Vector v = new Vector(3);
//         Exception exception = assertThrows(IllegalArgumentException.class, () ->
//                 DataGenerator.generateVectorAtDistance(v, -5.0f)
//         );
//         assertTrue(exception.getMessage().contains("distance must be non-negative"));
//     }

//     @Test
//     void testGenerateNormalisedVectorAtDistance() {
//         Vector v = new Vector(new float[] {1.0f, 2.0f, 3.0f}).normalize();
//         float distance = 1.2f;
//         Vector generated = DataGenerator.selectPointOnSphere(v, distance);
//         System.out.println(generated.toString());
//         assertEquals(distance, generated.distance(v), 0.1, "Generated vector should be at the expected distance");
//         assertEquals(1.0, generated.magnitude(), 0.01, "Generated vector should be at the expected distance");
//     }

//     @Test
//     void testGenerateFile(@TempDir Path tempDir) throws Exception {
//         Path testFile = tempDir.resolve("test_vectors.txt");
//         Vector v = new Vector(new float[] {1.0f, 2.0f, 3.0f});

//         int amount = 10;
//         float[] distances = {1.0f, 2.0f, 3.0f};

//         int writtenCount = DataGenerator.generateFile(testFile, v, amount, distances);

//         assertEquals(amount, writtenCount, "Number of written vectors should match the requested amount");

//         List<String> lines = Files.readAllLines(testFile);
//         assertEquals(amount, lines.size(), "File should contain the correct number of lines");

//         for (String line : lines) {
//             assertTrue(line.matches("^\\[.*\\]:\\d+:\\d+\\.\\d+$"), "Each line should have correct format");
//         }
//     }
// }
