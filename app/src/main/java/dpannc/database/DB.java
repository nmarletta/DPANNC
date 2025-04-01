package dpannc.database;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

import dpannc.Vector;

public class DB {
    private final String dbUrl;
    private final Connection conn;

    public DB(String dbFilename) throws SQLException, ClassNotFoundException {
        this.dbUrl = "jdbc:sqlite:" + dbFilename;
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS vectors");
            stmt.execute("CREATE TABLE IF NOT EXISTS vectors (label TEXT PRIMARY KEY, data TEXT)");
        }
    }

    public void loadVectorsIntoDB(Path filePath, int n, int d) throws SQLException, IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO vectors(label, data) VALUES (?, ?)")) {

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
            System.out.println("Vectors loaded into DB...");
        }
    }

    public List<Vector> loadVectorsFromDB(int n) throws SQLException {
        List<Vector> vectors = new ArrayList<>();
        String query = "SELECT data FROM vectors LIMIT ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, n);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String data = rs.getString("data");
                vectors.add(Vector.fromString(data));
            }
        }
        return vectors;
    }

    public Vector getVectorByLabel(String label) throws SQLException {
        String query = "SELECT data FROM vectors WHERE label = ?";
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

    public DBiterator iterator() throws SQLException {
        return new DBiterator(conn);
    }

    public Connection getConnection() {
        return conn;
    }
}
