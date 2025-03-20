package dpannc.AIMN;

import java.nio.file.Path;
import java.nio.file.Paths;

import dpannc.NashDevice;
import dpannc.HY.HY;

public class HYrunner {
    public static void main(String[] args) throws Exception {
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        Path filePath = Paths.get("resources", "generated", "4D_100.txt");
        // Path filePath = Paths.get("resources", "generated", "points.txt");
        int n = 100;
        int d = 4;
        HY hy = new HY(sensitivity, epsilon, delta);
        hy.populate(n, d, filePath);
        hy.print();
    }
}
