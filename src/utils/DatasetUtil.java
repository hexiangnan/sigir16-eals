package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import data_structure.SparseMatrix;
import utils.StopwordsFilter;
import data_structure.SparseVector;
/**
 * Represent each review.
 * @author HeXiangnan
 *
 */
class Vote {
	public String user;
	public String item;
	public double rating;
	public Integer time;
	public int wordCount;
	public String review;
	
	public Vote(String user, String item, double rating, int time, int wordCount, String review) {
		this.user = user;
		this.item = item;
		this.rating = rating;
		this.time = time;
		this.wordCount = wordCount;
		this.review = review;
	}
	
	/**
	 * Sort votes by the review time, small (old) -> large (recent)
	 * @param votes
	 * @return
	 */
	public static void sortByTime(ArrayList<Vote> votes) {
		Comparator<Vote> comparator = new Comparator<Vote> () {
			public int compare(Vote vote0, Vote vote1) {
				return vote0.time.compareTo(vote1.time);
			}
		};
		Collections.sort(votes, comparator);
	}
	
	@Override
	public String toString() {
		String line = String.format("%s %s %.1f %d %d %s", user, item, rating, time, wordCount, review);
		return line;
	}
}

public class DatasetUtil {

	private BufferedReader reader;
	
	public DatasetUtil() {
	}
	/*==============================================================================================
	 * Process datasets, e.g. converting to .votes file, 
	 * splitting(train, test, validation) and filtering dataset.
	 *==============================================================================================*/
	/**
	 * Convert the original Amazon datasets into votes file (originally provided by HFT, Recsys'13 paper)
	 * Input file format example:
	 * 		amazon_datasets/arts.txt
	 * Output file format:
	 * 	  	A list of quadruple of form (userID, itemID, rating, time), followed by #words of the review, 
	 *    	followed by the words themselves (lower-cased).
	 *    	See example of amazon_datasets/arts.votes
	 * @param inputfileDir Directory of input dataset.
	 * @param dataset Dataset name.
	 * @throws IOException 
	 */
	public void ConvertTxtToVotesFile(String inputfileDir, String dataset) 
			throws IOException {
		String inputfileName = inputfileDir + dataset + ".txt";
		String outputfileName = inputfileDir + dataset + ".votes";
		System.out.println("\nConverting to .votes file: " + inputfileName);
		
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfileName)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfileName));
		
		String line;
		String productId="", userId="", rating="", time="";
		int count = 0;
		while((line = reader.readLine()) != null) {
			if (line.contains(":")) {
				String[] segments = line.split(":");
				String linename = segments[0].trim();
				if (linename.equals("product/productId")) {
					productId = segments[1].trim();
				}
				if (linename.equals("review/userId")) {
					userId = segments[1].trim();
				}
				if (linename.equals("review/score")) {
					rating = segments[1].trim();
				}
				if (linename.equals("review/time")) {
					time = segments[1].trim();
				}
				if (linename.equals("review/text")) {
					String review_text = segments[1].trim();
					String parse_review_text = "";
					/*String[] review_words = parseSentence(review_text);
					for (String review_word : review_words) {
						review_word = review_word.toLowerCase();
						parse_review_text = parse_review_text + review_word + " ";
					}*/
					// Output to the votes file.
					writer.println(userId + " " + productId + " " + rating + " " + 
							time + " " + review_text.split(" ").length + " " + review_text);
					productId = userId = rating = time = "";
				}
			}
			if (count++ % 10000 == 0)
				System.out.print(".");
		}
		
		reader.close();
		writer.close();
	}
	
	/**
	 * Convert the original Yelp Challenge datasets into votes file.
	 * Input file format example:
	 * 		yelp_datasets/yelp_reviews_220K.json
	 * Output file format:
	 * 	  	A list of quadruple of form (userID, itemID, rating, time), followed by #words of the review, 
	 *    	followed by the words themselves (lower-cased).
	 *    	See example of amazon_datasets/arts.votes
	 * @param inputfileDir
	 * @param dataset
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws java.text.ParseException 
	 */
	public void ConvertJsonToVotesFile(String inputfileDir, String dataset) throws IOException, ParseException, java.text.ParseException {
		String inputfileName = inputfileDir + dataset + ".json";
		String outputfileName = inputfileDir + dataset + ".votes";
		System.out.println("\nConverting to .votes file: " + inputfileName);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfileName)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfileName));
		
		String line;
		JSONParser parser=new JSONParser();
		int count = 0;
		while ((line = reader.readLine()) != null) {
			JSONObject obj = (JSONObject) parser.parse(line);
			String user_id = (String) obj.get("user_id");
			String business_id = (String) obj.get("business_id");
			String score = (Long) obj.get("stars") + ".0";
			// Parse time to unix time.
			String date = (String) obj.get("date");
			String time = date.replace("-", "") + "0800"; 
			DateFormat dfm = new SimpleDateFormat("yyyyMMddHHmm");
			Long unixtime = dfm.parse(time).getTime() / 1000;
			String review_text = (String) obj.get("text");
			review_text = review_text.replace("|", " ").replace("\n", " ");
			
			// Parse review words.
			String[] review_words = parseSentence((String) obj.get("text"));
			String parse_review_text = "";
			for (String review_word : review_words) {
				parse_review_text = parse_review_text + review_word.toLowerCase() + " ";
			}
			// Output to the .votes file.
			writer.println(user_id + " " + business_id + " " + score + " " + 
					unixtime + " " + review_words.length + " " + parse_review_text);
			//writer.println(user_id + "|" + business_id + "|" + score + "|" + 
				//unixtime + "|" + review_text);
			if (count++ % 10000 == 0)
				System.out.print(".");
		}
		
		System.out.println("#reviews: " + count);
		reader.close();
		writer.close();
	}
	
	/**
	 * Convert the original Yelp Challenge datasets into .raw file for lexicon construction.
	 * The .raw data is used by the tool thuir-sentires.jar.
	 * The format is <DOC>review_text</DOC>
	 * @param inputfileDir
	 * @param dataset
	 * @throws IOException
	 * @throws ParseException
	 * @throws java.text.ParseException
	 */
	public void ConvertJsonToRawFile(String inputfileDir, String dataset) throws IOException, ParseException, java.text.ParseException {
		String inputfileName = inputfileDir + dataset + ".json";
		String outputfileName = inputfileDir + dataset + ".raw";
		System.out.println("\nConverting to .raw file: " + inputfileName);
		
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfileName)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfileName));
		
		String line;
		JSONParser parser=new JSONParser();
		int count = 0;
		while ((line = reader.readLine()) != null) {
			JSONObject obj = (JSONObject) parser.parse(line);
			// Parse review words.
			String review = (String) obj.get("text");
			// Output to the .raw file.
			writer.println("<DOC>");
			writer.println(review);
			writer.println("</DOC>");
			if (count++ % 10000 == 0)
				System.out.print(".");
		}
		
		System.out.println("\nGenerated .raw file" + outputfileName);
		reader.close();
		writer.close();
	}
	
	/**
	 * Format of .rating file:
	 * Each line is: 	user_id\t item_id\t ratingScore
	 * @param inputfileDir
	 * @param dataset
	 * @throws IOException
	 */
	public void ConvertVotesToRatingFile(String inputfileDir, String dataset) throws IOException {
		String inputfileName = inputfileDir + dataset + ".votes";
		String outputfileName = inputfileDir + dataset + ".rating";
		System.out.println("\nConverting .votes to .rating file: " + inputfileName);
		
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfileName)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfileName));
		String line;
		while ((line = reader.readLine()) != null) {
			String user = parseVotesLine(line).user;
			String item = parseVotesLine(line).item;
			double rating = parseVotesLine(line).rating;
			// Output to the .raw file.
			writer.printf("%s\t%s\t%f\n",user,item,rating);
		}
		System.out.println("Generated .rating file" + outputfileName);
		reader.close();
		writer.close();
	}
	
	public void ConvertVotesToRawFile(String inputfileDir, String dataset) throws IOException {
		String inputfileName = inputfileDir + dataset + ".votes";
		String outputfileName = inputfileDir + dataset + ".raw";
		System.out.println("\nConverting .votes to .raw file: " + inputfileName);
		
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfileName)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfileName));
		String line;
		int count = 0;
		while ((line = reader.readLine()) != null) {
			String review = parseVotesLine(line).review;
			// Output to the .raw file.
			writer.println("<DOC>");
			writer.println(review);
			writer.println("</DOC>");
			if (count++ % 10000 == 0)
				System.out.print(".");
		}
		System.out.println("\nGenerated .raw file" + outputfileName);
		reader.close();
		writer.close();
	}
	
	
	public void ConvertTxtToRawFile(String inputfileDir, String dataset) throws IOException {
		String inputfileName = inputfileDir + dataset + ".txt";
		String outputfileName = inputfileDir + dataset + ".raw";
		System.out.println("\nConverting to .raw file: " + inputfileName);
		
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfileName)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfileName));
		
		String line;
		int count = 0;
		while((line = reader.readLine()) != null) {
			if (line.contains(":")) {
				String[] segments = line.split(":");
				String linename = segments[0].trim();
				if (linename.equals("review/text")) {
					String review = segments[1].trim();
					// Output to the raw file.
					writer.println("<DOC>");
					writer.println(review);
					writer.println("</DOC>");
				}
				if (count++ % 10000 == 0)
					System.out.print(".");
			}
		}
		
		reader.close();
		writer.close();
	}
	
	/**
	 * If a user has rated an item multiple times, using the recent one.
	 * @param inputDir
	 * @param dataset
	 * @throws IOException 
	 */
	public void RemoveDuplicateInVotesFile(String inputDir, String dataset) throws IOException {
		String inputFile = inputDir + dataset +".votes";
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
		String outputFile = inputDir + dataset + ".votes.noDuplicate";
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputFile));
		
		// Build map, where key is userID_itemID
		HashMap<String, ArrayList<Vote>> map = new HashMap<String, ArrayList<Vote>>();
		String line;
		int count = 0;
		while((line = reader.readLine()) != null) {
			Vote vote = parseVotesLine(line);
			String key = vote.user + "_" + vote.item;
			if (!map.containsKey(key)) {
				map.put(key, new ArrayList<Vote>());
			}
			map.get(key).add(vote);
			count ++;
		}
		
		// Write file.
		for (Entry<String, ArrayList<Vote>> it : map.entrySet()) {
			ArrayList<Vote> votes = it.getValue();
			if(it.getValue().size() > 1) { // write the latest vote.
				Vote.sortByTime(votes);
			}
			writer.println(votes.get(votes.size() - 1).toString());
		}
		
		System.out.printf("Before removing duplicates, #lines: %d, after: %d\n", count, map.size());
		System.out.printf("Generated file: %s\n", outputFile);
		reader.close();
		writer.close();
	}
	
	/**
	 * 
	 * @param inputfileDir
	 * @param K Number of test items to holdout for each user.
	 * @throws IOException 
	 */
	public void SplitVotesFileRandomAllButK(String inputfileDir, String dataset, int K) throws IOException {
		String inputfile = inputfileDir+"all/" + dataset + ".votes";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		System.out.printf("Spliting .votes file %s randomly All-But-%d\n", dataset, K);
		
		// Step 1: Build votes dictionary of each user.
		HashMap<String, ArrayList<Vote>> user_votes = new HashMap<String, ArrayList<Vote>>();
		String line;
		int numReviews = 0;
		while ((line = reader.readLine()) != null ) {
			Vote vote = parseVotesLine(line);
			if (vote != null) {
				if (!user_votes.containsKey(vote.user)) {
					user_votes.put(vote.user, new ArrayList<Vote>());
				}
				user_votes.get(vote.user).add(vote);
				numReviews ++;
				if (numReviews % 10000 == 0)	System.out.print(".");
			}
		}
		//System.out.print("\n\t #reviews: " + numReviews + ", #users: " + user_votes.size());
		reader.close();
		
		// Step 2: Write the train/valid/test file.
		//System.out.print("\n  2nd Step: Writing train/validation/split files.");
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		String outputfileTrain = inputfileDir + "train\\" + dataset + ".votes";
		String outputfileValid = inputfileDir + "validation\\" + dataset + ".votes";
		String outputfileTest =  inputfileDir + "test\\" + dataset + ".votes";
		PrintWriter writerTrain = new PrintWriter (new FileOutputStream(outputfileTrain));
		PrintWriter writerValid = new PrintWriter (new FileOutputStream(outputfileValid));
		PrintWriter writerTest  = new PrintWriter (new FileOutputStream(outputfileTest));
		
		int numTrain = 0, numValid = 0, numTest = 0;
		for (String user : user_votes.keySet()) {
			ArrayList<Vote> votes = user_votes.get(user);
			HashSet<Integer> samples = new HashSet<Integer>();
			// Generate for test set and valid set first.
			while (true) {
				if (samples.size() >= 2*K)	break;
				int sample = (int) (votes.size() * Math.random());
				if (!samples.contains(sample)) { 
					samples.add(sample);
					if (samples.size() <= K) { // add to test.
						writerTest.println(votes.get(sample));
						numTest ++;
					} else { // add to valid.
						writerValid.println(votes.get(sample));
						numValid ++;
					}
				}
			}
			// Add the remaining into training.
			for (int i = 0; i < votes.size(); i++) {
				if (!samples.contains(i)) {
					writerTrain.println(votes.get(i));
					numTrain ++;
				}
			}
		}
		
		//System.out.print("\n\t #train: " + numTrain + ", #valid: " + numValid + ", #test: " + numTest);
		reader.close();
		writerTrain.close();
		writerValid.close();
		writerTest.close();
		
		/*System.out.print("\n Write splitted files into: \n");
		System.out.println(outputfileTrain);
		System.out.println(outputfileValid);
		System.out.println(outputfileTest);*/
	}
	
	/**
	 * 
	 * @param inputfileDir
	 * @param K Number of test/validation items to holdout for each user. 
	 * @throws IOException 
	 */
	public void SplitVotesFileByTimeAllButK(String inputfileDir, String dataset, int K) throws IOException {
		String inputfile = inputfileDir + dataset + ".votes";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		System.out.printf("Spliting .votes file %s by time All-But-%d\n", dataset, K);
		
		// Step 1: Build votes dictionary of each user.
		HashMap<String, ArrayList<Vote>> user_votes = new HashMap<String, ArrayList<Vote>>();
		String line;
		int numReviews = 0;
		while ((line = reader.readLine()) != null ) {
			Vote vote = parseVotesLine(line);
			if (vote != null) {
				if (!user_votes.containsKey(vote.user)) {
					user_votes.put(vote.user, new ArrayList<Vote>());
				}
				user_votes.get(vote.user).add(vote);
				numReviews ++;
				if (numReviews % 10000 == 0)	System.out.print(".");
			}
		}
		//System.out.print("\n\t #reviews: " + numReviews + ", #users: " + user_votes.size());
		reader.close();
		
		// Step 2: Sort each user's votes.
		//System.out.print("\n  2nd Step: Sort each user's votes.");
		for (String user : user_votes.keySet()) {
			Vote.sortByTime(user_votes.get(user));
		}
		
		// Step 3: Write the train/valid/test file.
		//System.out.print("\n  3rd Step: Writing train/validation/split files.");
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		String outputfileTrain = inputfileDir + "train/" + dataset + ".votes";
		String outputfileValid = inputfileDir + "validation/" + dataset + ".votes";
		String outputfileTest =  inputfileDir + "test/" + dataset + ".votes";
		PrintWriter writerTrain = new PrintWriter (new FileOutputStream(outputfileTrain));
		PrintWriter writerValid = new PrintWriter (new FileOutputStream(outputfileValid));
		PrintWriter writerTest  = new PrintWriter (new FileOutputStream(outputfileTest));
		
		int numTrain = 0, numValid = 0, numTest = 0;
		for (String user : user_votes.keySet()) {
			ArrayList<Vote> votes = user_votes.get(user);
			int trainCount = votes.size() - 2 * K;
			int validCount = K;
			int testCount = K;
			for (int i = 0; i < votes.size(); i++) {
				if (i < trainCount)	{
					writerTrain.println(votes.get(i));
				} else if (i < trainCount + validCount) {
					writerValid.println(votes.get(i));
				} else {
					writerTest.println(votes.get(i));
				}
			}
			numTrain += trainCount;
			numValid += validCount;
			numTest  += testCount;
		}
		
		System.out.print("\n\t #train: " + numTrain + ", #valid: " + numValid + ", #test: " + numTest);
		reader.close();
		writerTrain.close();
		writerValid.close();
		writerTest.close();
	}
	
	/**
	 * Split the .vote Review dataset by reviewing time on each user basis.
	 * For each user, first select the oldest reviews as train, then randomly split valid and test. 
	 * If a user's review number (N) is less than 10, split as <N-2, 1, 1> for <train, valid, test)
	 * Output three files : train/dataset.votes, validation/dataset.votes, test/dataset.votes. 
	 * Three steps:
	 * 	1. Build votes dictionary of each user.
	 *  2. Sort each user's votes.
	 *  3. Write the train/valid/test file.
	 * @throws IOException 
	 */
	public void SplitVotesFileByTimePerUser(String inputfileDir, String dataset, 
			double trainRatio, double validRatio, double testRatio) throws IOException {
		if (trainRatio + validRatio + testRatio != 1.0) {
			System.out.println("Error - Sum of all train,valid,test ratios are not 1, can not split!");
			return ;
		}
		
		String inputfile = inputfileDir+"all/" + dataset + ".votes";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		System.out.print("\nSpliting .votes file (by review time per user): " + inputfile);
		
		// Step 1: Build votes dictionary of each user.
		HashMap<String, ArrayList<Vote>> user_votes = new HashMap<String, ArrayList<Vote>>();
		String line;
		int numReviews = 0;
		while ((line = reader.readLine()) != null ) {
			Vote vote = parseVotesLine(line);
			if (vote != null) {
				if (!user_votes.containsKey(vote.user)) {
					user_votes.put(vote.user, new ArrayList<Vote>());
				}
				user_votes.get(vote.user).add(vote);
				numReviews ++;
				if (numReviews % 10000 == 0)	System.out.print(".");
			}
		}
		System.out.print("\n\t #reviews: " + numReviews + ", #users: " + user_votes.size());
		reader.close();
		
		// Step 2: Sort each user's votes.
		System.out.print("\n  2nd Step: Sort each user's votes.");
		for (String user : user_votes.keySet()) {
			Vote.sortByTime(user_votes.get(user));
		}
		
		// Step 3: Write the train/valid/test file.
		System.out.print("\n  3rd Step: Writing train/validation/split files.");
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		String outputfileTrain = inputfileDir + "train/" + dataset + ".votes";
		String outputfileValid = inputfileDir + "validation/" + dataset + ".votes";
		String outputfileTest =  inputfileDir + "test/" + dataset + ".votes";
		PrintWriter writerTrain = new PrintWriter (new FileOutputStream(outputfileTrain));
		PrintWriter writerValid = new PrintWriter (new FileOutputStream(outputfileValid));
		PrintWriter writerTest  = new PrintWriter (new FileOutputStream(outputfileTest));
		
		int numTrain = 0, numValid = 0, numTest = 0;
		for (String user : user_votes.keySet()) {
			ArrayList<Vote> votes = user_votes.get(user);
			int trainCount, validCount, testCount;
			if (votes.size() < 3) {
				trainCount = votes.size();
				validCount = 0;
				testCount = 0;
			}
			if (votes.size() < 10) {
				trainCount = votes.size() - 2;
				validCount = 1;
				testCount = 1;
			} else {
				testCount = (int) (votes.size() * testRatio);
				validCount = (int) (votes.size() * validRatio);
				trainCount = votes.size() - testCount - validCount;
			}
			
			for (int i = 0; i < votes.size(); i++) {
				if (i < trainCount)	{
					writerTrain.println(votes.get(i));
				} else {
					if (i < trainCount + validCount)	writerValid.println(votes.get(i));
					else 	writerTest.println(votes.get(i));
				}
			}
			numTrain += trainCount;
			numValid += validCount;
			numTest  += testCount;
		}
		
		System.out.print("\n\t #train: " + numTrain + ", #valid: " + numValid + ", #test: " + numTest);
		reader.close();
		writerTrain.close();
		writerValid.close();
		writerTest.close();
		
		System.out.print("\n Write splitted files into: \n");
		System.out.println(outputfileTrain);
		System.out.println(outputfileValid);
		System.out.println(outputfileTest);
	}
	
	/**
	 * Only retain users whose number of reviews is not in the range of [min_reviews, max_reviews]
	 * @param inputfileDir
	 * @param dataset
	 * @param min_reviews
	 * @param max_reviews
	 * @throws IOException 
	 */
	public void FilterVotesFileByUsers(String inputfileDir, String dataset, int min_reviews, int max_reviews) throws IOException {
		String inputfile = inputfileDir + dataset  + ".votes";
		String outputfile= inputfileDir + dataset + "_u" + min_reviews + "_" + max_reviews + ".votes";
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		
		System.out.printf("Filtering reviews with range [%d, %d] reviews/user for %s \n", 
				min_reviews, max_reviews, dataset);
		// Step 1: count how many reviews per user.
		HashMap<String, Integer> map_user_count = new HashMap<String, Integer>();
		String line;
		while ((line = reader.readLine()) != null ) {
			String user_id = line.split(" ")[0];
			if (!map_user_count.containsKey(user_id)) {
				map_user_count.put(user_id, 0);
			}
			map_user_count.put(user_id, map_user_count.get(user_id) + 1);
		}
		reader.close();
		System.out.println("Before filtering, #users: " + map_user_count.size());
		
		// Step 2: output the new filtered file.
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfile));
		while ((line = reader.readLine())!= null) {
			String user_id = line.split(" ")[0];
			if (map_user_count.containsKey(user_id) && map_user_count.get(user_id) >= min_reviews &&
					map_user_count.get(user_id) <= max_reviews) {
				writer.println(line);
			} else {
				map_user_count.remove(user_id);
			}
		}
		reader.close();
		writer.close();
		System.out.println("After filtering, #users: " + map_user_count.size());
		System.out.println("Write the filtered file in: " + outputfile);
	}
	
	/**
	 * Filter a user if his/her number of reviews is less than the input threshold min_reviews.
	 * @param inputfileDir
	 * @param dataset
	 * @param min_reviews
	 * @throws IOException 
	 */
	public void FilterVotesFileByUsers(String inputfileDir, String dataset, int min_reviews) throws IOException {
		String inputfile = inputfileDir + dataset  + ".votes";
		String outputfile= inputfileDir + dataset + "_u" + min_reviews + ".votes";
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		
		System.out.println("Filtering " + inputfile + " with min_reviews per user: " + min_reviews);
		// Step 1: count how many reviews per user.
		HashMap<String, Integer> map_user_count = new HashMap<String, Integer>();
		String line;
		while ((line = reader.readLine()) != null ) {
			String user_id = line.split(" ")[0];
			if (!map_user_count.containsKey(user_id)) {
				map_user_count.put(user_id, 0);
			}
			map_user_count.put(user_id, map_user_count.get(user_id) + 1);
		}
		reader.close();
		System.out.println("Before filtering, #users: " + map_user_count.size());
		
		// Step 2: output the new filtered file.
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfile));
		while ((line = reader.readLine())!= null) {
			String user_id = line.split(" ")[0];
			if (map_user_count.containsKey(user_id) && map_user_count.get(user_id) >= min_reviews) {
				writer.println(line);
			} else {
				map_user_count.remove(user_id);
			}
		}
		reader.close();
		writer.close();
		System.out.println("After filtering, #users: " + map_user_count.size());
		System.out.println("Write the filtered file in: " + outputfile);
	}

	/**
	 * Filter an item if its number of reviews is less than the input threshold min_reviews.
	 * @param inputfileDir
	 * @param dataset
	 * @param min_reivews
	 * @throws IOException 
	 */
	public void FilterVotesFileByItems(String inputfileDir, String dataset, int min_reviews) throws IOException {
		String inputfile = inputfileDir + dataset + ".votes";
		String outputfile= inputfileDir + dataset + "_i" + min_reviews + ".votes";
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		
		System.out.println("Filtering " + inputfile + " with min_reviews per item: " + min_reviews);
		// Step 1: count how many reviews per item.
		HashMap<String, Integer> map_item_count = new HashMap<String, Integer>();
		String line;
		while ((line = reader.readLine()) != null ) {
			String item_id = line.split(" ")[1];
			if (!map_item_count.containsKey(item_id)) {
				map_item_count.put(item_id, 0);
			}
			map_item_count.put(item_id, map_item_count.get(item_id) + 1);
		}
		reader.close();
		System.out.println("Before filtering, #item: " + map_item_count.size());
		
		// Step 2: output the new filtered file.
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfile));
		while ((line = reader.readLine())!= null) {
			String item_id = line.split(" ")[1];
			if (map_item_count.containsKey(item_id) && map_item_count.get(item_id) >= min_reviews) {
				writer.println(line);
			} else {
				map_item_count.remove(item_id);
			}
		}
		reader.close();
		writer.close();
		System.out.println("After filtering, #items: " + map_item_count.size());
		System.out.println("Write the filtered file in: " + outputfile);
	}
	
	/**
	 * Check the user overlap of two votes datasets
	 * @param dir
	 * @param dataset1
	 * @param dataset2
	 * @throws IOException 
	 */
	public void checkOverlapUsers(String dir, String dataset1, String dataset2) throws IOException {
		String file1 = dir + dataset1 + ".votes";
		String file2 = dir + dataset2 + ".votes";
		int userIndex = 0; // the index of user in votes file.
		
		// Read users of dataset1
		HashSet<String> users1 = new HashSet<String>();
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));
		String line;
		int count=0;
		while ((line = reader.readLine()) != null) {
			if (count++ % 100000 == 0)	System.out.print(".");
			String user = line.split(" ")[userIndex];
			users1.add(user);
		}
		reader.close();
		System.out.println("");
		
		// Read users of dataset2
		HashSet<String> users2 = new HashSet<String>();
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file2)));
		while ((line = reader.readLine()) != null) {
			if (count++ % 100000 == 0)	System.out.print(".");
			String user = line.split(" ")[userIndex];
			users2.add(user);
		}
		System.out.println("");

		HashSet<String> intersection = new HashSet<String>(users1);
		intersection.retainAll(users2);
		System.out.printf("#overlap users of <%s, %s>: %d \t %.2f%%, %.2f%%\n", 
				dataset1, dataset2, intersection.size(), intersection.size()/ (users1.size()/100.0), 
				intersection.size()/ (users2.size()/100.0));
		reader.close();
	}
	
	/**
	 * Only retain top occurrence words of a review. 
	 * @param inputfileDir
	 * @param dataset
	 * @param maxWords The number of (top occurrence) words in the word dictionary.
	 * @throws IOException
	 * @throws LangDetectException 
	 */
	public void FilterVotesReviewsByWords(String inputfileDir, String dataset, int maxWords) 
			throws IOException {
		String inputfile = inputfileDir + dataset + ".votes";
		String outputfile = inputfileDir + dataset + "_w" + maxWords/1000 + "k.votes";
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		
		System.out.print("\nFiltering reviews by words: " + dataset);
		// Step 1: Build word dictionary.
		HashMap<String, Integer> map_word_id = new HashMap<String, Integer>();
		this.buildWordsDictionary(inputfile, map_word_id, maxWords);
		
		// Step 2: Write the filtered file.
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputfile));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] arr = line.split(" ");
			String filtered_review_text = "";
			int wordcount = 0;
			for (int i = 5; i < arr.length; i++) {
				String word = arr[i];
				if (map_word_id.containsKey(word)) {
					wordcount ++;
					filtered_review_text = filtered_review_text + word + " ";
				}
			}
			writer.printf("%s %s %s %s %d %s\n", 
					arr[0], arr[1], arr[2], arr[3], wordcount, filtered_review_text);
		}
		
		System.out.println("\nWrite the filtered file in: " + outputfile);
		writer.close();
		reader.close();
	}
	
	/**
	 * Write a matrix into file. 
	 * Format of each line: row_id [non-zero entryCount]: (col1, val1), (col2, val2) ... 
	 * @param matrix
	 * @param filename
	 * @throws IOException
	 */
	public static void writeMatrixToFile(SparseMatrix matrix, String filename) throws IOException {
		PrintWriter writer = new PrintWriter (new FileOutputStream(filename));
		int rowCount = matrix.length()[0];
		for (int i = 1; i < rowCount; i++) {
			ArrayList<Integer> indexList = matrix.getRowRef(i).indexList();
			String line;
			if (indexList.size() == 0) {
				line = String.format("%d [0]:\t", i);
			} else {
				line = String.format("%d [%d]:\t", i, indexList.size());
				for (int j : indexList) {
					line += String.format("(%d, %.4f)\t", j, matrix.getValue(i, j));
				}
			}
			writer.println(line);
		}
		writer.close();
	}
	
	
	/**
	 * Process the .lexicon file (generate by thuir-sentires.rar tool), and generate feature set.
	 * Select top features by descending order of number of opinions.
	 * 
	 * @param lexiconFile
	 * @param aspectRatio Percentage of top aspects to read. 
	 * @return
	 * @throws IOException
	 */
	static public HashMap<String, HashSet<String>> loadFeaturesFromLexiconFile(String lexiconFile, 
			double aspectRatio) throws IOException {
		HashMap<String, HashSet<String>> map_feature_opinion = 
				new HashMap<String, HashSet<String>>();
		
		// System.out.println("Loading features from lexicon file: " + lexiconFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lexiconFile)));
		String line;
		while ((line = reader.readLine()) != null) {
			String feature_opinion = line.split("\t")[1];
			String feature = feature_opinion.split("\\|")[0].replaceAll("!", "").trim();
			String opinion = feature_opinion.split("\\|")[1].trim();
			if (!map_feature_opinion.containsKey(feature)) {
				map_feature_opinion.put(feature, new HashSet<String>());
			}
			map_feature_opinion.get(feature).add(opinion);
		}
		System.out.println("Feature count in total: " + map_feature_opinion.size()); 
		
		// Select features by descending order of number of opinions.
		int aspectNum = (int) (map_feature_opinion.size() * aspectRatio);
		HashMap<String, Integer> map_feature_count = new HashMap<String, Integer>();
		for (Map.Entry<String, HashSet<String>> entry : map_feature_opinion.entrySet()) {
			map_feature_count.put(entry.getKey(), entry.getValue().size());
		}
		HashSet<String> topFeatures = new HashSet<String>(
				CommonUtils.TopKeysByValue(map_feature_count, aspectNum, null));
		Set<String> featureSet = new HashSet<String>(map_feature_opinion.keySet());
		for (String feature : featureSet) {
			if (!topFeatures.contains(feature)) {
				map_feature_opinion.remove(feature);
			}
		}
		
		reader.close();
		// System.out.println("# of features loaded: " + map_feature_opinion.size());
		return map_feature_opinion;
	}
	
	static public HashSet<String> loadFeaturesFromFeatureFile(String featureFile) 
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(featureFile)));
		HashSet<String> features = new HashSet<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] arr = line.trim().split("\t");
			if (arr != null && arr.length > 1) {
				features.add(arr[0]);
			}
		}
		reader.close();
		return features;
	}
	
	/**
	 * Load positive Features.
	 * @param lexiconFile
	 * @return
	 * @throws IOException
	 */
	static public HashMap<String, HashSet<String>> loadPosFeaturesFromLexiconFile(String lexiconFile, 
			double aspectRatio) throws IOException {
		HashMap<String, HashSet<String>> map_feature_opinion = 
				new HashMap<String, HashSet<String>>();
		
		// System.out.println("Loading features from lexicon file: " + lexiconFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lexiconFile)));
		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.contains("[1]"))	continue; // Only consider positive FO pairs.
			String feature_opinion = line.split("\t")[1];
			String feature = feature_opinion.split("\\|")[0].replaceAll("!", "").trim();
			String opinion = feature_opinion.split("\\|")[1].trim();
			if (!map_feature_opinion.containsKey(feature)) {
				map_feature_opinion.put(feature, new HashSet<String>());
			}
			map_feature_opinion.get(feature).add(opinion);
		}
		System.out.printf("Feature count in total: %d. ", map_feature_opinion.size()); 
		
		// Select features by descending order of number of opinions.
		int aspectNum = (int) (map_feature_opinion.size() * aspectRatio);
		HashMap<String, Integer> map_feature_count = new HashMap<String, Integer>();
		for (Map.Entry<String, HashSet<String>> entry : map_feature_opinion.entrySet()) {
			map_feature_count.put(entry.getKey(), entry.getValue().size());
		}
		HashSet<String> topFeatures = new HashSet<String>(
				CommonUtils.TopKeysByValue(map_feature_count, aspectNum, null));
		Set<String> featureSet = new HashSet<String>(map_feature_opinion.keySet());
		for (String feature : featureSet) {
			if (!topFeatures.contains(feature)) {
				map_feature_opinion.remove(feature);
			}
		}
		
		// Count number of F-O pairs.
		int count = 0;
		for (String feature : map_feature_opinion.keySet()) {
			count += map_feature_opinion.get(feature).size();
		}
		System.out.printf("Positive F-O pairs: %d. ", count);

		reader.close();
		return map_feature_opinion;
	}
	
	/**
	 * Filter FO pairs that do not occur in the training votes file.
	 */
	static public HashMap<String, ArrayList<String>> filterFOpairs(
			HashMap<String, ArrayList<String>> feature_opinions, String votesFile) throws IOException {
		HashSet<String> features = new HashSet<String>(feature_opinions.keySet());
		HashMap<String, ArrayList<String>> filteredFO = new HashMap<String, ArrayList<String>>();
		for (String feature : feature_opinions.keySet()) {
			filteredFO.put(feature, new ArrayList<String>());
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(votesFile)));
		String line;
		while ((line = reader.readLine()) != null) {
			Vote vote = parseVotesLine(line);
			// Find all features occurred in the review.
			HashSet<String> find_features = findFeaturesFromReview(features, vote.review);
			for (String feature : find_features) {
				// Find all opinions occurred in the review.
				HashSet<String> opinions = new HashSet<String>(feature_opinions.get(feature));
				opinions = findFeaturesFromReview(opinions, vote.review);
				for (String opinion : opinions) {
					filteredFO.get(feature).add(opinion);
					feature_opinions.get(feature).remove(opinion);
				}
			}
		}
		reader.close();
		// Count number of F-O pairs.
		int count = 0;
		for (String feature : filteredFO.keySet()) {
			count += filteredFO.get(feature).size();
		}
		System.out.println("Filtered F-O pairs: " + count);
		
		return filteredFO;
	}
	
	/*==============================================================================================
	 * Private and protected functions.
	 *==============================================================================================*/
	
	/** Build itemWordsMatrix and userWordsMatrix based on the input user, item and word dictionary.
	 * 
	 * @param trainFileName
	 * @param itemWordsMatrix
	 * @param userWordsMatrix
	 * @param map_item_id Dictionary of all items (id starts from 1)
	 * @param map_user_id Dictionary of all users (id starts from 1)
	 * @param map_word_id Dictionary of all words (id starts from 1)
	 * @throws IOException 
	 */
	public void buildWordsMatrix(String fileName, SparseMatrix itemWordsMatrix, 
			SparseMatrix userWordsMatrix, HashMap<String, Integer> map_item_id, 
			HashMap<String, Integer> map_user_id, HashMap<String, Integer> map_word_id) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] arr = line.split(" ");
			if (arr.length >= 4) {
				// Extract item, user and review words.
				int userID = map_user_id.get(arr[0]);
				int itemID = map_item_id.get(arr[1]);
				for (int i = 5; i < arr.length; i++) {
					String word = arr[i].trim();
					if (map_word_id.containsKey(word)) {
						int wordID = map_word_id.get(word);
						userWordsMatrix.setValue(userID, wordID, userWordsMatrix.getValue(userID, wordID) + 1);
						itemWordsMatrix.setValue(itemID, wordID, itemWordsMatrix.getValue(itemID, wordID) + 1);
					}
				}
			}
		}
		reader.close();
	}

	/**
	 * Build words dictionary (from votes file).
	 * Only consider English reviews.
	 * 
	 * @param fileName
	 * @param map_word_id Save the results of word dictionary.
	 * @param maxWords The maximum words in the dictionary (select top words). To disable the function, set it as 0.
	 * @throws IOException
	 * @throws LangDetectException 
	 */
	public void buildWordsDictionary(String fileName, HashMap<String, Integer> map_word_id, 
			int maxWords) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		
		// map from word to its number of occurrence.
		HashMap<String, Integer> map_word_count = new HashMap<String, Integer>();
		StopwordsFilter.init("lib/stopwords.txt");
		
		String line;
		int linecount=0;
		while ((line = reader.readLine()) != null) {
			Vote vote = parseVotesLine(line);
			if (vote!=null) {
				// Process review words.
				String review_text = vote.review.trim();
				// if (!LanguageDetector.isEnglish(review_text))  continue; // Filter nonEnglish reviews.
				for (String word : review_text.split(" ")) {
					if (StopwordsFilter.isStopword(word))	continue; // Filter stopwords.
					if (word.matches(".*\\d+.*"))	continue;// Filter word that contains digit.
					if (!map_word_count.containsKey(word)) {
						map_word_count.put(word, 0);
					}
					map_word_count.put(word, map_word_count.get(word) + 1);
				}
			}
			if (linecount % 10000 == 0) {
				System.out.print(".");
			}
			linecount++;
		}
		
		// System.out.print("\nBefore filtering, dictionary_size: " + map_word_count.size() +", after filtering: " + maxWords);
		
		// Use the most frequent maxWords as the word dictionary.
		List<Map.Entry<String, Integer>> sortedMap;
		if (maxWords > 0) {
			sortedMap = mostFrequentEntries(map_word_count, maxWords);
		} else {
			sortedMap = CommonUtils.SortMapByValue(map_word_count);
		}
		
		// Words are sorted by its number of occurrence. 
		for (Map.Entry<String, Integer> entity : sortedMap) {
			map_word_id.put(entity.getKey(), map_word_id.size());
		}	
		reader.close();
	}
	
	/**
	 * Build aspects matrix of the input .votes file, given the user/item/aspect dictionary.
	 * In this function, an aspect represents a F-O pair.
	 * @param votesFile
	 * @param map_user_id
	 * @param map_item_id
	 * @param map_aspect_id
	 * @param itemAspect
	 * @param userAspect
	 * @throws IOException
	 */
	static public void buildAspectsMatrix_FO(String votesFile, HashMap<String, Integer> map_user_id, 
			 HashMap<String, Integer> map_item_id,  HashMap<String, Integer> map_aspect_id, 
			 SparseMatrix itemAspect, SparseMatrix userAspect, 
			 HashMap<String, HashSet<String>> map_feature_opinions) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(votesFile)));
		HashSet<String> features = new HashSet<String>(map_feature_opinions.keySet());
		String line;
		while((line = reader.readLine()) != null) {
			Vote vote = parseVotesLine(line);
			int userId = map_user_id.get(vote.user);
			int itemId = map_item_id.get(vote.item);
			HashSet<String> find_features = findFeaturesFromReview(features, vote.review);
			for (String feature : find_features) {
				HashSet<String> find_opinions = findOpinionsFromReview(map_feature_opinions.get(feature), vote.review);
				for (String opinion: find_opinions) {
					String aspect = feature + "|" + opinion;
					int aspectId = map_aspect_id.get(aspect);
					itemAspect.setValue(itemId, aspectId, itemAspect.getValue(itemId, aspectId) + 1);
					userAspect.setValue(userId, aspectId, userAspect.getValue(userId, aspectId) + 1);
				}
			}
		}
		
		reader.close();
	}
	
	/**
	 * Find features from the review.
	 * @param features
	 * @param review
	 * @return
	 */
	private static HashSet<String> findFeaturesFromReview(HashSet<String> features, String review) {
		// A feature may contain at most 3 words.
		HashSet<String> grams = new HashSet<String>();
		grams.addAll(CommonUtils.StringToGramSet(review, 1));
		grams.addAll(CommonUtils.StringToGramSet(review, 2));
		grams.addAll(CommonUtils.StringToGramSet(review, 3));
		
		grams.retainAll(features);
		return grams;
	}
	
	private static HashSet<String> findOpinionsFromReview(HashSet<String> opinions, String review) {
		HashSet<String> grams = new HashSet<String>();
		grams.addAll(CommonUtils.StringToGramSet(review, 1));
		
		grams.retainAll(opinions);
		return grams;		
	}
	
	protected void statReviewsPerItem(String inputfileDir, String dataset) throws IOException {
		String inputfile = inputfileDir + dataset + ".votes";
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		
		// Build <item, count> dictionary.
		System.out.print(dataset);
		HashMap<String, Integer> map_item_count = new HashMap<String, Integer>();
		String line;
		while ((line = reader.readLine()) != null) {
			String item_id = line.split(" ") [1];
			if (!map_item_count.containsKey(item_id)) {
				map_item_count.put(item_id, 0);
				if (map_item_count.size() % 10000 == 0)
					System.out.print(".");
			}
			map_item_count.put(item_id, map_item_count.get(item_id) + 1);
		}
		
		// Revert the dictionary to <count, number of items>
		HashMap<Integer, Integer> map_count_items = new HashMap<Integer, Integer>();
		for (String item : map_item_count.keySet()) {
			int count = map_item_count.get(item);
			if (!map_count_items.containsKey(count)) 
				map_count_items.put(count, 0);
			map_count_items.put(count, map_count_items.get(count) + 1);
		}
		
		// Print map_count_items statistics.
		int itemCount = map_item_count.size();
		System.out.println("\nAll Items: " + itemCount);
		System.out.println("#Reviews: \t #Items: \t Percentage");
		ArrayList<Integer> counts = new ArrayList<Integer>(map_count_items.keySet());
		Collections.sort(counts);
		int count10 = 0;
		for (Integer count : counts) {
			int items = map_count_items.get(count);
			if (count < 10)
				System.out.printf ("%d \t\t %d \t\t %.4f \n", count, items, (double)items / itemCount * 100);
			else 
				count10 += items;
		}
		System.out.printf (">=10 \t\t %d \t\t %.4f \n", count10, (double)count10 / itemCount * 100);
		reader.close();
	}
	
	protected void statReviewsPerUser(String inputfileDir, String dataset) throws IOException {
		String inputfile = inputfileDir + dataset + ".votes";
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile)));
		
		// Build <user, count> dictionary.
		System.out.print(dataset);
		HashMap<String, Integer> map_user_count = new HashMap<String, Integer>();
		String line;
		while ((line = reader.readLine()) != null) {
			String user_id = line.split(" ") [0];
			if (!map_user_count.containsKey(user_id)) {
				map_user_count.put(user_id, 0);
				if (map_user_count.size() % 10000 == 0)
					System.out.print(".");
			}
			map_user_count.put(user_id, map_user_count.get(user_id) + 1);
		}
		
		// Revert the dictionary to <count, number of users>
		HashMap<Integer, Integer> map_count_users = new HashMap<Integer, Integer>();
		for (String user : map_user_count.keySet()) {
			int count = map_user_count.get(user);
			if (!map_count_users.containsKey(count)) 
				map_count_users.put(count, 0);
			map_count_users.put(count, map_count_users.get(count) + 1);
		}
		
		// Print map_count_users statistics.
		int userCount = map_user_count.size();
		System.out.println("\nAll Users: " + userCount);
		System.out.println("#Reviews: \t #Users: \t Percentage");
		ArrayList<Integer> counts = new ArrayList<Integer>(map_count_users.keySet());
		Collections.sort(counts);
		int count10 = 0;
		for (Integer count : counts) {
			int users = map_count_users.get(count);
			if (count < 10)
				System.out.printf ("%d \t\t %d \t\t %.4f \n", count, users, (double)users / userCount * 100);
			else 
				count10 += users;
		}
		System.out.printf (">=10 \t\t %d \t\t %.4f \n", count10, (double)count10 / userCount * 100);
		reader.close();
	}
	
	/**
	 * Select the top entries (according to the weight) of a map.
	 * @param map_feature_weight A map from feature to its weight.
	 * @param maxEntities Maximum entries of the map to select.
	 * 
	 * @return Sorted top entries (by its weight)
	 */
	private List<Map.Entry<String, Integer>> mostFrequentEntries(HashMap<String, Integer >map_feature_weight, int maxEntities) {
		List<Map.Entry<String, Integer>> sortedEntities = CommonUtils.SortMapByValue(map_feature_weight);
		List<Map.Entry<String, Integer>> topEntities = new ArrayList<Map.Entry<String, Integer>>();
		int count = 0;
		for (Map.Entry<String, Integer> entity : sortedEntities) {
			topEntities.add(entity);
			// Output the top words and their weight.
			// System.out.println(""+ count++ +"\t"+ entity.getKey() + "\t" + entity.getValue());
			if (topEntities.size() >= maxEntities)
				break;
		}
		return topEntities;
	}
	
	/**
	 * Revert an ID_Map (value is an unique ID)
	 * @param map_feature_id Map from feature name to its ID.
	 */
	static public HashMap<Integer, String> revertIDMap(HashMap<String, Integer> map_feature_id) {
		HashMap<Integer, String> map_id_feature = new HashMap<Integer, String>();
		for (Map.Entry<String, Integer> entry : map_feature_id.entrySet()) {
			map_id_feature.put(entry.getValue(), entry.getKey());
		}
		return map_id_feature;
	}
	
	/**
	 * Parse each line in votes file.
	 * @param line A line in votes file
	 * @return A Vote object. If it is not a valid votes line, return null.
	 */
	private static Vote parseVotesLine(String line) {
		String[] arr = line.split(" ");
		if (arr.length > 3) {
			String user = arr[0];
			String item = arr[1];
			double score = Double.parseDouble(arr[2]);
			int time  = Integer.parseInt(arr[3]);
			int wordCount = Integer.parseInt(arr[4]);
			String review = "";
			for (int i = 5; i < arr.length; i++) {
				review = review + arr[i] + " ";
			}
			return new Vote(user, item, score, time, wordCount, review);
		}
		return null;
	}
	
	/**
	 * Parse a sentence to words. 
	 * @param sentence Input sentence to parse
	 * @return A String array containing English words in the sentence.
	 */
	private static String[] parseSentence(String sentence) {
		String[] words = sentence.split("\\s+");
		for (int i = 0; i < words.length; i++) {
		    // Check for a non-word character.
		    words[i] = words[i].replaceAll("[^\\w]", "");
		}
		return words;
	}
	
	/**
	 * Convert a string to word HashMap, where key is word and value is the frequency of the word.
	 * @param str
	 * @return
	 */
	private static HashMap<String, Integer> stringToSet(String str) {
		String[] words = str.split(" ");
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (String word : words) {
			if (!map.containsKey(word)) {
				map.put(word, 0);
			}
			map.put(word, map.get(word) + 1);
		}
		return map;
	}
	
	
	private static void replaceFileWithKeyword(String inputFile, String oldWord, String outputFile, 
			String newWord) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(outputFile));
		String line;
		while ((line = reader.readLine())!= null) {
			String newLine = line.replace(oldWord, newWord);
			writer.println(newLine);
		}
			
		writer.close();
		reader.close();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws java.text.ParseException 
	 * @throws LangDetectException 
	 */
	public static void main(String[] args) throws IOException, ParseException, java.text.ParseException {

		DatasetUtil util = new DatasetUtil();
		String Dir = "/Users/xiangnanhe/Workspace/yelp-challenge/";
		int thres = 50;
		//util.ConvertJsonToVotesFile("/Users/xiangnanhe/Workspace/yelp-challenge/all/", "yelp");
		//util.RemoveDuplicateInVotesFile(Dir + "all/", "yelp");
		//util.FilterVotesReviewsByWords("/Users/xiangnanhe/Workspace/yelp-challenge/", "yelp_1M_u3", 20000);
		
		
		util.FilterVotesFileByUsers(Dir +"all/", "yelp", thres);
		util.SplitVotesFileByTimePerUser(Dir,  "yelp_u" + thres, 0.6, 0.2, 0.2);
		
		util.ConvertVotesToRatingFile(Dir + "train/", "yelp_u" + thres);
	    util.ConvertVotesToRatingFile(Dir + "test/", "yelp_u" + thres);
	    util.ConvertVotesToRatingFile(Dir + "validation/", "yelp_u" + thres); 

		System.out.println("end");
	}
}
