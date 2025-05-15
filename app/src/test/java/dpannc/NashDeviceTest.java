package dpannc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dpannc.AIMN.NashDevice;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

class NashDeviceTest {
    private int d = 3;
    private int dPrime = 2;

    private NashDevice device;
    private Random random = new Random(100);

    @BeforeEach
    void setUp() {
        device = new NashDevice(d, dPrime, random);
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
        double expectedMagnitude = 1.0;

        // Loose check, since it can vary due to cosine/sine
        assertTrue(magnitude > 0.99 * expectedMagnitude && magnitude < 1.01 * expectedMagnitude,
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

