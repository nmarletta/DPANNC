package dpannc.HY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import dpannc.Vector;

import static org.junit.jupiter.api.Assertions.*;

class HYTest {
    private HY hy;
    private static final double EPSILON = 1e-9;

    @BeforeEach
    void setUp() {
        hy = new HY(1.0, 0.05, 0.01); // Example sensitivity, epsilon, and delta
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
        hy.populate(3, 3, tempFile.toPath());

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
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, first.get(), EPSILON);
    }

    @Test
    void testPrintTree(@TempDir Path tempDir) throws Exception {
        File tempFile = tempDir.resolve("test_data.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("a 1.0 2.0 3.0\n");
            writer.write("b 4.0 5.0 6.0\n");
            writer.write("c 7.0 8.0 9.0\n");
        }

        hy.populate(3, 3, tempFile.toPath());

        // Redirect System.out to capture output
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        hy.print(); // Calls printTree()

        String output = out.toString().trim();
        assertFalse(output.isEmpty(), "printTree should produce output");
    }

    @Test
    void testShrink() {
        hy.setNoiseCtrl(0);
        Collection<Vector> dataset = new ArrayList<>();
        dataset.add(new Vector(new double[]{-9.0, 10.0}));
        dataset.add(new Vector(new double[]{9.0, -10.0}));
        dataset.add(new Vector(new double[]{5.0, 5.0}));
        dataset.add(new Vector(new double[]{-5.0, -5.0}));
        dataset.add(new Vector(new double[]{-2.0, -3.0}));
        dataset.add(new Vector(new double[]{4.0, -6.0}));
        dataset.add(new Vector(new double[]{7.0, -2.0}));

        Box rootBox = new Box(2, dataset);
        HY.Cell root = new HY(1.0, 0.00005, 0.01).new Cell(rootBox);

        assertNotNull(root, "root cell is null");
        assertEquals(7, root.count, "initial count not correct");
        // Box left = root.outer;
        Box shrunkBox = root.shrink(0, 0.01);
        // assertEquals(4, left.count);
        assertEquals(3, shrunkBox.count);

        assertNotNull(shrunkBox, "Right child should exist after shrinking");
    }
}

