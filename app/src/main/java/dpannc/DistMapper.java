package dpannc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import dpannc.database.DB;

public class DistMapper {

    private static PolynomialSplineFunction meanSpline;
    private static PolynomialSplineFunction stddevSpline;
    private static PolynomialSplineFunction p95Spline;
    private static PolynomialSplineFunction medianSpline;

    static {
        try {
            Path path = Paths.get(System.getProperty("user.dir"), "../results", "nash", "dist_map.csv");
            loadFromCSV(path.toString());
        } catch (IOException e) {
            System.err.println("Failed to initialize DistMapper: " + e.getMessage());
        }
    }

    private static void loadFromCSV(String filePath) throws IOException {
        ArrayList<Double> rList = new ArrayList<>();
        ArrayList<Double> medianList = new ArrayList<>();
        ArrayList<Double> meanList = new ArrayList<>();
        ArrayList<Double> stddevList = new ArrayList<>();
        ArrayList<Double> p95List = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Skip headers
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.startsWith("initial"))
                    continue;
                String[] parts = line.split(",");
                if (parts.length < 6)
                    continue;

                double r = Double.parseDouble(parts[0]);
                double median = Double.parseDouble(parts[1]);
                double mean = Double.parseDouble(parts[2]);
                double stddev = Double.parseDouble(parts[3]);
                double p95 = Double.parseDouble(parts[5]);

                rList.add(r);
                medianList.add(median);
                meanList.add(mean);
                stddevList.add(stddev);
                p95List.add(p95);
            }
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        double[] r = rList.stream().mapToDouble(Double::doubleValue).toArray();

        medianSpline = interpolator.interpolate(r, medianList.stream().mapToDouble(Double::doubleValue).toArray());
        meanSpline = interpolator.interpolate(r, meanList.stream().mapToDouble(Double::doubleValue).toArray());
        stddevSpline = interpolator.interpolate(r, stddevList.stream().mapToDouble(Double::doubleValue).toArray());
        p95Spline = interpolator.interpolate(r, p95List.stream().mapToDouble(Double::doubleValue).toArray());
    }

    // Accessors
    public static double getMedian(double r) {
        try {
            if (medianSpline == null) {
                throw new IllegalStateException("medianSpline is not initialized.");
            }
            return medianSpline.value(r);
        } catch (Exception e) {
            System.err.println("DistMapper.getMedian() failed for r = " + r + ": " + e.getMessage());
            return Double.NaN; 
        }
    }

    public static double getMean(double r) {
        return meanSpline.value(r);
    }

    public static double getStddev(double r) {
        return stddevSpline.value(r);
    }

    public static double getP95(double r) {
        return p95Spline.value(r);
    }

    public static void main(String args[]) throws Exception {
        // System.out.println(get(0.35));
        exp();
    }

    public static void exp() throws Exception {
        DB db = new DB("dpannc");

        String name = "mapper";
        Path filepathTarget = Paths.get("results/nash", name + ".csv");
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString())) {
            // CSV header
            writer.write("initial distance value / mapped values from DistMapper\n");
            writer.write("0\n");
            writer.write("1,2,3,4\n");
            writer.write("r,median,mean,stddev,p95\n");

            // settings
            double min = 0.1;
            double max = 4.5;
            double inc = 0.01;

            for (double r = min; r <= max; r += inc) {
                double median = DistMapper.getMedian(r);
                double mean = DistMapper.getMean(r);
                double stddev = DistMapper.getStddev(r);
                double p95 = DistMapper.getP95(r);

                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f,%.5f\n",
                        r, median, mean, stddev, p95));
            }

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

}
