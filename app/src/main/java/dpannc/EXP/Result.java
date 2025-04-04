package dpannc.EXP;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dpannc.Vector;
import dpannc.database.DB;
import dpannc.database.DBiterator;

public class Result {
    ArrayList<Element> list;

    public Result() {
        list = new ArrayList<Element>();
    }

    public Result loadDistancesBetween(Vector query, Collection<String> vectors, String table, DB db, boolean normalize) throws SQLException {
        Vector q = query.clone();
        list = new ArrayList<Element>();

        for (String label : vectors) {
            Vector v = db.getVectorByLabel(label, table);
            if (q.getLabel().equals(v.getLabel()))
                continue;
            if (normalize) {
                v.normalize();
                q.normalize();
            }
            double dist = q.distance(v);
            Element el = new Element(label, dist);
            list.add(el);
        }
        return this;
    }

    public Result loadDistancesBetween(Vector query, String table, DB db, boolean normalize) throws SQLException {
        Vector q = query.clone();
        list = new ArrayList<Element>();
        DBiterator it = db.iterator(table);
        while (it.hasNext()) {
            Vector v = it.next();
            if (q.getLabel().equals(v.getLabel()))
                continue;
            if (normalize) {
                v.normalize();
                q.normalize();
            }
            double dist = q.distance(v);
            Element el = new Element(v.getLabel(), dist);
            list.add(el);
        }
        return this;
    }

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
            if (e.value <= val)
                count++;
        }
        return count;
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

    public int amountWithin(double min, double max) {
        if (min <= 0 || max <= 0)
            throw new IllegalArgumentException("min and max cannot be less or equal to 0");
        if (max <= min)
            throw new IllegalArgumentException("max cannot be less or equal to min");
        int count = 0;
        for (Element e : list) {
            if (e.value > min && e.value < max)
                count++;
        }
        return count;
    }

    // public double minVal() {
    //     return min.value;
    // }

    // public double maxVal() {
    //     return max.value;
    // }

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

    public double msd() {
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
