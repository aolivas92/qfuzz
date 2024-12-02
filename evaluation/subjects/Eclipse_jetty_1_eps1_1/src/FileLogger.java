import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class FileLogger {
    private final String fileName;

    public FileLogger(String fileName) {
        this.fileName = fileName;
    }

    // public void appendToLog(double newData) {
    //     try {
    //         // Check if file exists, if not create it
    //         Path path = Paths.get(this.fileName);
    //         if (!Files.exists(path)) {
    //             createEmptyLog();
    //             // TODO: Throw an error that the file wasn't created successfully
    //             // if (!fileCreated) {
    //             // }
    //         }

    //         // Open file and write message
    //         FileWriter writer = new FileWriter(this.fileName, true); // (file name, append)
    //         PrintWriter out = new PrintWriter(writer);
    //         out.print("[");
    //         // for (int i = 0; i < newData.length; i++) {
    //         //     out.print(newData[i] + ", ");
    //         // }
    //         out.print(newData + ", ");
    //         out.print("]");
    //         out.println();
    //         out.close();
    //         writer.close();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    /* Logging Algorithm */
    public void appendToLog(String fileName, String data) {
        try {
        FileWriter writer = new FileWriter(fileName, true);
        PrintWriter out = new PrintWriter(writer);

        out.println(data);
        
        out.close();
        writer.close();
        } catch (IOException e) {
        e.printStackTrace();
        }
    }

    /* Read Log File */
    public Set<Double> readDoubleSetLog(String fileName) {
        Set<Double> doubleSet = new HashSet<>();

        File file = new File(fileName);
  
        try {
            // If file doesn't exists, create one and return empty set
            if (!file.exists()) {
                file.createNewFile();
                return doubleSet;
            }

            // Read file if it exists
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                  double value = Double.parseDouble(line.trim());
                  doubleSet.add(value);
                } catch (NumberFormatException e) {
                  e.printStackTrace();
                }
            }
  
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
  
        return doubleSet;
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

    public Double[] readNumArray() {
        List<Double> numList = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.fileName));
            String line;
            // TODO: See if you need this? Just have one line
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                for (String token : tokens) {
                    try {
                        Double value = Double.parseDouble(token);
                        numList.add(value);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return numList.toArray(new Double[0]);
    }
}