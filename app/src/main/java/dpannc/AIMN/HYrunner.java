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
        Path filePath = Paths.get("resources", "GloVe-6B", "glove.6B.300d.txt");
        // Path filePath = Paths.get("resources", "generated", "points.txt");
        int n = 100;
        int d = 300;
        HY hy = new HY(sensitivity, epsilon, delta);
        hy.populate(n, d, filePath);
    }
}
