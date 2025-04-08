package dpannc;
import dpannc.Vector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
public class VectorTest {
    @Test
    void testFromString() {
        String str = "label 0.1234 -0.1231 1.2314";
        Vector v = Vector.fromString(str);

        assertEquals("label", v.getLabel());

        double[] expected = {0.1234, -0.1231, 1.2314};
        assertArrayEquals(expected, v.get(), 0.00001);
    }

    @Test
    void testSampleOnSphere() {
        Random rnd = new Random();
        
        double mag = 3.0;
        double dist = 1.2;
        Vector v = new Vector(10).randomGaussian(rnd).setMagnitude(mag);
        Vector w = v.sampleOnSphere(dist, rnd);
        assertEquals(dist, v.distance(w), 0.0001);
        assertEquals(mag, w.magnitude(), 0.0001);
    }
    @ParameterizedTest
    @CsvSource({
        "1.0, 1.2",
        "2.0, 0.5",
        "3.5, 1.0",
        "5.0, 2.5"
    })
    void testSampleOnSphere(double mag, double dist) {
        Random rnd = new Random(42); // Seed for reproducibility

        Vector v = new Vector(10).randomGaussian(rnd).setMagnitude(mag);
        Vector w = v.sampleOnSphere(dist, rnd);

        assertEquals(dist, v.distance(w), 0.0001, "Distance from original vector should match dist");
        assertEquals(mag, w.magnitude(), 0.0001, "Magnitude of sampled vector should match original magnitude");
    }

    @Test
    void testSampleInSpace() {
        Random rnd = new Random();
        
        double mag = 3.0;
        double dist = 1.2;
        Vector v = new Vector(10).randomGaussian(rnd).setMagnitude(mag);
        Vector w = v.sampleWithDistance(dist, rnd);
        assertEquals(dist, v.distance(w), 0.0001);
    }

    

    @ParameterizedTest
    @CsvSource({
        "1.0, 1.2",
        "2.0, 0.5",
        "3.5, 1.0",
        "5.0, 2.5"
    })
    void testSampleInSpace(double mag, double dist) {
        Random rnd = new Random(42); // Seed for reproducibility

        Vector v = new Vector(10).randomGaussian(rnd).setMagnitude(mag);
        Vector w = v.sampleWithDistance(dist, rnd);

        assertEquals(dist, v.distance(w), 0.0001, "Distance from original vector should match dist");
    }
}



