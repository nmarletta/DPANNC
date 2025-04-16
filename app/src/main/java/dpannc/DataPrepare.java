package dpannc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataPrepare {
    public static void main(String[] args) {
        MNISTcsv2txt();        
    }

    public static void MNISTcsv2txt() {
        String nameSource = "fashionMNIST_60K_784D";
        String nameTarget = nameSource;
        Path filepathSource = Paths.get("app/resources/FashionMNIST", nameSource + ".csv");
        Path filepathTarget = Paths.get("app/resources/FashionMNIST", nameTarget + ".txt");
        String[] label = new String[]{"Top", "Trouser", "Pullover", "Dress", "Coat", "Sandal", "Shirt", "Sneaker", "Bag", "Boot"};
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
        } catch(Exception e) {
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
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
