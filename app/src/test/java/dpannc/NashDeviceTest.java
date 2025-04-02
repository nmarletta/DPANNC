package dpannc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

class NashDeviceTest {
    private int d = 3;
    private int dPrime = 2;
    private double sigma = 1.0;
    private NashDevice device;
    private Random random = new Random(100);

    @BeforeEach
    void setUp() {
        device = new NashDevice(d, dPrime, sigma, random);
    }

    @Test
    void testRandomGaussianVectorsAreNormalized() {
        for (Vector g : device.randomGaussians) {
            double norm = Math.sqrt(g.dot(g));
            assertTrue(Math.abs(norm - 1.0) < 1e-6, "Gaussian vector should be normalized");
        }
    }

    @Test
    void testTransformOutputDimension() {
        Vector input = new Vector(d).randomGaussian(random).normalize();
        Vector output = device.transform(input);
        assertEquals(dPrime, output.dimensionality(), "Transformed vector should have dimension dPrime");
    }

    @Test
    void testTransformPreservesLabel() {
        Vector input = new Vector(d).randomGaussian(random).normalize().setLabel("word");
        Vector output = device.transform(input);
        assertEquals("word", output.getLabel(), "Transformed vector should preserve the label");
    }

    @Test
    void testTransformReasonableMagnitude() {
        Vector input = new Vector(d).randomGaussian(random).normalize();
        Vector output = device.transform(input);

        double magnitude = Math.sqrt(output.dot(output));
        double expectedMagnitude = Math.sqrt(dPrime) / sigma;

        // Loose check, since it can vary due to cosine/sine
        assertTrue(magnitude > 0.5 * expectedMagnitude && magnitude < 1.5 * expectedMagnitude,
                   "Output magnitude should be within reasonable range");
    }

    @Test
    void testTransformIsStable() {
        Vector input = new Vector(new double[] {2.0, 3.0, 4.0});
        Vector output = device.transform(input);

        assertNotNull(output, "Transformed vector should not be null");
        assertEquals(dPrime, output.dimensionality(), "Transformed vector should have correct size");
    }
}

