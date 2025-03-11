package dpannc.HY;

import java.util.*;

public class HY {
    private double sensitivity;
    private double epsilon;
    private double delta;

    // private Node root;

    public HY(double sensitivity, double epsilon, double delta) {
        this.sensitivity = sensitivity;
        this.epsilon = epsilon;
        this.delta = delta;

        // this.root = new Node(0, "0");
    }

    private class Cell {
        double size; // length of the longest side of the outer box
        Box innie;
        Box outie;
        int count;

        Cell left;
        Cell right;

        public Cell(Box box) {
            outie = box;
        }

        public double size() {
            return size;
        }

        public int count() {
            return count;
        }

        public void shrink() {

        }
        
        public void split() {
            int splitIndex = outie.longestIndex();
            double splitValue = (outie.max[splitIndex] - outie.min[splitIndex]) / 2;
            
            Box leftBox = outie.clone().setMax(splitIndex, splitValue);
            Box rightBox = outie.clone().setMin(splitIndex, splitValue);

            // create child cells
            left = new Cell(leftBox); 
            right = new Cell(rightBox); 
        }
    }

    private class Box {
        int d;
        double[] min;
        double[] max;

        public Box(double[] min, double[] max) {
            this.min = min;
            this.max = max;
            d = min.length;
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

        public Box setMin(int index, double value) {
            min[index] = value;
            return this;
        }

        public Box setMax(int index, double value) {
            max[index] = value;
            return this;
        }

        public Box clone() {
            return new Box(min, max);
        }
    }
}

