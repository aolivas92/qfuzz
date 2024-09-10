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

/*
 * Created a H2 database called Tomcat with USERSm ROLES, and USERs_ROLES
 * tables. Check http://www.h2database.com/html/tutorial.html for creating a db.
 * To start db: cd h2/bin, and type: java -jar h2*.jar
 *
 */
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

		String public_user;
		String[] secret_users = new String[K];
		String pw = "1234";

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
			public_user = new String(tmp);

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
				secret_users[i] = new String(tmp);
			}

		} catch (IOException e) {
			System.err.println("Error reading input");
			e.printStackTrace();
			return;
		}

		System.out.println("public_actual = " + public_user);
		for (int i = 0; i < K; i++) {
			System.out.println("secrets_expected " + i + " = " + secret_users[i]);
		}
		
		DataSourceRealm DSR = new DataSourceRealm();
		/* Create Connection do database. */
		Connection dbConnection = null;

		// Ensure that we have an open database connection
		dbConnection = DSR.open();
		if (dbConnection == null) {
			// If the db connection open fails, return "not authenticated"
			System.out.println("DB connection failed...");
		}
		/* Prepare database. */
		Statement st;
		try {
			st = dbConnection.createStatement();
			st.execute("delete from users;");
			st.execute("insert into users (user_name, user_pass) values ('" + public_user + "', '" + pw + "');");
		} catch (Exception e) {
			System.out.println("Could not insert user in the table...");
			throw new RuntimeException(e);
		}

		long[] observations = new long[K];
		Mem.clear(true);
		for (int i = 0; i < K; i++) {
			Mem.clear(false);
			DSR.authenticate_unsafe(dbConnection, secret_users[i], pw);
			observations[i] = Mem.instrCost;
		}
		System.out.println("observations: " + Arrays.toString(observations));



		/* Clean database. */
		try {
			st.execute("delete from users;");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			DSR.close(dbConnection);
		}
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
      if (uniqueValues.size() >= min_num_tail && uniqueValues.size() % 5 == 0 && expTest(threshold, uniqueValues) > 0) {
        writeToLog(logPath, "-1", dirPath, true);

        long currTime = System.nanoTime();
        String time = String.valueOf(currTime);
        String testInfo = count + " " + uniqueValues.size() + " " + time;
        writeToLog(testPassedLog, testInfo, dirPath, true);

      }
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
