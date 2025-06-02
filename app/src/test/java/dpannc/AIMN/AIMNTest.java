package dpannc.AIMN;

import dpannc.Vector;
import dpannc.database.DB;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AIMNTest {
    DB db;
    AIMN aimn;
    String vectorTable = "vectors";

    @BeforeEach
    void setup() throws SQLException {
        db = new DB(":memory:", false);

        db.dropTable(vectorTable);
        db.dropTable("nodesTable");

        db.getConnection().createStatement()
                .execute("CREATE TABLE " + vectorTable + " (label TEXT PRIMARY KEY, data TEXT)");
        db.getConnection().createStatement()
                .execute("CREATE TABLE nodesTable (label TEXT PRIMARY KEY, data TEXT)");

        aimn = new AIMN(10, 2, 1.0, 1.0, 2.0, 0.1, 0.01, db);
    }

    @Test
    void testQueryNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> aimn.query(null));
    }

    @Test
    void testQueryWrongDimensionThrows() {
        Vector bad = new Vector(3);
        assertThrows(IllegalArgumentException.class, () -> aimn.query(bad));
    }

    // @Test
    // void testInsertAndQuery() throws Exception {

    //     Vector v1 = new Vector(new double[] {0.1, 0.2}).setLabel("v1");
    //     Vector v2 = new Vector(new double[] {0.5, 0.4}).setLabel("v2");

    //     db.insertRow(v1.getLabel(), v1.dataString(), vectorTable);
    //     db.insertRow(v2.getLabel(), v2.dataString(), vectorTable);

    //     aimn.populateFromDB(vectorTable);

    //     assertEquals(0, aimn.remainder());

    //     Vector q = new Vector(new double[] {0.1, 0.2});
    //     int count = aimn.query(q);
    //     List<String> labels = aimn.queryList();

    //     assertTrue(count > 0);
    //     assertTrue(labels.contains("v1"));
    // }
}
