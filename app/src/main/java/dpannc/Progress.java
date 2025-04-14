package dpannc;

public class Progress {
    private static String barName;
    private static int barTotal;
    private static int currentBarProgress = 0;
    private static boolean hasBar = false;

    private static String statusName;
    private static int statusTotal;
    private static int currentStatusProgress = 0;
    private static boolean hasStatus = false;

    // Create new main progress bar
    public static void newBar(String name, int total) {
        barName = name;
        barTotal = total;
        currentBarProgress = 0;
        hasBar = true;
        hasStatus = false;

        System.out.println("---------------------");
        System.out.println();
        System.out.println(barName);
        System.out.println(buildBar(0));
        System.out.println();
    }

    // Update main progress bar
    public static void updateBar(int progress) {
        // currentBarProgress = progress;
        // moveCursorUp(hasStatus ? 2 : 1);
        // clearLine();
        // System.out.println(buildBar(progress));
        // if (hasStatus)
        //     moveCursorDown(1);

        currentBarProgress = progress;
        moveCursorUp(2);
        clearLine();
        System.out.println(buildBar(progress));
        if (hasStatus)
            moveCursorDown(1);
        else 
            System.out.println();
    }

    // Create new subprocess/status line
    public static void newStatus(String name, int total) {
        statusName = name;
        statusTotal = total;
        currentStatusProgress = 0;
        hasStatus = true;
        moveCursorUp(1);
        clearLine();
        System.out.println(statusLine(0));
    }

    // Update subprocess/status line
    public static void updateStatus(int progress) {
        currentStatusProgress = progress;
        moveCursorUp(1);
        clearLine();
        System.out.println(statusLine(progress));
    }

    // Clear subprocess/status line
    public static void clearStatus() {
        moveCursorUp(1);
        clearLine();
        hasStatus = false;
        System.out.println();
    }

    // Print content above the progress bar & status
    public static void printAbove(String content) {
        if (hasBar) {
            int linesToMove = 4;
            moveCursorUp(4);

            // Clear bar + status lines
            for (int i = 0; i < linesToMove; i++) {
                clearLine();
                System.out.println();
            }

            moveCursorUp(linesToMove);

            // Move to where new content should go (below previous output)
            System.out.print(content.endsWith("\n") ? content : content + "\n");
            System.out.println();

            // Redraw bar & status
            System.out.println(barName);
            System.out.println(buildBar(currentBarProgress));

            if (hasStatus) {
                System.out.println(statusLine(currentStatusProgress));
            } else {
                System.out.println();
            }
        } else {
            System.out.print(content.endsWith("\n") ? content : content + "\n");
            System.out.println();
        }

    }

    // Internal: Build progress bar string
    private static String buildBar(int progress) {
        int width = 30; // Change this if you want a wider/narrower bar
        int filled = (int) ((progress / (double) barTotal) * width);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            sb.append(i < filled ? "#" : "-");
        }
        sb.append("] ").append(percent(progress, barTotal));
        return sb.toString();
    }

    public static void end() {
        currentBarProgress = 0;
        hasBar = false;
        hasStatus = false;
        System.out.println(barName + " completed");
        System.out.println();
    }

    // Internal: Build status line
    private static String statusLine(int progress) {
        return statusName + " - " + percent(progress, statusTotal);
    }

    // Internal: Convert progress to percent string
    private static String percent(int progress, int total) {
        return String.format("%3d%%", (int) ((progress / (double) total) * 100));
    }

    // Internal: Move cursor
    private static void moveCursorUp(int lines) {
        System.out.print(String.format("\033[%dA", lines));
    }

    private static void moveCursorDown(int lines) {
        System.out.print(String.format("\033[%dB", lines));
    }

    // Internal: Clear line
    private static void clearLine() {
        System.out.print("\033[2K");
    }

    public static void main(String[] args) throws InterruptedException {
        Progress.printAbove("test");
        Progress.newBar("Main Process", 10);

        for (int i = 1; i <= 10; i++) {
            Progress.updateBar(i);

            Progress.newStatus("Sub Task", 5);
            for (int j = 1; j <= 5; j++) {
                Progress.updateStatus(j);
                Progress.printAbove("j:" + j);
                Thread.sleep(100);
            }
            Progress.clearStatus();
            Progress.printAbove("i:" + i);
            Thread.sleep(300);
        }
    }
}
