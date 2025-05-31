package dpannc.EXP;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {
    private Result result;

    @BeforeEach
    public void setUp() {
        result = new Result();
        result.add("A", 1.0);
        result.add("B", 2.0);
        result.add("C", 3.0);
        result.add("D", 4.0);
        result.add("E", 5.0);
    }

    @Test
    public void testAddAndSize() {
        assertEquals(5, result.size());
    }

    @Test
    public void testAmountLessThan() {
        assertEquals(2, result.amountLessThan(2.5));
        assertThrows(IllegalArgumentException.class, () -> result.amountLessThan(0));
    }

    @Test
    public void testLessThan() {
        Set<String> expected = new HashSet<>(Arrays.asList("A", "B"));
        assertEquals(expected, result.lessThan(2.5));
    }

    @Test
    public void testAmountWithin() {
        assertEquals(2, result.amountWithin(2.0, 4.0));
        assertThrows(IllegalArgumentException.class, () -> result.amountWithin(0, 2));
        assertThrows(IllegalArgumentException.class, () -> result.amountWithin(3, 2));
    }

    @Test
    public void testWithin() {
        Set<String> expected = new HashSet<>(Arrays.asList("C", "D"));
        assertEquals(expected, result.within(2.0, 4.5));
    }

    @Test
    public void testAmountGreaterThan() {
        assertEquals(3, result.amountGreaterThan(2.0));
        assertThrows(IllegalArgumentException.class, () -> result.amountGreaterThan(0));
    }

    @Test
    public void testGreaterThan() {
        Set<String> expected = new HashSet<>(Arrays.asList("C", "D", "E"));
        assertEquals(expected, result.greaterThan(2.0));
    }

    @Test
    public void testAll() {
        Set<String> expected = new HashSet<>(Arrays.asList("A", "B", "C", "D", "E"));
        assertEquals(expected, result.all());
    }

    @Test
    public void testDiffBetween() throws Exception {
        Result other = new Result();
        other.add("A", 0.5);
        other.add("B", 1.5);
        other.add("C", 2.5);
        other.add("D", 3.5);
        other.add("E", 4.5);

        Result diff = result.diffBetween(other);
        assertEquals(0.5, diff.get(0).value);
        assertEquals(0.5, diff.get(1).value);
    }

    @Test
    public void testChangeBetween() throws Exception {
        Result other = new Result();
        other.add("A", 0.5);
        other.add("B", 1.0);
        other.add("C", 1.5);
        other.add("D", 2.0);
        other.add("E", 2.5);

        Result change = result.changeBetween(other);
        assertEquals(0.5, change.get(0).value);
        assertEquals(0.5, change.get(1).value);
    }

    @Test
    public void testMean() {
        assertEquals(3.0, result.mean());
    }

    @Test
    public void testMedian() {
        assertEquals(3.0, result.median());
    }

    @Test
    public void testStdDev() {
        double stddev = result.stddev();
        assertEquals(1.4142, stddev, 0.0001);
    }

    @Test
    public void testMAD() {
        double mad = result.mad();
        assertEquals(1.0, mad);
    }

    @Test
    public void testPercentile() {
        assertEquals(1.0, result.percentile(0.0));
        assertEquals(3.0, result.percentile(0.5));
        assertEquals(5.0, result.percentile(1.0));
        assertThrows(IllegalArgumentException.class, () -> result.percentile(-0.1));
    }

    @Test
    public void testDistanceToKNearest() {
        assertEquals(2.0, result.distanceToKNearest(2));
        assertThrows(IllegalArgumentException.class, () -> result.distanceToKNearest(0));
        assertThrows(IllegalArgumentException.class, () -> result.distanceToKNearest(10));
    }

    @Test
    public void testClone() {
        Result clone = result.clone();
        assertEquals(result.size(), clone.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals(result.get(i).label, clone.get(i).label);
            assertEquals(result.get(i).value, clone.get(i).value);
        }
    }
}

