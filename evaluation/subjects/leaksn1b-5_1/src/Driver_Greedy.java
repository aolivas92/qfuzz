import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
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

    int public_guess;
    int[] secrets = new int[K];

    // Read all inputs.
    try (FileInputStream fis = new FileInputStream(args[0])) {
      byte[] bytes = new byte[Integer.BYTES];

      if (fis.read(bytes) < 0) {
        throw new RuntimeException("Not enough input data...");
      }
      public_guess = Math.floorMod(ByteBuffer.wrap(bytes).getInt(), (int) Math.pow(2, 30));

      for (int i = 0; i < secrets.length; i++) {
        if (fis.read(bytes) < 0) {
          throw new RuntimeException("Not enough input data...");
        }
        secrets[i] = Math.floorMod(ByteBuffer.wrap(bytes).getInt(), (int) Math.pow(2, 28));
      }

    } catch (IOException e) {
      System.err.println("Error reading input");
      e.printStackTrace();
      return;
    }

    System.out.println("public_guess=" + public_guess);
    for (int i = 0; i < secrets.length; i++) {
      System.out.println("secret" + i + "=" + secrets[i]);
    }

    long[] observations = new long[K];
    Mem.clear(true);
    for (int i = 0; i < K; i++) {
      Mem.clear(false);
      leaks_n1s.leaks_n1s(secrets[i], public_guess);
      observations[i] = Mem.instrCost;
    }
    System.out.println("observations: " + Arrays.toString(observations));

    PartitionSet clusters = PartitionSet.createFromObservations(epsilon, observations, clusterAlgorithm);
    Kelinci.setObserverdClusters(clusters.getClusterAverageValues(), clusters.getMinimumDeltaValue());

    // Calculate analytics, Nathan
    double analytics = Math.abs(observations[0] - observations[1]);

    // Log Everything, Alex
    String logPath = "./log/Log.txt";
    String data = Double.toString(analytics);
    appendToLog(logPath, data);

    // Read Everything from Unique file, Alex
    String uniqueLogPath = "./log/Unique_Log.txt";
    SortedSet<Double> uniqueValues = readDoubleSetLog(uniqueLogPath);

    if (!uniqueValues.contains(analytics)) {
      uniqueValues.add(analytics);
      appendToLog(uniqueLogPath, Double.toString(analytics));
    }

    // Size threshold -- should be set to 10, but for this subject there is only 3
    // unique values
    int threshold = 2;

    if (uniqueValues.size() > threshold) {
      if (expTest(uniqueValues, threshold)) {
        appendToLog(logPath, "-1");
      }
    }

    System.out.println("Done.");
  }

  /* Logging Algorithm */
  public static void appendToLog(String fileName, String data) {
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

  public static boolean expTest(SortedSet<Double> arr, int threshold) {
    double sum = 0;
    double mean = 0;
    double standardDeviation = 0;
    int i = 0;
    for (double item : arr) {
      if (i > threshold) {
        break;
      }
      sum += item;
      i++;
    }
    mean = sum / i;
    int j = 0;

    for (double item : arr) {
      if (j > threshold) {
        break;
      }
      standardDeviation += Math.sqrt(Math.pow(item - mean, 2));
      j++;
    }
    return standardDeviation / mean > 1;
  }
}
