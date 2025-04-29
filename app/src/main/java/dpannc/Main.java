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
                case "nash9" -> NashDeviceExperiment.exp9();
                case "nash14" -> NashDeviceExperiment.exp14();
                case "nash15" -> NashDeviceExperiment.exp15();
                case "nash16" -> NashDeviceExperiment.exp16();
                case "nash17" -> NashDeviceExperiment.exp17();
                case "aimn1" -> AIMNexperiments.exp1();
                case "aimn2" -> AIMNexperiments.exp2();
                case "aimn3" -> AIMNexperiments.exp3();
                case "aimn4" -> AIMNexperiments.exp4();
                case "aimn5" -> AIMNexperiments.exp5();
                // case "aimn7" -> AIMNexperiments.exp7();
                case "hy1" -> HYexperiments.exp1();
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

