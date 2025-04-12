package dpannc.database;

import dpannc.Vector;

import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DBTest {
    private static final String DB_NAME = "testdb";
    private DB db;

    @BeforeEach
    void setUp() {
        db = new DB(DB_NAME);
    }

    @AfterEach
    void tearDown() throws SQLException {
        db.dropTable("vectors");
        db.dropTable("results");
    }

    @Test
    void testLoadAndGetVector() throws Exception {
        String content = """
                v1 1.0 2.0 3.0
                v2 4.0 5.0 6.0
                """;
        Path tmpFile = Files.createTempFile("vectors", ".txt");
        Files.writeString(tmpFile, content);

        db.loadVectorsIntoDB("vectors", tmpFile, 10, 3);

        Vector v1 = db.getVectorByLabel("v1", "vectors");
        assertNotNull(v1);
        assertEquals("v1", v1.getLabel());
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, v1.get());

        Vector v2 = db.getVectorByLabel("v2", "vectors");
        assertNotNull(v2);

        Files.deleteIfExists(tmpFile);
    }

    @Test
    void testLoadAndRetrieveAllVectors() throws Exception {
        String content = """
                v1 1.0 1.0 1.0
                v2 2.0 2.0 2.0
                """;
        Path tmpFile = Files.createTempFile("vectors", ".txt");
        Files.writeString(tmpFile, content);

        db.loadVectorsIntoDB("vectors", tmpFile, 10, 3);

        List<Vector> vectors = db.loadVectorsFromDB("vectors");
        assertEquals(2, vectors.size());

        Files.deleteIfExists(tmpFile);
    }

    @Test
    void testRandomVector() throws Exception {
        String content = """
                a 1.0 2.0 3.0
                b 4.0 5.0 6.0
                """;
        Path tmpFile = Files.createTempFile("vectors", ".txt");
        Files.writeString(tmpFile, content);

        db.loadVectorsIntoDB("vectors", tmpFile, 10, 3);

        Random random = new Random(42);
        Vector v = db.getRandomVector("vectors", random);
        assertNotNull(v);

        Files.deleteIfExists(tmpFile);
    }

    @Test
    void testApplyTransformation() throws Exception {
        String content = """
                v1 1.0 2.0
                """;
        Path tmpFile = Files.createTempFile("vectors", ".txt");
        Files.writeString(tmpFile, content);

        db.loadVectorsIntoDB("vectors", tmpFile, 1, 2);

        db.applyTransformation(data -> {
            String transformed = Arrays.stream(data.split(" "))
                        .mapToDouble(Double::parseDouble)
                        .map(v -> v / 2)
                        .mapToObj(Double::toString)
                        .collect(Collectors.joining(" "));
                return transformed;
        }, "vectors");
        
        Vector transformed = db.getVectorByLabel("v1", "vectors");
        assertArrayEquals(new double[]{0.5, 1.0}, transformed.get());
        assertEquals(2, transformed.dimensionality());

        Files.deleteIfExists(tmpFile);
    }
}