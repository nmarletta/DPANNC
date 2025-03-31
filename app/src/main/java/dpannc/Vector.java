package dpannc;

import java.util.Random;

public class Vector {
    private String label;
    private double[] components;
    private int nextEmpty;

    /**
     * Constructs a vector with the given components.
     *
     * @param v the array of components to initialize the vector with.
     */
    public Vector(double[] v) {
        this.components = v.clone();
        this.nextEmpty = components.length;
    }

    /**
     * Constructs a vector with the specified dimension, initialized to zero.
     *
     * @param d the number of dimensions of the vector.
     */
    public Vector(int d) {
        this.components = new double[d];
        this.nextEmpty = 0;
    }

    /**
     * Set label of vector.
     *
     * @param str the label of the vector.
     * @return the updated vector.
     */
    public Vector setLabel(String str) {
        this.label = str;
        return this;
    }

    /**
     * Get label of vector.
     *
     * @return the label of the vector as string.
     */
    public String getLabel() {
        if (label == null)
            return "noLabel";
        // if (label == null) throw new IllegalArgumentException("Label is null");
        return label;
    }

    /**
     * Returns the number of dimensions of the vector.
     *
     * @return the number of dimensions.
     */
    public int getDimensions() {
        return components.length;
    }

    /**
     * Populates the vector with random values sampled from a Gaussian distribution.
     *
     * @param random the random object
     * @return the updated vector with random Gaussian values.
     */
    public Vector randomGaussian(Random random) {
        for (int c = 0; c < components.length; c++) {
            this.components[c] = random.nextGaussian();
        }
        this.nextEmpty = components.length;
        return this;
    }

    /**
     * Populates the vector with random values uniformly distributed within some range.
     *
     * @param random the random object
     * @param range the range each component can be
     * @return the updated vector with random Gaussian values.
     */
    public Vector random(Random random, double range) {
        for (int c = 0; c < components.length; c++) {
            this.components[c] = random.nextDouble() * range - (range/2);
        }
        this.nextEmpty = components.length;
        return this;
    }

    /**
     * Assigns a value to the next empty component.
     *
     */
    public void setNext(double value) {
        if (nextEmpty >= getDimensions()) {
            System.out.println("error: " + value);
        }
        components[nextEmpty] = value;
        nextEmpty += 1;
    }

    /**
     * Returns the components of the vector.
     *
     * @return the array of vector components.
     */
    public double[] get() {
        return components;
    }

    /**
     * Retrieves a specific component of the vector.
     *
     * @param c the index of the component to retrieve.
     * @return the value of the specified component.
     */
    public double get(int c) {
        return components[c];
    }

    /**
     * Computes the magnitude (Euclidean norm) of the vector.
     *
     * @return the magnitude of the vector.
     */
    public double magnitude() {
        double mag = 0.0f;
        for (double c : components) {
            mag += c * c;
        }
        return Math.sqrt(mag);
    }

    /**
     * Normalizes the vector to unit length.
     *
     * @return the normalized vector.
     */
    public Vector normalize() {
        double mag = this.magnitude();
        return this.divide(mag);
    }

    /**
     * Copies the components of another vector into this vector.
     *
     * @param v the vector to copy from.
     * @return the updated vector.
     */
    public Vector set(Vector v) {
        for (int c = 0; c < components.length; c++) {
            this.components[c] = v.get()[c];
        }
        return this;
    }

    /**
     * Sets the magnitude of the vector to a specified length while maintaining
     * direction.
     *
     * @param len the desired magnitude.
     * @return the updated vector.
     */
    public Vector setMagnitude(double len) {
        this.normalize();
        this.multiply(len);
        return this;
    }

    /**
     * Add each component of the vector by the components of the other.
     *
     * @param v the vector to add.
     * @return the updated vector.
     */
    public Vector add(Vector v) {
        for (int c = 0; c < components.length; c++) {
            this.components[c] += v.get(c);
        }
        return this;
    }

