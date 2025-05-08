// package dpannc.AIMN;

// import dpannc.Vector;
// import dpannc.database.DB;
// import org.junit.jupiter.api.*;

// import java.sql.SQLException;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// class AIMNTest {
//     DB db;
//     AIMN aimn;
//     String vectorTable = "vectors";

//     @BeforeEach
//     void setup() throws SQLException {
//         // In-memory SQLite DB for testing
//         db = new DB(":memory:", false);

//         // Create tables
//         db.dropTable(vectorTable);
//         db.dropTable("gaussianTable");
//         db.dropTable("nodesTable");

//         db.getConnection().createStatement()
//                 .execute("CREATE TABLE " + vectorTable + " (label TEXT PRIMARY KEY, data TEXT)");
//         db.getConnection().createStatement().execute("CREATE TABLE gaussianTable (label TEXT PRIMARY KEY, data TEXT)");
//         db.getConnection().createStatement().execute("CREATE TABLE nodesTable (label TEXT PRIMARY KEY, data TEXT)");

//         // Init AIMN
//         aimn = new AIMN(10, 2, 1.2, 1.0, 2.0, 0.001, db);
//     }

//     // @Test
//     // void testInsertAndQuery() throws Exception {
//     //     Vector v1 = new Vector(new double[] {0.1, 0.2}).setLabel("v1");
//     //     Vector v2 = new Vector(new double[] {0.5, 0.4}).setLabel("v2");

//     //     db.insertRow(v1.getLabel(), v1.dataString(), vectorTable);
//     //     db.insertRow(v2.getLabel(), v2.dataString(), vectorTable);

//     //     aimn.populateFromDB(vectorTable, db);

//     //     assertEquals(0, aimn.remainder()); // All vectors inserted properly

//     //     // Query a point close to v1
//     //     Vector q = new Vector(new double[] {0.1, 0.2});
//     //     int count = aimn.query(q);
//     //     List<String> labels = aimn.queryList();

//     //     assertTrue(count > 0);
//     //     assertTrue(labels.contains("v1"));
//     // }

//     @Test
//     void testQueryNullThrows() {
//         assertThrows(IllegalArgumentException.class, () -> aimn.query(null));
//     }

//     @Test
//     void testQueryWrongDimensionThrows() {
//         Vector bad = new Vector(3);
//         assertThrows(IllegalArgumentException.class, () -> aimn.query(bad));
//     }
// }
