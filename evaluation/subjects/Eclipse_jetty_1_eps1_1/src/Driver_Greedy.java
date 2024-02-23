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
  public final static double epsilon = 1.0;

  /* Cluster Algorithm */
  public static PartitionAlgorithm clusterAlgorithm = new Greedy(false);

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Expects file name as parameter");
      return;
    }

    int numberOfVariables = K + 1; // how many variables
    int max_password_length = 16; // bytes

    // Read all inputs.
    List<Character> values = new ArrayList<>();
    try (FileInputStream fis = new FileInputStream(args[0])) {
      byte[] bytes = new byte[Character.BYTES];
      int i = 0;
      while ((fis.read(bytes) != -1) && (i < max_password_length * numberOfVariables)) {
        char value = ByteBuffer.wrap(bytes).getChar();
        int val = (((int) value) % 58) + 65;
        values.add((char) val);
        i++;
      }
    } catch (IOException e) {
      System.err.println("Error reading input");
      e.printStackTrace();
      return;
    }
    if (values.size() < numberOfVariables) {
      throw new RuntimeException("Too less data...");
    }

    int m = values.size() / numberOfVariables;

    // Read public.
    List<Character> public_lst = new ArrayList<>();
    int z = 0;
    for (int i = 0; i < m; i++) {
      // if(values.get(i) >= 'a' && values.get(i) <= 'z')
      // {
      // public_lst.add(values.get(i));
      // z += 1;
      // }
      public_lst.add(values.get(i));
    }
    int pub_len = public_lst.size();
    char[] public_arr = new char[pub_len];
    for (int i = 0; i < pub_len; i++)
      public_arr[i] = public_lst.get(i);
    String s2 = new String(public_arr);
    System.out.println("public" + "=" + s2);

    List<String> s1 = new ArrayList<>();
    // Read secret1.
    for (int j = 0; j < K; j++) {
      z = 0;
      List<Character> secret1_lst = new ArrayList<>();
      for (int i = 0; i < m; i++) {
        // if(values.get(i+m*(j+1)) >= 'a' && values.get(i+m*(j+1)) <= 'z')
        // {
        // secret1_lst.add(values.get(i+m*(j+1)));
        // z += 1;
        // }
        secret1_lst.add(values.get(i + m * (j + 1)));
      }
      int sec_len = secret1_lst.size();
      char[] secret1_arr = new char[sec_len];
      for (int i = 0; i < sec_len; i++)
        secret1_arr[i] = secret1_lst.get(i);
      String secret = new String(secret1_arr);
      s1.add(secret);
      System.out.println("secret" + j + "=" + secret);
    }

    long[] observations = new long[K];
    Mem.clear(true);
    for (int i = 0; i < K; i++) {
      Mem.clear(false);
      boolean result1 = Credential.stringEquals_original(s1.get(i), s2);
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
