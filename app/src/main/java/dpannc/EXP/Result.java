package dpannc.EXP;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dpannc.Progress;
import dpannc.Vector;
import dpannc.database.DB;
import dpannc.database.DBiterator;

public class Result {
    ArrayList<Element> list;

    public Result() {
        list = new ArrayList<Element>();
    }

    public Result loadDistancesBetween(Vector q, Collection<String> vectors, String table, DB db) throws Exception {
        Progress.newStatusBar("Calculating distances from list", vectors.size());
        int i = 0;
        list = new ArrayList<Element>();
        for (String label : vectors) {
            Vector v = db.getVectorByLabel(label, table);
            if (q.getLabel().equals(v.getLabel()))
                continue;
            double dist = q.distance(v);
            Element el = new Element(label, dist);
            list.add(el);
            i++;
            Progress.updateStatusBar(i);
        }
        Progress.clearStatus();
        return this;
    }

    public Result loadDistancesBetween(Vector q, String table, DB db) throws Exception {
        Progress.newStatusBar("Calculating distances from list", db.tableSize(table));
        int i = 0;
        list = new ArrayList<Element>();
        DBiterator it = db.iterator(table);
        while (it.hasNext()) {
            Vector v = it.next();
            if (q.getLabel().equals(v.getLabel()))
                continue;
            double dist = q.distance(v);
            Element el = new Element(v.getLabel(), dist);
            list.add(el);
            i++;
            Progress.updateStatusBar(i);
        }
        Progress.clearStatus();
        return this;
    }

    // public Result loadDotProductBetween(Vector q, Collection<String> vectors,
    // String table, DB db) throws Exception {
    // list = new ArrayList<Element>();
    // for (String label : vectors) {
    // Vector v = db.getVectorByLabel(label, table);
    // if (q.getLabel().equals(v.getLabel()))
    // continue;
    // double dist = q.dot(v);
    // Element el = new Element(label, dist);
    // list.add(el);
    // }
    // return this;
    // }

    // public Result loadDotProductBetween(Vector q, String table, DB db) throws
    // SQLException {
    // list = new ArrayList<Element>();
    // DBiterator it = db.iterator(table);
    // while (it.hasNext()) {
    // Vector v = it.next();
    // if (q.getLabel().equals(v.getLabel()))
    // continue;
    // double dist = q.dot(v);
    // Element el = new Element(v.getLabel(), dist);
    // list.add(el);
    // }
    // return this;
    // }

    public void add(String label, double val) {
        list.add(new Element(label, val));
    }

    public void add(Element e) {
        list.add(e);
    }

    public Element get(int i) {
        return list.get(i);
    }

    public List<Element> get() {
        return list;
    }

    public int size() {
        return list.size();
    }

    public int amountLessThan(double val) {
        if (val <= 0)
            throw new IllegalArgumentException("r cannot be less or equal to 0");
        int count = 0;
        for (Element e : list) {
            if (e.value < val)
                count++;
        }
        return count;
    }

    public Set<String> lessThan(double val) {
        if (val <= 0)
            throw new IllegalArgumentException("r cannot be less or equal to 0");
        Set<String> result = new HashSet<String>();
        for (Element e : list) {
            if (e.value < val)
                result.add(e.label);
        }
        return result;
    }

    public int amountWithin(double min, double max) {
        if (min <= 0 || max <= 0)
            throw new IllegalArgumentException("min and max cannot be less or equal to 0");
        if (max <= min)
            throw new IllegalArgumentException("max cannot be less or equal to min");
        int count = 0;
        for (Element e : list) {
            if (e.value >= min && e.value <= max)
                count++;
        }
        return count;
    }

    public Set<String> within(double min, double max) {
        if (min <= 0 || max <= min)
            throw new IllegalArgumentException("min or max not set correctly");
        Set<String> result = new HashSet<String>();
        for (Element e : list) {
            if (e.value >= min && e.value <= max)
                result.add(e.label);
        }
        return result;
    }

    public int amountGreaterThan(double val) {
        if (val <= 0)
            throw new IllegalArgumentException("r cannot be less or equal to 0");
        int count = 0;
        for (Element e : list) {
            if (e.value > val)
                count++;
        }
        return count;
    }

    public Set<String> greaterThan(double val) {
        if (val <= 0)
            throw new IllegalArgumentException("r cannot be less or equal to 0");
        Set<String> result = new HashSet<String>();
        for (Element e : list) {
            if (e.value > val)
                result.add(e.label);
        }
        return result;
    }

