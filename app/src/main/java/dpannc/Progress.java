package dpannc;

public class Progress {
    private static String barName;
    private static int barTotal;
    private static int currentBarProgress = 0;
    private static boolean hasBar = false;

    private static String statusName;
    private static int statusTotal;
    private static int currentStatusProgress = 0;

    private enum StatusType {
        NONE, BAR, LINE
    }

    private static StatusType statusType = StatusType.NONE;

    // Create new main progress bar
    public static void newBar(String name, int total) {
        barName = name;
        barTotal = total;
        currentBarProgress = 0;
        hasBar = true;
        statusType = StatusType.NONE;

        System.out.println("-----------------------");
        System.out.println();
        System.out.println(barName);
        System.out.println(buildBar(0));
        System.out.println();
    }

    // Update main progress bar
    public static void updateBar(int progress) {
        currentBarProgress = progress;
        moveCursorUp(2);
        clearLine();
        System.out.println(buildBar(progress));
        if (statusType != StatusType.NONE)
            moveCursorDown(1);
        else
            System.out.println(); // maintain fixed number of lines
    }

    // Create new status bar (with progress)
    public static void newStatusBar(String name, int total) throws Exception {
        if (statusType != StatusType.NONE)
            throw new Exception("Already has status: " + statusName + ", cannot create: " + name);

        statusName = name;
        statusTotal = total;
        currentStatusProgress = 0;
        statusType = StatusType.BAR;

        moveCursorUp(1);
        clearLine();
        System.out.println(statusLine(0));
    }

    // Update status bar
    public static void updateStatusBar(int progress) throws Exception {
        if (statusType != StatusType.BAR)
            throw new Exception("StatusBar is not active.");
        currentStatusProgress = progress;
        moveCursorUp(1);
        clearLine();
        System.out.println(statusLine(progress));
    }

    // Create new status line (message only)
    public static void newStatus(String message) throws Exception {
        if (statusType != StatusType.NONE)
            throw new Exception("Already has status, cannot create: " + message);

        statusName = message;
        statusType = StatusType.LINE;

        moveCursorUp(1);
        clearLine();
        System.out.println(message);
    }

    // Clear status (bar or line)
    public static void clearStatus() throws Exception {
        if (statusType == StatusType.NONE)
            throw new Exception("No status to clear.");
        moveCursorUp(1);
        clearLine();
        System.out.println(); // keep spacing consistent
        statusType = StatusType.NONE;
    }

    // Print string above the progress bar & status
    public static void printAbove(String str) {
        if (hasBar) {
            int linesToMove = 4; // bar name, bar, status, empty

            moveCursorUp(linesToMove);

            // Clear the block first
            for (int i = 0; i < linesToMove; i++) {
                clearLine();
                System.out.println();
            }
            moveCursorUp(linesToMove);

            // Print your content
            System.out.print(str.endsWith("\n") ? str : str + "\n");
            System.out.println();

            // Reprint bar + status
            System.out.println(barName);
            System.out.println(buildBar(currentBarProgress));
            if (statusType == StatusType.BAR) {
                System.out.println(statusLine(currentStatusProgress));
            } else if (statusType == StatusType.LINE) {
                System.out.println(statusName);
            } else {
                System.out.println(); // keep visual gap
            }

        } else {
            System.out.print(str.endsWith("\n") ? str : str + "\n");
            System.out.println();
        }
    }

    // Final cleanup
    public static void end() {
        currentBarProgress = 0;
        hasBar = false;
        statusType = StatusType.NONE;
        System.out.println(barName + " completed");
        System.out.println();
    }

    // Internal: build progress bar
    private static String buildBar(int progress) {
        int width = 40;
        int filled = (int) ((progress / (double) barTotal) * width);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            sb.append(i < filled ? "#" : "-");
        }
        sb.append("] ").append(percent(progress, barTotal));
        return sb.toString();
    }

    // Internal: build status bar string
    private static String statusLine(int progress) {
        return statusName + " - " + percent(progress, statusTotal);
    }

    // Internal: convert to percent string
    private static String percent(int progress, int total) {
        return String.format("%3d%%", (int) ((progress / (double) total) * 100));
    }

    // Internal: move cursor
    private static void moveCursorUp(int lines) {
        System.out.print(String.format("\033[%dA", lines));
    }

    private static void moveCursorDown(int lines) {
        System.out.print(String.format("\033[%dB", lines));
    }

    // Internal: clear line
    private static void clearLine() {
        System.out.print("\033[2K");
    }
}
