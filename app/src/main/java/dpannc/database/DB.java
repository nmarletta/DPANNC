package dpannc.database;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

import dpannc.Progress;
import dpannc.Vector;

public class DB {
    private String dbUrl;
    private Connection conn;

    public DB(String dbFilename, boolean deleteOnExit) {
        this.dbUrl = "jdbc:sqlite:" + dbFilename + ".db";

        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(dbUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to SQLite DB at " + dbUrl, e);
        }

        if (deleteOnExit) {
            new File(dbFilename + ".db").deleteOnExit();
        }
    }

    public void initTable(String table) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String drop = "DROP TABLE IF EXISTS " + table;
            String create = "CREATE TABLE " + table + " (label TEXT PRIMARY KEY, data TEXT)";

            stmt.execute(drop);
            stmt.execute(create);
        }
    }

    public void loadVectorsIntoDB(String table, Path filePath, int n, int d) throws Exception {
        try (Statement createStmt = conn.createStatement()) {
            Progress.newStatusBar("Initializing table: " + table, 1);
            String dropTable = "DROP TABLE IF EXISTS " + table;
            String createTable = "CREATE TABLE " + table + " (label TEXT PRIMARY KEY, data TEXT)";
            createStmt.execute(dropTable);
            Progress.updateStatusBar(1);
            createStmt.execute(createTable);
            Progress.clearStatus();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
                PreparedStatement stmt = conn
                        .prepareStatement("INSERT INTO " + table + "(label, data) VALUES (?, ?)")) {

            Progress.newStatusBar("Loading vectors into DB table: " + table, n);

            String line;
            int counter = 0;
            int batchSize = n / 100;

            while ((line = reader.readLine()) != null && counter < n) {
                String label = line.substring(0, line.indexOf(' '));
                String data = line.substring(line.indexOf(' ') + 1);

                String[] tokens = data.split(" ");
                if (tokens.length != d) {
                    throw new Exception("dimensions in data file does not match the specified d=" + d);
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tokens.length; i++) {
                    double value = Double.parseDouble(tokens[i]);
                    sb.append(value);
                    if (i != tokens.length - 1)
                        sb.append(" ");
                }

                stmt.setString(1, label);
                stmt.setString(2, sb.toString());
                stmt.addBatch();
                counter++;
                Progress.updateStatusBar(counter);

                if (n > 10 && counter % batchSize == 0) {
                    stmt.executeBatch();
                }
            }

            stmt.executeBatch(); // execute any remaining
            Progress.clearStatus();
        }
    }

    public void copyTable(String sourceTable, String targetTable) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            Progress.newStatusBar("Copying table " + sourceTable + " → " + targetTable, 3);
    
            // Drop target table if it already exists
            stmt.execute("DROP TABLE IF EXISTS " + targetTable);
            Progress.updateStatusBar(1);
    
            // Create the target table with the same schema
            stmt.execute("CREATE TABLE " + targetTable + " (label TEXT PRIMARY KEY, data TEXT)");
            Progress.updateStatusBar(2);
    
            // Copy all data from source table
            stmt.execute("INSERT INTO " + targetTable + " (label, data) SELECT label, data FROM " + sourceTable);
            Progress.updateStatusBar(3);
    
            Progress.clearStatus();
        }
    }

    public void insertRow(String label, String data, String table) throws SQLException {
        try (PreparedStatement stmt = conn
                .prepareStatement("INSERT OR IGNORE INTO " + table + " (label, data) VALUES (?, ?)")) {
            stmt.setString(1, label);
            stmt.setString(2, data);
            stmt.executeUpdate();
        }
    }

    public void insertVector(Vector v, String table) throws SQLException {
        try (PreparedStatement stmt = conn
                .prepareStatement("INSERT OR IGNORE INTO " + table + " (label, data) VALUES (?, ?)")) {
            stmt.setString(1, v.getLabel());
            stmt.setString(2, v.dataString());
            stmt.executeUpdate();
        }
    }

    public List<Vector> loadVectorsFromDB(String table) throws SQLException {
        List<Vector> vectors = new ArrayList<>();
        String query = "SELECT label, data FROM " + table;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String label = rs.getString("label");
                String data = rs.getString("data");
                vectors.add(Vector.fromString(label, data));
            }
        }
        return vectors;
    }

    public Vector getVectorByLabel(String label, String table) throws Exception {
        String query = "SELECT data FROM " + table + " WHERE label = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, label);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String data = rs.getString("data");
                return Vector.fromString(label, data);
            } else {
                throw new Exception(label + " not found in table " + table);
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
            throw new IllegalStateException("No vectors found in table: " + table);

        int offset = random.nextInt(rowCount);
        String query = "SELECT label, data FROM " + table + " LIMIT 1 OFFSET ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, offset);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Vector.fromString(rs.getString("label"), rs.getString("data"));
            } else {
                return null;
            }
        }
    }

    public List<String> getColumnWhereEquals(String targetColumn, String value, String table, String resultColumn)
            throws SQLException {
        List<String> results = new ArrayList<>();

        String sql = "SELECT " + resultColumn + " FROM " + table + " WHERE " + targetColumn + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(rs.getString(resultColumn));
            }
        }
        return results;
    }

    public boolean elementExists(String label, String table) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE label = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, label);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public void applyTransformation(Function<String, String> transformer, String table) throws Exception {
        String selectQuery = "SELECT label, data FROM " + table;
        String updateQuery = "UPDATE " + table + " SET data = ? WHERE label = ?";

        int totalRows = 0;
        try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM " + table);
                ResultSet countRs = countStmt.executeQuery()) {
            if (countRs.next()) {
                totalRows = countRs.getInt(1);
            }
        }

        try (
                PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                ResultSet rs = selectStmt.executeQuery()) {
            Progress.newStatusBar("Transforming data in table: " + table, totalRows);
            int counter = 0;
            while (rs.next()) {
                String label = rs.getString("label");
                String data = rs.getString("data");
                String transformed = transformer.apply(data);
                updateStmt.setString(1, transformed);
                updateStmt.setString(2, label);
                updateStmt.addBatch();

                counter++;
                if (counter % 100 == 0) {
                    updateStmt.executeBatch();
                }
                Progress.updateStatusBar(counter);
            }
            updateStmt.executeBatch();
            Progress.clearStatus();
        }
    }

    public void dropTable(String table) throws SQLException {
        try (Statement stmt = conn.createStatement();) {
            stmt.execute("DROP TABLE IF EXISTS " + table);
        }
    }

    public int tableSize(String table) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM " + table;
        try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        }
    }

    public void deleteDB() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            String filename = dbUrl.replace("jdbc:sqlite:", "");
            File file = new File(filename);
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("Deleted DB file: " + file.getAbsolutePath());
                } else {
                    System.out.println("Failed to delete DB file: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DBiterator iterator(String table) throws SQLException {
        return new DBiterator(conn, table);
    }

    public Connection getConnection() {
        return conn;
    }
}