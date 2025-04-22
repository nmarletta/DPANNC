package dpannc.HY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import dpannc.Vector;
import dpannc.database.DB;

import static org.junit.jupiter.api.Assertions.*;

class HYTest {
    DB db;
    private HY hy;
    private static final double E = 1e-9;

    @BeforeEach
    void setUp() {
        db = new DB(":memory:", false);
        hy = new HY(1.0, 0.05, 0.01, db);
    }

    @Test
    void testPopulateWithValidData(@TempDir Path tempDir) throws Exception {
        // Create a temporary data file
        File tempFile = tempDir.resolve("test_data.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("a 1.0 2.0 3.0\n");
            writer.write("b 4.0 5.0 6.0\n");
            writer.write("c 7.0 8.0 9.0\n");
        }

        // Populate HY with dataset
        hy.populateFromFile(3, 3, tempFile.toPath());

        // Ensure structure is initialized
        assertNotNull(hy, "HY instance should be initialized");
    }

    @Test
    void testLoadFile(@TempDir Path tempDir) throws Exception {
        File tempFile = tempDir.resolve("test_data.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("a 1.0 2.0 3.0\n");
            writer.write("b 4.0 5.0 6.0\n");
            writer.write("c 7.0 8.0 9.0\n");
        }

        Collection<Vector> loadedData = hy.loadfile(3, tempFile.toPath());
        assertEquals(3, loadedData.size(), "Should load exactly 3 vectors");

        // Check contents of loaded vectors
        Vector first = loadedData.iterator().next();
        assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, first.get(), E);
    }

    @Test
    void testShrink() {
        Collection<Vector> dataset = new ArrayList<>();
        dataset.add(new Vector(new double[] { -9.0, 10.0 }));
        dataset.add(new Vector(new double[] { 9.0, -10.0 }));
        dataset.add(new Vector(new double[] { 5.0, 5.0 }));
        dataset.add(new Vector(new double[] { -5.0, -5.0 }));
        dataset.add(new Vector(new double[] { -2.0, -3.0 }));
        dataset.add(new Vector(new double[] { 4.0, -6.0 }));
        dataset.add(new Vector(new double[] { 7.0, -2.0 }));

        Box rootBox = new Box(2, dataset);
        HY.Cell root = new HY(1.0, 0.00005, 0.01, db).new Cell(rootBox);

        assertNotNull(root, "root cell is null");
        assertEquals(7, root.count, "initial count not correct");
        // Box left = root.outer;
        Box[] shrinkResult = root.shrink(0, 0.01);
        assertNotNull(shrinkResult[0], "outer should not be null");
        assertNotNull(shrinkResult[1], "inner should not be null");

        Box outer = shrinkResult[0];
        Box inner = shrinkResult[1];
        assertEquals(4, outer.count);
        assertEquals(3, inner.count);

    }
}
