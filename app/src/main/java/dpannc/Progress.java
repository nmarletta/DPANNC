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

    // create new main progress bar
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

    // update main progress bar
    public static void updateBar(int progress) {
        currentBarProgress = progress;
        moveCursorUp(2);
        clearLine();
        System.out.println(buildBar(progress));
        if (hasStatus)
            moveCursorDown(1);
        else 
            System.out.println();
    }

    // create new status line
    public static void newStatus(String name, int total) {
        statusName = name;
        statusTotal = total;
        currentStatusProgress = 0;
        hasStatus = true;
        moveCursorUp(1);
        clearLine();
        System.out.println(statusLine(0));
    }

    // update status line
    public static void updateStatus(int progress) {
        currentStatusProgress = progress;
        moveCursorUp(1);
        clearLine();
        System.out.println(statusLine(progress));
    }

    // clear status line
    public static void clearStatus() {
        moveCursorUp(1);
        clearLine();
        hasStatus = false;
        System.out.println();
    }

    // print string above the progress bar & status
    public static void printAbove(String str) {
        if (hasBar) {
            int linesToMove = 4;
            moveCursorUp(4);

            // clear bar and status lines
            for (int i = 0; i < linesToMove; i++) {
                clearLine();
                System.out.println();
            }
            moveCursorUp(linesToMove);
            // print string
            System.out.print(str.endsWith("\n") ? str : str + "\n");
            System.out.println();

            // reprint bar and status
            System.out.println(barName);
            System.out.println(buildBar(currentBarProgress));
            if (hasStatus) {
                System.out.println(statusLine(currentStatusProgress));
            } else {
                System.out.println();
            }

        } else {
            System.out.print(str.endsWith("\n") ? str : str + "\n");
            System.out.println();
        }

    }

    // 
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

    public static void end() {
        currentBarProgress = 0;
        hasBar = false;
        hasStatus = false;
        System.out.println(barName + " completed");
        System.out.println();
    }

    private static String statusLine(int progress) {
        return statusName + " - " + percent(progress, statusTotal);
    }

    // convert progress to percent string
    private static String percent(int progress, int total) {
        return String.format("%3d%%", (int) ((progress / (double) total) * 100));
    }

    // move cursor
    private static void moveCursorUp(int lines) {
        System.out.print(String.format("\033[%dA", lines));
    }
    private static void moveCursorDown(int lines) {
        System.out.print(String.format("\033[%dB", lines));
    }

    // clear line
    private static void clearLine() {
        System.out.print("\033[2K");
    }
}
