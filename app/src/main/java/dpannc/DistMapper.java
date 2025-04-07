package dpannc;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class DistMapper {
    
    static double[] r = { 0, 0.001, 0.101, 0.201, 0.301, 0.401, 0.501, 0.601, 0.701, 0.801, 0.901, 1.001, 1.101, 1.201,
            1.301,
            1.401, 1.501, 1.601, 1.701, 1.801, 1.901, 2.001, 2.101, 2.201, 2.301, 2.401, 2.501, 2.601, 2.701, 2.801,
            100 };
    static double[] rPrime = { 0, 9.905963792590166E-4, 0.10124567557664202, 0.1939216617904176, 0.2947252280705157,
            0.3947574312850263, 0.48555330167521804, 0.5723070580770659, 0.6551256342599246, 0.7438810760746326,
            0.8165559072516011, 0.8877534913546392, 0.9532642858994862, 1.0145952792530357, 1.0691616334899205,
            1.1149452323756808, 1.156109232629915, 1.193856090799152, 1.2416551501917046, 1.2697969842726833,
            1.300177303623643, 1.320597998514748, 1.332618516999728, 1.357685602078289, 1.3633603791770155,
            1.3735641092228388, 1.3865952735047942, 1.3890284960285182, 1.3968963063563349, 1.4002169480919637, 1.4 };

    private SplineInterpolator interpolator = new SplineInterpolator();
    private PolynomialSplineFunction spline = interpolator.interpolate(r,rPrime);

    public double get(double r) {
        return spline.value(r);
    }

    public static void main(String args[]) {
        System.out.println(r.length);
        System.out.println(rPrime.length);
    }
}
