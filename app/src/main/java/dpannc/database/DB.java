package dpannc.database;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

import dpannc.NashDevice;
import dpannc.Vector;
import dpannc.EXP.Result;

public class DB {
    private final String dbUrl;
    private final Connection conn;

    public DB(String dbFilename) throws SQLException, ClassNotFoundException {
        this.dbUrl = "jdbc:sqlite:" + dbFilename + ".db";
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection(dbUrl);

        // drop all existing tables
        // try (Statement stmt = conn.createStatement();
        //         ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
        //     while (rs.next()) {
        //         String tableName = rs.getString("name");
        //         if (!tableName.equals("sqlite_sequence")) {
        //             stmt.execute("DROP TABLE IF EXISTS " + tableName);
        //         }
        //     }
        // }
    }

    public void loadVectorsIntoDB(String table, Path filePath, int n, int d) throws SQLException, IOException {
        try (Statement createStmt = conn.createStatement()) {
            String dropTableSQL = "DROP TABLE IF EXISTS " + table;
            String createTableSQL = "CREATE TABLE " + table + " (label TEXT PRIMARY KEY, data TEXT)";
            createStmt.execute(dropTableSQL);
            createStmt.execute(createTableSQL);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
                PreparedStatement stmt = conn
                        .prepareStatement("INSERT INTO " + table + "(label, data) VALUES (?, ?)")) {

            String line;
            int counter = 0;
            while ((line = reader.readLine()) != null && counter < n) {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length != d + 1) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                String label = tokens[0];
                
                stmt.setString(1, label);
                stmt.setString(2, line);
                stmt.addBatch();
                counter++;
            }
            stmt.executeBatch();
            System.out.println("Vectors loaded into DB table '" + table + "'");
        }
    }

    public void loadResultIntoDB(String table, Result result) throws SQLException, IOException {
        try (Statement createStmt = conn.createStatement()) {
            String dropTableSQL = "DROP TABLE IF EXISTS " + table;
            String createTableSQL = "CREATE TABLE " + table + " (label TEXT PRIMARY KEY, data TEXT)";
            createStmt.execute(dropTableSQL);
            createStmt.execute(createTableSQL);
        }

        try (PreparedStatement stmt = conn
                        .prepareStatement("INSERT INTO " + table + "(label, data) VALUES (?, ?)")) {

            for (Result.Element e : result.get()) {
                String label = e.label;
                stmt.setString(1, label);
                stmt.setString(2, "" + e.value);
                stmt.addBatch();
            }
            stmt.executeBatch();
            System.out.println("Result loaded into DB table '" + table + "'");
        }
    }

    public List<Vector> loadVectorsFromDB(String table) throws SQLException {
        List<Vector> vectors = new ArrayList<>();
        String query = "SELECT data FROM " + table;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String data = rs.getString("data");
                vectors.add(Vector.fromString(data));
            }
        }
        return vectors;
    }

    public Vector getVectorByLabel(String label, String table) throws SQLException {
        String query = "SELECT data FROM " + table + " WHERE label = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, label);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String data = rs.getString("data");
                return Vector.fromString(data);
            } else {
                return null;
            }
        }
    }

    public Vector getRandomVector(String table, Random random) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM " + table;
        int rowCount = 0;
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next()) {
                rowCount = rs.getInt(1);
            }
        }

        if (rowCount == 0)
            return null;

        int offset = random.nextInt(rowCount);
        String query = "SELECT data FROM " + table + " LIMIT 1 OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, offset);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Vector.fromString(rs.getString("data"));
            } else {
                System.out.println("null");
                return null;
            }
        }
    }

    public void applyNashTransform(NashDevice nd, String table) throws SQLException {
        String selectQuery = "SELECT label, data FROM " + table;
        String updateQuery = "UPDATE " + table + " SET data = ? WHERE label = ?";

        try (
                PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                ResultSet rs = selectStmt.executeQuery()) {
            int counter = 0;
            while (rs.next()) {
                String label = rs.getString("label");
                String data = rs.getString("data");
                Vector original = Vector.fromString(data);

                Vector transformed = nd.transform(original);

                // update the database
                updateStmt.setString(1, transformed.toString());
                updateStmt.setString(2, label);
                updateStmt.addBatch();

                counter++;
                if (counter % 100 == 0) {
                    updateStmt.executeBatch();
                }
            }
            updateStmt.executeBatch();
            System.out.println("Applied Nash transform to " + counter + " vectors in table '" + table + "'");
        }
    }

    public void dropTable(String table) throws SQLException {
        try (Statement stmt = conn.createStatement();) {
            stmt.execute("DROP TABLE IF EXISTS " + table);
        }
    }

    public DBiterator iterator(String table) throws SQLException {
        return new DBiterator(conn, table);
    }

    public Connection getConnection() {
        return conn;
    }
}
