package dpannc.HY;

import org.junit.jupiter.api.Test;

import dpannc.Vector;

import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


class BoxTest {
    private Box box;
    private Collection<Vector> dataset;

    @BeforeEach
    void setUp() {
        dataset = new ArrayList<>();
        dataset.add(new Vector(new double[]{-9.0, 10.0}));
        dataset.add(new Vector(new double[]{9.0, -10.0}));
        dataset.add(new Vector(new double[]{5.0, 5.0}));
        dataset.add(new Vector(new double[]{-5.0, -5.0}));
        dataset.add(new Vector(new double[]{-2.0, -3.0}));
        dataset.add(new Vector(new double[]{4.0, -6.0}));
        dataset.add(new Vector(new double[]{7.0, -2.0}));

        // initialize box with dataset
        box = new Box(2, dataset);
    }

    @Test
    void testConstructorWithValidDataset() {
        assertEquals(2, box.d);
        assertEquals(7, box.count);
        assertNotNull(box.min);
        assertNotNull(box.max);
        assertNotNull(box.data);

        // Check min and max values
        assertArrayEquals(new double[]{-9.0, -10.0}, box.min);
        assertArrayEquals(new double[]{9.0, 10.0}, box.max);
    }

    @Test
    void testConstructorWithEmptyDatasetThrowsException() {
        Collection<Vector> emptyDataset = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> new Box(2, emptyDataset));
    }

    @Test
    void testLongestIndex() {
        int index = box.longestIndex();
        assertEquals(1, index, "The longest side should be along dimension 1");
    }

    @Test
    void testSplit() {
        Box[] split = box.split();

        Box left = split[0];
        Box right = split[1];
        assertEquals(2, split.length);
        assertNotNull(left);
        assertNotNull(right);

        // Ensure data distribution
        assertTrue(left.count + right.count == box.count);

        assertEquals(5, left.count);
        assertEquals(2, right.count);
    }

    @Test
    void testAddPoint() {
        Vector newVector = new Vector(new double[]{10.0, 11.0, 12.0});
        box.addPoint(newVector);

        assertEquals(8, box.count);
        assertTrue(box.data.contains(newVector));
    }

    @Test
    void testClone() {
        Box clone = box.emptyClone();

        assertNotSame(box, clone); // Ensure different object references
        assertArrayEquals(box.min, clone.min);
        assertArrayEquals(box.max, clone.max);
        assertEquals(0, clone.count);
    }

    @Test
    void testEquals() {
        Box sameBox = new Box(2, dataset);
        assertTrue(box.equals(sameBox));

        Box differentBox = new Box(new double[]{0, 0, 0}, new double[]{10, 10, 10});
        assertFalse(box.equals(differentBox));
    }

    @Test
    void testSetMinAndMax() {
        Box modifiedBox = box.emptyClone();
        modifiedBox.setMin(0, 0.5);
        modifiedBox.setMax(0, 7.5);

        assertEquals(0.5, modifiedBox.min[0]);
        assertEquals(7.5, modifiedBox.max[0]);
    }

    @Test
    void testIntersectsBall() {
        Vector q;
        double r;

        // intersection along side
        q = new Vector(new double[] {-10.5, 0});
        r = 2;
        assertTrue(box.intersectsBall(q, r), "wrong indication of intersection along side (false - should be true)");

        // no intersection along side
        q = new Vector(new double[] {-11.2, 0});
        r = 2;
        assertFalse(box.intersectsBall(q, r), "wrong indication of intersection along side (true - should be false)");

        // intersection on corner
        q = new Vector(new double[] {-11, 10});
        r = 2;
        assertTrue(box.intersectsBall(q, r), "wrong indication of intersection on corner (false - should be true)");

        // no intersection on corner
        q = new Vector(new double[] {-12, 1});
        r = 2;
        assertFalse(box.intersectsBall(q, r), "wrong indication of intersection on corner (true - should be false)");

        // ball enclosed in box
        q = new Vector(new double[] {2, 1});
        r = 2;
        assertTrue(box.intersectsBall(q, r), "wrong indication of intersection, ball enclosed in box (false - should be true)");

        // box enclosed in ball
        q = new Vector(new double[] {2, 1});
        r = 30;
        assertTrue(box.intersectsBall(q, r), "wrong indication of intersection, box enclosed in ball (false - should be true)");

    }

    @Test
    void testIsSubsetOfBall() {
        Vector q;
        double r;

        // intersection along side
        q = new Vector(new double[] {-10.5, 0});
        r = 2;
        assertFalse(box.isSubsetOfBall(q, r), "intersects along side - should be false");

        // no intersection
        q = new Vector(new double[] {-11.2, 0});
        r = 2;
        assertFalse(box.isSubsetOfBall(q, r), "no intersection - should be false");

        // intersection on corner
        q = new Vector(new double[] {-11, 10});
        r = 2;
        assertFalse(box.isSubsetOfBall(q, r), "intersects on corner - should be false");

        // ball enclosed in box
        q = new Vector(new double[] {2, 1});
        r = 2;
        assertFalse(box.isSubsetOfBall(q, r), "ball enclosed in box - should be false");

        // box enclosed in ball
        q = new Vector(new double[] {2, 1});
        r = 30;
        assertTrue(box.isSubsetOfBall(q, r), "box enclosed in ball - should be true");

    }
    
}