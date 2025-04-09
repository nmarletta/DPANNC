package dpannc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataPrepare {
    public static void main(String[] args) {
        
    }

    public static void MNISTcsv2txt() {
        String nameSource = "fashion-mnist_train";
        Path filepathSource = Paths.get("resources/FashionMNIST", nameSource + ".csv");
        String nameTarget = nameSource;
        Path filepathTarget = Paths.get("resources/FashionMNIST", nameTarget + ".txt");
        String[] label = new String[]{"Top", "Trouser", "Pullover", "Dress", "Coat", "Sandal", "Shirt", "Sneaker", "Bag", "Boot"};
        int[] id = new int[10];
        try (FileWriter writer = new FileWriter(filepathTarget.toAbsolutePath().toString());
        BufferedReader reader = new BufferedReader(new FileReader(filepathSource.toFile()))) {
            reader.readLine();
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
}
