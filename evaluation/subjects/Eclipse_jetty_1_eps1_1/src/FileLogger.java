import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class FileLogger {
    private final String fileName;

    public FileLogger(String fileName) {
        this.fileName = fileName;
    }

    public void appendToLog(long[] newData) {
        try {
            // Check if file exists, if not create it
            Path path = Paths.get(this.fileName);
            if (!Files.exists(path)) {
                createEmptyLog();
                // TODO: Throw an error that the file wasn't created successfully
                // if (!fileCreated) {
                // }
            }

            // Open file and write message
            FileWriter writer = new FileWriter(this.fileName, true); // (file name, append)
            PrintWriter out = new PrintWriter(writer);
            out.print("[");
            for (int i = 0; i < newData.length; i++) {
                out.print(newData[i] + ", ");
            }
            out.print("]");
            out.println();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createEmptyLog() {
        try {
            FileWriter writer = new FileWriter(this.fileName);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLogFile() {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader (this.fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Error";
    }
}