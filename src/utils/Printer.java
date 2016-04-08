package utils;

/**
 * This is a class containing printing functions
 * in human-readable format from various kinds of data.
 * 
 * @author Joonseok Lee 
 * @author Mingxuan Sun 
 * @since 2012. 4. 20
 * @version 1.1
 */
public class Printer {
	/**
	 * Print each element in a double array.
	 * 
	 * @param A The array to print
	 */
	public static void printArray(double[] A) {
		for(int i = 0; i < A.length; i++){
			System.out.print(A[i] + "\t");
		}
	}

	/**
	 * Print each element in an integer array
	 * 
	 * @param A The array to print
	 */
	public static void printArray(int[] A) {
		for(int i = 0; i < A.length; i++){
			System.out.print(A[i] + "\t");
		}
	}

	/**
	 * Print each element in a 2-D double matrix.
	 * 
	 * @param A The array to print
	 */
	public static void printArray(double[][] A) {
		for(int i = 0; i < A.length; i++){
			printArray(A[i]);
			System.out.println();
		}
	}

	/**
	 * Print each element in a 3-D double matrix.
	 * 
	 * @param A The array to print
	 */
	public static void printArray(double[][][] A) {
		for(int i = 0; i < A.length; i++){
			printArray(A[i]);
			System.out.println();
		}
	}

	/**
	 * Convert time in milliseconds to human-readable format.
	 * 
	 * @param msType The time in milliseconds
	 * @return a human-readable string version of the time
	 */
	public static String printTime(long msType) {
		long original = msType;
		int ms = (int) (msType % 1000);

		original = original / 1000;
		int sec = (int) (original % 60);

		original = original / 60;
		int min = (int) (original % 60);

		original = original / 60;
		int hr = (int) (original % 24);

		original = original / 24;
		int day = (int) original;

		if (day > 1) {
			return String.format("%d days, %02d:%02d:%02d.%03d", day, hr, min, sec, ms);
		}
		else if (day > 0) {
			return String.format("%d day, %02d:%02d:%02d.%03d", day, hr, min, sec, ms);
		}
		else {
			return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
		}
	}
}
