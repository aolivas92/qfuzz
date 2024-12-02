import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.SortedSet;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.Greedy;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.lang.NumberFormatException;
import java.io.BufferedReader;
import java.io.PrintWriter;

/* Side-Channel not for password but to check whether username exists in system. */
public class Driver_Greedy_Guarantee {

    /* Maximum number of different observations. */
    public final static int K = 2;

    /* Minimum distance between clusters. */
    public final static double epsilon = 1.0;

    /* Cluster Algorithm */
    public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

    ////////////////////////////////////////////////////////////////////////

    public static final int MAX_PASSWORD_LENGTH = 16; // bytes

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        String public_actual;
        String[] secrets_expected = new String[K];

        // Read all inputs.
        try (FileInputStream fis = new FileInputStream(args[0])) {

            /* Read public value for public_actual */
            byte[] bytes = new byte[MAX_PASSWORD_LENGTH];
            if (fis.read(bytes) < 0) {
                throw new RuntimeException("Not enough input data...");
            }
            char[] tmp = new char[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                tmp[i] = (char) (bytes[i] % 128);
            }
            public_actual = new String(tmp);

            /* Generate secrets. */
            for (int i = 0; i < K; i++) {

                bytes = new byte[MAX_PASSWORD_LENGTH];
                if (fis.read(bytes) < 0) {
                    throw new RuntimeException("Not enough input data...");
                }
                tmp = new char[bytes.length];
                for (int j = 0; j < bytes.length; j++) {
                    byte value = bytes[j];
                    /* each char value must be between 0 and 127 and a printable character */
                    char charValue = (char) (value % 128);
                    tmp[j] = charValue;
                }
                secrets_expected[i] = new String(tmp);
            }

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        System.out.println("public_actual = " + public_actual);
        for (int i = 0; i < K; i++) {
            System.out.println("secrets_expected " + i + " = " + secrets_expected[i]);
        }

        long[] observations = new long[K];
        Mem.clear(true);
        for (int i = 0; i < K; i++) {
            Mem.clear(false);
            PasswordEncoderUtils.equals_unsafe(secrets_expected[i], public_actual);
            observations[i] = Mem.instrCost;
        }
        System.out.println("observations: " + Arrays.toString(observations));

        // Start of research

        // Uncomment below to use clustering:
        // PartitionSet clusters = PartitionSet.createFromObservations(epsilon,
        // observations, clusterAlgorithm);
        // Kelinci.setObserverdClusters(clusters.getClusterAverageValues(),
        // clusters.getMinimumDeltaValue());

        // Calculate analytics
        double analytics = Math.abs(observations[0] - observations[1]);
        long analyticsLong = (long) analytics;

        // Uncomment below to use cost, don't forget to update run script
        Kelinci.addCost(analyticsLong);

        String dirPath = "./log/log_30min_2/";

        // Log Everything
        String logPath = "Log.txt";
        String data = Double.toString(analytics);
        writeToLog(logPath, data, dirPath, true);

        // Read Everything from Unique file
        String uniqueLogPath = "Unique_Log.txt";
        SortedSet<Double> uniqueValues = readDoubleSetLog(dirPath + uniqueLogPath);

        // Read Test Log file
        String countLog = "Count_Log.txt";
        Long count = readLongLog(dirPath + countLog);
        count += 1;
        writeToLog(countLog, Long.toString(count), dirPath, false);

        int min_num_tail = 15;
        int threshold = 10;

        String testPassedLog = "Test_Passed_Log.txt";

        // Unique Value Test
        if (!uniqueValues.contains(analytics)) {
            uniqueValues.add(analytics);
            writeToLog(uniqueLogPath, Double.toString(analytics), dirPath, true);

            // If we found a new unique value, we have more than 15 unique values, and the
            // size of unique values is divisble by 5, try the Exponential Test.
            if (uniqueValues.size() >= min_num_tail && uniqueValues.size() % 5 == 0
                    && expTest(threshold, uniqueValues) > 0) {
                writeToLog(logPath, "-1", dirPath, true);

                long currTime = System.nanoTime();
                String time = String.valueOf(currTime);
                String testInfo = count + " " + uniqueValues.size() + " " + time;
                writeToLog(testPassedLog, testInfo, dirPath, true);

            }
        }

        System.out.println("Done.");
    }

    /* Logging Algorithm */
    public static void writeToLog(String fileName, String data, String dirPath, Boolean append) {
        try {
            // Check if dir exists
            // Folder Path
            File directory = new File(dirPath);
            if (!directory.exists()) {
                directory.mkdirs();
                long startTime = System.nanoTime();
                String time = "0 " + "0 " + String.valueOf(startTime);
                writeToLog("Test_Passed_Log.txt", time, dirPath, false);
            }

            // Write to log file
            fileName = dirPath + fileName;
            FileWriter writer = new FileWriter(fileName, append);
            PrintWriter out = new PrintWriter(writer);

            out.println(data);

            out.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Read Log File with a Set of Doubles */
    public static SortedSet<Double> readDoubleSetLog(String fileName) {
        SortedSet<Double> doubleSet = new TreeSet<>();

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

    /* Read Log File with Long values */
    public static long readLongLog(String fileName) {
        File file = new File(fileName);
        Long num = (long) -1;

        try {
            // If file doesn't exists, create one and return empty set
            if (!file.exists()) {
                file.createNewFile();
                // testLog: total count, if test passed, locationg passed, count after passed,
                // num Unique samples when passed.
                return 0;
            }

            // Read file if it exists
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                num = Long.parseLong(line);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return num;
    }

    /* Exponential Test */
    public static Integer expTest(int threshold, SortedSet<Double> sortedSet) {
        int maxNumTailSamples = Math.min(sortedSet.size(), 50);

        // NumTailSamples will be at most will be 50.
        for (int j = threshold; j <= maxNumTailSamples; j++) {
            // Sorted list of the cost difference that starts at j-1 to the end.
            List<Double> xTail = new ArrayList<>(sortedSet).subList(sortedSet.size() - j, sortedSet.size());

            // Do the exponential testing
            double m = xTail.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double st = Math.sqrt(xTail.stream().mapToDouble(val -> Math.pow(val - m, 2)).average().orElse(0));
            double cv = 0;
            if (m == 0) {
                cv = st / m;
                continue;
            }

            // If the cv is greater than 1 then break.
            if (cv > 1) {
                return -1;
            }
            // If j gets to the last sample then exp test has passed and return true.
            if (j == (maxNumTailSamples - threshold)) {
                return maxNumTailSamples;
            }
        }
        return -1; // Default return value
    }
}
