package dpannc.HY;

import java.nio.file.Path;
import java.nio.file.Paths;

import dpannc.DataGenerator;
import dpannc.NashDevice;
import dpannc.Vector;

public class HYrunner {
    public static void main(String[] args) throws Exception {
        double sensitivity = 1.0;
        double epsilon = 2.0;
        double delta = 0.0001;
        int n = 100;
        int d = 3;
        Path filePath = Paths.get("resources", "generated", d + "D_" + n + ".txt");
        DataGenerator.generateRandom(filePath, d, n, 10, 10);
        HY hy = new HY(sensitivity, epsilon, delta);
        hy.populate(n, d, filePath);
        hy.print();

        Vector q = new Vector(new double[] {0, 0, 0});
        int result = hy.query(q, 0.4, 10.0);
        System.out.println("result: " + result);
    }
}