    /**
     * Subtracts each component of the vector by the components of the other.
     *
     * @param v the vector to subtract by.
     * @return the updated vector.
     */
    public Vector subtract(Vector v) {
        for (int c = 0; c < components.length; c++) {
            this.components[c] -= v.get(c);
        }
        return this;
    }

    /**
     * Divides each component of the vector by a scalar.
     *
     * @param n the scalar value to divide by.
     * @return the updated vector.
     */
    public Vector divide(double n) {
        for (int c = 0; c < components.length; c++) {
            this.components[c] /= n;
        }
        return this;
    }

    /**
     * Divides each component of the vector by a scalar.
     *
     * @param n the scalar value to divide by.
     * @return the updated vector.
     */
    public Vector divide(Vector v) {
        for (int c = 0; c < components.length; c++) {
            this.components[c] /= v.get(c);
        }
        return this;
    }

    /**
     * Multiplies each component of the vector by a scalar.
     *
     * @param n the scalar value to multiply by.
     * @return the updated vector.
     */
    public Vector multiply(double n) {
        for (int c = 0; c < components.length; c++) {
            this.components[c] *= n;
        }
        return this;
    }

    /**
     * Computes the dot product of this vector with another vector.
     *
     * @param v the other vector to compute the dot product with.
     * @return the dot product value.
     * @throws IllegalArgumentException if the vectors do not have the same
     *                                  dimension.
     */
    public double dot(Vector v) {
        if (this.getDimensions() != v.getDimensions()) {
            throw new IllegalArgumentException("Vectors must have the same dimension for dot product. Vectors: " + this.label + " and " + v.getLabel());
        }
        double product = 0.0f;
        for (int c = 0; c < components.length; c++) {
            product += this.components[c] * v.get(c);
        }
        return product;
    }

    /**
     * Computes the Euclidean distance between this vector and another vector.
     *
     * @param v the other vector to compute the distance to.
     * @return the Euclidean distance between the two vectors.
     * @throws IllegalArgumentException if the vectors do not have the same
     *                                  dimension.
     */
    public double distance(Vector v) {
        if (this.getDimensions() != v.getDimensions()) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimension for Euclidean distance calculation");
        }

        double sum = 0.0;
        for (int c = 0; c < components.length; c++) {
            double diff = components[c] - v.get(c);
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    public double angle(Vector v) {
        double dotProduct = this.dot(v);
        double magnitudeProduct = this.magnitude() * v.magnitude();
        if (magnitudeProduct == 0) {
            throw new ArithmeticException("Cannot compute angle with a zero vector");
        }
        return Math.acos(dotProduct / magnitudeProduct);
    }

    /**
     * Returns a clone of this vector.
     *
     * @return a copy of the vector with identical components.
     */
    public Vector clone() {
        return new Vector(components);
    }

    /**
     * Returns a clone of this vector.
     *
     * @return the updated vector.
     */
    public Vector clear() {
        components = new double[components.length];
        nextEmpty = 0;
        return this;
    }

    // @Override
    // public int hashCode() {
    //     return label != null ? label.hashCode() : 0;
    // }

    // @Override
    // public boolean equals(Object obj) {
    //     if (this == obj)
    //         return true; // Same object reference
    //     if (obj == null || getClass() != obj.getClass())
    //         return false; // Ensure type match

    //     Vector vector = (Vector) obj;
    //     return label != null ? label.equals(vector.label) : vector.label == null;
    // }

    /**
     * Returns a string representation of the vector.
     *
     * @return a string containing the vector components.
     */
    public String toString() {
        String s = getLabel();
        for (int i = 0; i < components.length; i++) {
            s += " " + components[i];
        }
        return s;
    }

    /**
     * Prints the vector to the standard output.
     */
    public void print() {
        System.out.println(toString());
    }

    /**
     * Returns a Vector parsed from String.
     *
     * @return a Vector object with the .
     */
    public static Vector fromString(String str) {
        String[] split = str.split(" ");
        String label = split[0];
        double[] components = new double[split.length-1];
        for (int i = 0; i < components.length; i++) {
            components[i] = Double.parseDouble(split[i+1]);
        }
        return new Vector(components).setLabel(label);
    }
}
