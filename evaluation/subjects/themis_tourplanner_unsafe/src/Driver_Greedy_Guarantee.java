import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import com.graphhopper.GraphHopper;
import com.graphhopper.tour.Matrix;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.Greedy;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.nio.ByteBuffer;
import java.util.stream.Stream;
import java.util.TreeSet;
import java.util.List;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.Greedy;

import java.io.PrintWriter;
import java.lang.NumberFormatException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;

/* Side-Channel not for password but to check whether username exists in system. */
public class Driver_Greedy_Guarantee {

    /* Maximum number of different observations. */
    public final static int K = 2;

    /* Minimum distance between clusters. */
    public final static double epsilon = 1.0;

    /* Cluster Algorithm */
    public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

    ////////////////////////////////////////////////////////////////////////

    /* 25 valid points/cities */
    public static String[] validPointNames = { "Boston", "Worcester", "Taunton", "Lowell", "Brockton", "Revere",
            "Peabody", "Weymouth", "Springfield", "Newton", "Quincy", "Lynn", "Somerville", "Plymouth", "Fall River",
            "Malden", "Waltham", "Brookline", "New Bedford", "Lawrence", "Cambridge", "Framingham", "Medford",
            "Chicopee", "Haverhill" };
    public static int numberOfCities = validPointNames.length;
    public static Map<String, String> validPoints = new HashMap<>();
    static {
        validPoints.put("Boston", "<42.3604823,-71.0595678>");
        validPoints.put("Worcester", "<42.2625932,-71.8022934>");
        validPoints.put("Springfield", "<42.1014831,-72.589811>");
        validPoints.put("Lowell", "<42.6334247,-71.3161718>");
        validPoints.put("Cambridge", "<42.3750997,-71.1056157>");
        validPoints.put("New Bedford", "<41.6362152,-70.934205>");
        validPoints.put("Brockton", "<42.0834335,-71.0183787>");
        validPoints.put("Quincy", "<42.2528772,-71.0022705>");
        validPoints.put("Lynn", "<42.466763,-70.9494939>");
        validPoints.put("Fall River", "<41.7010642,-71.1546367>");
        validPoints.put("Newton", "<42.3370414,-71.2092214>");
        validPoints.put("Lawrence", "<42.7070354,-71.1631137>");
        validPoints.put("Somerville", "<42.3875968,-71.0994968>");
        validPoints.put("Framingham", "<42.2792625,-71.416172>");
        validPoints.put("Haverhill", "<42.7777829,-71.0767724>");
        validPoints.put("Waltham", "<42.3756401,-71.2358004>");
        validPoints.put("Malden", "<42.4250964,-71.066163>");
        validPoints.put("Brookline", "<42.3317642,-71.1211635>");
        validPoints.put("Plymouth", "<41.9584367,-70.6672577>");
        validPoints.put("Medford", "<42.4184296,-71.1061639>");
        validPoints.put("Taunton", "<41.900101,-71.0897675>");
        validPoints.put("Chicopee", "<42.1487043,-72.6078672>");
        validPoints.put("Weymouth", "<42.2212188,-70.9391625>");
        validPoints.put("Revere", "<42.4084302,-71.0119948>");
        validPoints.put("Peabody", "<42.5278731,-70.9286609>");
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        DummyRequest[] requests = new DummyRequest[K];

        // Read all inputs.
        try (FileInputStream fis = new FileInputStream(args[0])) {
            byte[] values = new byte[1];

            for (int i = 0; i < K; i++) {
                if (fis.read(values) == -1) {
                    throw new RuntimeException("Not enough data!");
                }
                int n = values[0] % numberOfCities;
                n = (n < 0) ? n + numberOfCities : n;
                System.out.println("n_" + i + ": " + n);

                /* Read cities for request. */
                String[] points = new String[n];
                int j = 0;
                while (fis.read(values) != -1 && j < n) {
                    int cityId = values[0] % numberOfCities;
                    cityId = (cityId < 0) ? cityId + numberOfCities : cityId;
                    points[j] = validPoints.get(validPointNames[cityId]);
                    j++;
                }
                if (j < n) {
                    throw new RuntimeException("Not enough data!");
                }

                requests[i] = new DummyRequest();
                requests[i].setAttribute("point", points);
            }

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        // Prepare application.
        String matrixLocation = "./data/matrix.csv";
        String graphHopperDirectory = "./data/massachusetts-latest.osm-gh";
        Matrix matrix = null;
        try {
            matrix = Matrix.readCsv(new File(matrixLocation));
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        GraphHopper gh = new GraphHopper();
        gh.load(graphHopperDirectory);
        TourServlet ts = new TourServlet(matrix, gh, false);

        long[] observations = new long[K];
        Mem.clear(true);
        for (int i = 0; i < K; i++) {
            Mem.clear(false);
            DummyResponse res = new DummyResponse();
            try {
                ts.doGet(requests[i], res);
            } catch (ServletException | IOException e) {
            }
            observations[i] = res.getResponseLength();
        }
        System.out.println("observations: " + Arrays.toString(observations));

        // Start of research
        // Calculate analytics, Nathan
        double analytics = Math.abs(observations[0] - observations[1]);
        long analyticsLong = (long) analytics;
        Kelinci.addCost(analyticsLong);

        String dirPath = "./log/log_30min_1/";

        // Log Everything, Alex
        String logPath = "Log.txt";
        String data = Double.toString(analytics);
        writeToLog(logPath, data, dirPath, true);

        // Read Everything from Unique file, Alex
        String uniqueLogPath = "Unique_Log.txt";
        SortedSet<Double> uniqueValues = readDoubleSetLog(dirPath + uniqueLogPath);

        // Read Test Log file, Alex
        String countLog = "Count_Log.txt";
        Long count = readLongLog(dirPath + countLog);
        count += 1;
        writeToLog(countLog, Long.toString(count), dirPath, false);

        // Size threshold -- should be set to 10, but for this subject there is only 3
        // unique values
        int min_num_tail = 15;
        int threshold = 10;

        String testPassedLog = "Test_Passed_Log.txt";

        if (!uniqueValues.contains(analytics)) {
            uniqueValues.add(analytics);
            writeToLog(uniqueLogPath, Double.toString(analytics), dirPath, true);

            // If we found a new unique value, we have more than 15 unique values, and the
            // size of unique values is divisble by 5.
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

    /* Read Log File */
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

    /* Read Log File */
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

    public static Integer expTest(int threshold, SortedSet<Double> sortedSet) {
        int maxNumTailSamples = Math.min(sortedSet.size(), 50);

        // NumTailSamples will be given and at most will be 50.
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
