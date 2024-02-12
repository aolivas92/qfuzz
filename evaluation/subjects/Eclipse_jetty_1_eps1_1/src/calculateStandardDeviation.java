
public class calculateStandardDeviation {
    public static void calculateStandardDeviation() {
    }

    public double calculateStandardDeviation(long[] array) {
        // get the sum of array
        long sum = 0;
        for (long i : array) {
            sum += i;
        }

        // get the mean of array
        int length = array.length;
        double mean = (double) sum / length;

        // calculate the standard deviation
        double standardDeviation = 0.0;
        for (long num : array) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }
}