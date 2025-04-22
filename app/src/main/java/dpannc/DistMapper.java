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
    private static PolynomialSplineFunction meanSplineRev;
    private static PolynomialSplineFunction p95Spline;
    private static PolynomialSplineFunction p95SplineRev;
    private static PolynomialSplineFunction medianSpline;
    private static PolynomialSplineFunction medianSplineRev;

    static {
        try {
            String name = "dist_map";
            Path path = Paths.get("results/nash/" + name + ".csv");
            loadFromCSV(path.toString());
        } catch (IOException e) {
            System.err.println("Failed to initialize DistMapper: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadFromCSV(String filePath) throws IOException {
        ArrayList<Double> rList = new ArrayList<>();
        ArrayList<Double> medianList = new ArrayList<>();
        ArrayList<Double> meanList = new ArrayList<>();
        ArrayList<Double> p95List = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            // skip headers
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.startsWith("initial"))
                    continue;
                String[] parts = line.split(",");
                if (parts.length < 5)
                    continue;

                double r = Double.parseDouble(parts[0]);
                double median = Double.parseDouble(parts[1]);
                double mean = Double.parseDouble(parts[2]);
                double p95 = Double.parseDouble(parts[4]);

                rList.add(r);
                medianList.add(median);
                meanList.add(mean);
                p95List.add(p95);
            }
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        double[] r = rList.stream().mapToDouble(Double::doubleValue).toArray();

        medianSpline = interpolator.interpolate(r, medianList.stream().mapToDouble(Double::doubleValue).toArray());
        medianSplineRev = interpolator.interpolate(medianList.stream().mapToDouble(Double::doubleValue).toArray(), r);
        meanSpline = interpolator.interpolate(r, meanList.stream().mapToDouble(Double::doubleValue).toArray());
        meanSplineRev = interpolator.interpolate(meanList.stream().mapToDouble(Double::doubleValue).toArray(), r);
        p95Spline = interpolator.interpolate(r, p95List.stream().mapToDouble(Double::doubleValue).toArray());
        p95SplineRev = interpolator.interpolate(p95List.stream().mapToDouble(Double::doubleValue).toArray(), r);
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
            e.printStackTrace();
            return Double.NaN; 
        }
    }
    public static double getMedianRev(double r) {
        try {
            if (medianSplineRev == null) {
                throw new IllegalStateException("medianSplineRev is not initialized.");
            }
            return medianSplineRev.value(r);
        } catch (Exception e) {
            System.err.println("DistMapper.getMedianRev() failed for r = " + r + ": " + e.getMessage());
            e.printStackTrace();
            return Double.NaN; 
        }
    }

    public static double getMean(double r) {
        try {
            if (meanSpline == null) {
                throw new IllegalStateException("meanSpline is not initialized.");
            }
            return meanSpline.value(r);
        } catch (Exception e) {
            System.err.println("DistMapper.getMean() failed for r = " + r + ": " + e.getMessage());
            e.printStackTrace();
            return Double.NaN; 
        }
    }

    public static double getMeanRev(double r) {
        try {
            if (meanSplineRev == null) {
                throw new IllegalStateException("meanSplineRev is not initialized.");
            }
            return meanSplineRev.value(r);
        } catch (Exception e) {
            System.err.println("DistMapper.getMeanRev() failed for r = " + r + ": " + e.getMessage());
            e.printStackTrace();
            return Double.NaN; 
        }
    }


    public static double getP95(double r) {
        try {
            if (p95Spline == null) {
                throw new IllegalStateException("p95Spline is not initialized.");
            }
            return p95Spline.value(r);
        } catch (Exception e) {
            System.err.println("DistMapper.getP95() failed for r = " + r + ": " + e.getMessage());
            e.printStackTrace();
            return Double.NaN; 
        }
    }

    public static double getP95Rev(double r) {
        try {
            if (p95SplineRev == null) {
                throw new IllegalStateException("p95SplineRev is not initialized.");
            }
            return p95SplineRev.value(r);
        } catch (Exception e) {
            System.err.println("DistMapper.getP95Rev() failed for r = " + r + ": " + e.getMessage());
            e.printStackTrace();
            return Double.NaN; 
        }
    }

    public static void main(String args[]) throws Exception {
        // System.out.println(get(0.35));
        double a = 0.822;
        double median = DistMapper.getMedian(a);
        System.out.println(median);
        // exp();
    }

    public static void exp() throws Exception {
        String name = "mapper";
        DB db = new DB("DB/Mapper_" + name, true);

        Path filepathTarget = Paths.get("app/results/nash/" + name + ".csv");
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
                double p95 = DistMapper.getP95(r);

                writer.write(String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f\n",
                        r, median, mean, p95));
            }

        } catch (IOException e) {
            System.err.println("Error writing results: " + e.getMessage());
        }
    }

}
