package dpannc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class DataPrepare {
    public static void main(String[] args) {
        // MNISTcsv2txt();
        truncateVectors();
        // nashVectors();
    }

    public static void MNISTcsv2txt() {
        String nameSource = "fashionMNIST_60K_784D";
        String nameTarget = nameSource;
        Path filepathSource = Paths.get("app/resources/FashionMNIST", nameSource + ".csv");
        Path filepathTarget = Paths.get("app/resources/FashionMNIST", nameTarget + ".txt");
        String[] label = new String[] { "Top", "Trouser", "Pullover", "Dress", "Coat", "Sandal", "Shirt", "Sneaker",
                "Bag", "Boot" };
        int[] id = new int[10];
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString());
                BufferedReader reader = new BufferedReader(new FileReader(filepathSource.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                int l = Integer.parseInt(tokens[0]);
                String newLine = label[l] + "-" + id[l]++;
                for (int i = 1; i < tokens.length; i++) {
                    newLine += " " + tokens[i];
                }
                writer.write(newLine + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFirstLine() {
        String nameSource = "english_2M_300D";
        String nameTarget = nameSource + "s";
        Path filepathSource = Paths.get("app/resources/fasttext", nameSource + ".txt");
        Path filepathTarget = Paths.get("app/resources/fasttext", nameTarget + ".txt");
        try {
            Files.createDirectories(filepathTarget.getParent());
            FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString());
            BufferedReader reader = new BufferedReader(new FileReader(filepathSource.toFile()));
            reader.readLine();
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 10) {
                writer.write(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void truncateVectors() {
        String nameSource = "fashionMNIST_60K_784D";
        Path filepathSource = Paths.get("app/resources/fashionMNIST", nameSource + ".txt");

        int n = 500_000;
        int[] dimensions = new int[] { 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 100, 150, 200,
                250, 500 };
        for (int d : dimensions) {
            String nameTarget = "trun_fashionMNIST_60K_" + d + "D";
            Path filepathTarget = Paths.get("app/resources/fashionMNIST/truncated", nameTarget + ".txt");
            try {
                Files.createDirectories(filepathTarget.getParent());
                FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString());
                BufferedReader reader = new BufferedReader(new FileReader(filepathSource.toFile()));
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < n) {
                    String[] tokens = line.split(" ");
                    String newLine = tokens[0];
                    for (int i = 1; i <= d; i++) {
                        newLine += " " + tokens[i];
                    }
                    writer.write(newLine + "\n");
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void nashVectors() {
        String nameSource = "fashionMNIST_60K_784D";
        Path filepathSource = Paths.get("app/resources/fasttext", nameSource + ".txt");

        int n = 500_000;
        int[] dimensions = new int[] { 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 100, 150, 200,
                250 };

        Random random = new Random(100);
        for (int d : dimensions) {
            String nameTarget = "nash_fashionMNIST_60K_" + d + "D";
            Path filepathTarget = Paths.get("app/resources/fashionMNIST/nashed", nameTarget + ".txt");
            NashDevice nd = new NashDevice(300, d, random);
            try {
                Files.createDirectories(filepathTarget.getParent());
                FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString());
                BufferedReader reader = new BufferedReader(new FileReader(filepathSource.toFile()));
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < n) {
                    String label = line.substring(0, line.indexOf(' '));
                    String data = line.substring(line.indexOf(' ') + 1);
                    Vector v = nd.transform(Vector.fromString(label, data));
                    writer.write(v.toString() + "\n");
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
