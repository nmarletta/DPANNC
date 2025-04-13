package dpannc.AIMN;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import dpannc.EXP.DataGenerator;
import dpannc.Vector;

public class Runner {
    static int SEED = 123;

    public static void main(String[] args) throws Exception {

    }

    private static void printProgress(int c, int n, int step) {
        double progress = (double) c / n * 100;
        double epsilon = 1e-9;
        if (Math.abs(progress % step) < epsilon) {
            System.out.println((int) progress + "% completed");
        }
    }
}