import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.SortedSet;

import edu.cmu.sv.kelinci.Kelinci;
import edu.cmu.sv.kelinci.Mem;
import edu.cmu.sv.kelinci.quantification.PartitionAlgorithm;
import edu.cmu.sv.kelinci.quantification.PartitionSet;
import edu.cmu.sv.kelinci.quantification.Greedy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.NumberFormatException;
import java.io.PrintWriter;

public class Driver_Greedy {

    /* Maximum number of different observations. */
    public final static int K = 2;

    /* Minimum distance between clusters. */
    public final static double epsilon = 1.0;

    /* Cluster Algorithm */
    public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

    ////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Expects file name as parameter");
            return;
        }

        BigInteger public_base = null;
        BigInteger public_modulus = null;
        BigInteger[] secret_exponents = new BigInteger[K];

        byte[][] secret_bytes = new byte[K][];
        byte[] public_base_bytes = null;
        byte[] public_modulus_bytes = null;

        int maxNumVal = (K + 2) * Integer.BYTES;

        /* Read all values. */
        byte[] bytes;
        try (FileInputStream fis = new FileInputStream(args[0])) {

            // Determine size of byte array.
            try {
                int fileSize = Math.toIntExact(fis.getChannel().size());
                bytes = new byte[Math.min(fileSize, maxNumVal)];
            } catch (ArithmeticException e) {
                bytes = new byte[maxNumVal];
            }

            if (bytes.length < (K + 2)) {
                throw new RuntimeException("too less data");
            } else {
                fis.read(bytes);
            }
        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
            return;
        }

        int m = bytes.length / (K + 2);

        public_base_bytes = Arrays.copyOfRange(bytes, 0, m);
        public_modulus_bytes = Arrays.copyOfRange(bytes, m, 2 * m);
        for (int i = 0; i < K; i++) {
            secret_bytes[i] = Arrays.copyOfRange(bytes, (i + 2) * m, (i + 2 + 1) * m);
        }

        /* Use only positive values, first value determines the signum. */
        if (public_base_bytes[0] < 0) {
            public_base_bytes[0] = (byte) (public_base_bytes[0] * (-1) - 1);
        }
        if (public_modulus_bytes[0] < 0) {
            public_modulus_bytes[0] = (byte) (public_modulus_bytes[0] * (-1) - 1);
        }
        for (int i = 0; i < K; i++) {
            if (secret_bytes[i][0] < 0) {
                secret_bytes[i][0] = (byte) (secret_bytes[i][0] * (-1) - 1);
            }
        }

        /* We do not care about the bit length of the public values. */
        public_base = new BigInteger(public_base_bytes);
        public_modulus = new BigInteger(public_modulus_bytes);
        // Ensure that modulus is not zero.
        if (public_modulus.equals(BigInteger.ZERO)) {
            public_modulus = BigInteger.ONE;
        }

        /* Ensure secrets have same bit length */
        int smallestBitLength = Integer.MAX_VALUE;
        for (int i = 0; i < K; i++) {
            secret_exponents[i] = new BigInteger(secret_bytes[i]);

            /* Determine smallest bitlength. */
            int bitLength = (secret_exponents[i].equals(BigInteger.ZERO) ? 1 : secret_exponents[i].bitLength());
            if (bitLength < smallestBitLength) {
                smallestBitLength = bitLength;
            }
        }
        for (int i = 0; i < K; i++) {
            int bitLength = (secret_exponents[i].equals(BigInteger.ZERO) ? 1 : secret_exponents[i].bitLength());

            if (bitLength != smallestBitLength) {
                /*
                 * Trim bigger number to smaller bit length and ensure there is the 1 in the
                 * beginning of the bit
                 * representation, otherwise the zero would be trimmed again by the BigInteger
                 * constructor and hence it
                 * would have a smaller bit length.
                 */
                String bitStr = secret_exponents[i].toString(2);
                bitStr = "1" + bitStr.substring(bitLength - smallestBitLength + 1);
                secret_exponents[i] = new BigInteger(bitStr, 2);
            }
        }

        System.out.println("public_base=" + public_base);
        System.out.println("public_base.bitlength=" + public_base.bitLength());
        System.out.println("public_modulus=" + public_modulus);
        System.out.println("public_modulus.bitlength=" + public_modulus.bitLength());

        for (int i = 0; i < K; i++) {
            System.out.println("secret" + i + "_exponent=" + secret_exponents[i]);
            System.out.println("secret" + i + "_exponent.bitlength=" + secret_exponents[i].bitLength());
            System.out.println("secret" + i + "_exponent=" + secret_exponents[i].toString(2));
        }

        long[] observations = new long[K];
        Mem.clear(true);
        for (int i = 0; i < K; i++) {
            Mem.clear(false);
            GPT14.modular_exponentiation_inline_unsafe(public_base, secret_exponents[i], public_modulus);
            observations[i] = Mem.instrCost;
        }
        System.out.println("observations: " + Arrays.toString(observations));

        PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
        Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

        // Calculate analytics, Nathan
        double analytics = Math.abs(observations[0] - observations[1]);

        String dirPath = "./log/log_5min_1/";

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

                String testInfo = count + " " + uniqueValues.size();
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
