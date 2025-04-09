package dpannc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DataPrepare {
    public static void main(String[] args) {

        //// divide each component in each vector by something
            // db.applyTransformation(str -> {
            //     String transformed = Arrays.stream(str.split(" "))
            //             .mapToDouble(Double::parseDouble)
            //             .map(v -> v / 2)
            //             .mapToObj(Double::toString)
            //             .collect(Collectors.joining(" "));
            //     return transformed;
            // }, table1);

            removeFirstLine();
        
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
    public static void removeFirstLine() {
        String nameSource = "english_2M_300D";
        String nameTarget = nameSource + "s";
        Path filepathSource = Paths.get("app/src/main/resources/fasttext", nameSource + ".txt").toAbsolutePath();
        Path filepathTarget = Paths.get("app/src/main/resources/fasttext", nameTarget + ".txt").toAbsolutePath();
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