    public Set<String> all() {
        Set<String> result = new HashSet<String>();
        for (Element e : list) {
            result.add(e.label);
        }
        return result;
    }

    public Result diffBetween(Result b) throws Exception {
        int size;
        if (this.size() != b.size())
            throw new IllegalArgumentException("a and b are not the same size");
        else
            size = this.size();

        Result res = new Result();
        for (int i = 0; i < size; i++) {
            Element A = this.get(i);
            Element B = b.get(i);
            if (!A.label.equals(B.label))
                throw new IllegalArgumentException(
                        "elements do not have the same label: '" + A.label + "' - '" + B.label + "'");
            String label = A.label;
            double val = A.value - B.value;
            res.add(new Element(label, val));
        }
        return res;
    }

    public Result changeBetween(Result b) throws Exception {
        int size;
        if (this.size() != b.size())
            throw new IllegalArgumentException("a and b are not the same size");
        else
            size = this.size();

        Result res = new Result();
        for (int i = 0; i < size; i++) {
            Element A = this.get(i);
            Element B = b.get(i);
            if (!A.label.equals(B.label))
                throw new IllegalArgumentException(
                        "elements do not have the same label: '" + A.label + "' - '" + B.label + "'");
            String label = A.label;
            // double val = (A.value - B.value) / A.value;
            double val = (1 / A.value) * B.value;
            res.add(new Element(label, val));
        }
        return res;
    }

    public double mean() {
        if (list.isEmpty())
            return 0;
        double sum = 0;
        for (Element e : list) {
            sum += e.value;
        }
        return sum / list.size();
    }

    public double median() {
        if (list.isEmpty())
            return 0;
        List<Double> values = list.stream()
                .map(e -> e.value)
                .sorted()
                .toList();
        int mid = values.size() / 2;
        if (values.size() % 2 == 0) {
            return (values.get(mid - 1) + values.get(mid)) / 2.0;
        } else {
            return values.get(mid);
        }
    }

    public double stddev() {
        if (list.isEmpty())
            return 0;
        double mean = mean();
        double sum = 0;
        for (Element e : list) {
            sum += Math.pow(e.value - mean, 2);
        }
        return Math.sqrt(sum / size());
    }

    public double mad() {
        if (list.isEmpty())
            return 0;
        double med = median();
        List<Double> deviations = list.stream()
                .map(e -> Math.abs(e.value - med))
                .sorted()
                .toList();
        int mid = deviations.size() / 2;
        if (deviations.size() % 2 == 0) {
            return (deviations.get(mid - 1) + deviations.get(mid)) / 2.0;
        } else {
            return deviations.get(mid);
        }
    }

    public double percentile(double q) {
        if (list.isEmpty())
            return 0;
        if (q < 0 || q > 1) {
            throw new IllegalArgumentException("Percentile must be between 0 and 1");
        }

        List<Double> values = list.stream()
                .map(e -> e.value)
                .sorted()
                .toList();

        int n = values.size();
        if (n == 1)
            return values.get(0);

        double pos = q * (n - 1); // position in the sorted list
        int lower = (int) Math.floor(pos);
        int upper = (int) Math.ceil(pos);
        double fraction = pos - lower;

        // linear interpolation
        if (lower == upper) {
            return values.get(lower);
        } else {
            return values.get(lower) * (1 - fraction) + values.get(upper) * fraction;
        }
    }

    public double distanceToKNearest(int k) {
        if (k <= 0 || k > list.size()) {
            throw new IllegalArgumentException("k must be between 1 and the number of elements in the result");
        }

        return list.stream()
                .map(e -> e.value)
                .sorted()
                .toList()
                .get(k - 1);
    }

    @Override
    public String toString() {
        String str = "";
        for (Element e : list) {
            str += e.label + ": " + e.value + "\n";
        }
        return str;
    }

    @Override
    public Result clone() {
        Result c = new Result();
        for (Element e : this.list) {
            c.list.add(new Element(e.label, e.value));
        }
        return c;
    }

    public class Element implements Comparable<Element> {
        public String label;
        public double value;

        public Element(String label, double value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public int compareTo(Element e) {
            return (value < e.value) ? -1 : ((value == e.value) ? 0 : 1);
        }
    }
}
