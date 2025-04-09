package dpannc.database;

import java.sql.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

import dpannc.Vector;

public class DBiterator implements Iterator<Vector>, AutoCloseable {
    private final PreparedStatement stmt;
    private final ResultSet rs;
    private boolean hasNext;

    public DBiterator(Connection conn, String table) throws SQLException {
        this.stmt = conn.prepareStatement("SELECT label, data FROM " + table);
        this.rs = stmt.executeQuery();
        this.hasNext = rs.next();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public Vector next() {
        if (!hasNext) throw new NoSuchElementException();

        try {
            String label = rs.getString("label");
            String data = rs.getString("data");
            hasNext = rs.next(); // move cursor for next call
            return Vector.fromString(label, data);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            // log or ignore
        }
    }
}


