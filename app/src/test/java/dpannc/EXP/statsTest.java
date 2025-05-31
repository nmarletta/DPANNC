package dpannc.EXP;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class StatsTest {

    private Stats stats;
    private Result data;

    @BeforeEach
    public void setUp() {
        stats = new Stats();

        data = new Result();
        data.add("A", 1.0);
        data.add("B", 2.0);
        data.add("C", 3.0);
        data.add("D", 4.0);
        data.add("E", 5.0);
    }

    @Test
    public void testUpdate_AllCorrectlyCategorized() {
        double r = 2.0;
        double cr = 4.0;

        // Query contains: one inside r, two inside fuzzy zone, one outside
        Set<String> query = new HashSet<>(Arrays.asList("A", "B", "C", "E"));

        stats.update(data, query, r, cr);

        // Explanation:
        // lessThan(r): [A, B]  => includedInner=1, missedInner=0
        // within(r, cr): [C, D] => includedFuzzy=2, missedFuzzy=0
        // greaterThan(cr): [E] => includedOuter=1 (E), others not in query
        assertEquals(0.0, stats.missedInner);
        assertEquals(2.0, stats.includedInner);
        assertEquals(1.0, stats.missedFuzzy);
        assertEquals(1.0, stats.includedFuzzy);
        assertEquals(1.0, stats.includedOuter);
        assertEquals(4.0, stats.total);
        assertEquals(1, stats.queries);
    }

    @Test
    public void testUpdate_AllMissed() {
        double r = 2.0;
        double cr = 4.0;

        // Query contains no elements at all
        Set<String> query = new HashSet<>();

        stats.update(data, query, r, cr);

        // lessThan(r): [A, B] => missedInner=1
        // within(r, cr): [C, D] => missedFuzzy=2
        // greaterThan(cr): [E] => includedOuter=0 (since query empty)
        assertEquals(2.0, stats.missedInner);
        assertEquals(0.0, stats.includedInner);
        assertEquals(2.0, stats.missedFuzzy);
        assertEquals(0.0, stats.includedFuzzy);
        assertEquals(0.0, stats.includedOuter);
        assertEquals(0.0, stats.total);
        assertEquals(1, stats.queries);
    }

    @Test
    public void testUpdate_AllIncluded() {
        double r = 2.0;
        double cr = 4.0;

        // Query includes everything
        Set<String> query = new HashSet<>(Arrays.asList("A", "B", "C", "D", "E"));

        stats.update(data, query, r, cr);

        assertEquals(0.0, stats.missedInner);
        assertEquals(2.0, stats.includedInner);
        assertEquals(0.0, stats.missedFuzzy);
        assertEquals(2.0, stats.includedFuzzy);
        assertEquals(1.0, stats.includedOuter);
        assertEquals(5.0, stats.total);
        assertEquals(1, stats.queries);
    }

    @Test
    public void testReset() {
        double r = 2.0;
        double cr = 4.0;
        Set<String> query = new HashSet<>(Arrays.asList("A", "B", "C"));

        stats.update(data, query, r, cr);
        stats.reset();

        assertEquals(0.0, stats.missedInner);
        assertEquals(0.0, stats.includedInner);
        assertEquals(0.0, stats.missedFuzzy);
        assertEquals(0.0, stats.includedFuzzy);
        assertEquals(0.0, stats.includedOuter);
        assertEquals(0.0, stats.total);
        assertEquals(0, stats.queries);
    }

    @Test
    public void testStatsString() {
        double r = 2.0;
        double cr = 4.0;
        Set<String> query = new HashSet<>(Arrays.asList("A", "B", "C"));

        stats.update(data, query, r, cr);
        String statsStr = stats.stats();
        assertNotNull(statsStr);
    }

    @Test
    public void testPrString() {
        double r = 2.0;
        double cr = 4.0;
        Set<String> query = new HashSet<>(Arrays.asList("A", "B", "C"));

        stats.update(data, query, r, cr);
        String prStr = stats.pr();
        assertNotNull(prStr);
    }
}
