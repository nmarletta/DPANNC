package dpannc;

import dpannc.EXP.*;

// command examples:
// gradle run --args="exp nash1"
// gradle run --args="exp aimn1"

public class Main {
    public static void main(String[] args) {
        if (args.length < 2 || !args[0].equals("exp")) {
            System.err.println("Usage: exp <experiment>");
            System.exit(1);
        }

        String experiment = args[1];
        try {
            switch (experiment) {
                case "nash1" -> NashDeviceExperiment.exp1();
                case "nash2" -> NashDeviceExperiment.exp2();
                case "nash3" -> NashDeviceExperiment.exp3();
                case "nash9" -> NashDeviceExperiment.exp9();
                case "nash10" -> NashDeviceExperiment.exp10();
                case "nash14" -> NashDeviceExperiment.exp14();
                case "nash15" -> NashDeviceExperiment.exp15();
                case "nash16" -> NashDeviceExperiment.exp16();
                case "nash17" -> NashDeviceExperiment.exp17();
                case "accuracy1a" -> AccuracyExperiments.exp1a();
                case "accuracy1b" -> AccuracyExperiments.exp1b();
                case "accuracy2a" -> AccuracyExperiments.exp2a();
                case "accuracy2b" -> AccuracyExperiments.exp2b();
                case "accuracy3" -> AccuracyExperiments.exp3();
                case "accuracy4" -> AccuracyExperiments.exp4();
                case "accuracy5" -> AccuracyExperiments.exp5();
                case "accuracy10" -> AccuracyExperiments.exp10();
                case "accuracy11" -> AccuracyExperiments.exp11();
                case "accuracy100" -> AccuracyExperiments.exp100();
                case "time1" -> RunningtimeExperiments.exp1();
                case "time2" -> RunningtimeExperiments.exp2();
                case "time3" -> RunningtimeExperiments.exp3();
                case "dp1" -> DPExperiments.exp1();
                case "dp2" -> DPExperiments.exp2();
                default -> {
                    System.err.println("Unknown experiment: " + experiment);
                    System.exit(1);
                }
            }

            String[] plotCommand = switch (experiment) {
                case "aimn1", "aimn2", "aimn3" -> new String[] { "python3", "plot.py", "aimn", experiment };
                case "nash1", "nash2", "nash3", "nash4", "nash5", "nash6", "nash7", "nash8", "nash9", "nash10" -> new String[] { "python3", "plot.py", "nash", experiment };
                case "nash12" -> new String[] { "python3", "heat.py", "nash", experiment };
                default -> null;
            };
            
            if (plotCommand != null) {
                System.out.println("Running Python plot: " + String.join(" ", plotCommand));
                ProcessBuilder pb = new ProcessBuilder(plotCommand);
                pb.inheritIO(); // show output from Python
                pb.start().waitFor();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
