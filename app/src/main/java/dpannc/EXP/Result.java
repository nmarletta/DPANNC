package dpannc.EXP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import dpannc.Vector;
import dpannc.database.DB;
import dpannc.database.DBiterator;

public class Result {
    Vector query;
    ArrayList<Element> list;

    public Result(Vector q, Collection<String> vectors, DB db) throws SQLException {
        this.query = q;
        list = new ArrayList<Element>();

        for (String label : vectors) {
            Vector v = db.getVectorByLabel(label);
            double dist = q.distance(v);
            list.add(new Element(label, dist));
        }
    }

    public Result(Vector q, DB db) throws SQLException {
        this.query = q;
        list = new ArrayList<Element>();

        try (DBiterator it = db.iterator()) {
            while (it.hasNext()) {
                Vector v = it.next();
                double dist = q.distance(v);
                list.add(new Element(v.getLabel(), dist));
            }
        }
    }

    public int amountWithin(double r) {
        if (r <= 0)
            throw new IllegalArgumentException("r cannot be less or equal to 0");
        int count = 0;
        for (Element e : list) {
            if (e.dist < r)
                count++;
        }
        return count;
    }

    public int amountWithin(double min, double max) {
        if (min <= 0 || max <= 0)
            throw new IllegalArgumentException("min and max cannot be less or equal to 0");
        if (max <= min)
            throw new IllegalArgumentException("max cannot be less or equal to min");
        int count = 0;
        for (Element e : list) {
            if (e.dist > min && e.dist < max)
                count++;
        }
        return count;
    }

    private class Element implements Comparable<Element> {
        String label;
        double dist;

        public Element(String label, double dist) {
            this.label = label;
            this.dist = dist;
        }

        @Override
        public int compareTo(Element e) {
            return (dist < e.dist) ? -1 : ((dist == e.dist) ? 0 : 1);
        }
    }
}
