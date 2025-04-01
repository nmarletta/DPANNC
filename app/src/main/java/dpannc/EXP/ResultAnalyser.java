package dpannc.EXP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import dpannc.Vector;
import dpannc.database.DB;

public class ResultAnalyser {
    Vector query;
    HashMap<String, Double> dists;
    ArrayList<Element> list;

    // public ResultAnalyser(Vector q, Collection<String> vectors, DB db) {
    //     this.query = q;
    //     list = new ArrayList<Element>();

    //     try (BufferedReader reader = new BufferedReader(new FileReader(filepath.toAbsolutePath().toString()))) {
    //         String line;
    //         int counter = 0;
    //         while ((line = reader.readLine()) != null && counter < n) {
    //             Vector v = Vector.fromString(line);
    //             double dist = q.distance(v);
    //             list.add(new Element(v.getLabel(), dist));
    //             counter++;
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    public int amountWithin(double r) {
        if (r <= 0) throw new IllegalArgumentException("r cannot be less or equal to 0");
        int count = 0;
        for (Element e : list) {
            if (e.dist < r) count++;
        }
        return count;
    }

    public int amountWithin(double min, double max) {
        if (min <= 0 || max <= 0) throw new IllegalArgumentException("min and max cannot be less or equal to 0");
        if (max <= min) throw new IllegalArgumentException("max cannot be less or equal to min");
        int count = 0;
        for (Element e : list) {
            if (e.dist > min && e.dist < max) count++;
        }
        return count;
    }

    private class Element implements Comparable<Element>{
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
