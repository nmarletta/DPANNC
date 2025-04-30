package dpannc.HY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import dpannc.Vector;

public class Box {
    int d;
    int count;
    double size;
    Collection<Vector> data;
    ArrayList<String> list;
    double[] min;
    double[] max;

    public static void main(String[] args) {
        Collection<Vector> dataset = new ArrayList<>();
        dataset.add(new Vector(new double[]{-9.0, 10.0}));
        dataset.add(new Vector(new double[]{9.0, -10.0}));
        dataset.add(new Vector(new double[]{5.0, 5.0}));
        dataset.add(new Vector(new double[]{-5.0, -5.0}));
        dataset.add(new Vector(new double[]{-2.0, -3.0}));
        dataset.add(new Vector(new double[]{4.0, -6.0}));
        dataset.add(new Vector(new double[]{7.0, -2.0}));
        Box box = new Box(2, dataset);
        Box[] split = box.split();
        Box a = split[0];
        Box b = split[1];
    }

    public Box(double[] min, double[] max) {
        count = 0;
        this.min = min;
        this.max = max;
        d = min.length;
    }

    public Box(int d, Collection<Vector> dataset) {
        if (dataset.isEmpty()) {
            throw new IllegalArgumentException("Dataset cannot be empty");
        }

        // int d = dataset.iterator().next().getDimensions(); // alternativt
        this.d = d;
        min = new double[d];
        max = new double[d];

        Vector first = dataset.iterator().next();
        for (int i = 0; i < d; i++) {
            min[i] = first.get(i);
            max[i] = first.get(i);
        }

        for (Vector v : dataset) {
            for (int i = 0; i < v.dimensionality(); i++) {
                min[i] = Math.min(min[i], v.get(i));
                max[i] = Math.max(max[i], v.get(i));
            }
        }

        count = dataset.size();
        setSize();
        data = new ArrayList<>(dataset);
    }

    public int longestIndex() {
        double currLongest = 0;
        int longestIndex = 0;
        for (int i = 0; i < d; i++) {
            double length = max[i] - min[i];
            if (length > currLongest) {
                longestIndex = i;
                currLongest = length;
            }
        }
        return longestIndex;
    }

    public Box[] split() {
        int splitIndex = longestIndex();
        double splitValue = min[splitIndex] + (max[splitIndex] - min[splitIndex]) / 2;
        Box leftBox = this.emptyClone().setMax(splitIndex, splitValue).setSize();
        Box rightBox = this.emptyClone().setMin(splitIndex, splitValue).setSize();

        for (Vector v : data) {
            if (v.get(splitIndex) <= splitValue) {
                leftBox.addPoint(v);
            } else {
                rightBox.addPoint(v);
            }
        }

        leftBox.count = leftBox.data.size();
        rightBox.count = rightBox.data.size();

        return new Box[] { leftBox, rightBox };
    }

    // public boolean intersectsInnerRange(Vector q, double alpha, double diameter) {
    //     double distance = distanceFromPointToBox(q);
    //     return distance < (diameter/2);
    // }
    
    // public boolean isSubsetOfOuterRange(Vector q, double alpha, double diameter) {
    //     double distance = distanceFromPointToBox(q);
    //     return distance <= (1 + alpha) * (diameter/2);
    // }
    
    public boolean intersectsBall(Vector q, double radius) {
        double distSquared = 0.0;

        for (int i = 0; i < d; i++) {
            double c = q.get(i);
            double closest;

            if (c < min[i]) {
                closest = min[i];
            } else if (c > max[i]) {
                closest = max[i];
            } else {
                closest = c;
            }

            double delta = c - closest;
            distSquared += delta * delta;
        }
        return distSquared <= radius * radius;
    }

    public boolean isSubsetOfBall(Vector q, double radius) {
        double radiusSquared = radius * radius;
        int numCorners = 1 << d;  // 2^d corners in d-dimensional space
    
        for (int mask = 0; mask < numCorners; mask++) {
            double distSquared = 0.0;
            for (int i = 0; i < d; i++) {
                double cornerCoord = ((mask & (1 << i)) == 0) ? min[i] : max[i];
                double delta = cornerCoord - q.get(i);
                distSquared += delta * delta;
            }
            if (distSquared > radiusSquared) {
                return false;  // at least one corner is outside the ball
            }
        }
        return true; 
    }

    public void addPoint(Vector v) {
        data.add(v);
        count++;
    }

    public void addData(Collection<Vector> newData) {
        data.addAll(newData);
        count += newData.size();
    }

    public Box setMin(int index, double value) {
        min[index] = value;
        return this;
    }

    public Box setMax(int index, double value) {
        max[index] = value;
        return this;
    }

    // clones box but without data
    public Box emptyClone() {
        Box copy = new Box(Arrays.copyOf(min, d), Arrays.copyOf(max, d));
        copy.count = 0;
        copy.size = this.size;
        copy.data = new ArrayList<>();
        return copy;
    }

    public Box setSize() {
        size = max[0] - min[0];
        for (int i = 0; i < d; i++) {
            double length = max[i] - min[i];
            if (length > size) {
                size = length;
            }
        }
        return this;
    }

    public boolean equals(Box box) {
        double margin = 1e-9;
        for (int i = 0; i < d; i++) {
            if (Math.abs(min[i] - box.min[i]) > margin || Math.abs(max[i] - box.max[i]) > margin) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        String str = "Box:\n";
        for (int i = 0; i < d; i++) {
            str += "axis-" + i + ": " + min[i] + " <-> " + max[i] + "\n";
        }

        for (Vector v : data) {
            str += "point: " + v.toString() + "\n";
        }
        str += "---";
        return str;
    }

    public ArrayList<String> list() {
        ArrayList<String> list = new ArrayList<String>();
        for (Vector v : data) {
            list.add(v.getLabel());
        }
        return list;
    }
}