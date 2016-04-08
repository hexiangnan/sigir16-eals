package utils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class StopwordsFilter {

	private static HashSet<String> stopwords = new HashSet<String>();
	private static boolean isInitialized = false;
	
	public static void init(String stopwordsFile) throws IOException {
		if (!isInitialized) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(stopwordsFile)));
			
			String line;
			while ((line = reader.readLine()) != null) {
				String stopword = line.trim().toLowerCase();
				stopwords.add(stopword);
				stopword = stopword.replaceAll("[^\\w]", ""); // Add the processed version.
				stopwords.add(stopword);
			}
			stopwords.add("\t");
			reader.close();
			isInitialized = true;
		}
	}
	
	/**
	 * Check whether the input word is a stopword.
	 * @param word
	 * @return
	 */
	public static boolean isStopword(String word) {
		return stopwords.contains(word.toLowerCase());
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		StopwordsFilter.init("lib/stopwords.txt");
		
		System.out.println(isStopword("the"));
	}

}
