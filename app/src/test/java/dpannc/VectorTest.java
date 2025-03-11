package dpannc;
import dpannc.Vector;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
public class VectorTest {
    @Test
    void testFromString() {
        String str = "label 0.1234 -0.1231 1.2314";
        Vector v = Vector.fromString(str);

        assertEquals("label", v.getLabel());

        double[] expected = {0.1234, -0.1231, 1.2314};
        assertArrayEquals(expected, v.get(), 0.00001);
    }
}
