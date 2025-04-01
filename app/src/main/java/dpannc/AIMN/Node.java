package dpannc.AIMN;

import java.util.*;

import dpannc.Noise;
import dpannc.Vector;

public class Node {
    private int level;
    private int count;
    private int noisyCount;
    private boolean isLeaf;
    private String id;
    private Vector g;
    protected List<Node> childNodes;

    AIMNc ds;

    public Node(int level, String id, AIMNc ds) {
        this.ds = ds;
        this.level = level;
        this.isLeaf = (level >= ds.K);
        this.count = 0;
        this.noisyCount = 0;
        this.id = id;

        g = new Vector(ds.T).randomGaussian(ds.random);

        childNodes = new ArrayList<>();
    }

    public void insertPoint(Vector v) {
        if (isLeaf) {
            ds.buckets.computeIfAbsent(id, k -> new ArrayList<>()).add(v.getLabel());
            count++;
        } else {
            if (!sendToChildNode(v)) {
                ds.remainder++;
            }
        }
    }

    private boolean sendToChildNode(Vector v) {
        for (int i = 0; i < childNodes.size(); i++) {
            Node n = childNodes.get(i);
            if (n.accepts(v, ds.etaU)) {
                n.insertPoint(v);
                return true;
            }
        }

        while (childNodes.size() < ds.T) {
            int currID = childNodes.size();// < 1 ? 0 : childNodes.size();
            Node n = new Node(level + 1, id + ":" + currID, ds);
            if (n.accepts(v, ds.etaU)) {
                childNodes.add(n);
                n.insertPoint(v);
                return true;
            }
        }
        return false;
    }

    public int query(Vector q) {
        if (isLeaf) {
            ds.query.addAll(ds.buckets.get(id));
            return count;
        } else {
            int total = 0;
            for (Node n : childNodes) {
                if (n.accepts(q, ds.etaQ)) {
                    total += n.query(q);
                }
            }
            return total;
        }
    }

    public void addNoise() {
        count = count + (int) ds.noise.TLap(ds.sensitivity, ds.epsilon / ds.adjSen, ds.delta / ds.adjSen);
        if (noisyCount <= ds.threshold) {
            noisyCount = 0;
        }
    }

    private boolean accepts(Vector v, double etaU) {
        return g.dot(v) >= etaU;
    }

    // public List<Node> getChildNodes() {
    // return childNodes;
    // }

    public Vector gaussian() {
        return g;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public String id() {
        return id;
    }

    public int count() {
        return count;
    }
}
