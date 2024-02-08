import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class JsonFileHandler {
    // TODO: See if it's worth to implement the below, where you pass the file name and the caller can use an object.
    private String fileName;
    public JsonFileHandler(String fileName) {
        this.fileName = fileName;
    }
    
    // TODO: does the modification still need to be there?
    // TODO: can we implement Gson to be able to implement this method better? It needs to be added as a dependicy.
    public static Boolean appendToJsonFile(String fileName, char modification, String newData) {
        // Check if file exists, if not create it
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            Boolean fileCreated = createEmptyJsonFile(fileName);
            if (!fileCreated) {
                // TODO: Throw an error that the file wasn't created successfully
                return false;
            }
        }
        
        try {
            // Read existing JSON file
            StringBuilder jsonStringBuilder = new StringBuilder();
            try (Scanner scanner = new Scanner(new File(fileName))) {
                while (scanner.hasNextLine()) {
                    jsonStringBuilder.append((scanner.nextLine()));
                }
            }
            
            // Parse existing JSON
            String jsonData = jsonStringBuilder.toString().trim();
            if (!jsonData.isEmpty()) {
                jsonData = jsonData.substring(0, jsonData.length() - 1);
                if (jsonData.length() > 1) {
                    jsonData += ", ";
                }
            }
            
            // Append new data
            jsonData += "\"" + newData + "\"]";
            
            // Write back to file
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(jsonData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    private static boolean createEmptyJsonFile(String fileName) {
        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write("{}"); // Write an empty JSON Object
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String readJsonFile(String fileName) {
        try {
            // Read from file
            Scanner scanner = new Scanner(Paths.get(fileName));
            StringBuilder jsonStringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                jsonStringBuilder.append(scanner.nextLine());
            }
            scanner.close();
            return jsonStringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}