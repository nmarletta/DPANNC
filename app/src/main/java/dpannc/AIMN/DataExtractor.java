package dpannc.AIMN;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dpannc.database.DB;
import dpannc.Vector;

public class DataExtractor {

    /**
     * Retrieve a list of the vectors that assigned to a particular node.
     *
     * @param nodeID as string.
     * @return list of vectors.
     */
    public static List<Vector> getVectorsForNode(String nodeId) {
        List<Vector> vectors = new ArrayList<>();
        try {
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT vector FROM vectors WHERE nodeID = ?");
            stmt.setString(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                vectors.add(Vector.fromString(rs.getString("vector")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vectors;
    }

    /**
     * Retrieve a list of the vectors that are returned by a query.
     *
     * @param query point as Vector.
     * @return list of vectors.
     */
    public static List<Vector> getQuery(Vector q) {
        List<Vector> vectors = new ArrayList<>();
        try {
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT v.vector FROM vectors v " +
                            "JOIN queries q ON v.nodeID = q.nodeID " +
                            "WHERE q.label = ?");
            stmt.setString(1, q.getLabel());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                vectors.add(Vector.fromString(rs.getString("vector")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // System.out.println("query: " + vectors.size());
        return vectors;
    }

    /**
     * Retrieve a list of all vectors in the data structure.
     *
     * @return list of vectors.
     */
    public static List<Vector> getAllVectors() {
        List<Vector> vectors = new ArrayList<>();
        try {
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT v.vector FROM vectors v ");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                vectors.add(Vector.fromString(rs.getString("vector"))); // Convert back to Vector
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("returned: " + vectors.size());
        return vectors;
    }

    /**
     * Retrieve the total number of points in the data structure.
     *
     * @return number as int.
     */
    public static int getAllVectorsCount() {
        int count = 0;
        try {
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM vectors");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static int getQVectorsCount() {
        int count = 0;
        try {
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM vectors" +
                            "JOIN queries q ON v.nodeID = q.nodeID ");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static Map<String, Vector> getVectorsFromFile(Path filePath) throws Exception {
        Map<String, Vector> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toAbsolutePath().toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                int d = parts.length - 1;
                String word = parts[0];
                Vector vector = new Vector(d);
                for (int i = 1; i <= d; i++) {
                    vector.setNext(Float.parseFloat(parts[i]));
                }
                vector.normalize().setLabel(word);
                map.put(word, vector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Retrieve a list of all vectors in the data structure.
     *
     * @return list of vectors.
     */
    public static int getDistsCount(double min, double max) {
        int count = 0;
        try {
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM dists d " + 
                    "WHERE d.dist >= ? AND d.dist < ?");
            stmt.setDouble(1, min);
            stmt.setDouble(2, max);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}
