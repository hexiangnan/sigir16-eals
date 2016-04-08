package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeSet;

import algorithms.*;
import utils.DatasetUtil;
import data_structure.DenseVector;
import data_structure.Rating;
import data_structure.SparseMatrix;
import data_structure.SparseVector;
import utils.Printer;
import utils.CommonUtils;

import java.util.ArrayList;

/**
 * This is an abstract class for evaluating topK recommender systems (i.e. main functions.).
 * Define some variables to use, and member functions to load data.
 * 
 * @author HeXiangnan
 * @since 2014.12.16
 */

public abstract class main {

	/** Rating matrix for training. */ 
	public static SparseMatrix trainMatrix;
	
	/** Test ratings (sorted by time for global split). */
	public static ArrayList<Rating> testRatings;
	
	public static int topK = 100;
	public static int threadNum = 10;
	
	public static int userCount;
	public static int itemCount;
	
	public static void ReadRatings_GlobalSplit(String ratingFile, double testRatio)
			throws IOException {
		userCount = itemCount = 0;
		System.out.println("Global splitting with testRatio " + testRatio);
		// Step 1. Construct data structure for sorting.
		System.out.print("Read ratings and sort.");
		long startTime = System.currentTimeMillis();
		ArrayList<Rating> ratings = new ArrayList<Rating>();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(ratingFile)));
		String line;
		while((line = reader.readLine()) != null) {
			Rating rating = new Rating(line);
			ratings.add(rating);
			userCount = Math.max(userCount, rating.userId);
			itemCount = Math.max(itemCount, rating.itemId);
		}
		reader.close();
		userCount ++;
		itemCount ++;
		
		// Step 2. Sort the ratings by time (small->large).
		Comparator<Rating> c = new Comparator<Rating>() {
			public int compare(Rating o1, Rating o2) {
				if (o1.timestamp - o2.timestamp > 0)	return 1;
				else if (o1.timestamp - o2.timestamp < 0)	return -1;
				else return 0;
			}
		};
		Collections.sort(ratings, c);
		System.out.printf("[%s]\n", Printer.printTime(
				System.currentTimeMillis() - startTime));
		
		// Step 3. Generate trainMatrix and testStream
		System.out.printf("Generate trainMatrix and testStream.");
		startTime = System.currentTimeMillis();
		trainMatrix = new SparseMatrix(userCount, itemCount);
		testRatings = new ArrayList<Rating>();
		
		int testCount = (int) (ratings.size() * testRatio);
		int count = 0;
		for (Rating rating : ratings) {
			if (count < ratings.size() - testCount) {  // train
				trainMatrix.setValue(rating.userId, rating.itemId, 1);
			} else {  // test
				testRatings.add(rating);
			}
			count ++;
		}
		// Count number of new users/items/ratings in the test data
		HashSet<Integer> newUsers = new HashSet<Integer>();
		int newRatings = 0;
		for (int u = 0; u < userCount; u ++) {
			if (trainMatrix.getRowRef(u).itemCount() == 0)	newUsers.add(u);
		}
		for (Rating rating : testRatings) {
			if (newUsers.contains(rating.userId))	newRatings ++;
		}
		
		System.out.printf("[%s]\n", Printer.printTime(
				System.currentTimeMillis() - startTime));
		
		// Print some basic statistics of the dataset.
		System.out.println ("Data\t" + ratingFile);
		System.out.println ("#Users\t" + userCount + ", #newUser: " + newUsers.size());
		System.out.println ("#Items\t" + itemCount);
		System.out.printf("#Ratings\t %d (train), %d(test), %d(#newTestRatings)\n", 
				trainMatrix.itemCount(),  testRatings.size(), newRatings);
	}
	
	/**
	 *  Each line of .rating file is: userID\t itemID\t score\t timestamp.
	 *  userID starts from 0 to num_user-1
	 *  The items of each user is sorted by time (small->large).
	 */	
	public static void ReadRatings_HoldOneOut(String ratingFile) throws IOException {
			userCount = itemCount = 0;
			System.out.println("HoldOne out splitting.");
			// Step 1. Construct data structure for sorting.
			System.out.print("Sort items for each user.");
			long startTime = System.currentTimeMillis();
			ArrayList<ArrayList<Rating>> user_ratings = new ArrayList<ArrayList<Rating>>();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(ratingFile)));
			String line;
			while((line = reader.readLine()) != null) {
				Rating rating = new Rating(line);
				if (user_ratings.size() - 1 < rating.userId) { // create a new user
					user_ratings.add(new ArrayList<Rating>());
				}
				user_ratings.get(rating.userId).add(rating);
				userCount = Math.max(userCount, rating.userId);
				itemCount = Math.max(itemCount, rating.itemId);
			}
			reader.close();
			userCount ++;
			itemCount ++;
			assert userCount == user_ratings.size();
			
			// Step 2. Sort the ratings of each user by time (small->large).
			Comparator<Rating> c = new Comparator<Rating>() {
				public int compare(Rating o1, Rating o2) {
					if (o1.timestamp - o2.timestamp > 0)	return 1;
					else if (o1.timestamp - o2.timestamp < 0)	return -1;
					else return 0;
				}
			};
			for (int u = 0;  u < userCount; u ++) {
				Collections.sort(user_ratings.get(u), c);
			}
			System.out.printf("[%s]\n", Printer.printTime(
					System.currentTimeMillis() - startTime));
			
			// Step 3. Generated splitted matrices (implicit 0/1 settings). 
			System.out.printf("Generate rating matrices.");
			startTime = System.currentTimeMillis();
			trainMatrix = new SparseMatrix(userCount, itemCount);
			testRatings = new ArrayList<Rating>();
			for (int u = 0; u < userCount; u ++) {
				ArrayList<Rating> ratings = user_ratings.get(u);
				for (int i = ratings.size() - 1; i >= 0; i --) {
					int userId = ratings.get(i).userId;
					int itemId = ratings.get(i).itemId;
					if (i == ratings.size() - 1) { // test
						testRatings.add(ratings.get(i));
					} else { // train
						trainMatrix.setValue(userId, itemId, 1);
					} 
				}
			}
			System.out.printf("[%s]\n", Printer.printTime(
					System.currentTimeMillis() - startTime));
			
			// Print some basic statistics of the dataset.
			System.out.println ("Data\t" + ratingFile);
			System.out.println ("#Users\t" + userCount);
			System.out.println ("#Items\t" + itemCount);
			System.out.printf("#Ratings\t %d (train), %d(test)\n", 
					trainMatrix.itemCount(), testRatings.size());
		}
	
	/**
	 * Generate a smaller dataset. 
	 * @param threshold
	 * @throws IOException 
	 */
	public static void FilterRatingsWithThreshold(String ratingFile, 
			int userThreshold, int itemThreshold) throws IOException {
		ArrayList<ArrayList<Rating>> user_ratings = new ArrayList<ArrayList<Rating>>();
		System.out.println("Filter dataset with #user/item >= " + itemThreshold + 
				" and #item/user >= " + userThreshold);
		
		// Read user ratings.
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(ratingFile)));
		HashMap<Integer, Integer> map_item_count = new HashMap<Integer, Integer>();
		String line;
		while((line = reader.readLine()) != null) {
			Rating rating = new Rating(line);
			if (user_ratings.size() - 1 < rating.userId) { // create a new user
				user_ratings.add(new ArrayList<Rating>());
			}
			user_ratings.get(rating.userId).add(rating);
			if (!map_item_count.containsKey(rating.itemId)) {
				map_item_count.put(rating.itemId, 0);
			}
			map_item_count.put(rating.itemId, map_item_count.get(rating.itemId) + 1);
		}
		reader.close();
		
		// User filtering & item filtering
		PrintWriter writer = new PrintWriter (new FileOutputStream(
				ratingFile + "_i" + itemThreshold + "_u" + userThreshold));
		HashMap<String, Integer> map_user_id = new HashMap<String, Integer>();
		HashMap<String, Integer> map_item_id = new HashMap<String, Integer>();
		int count = 0;
		
		for (int u = 0; u < user_ratings.size(); u ++) {
			ArrayList<Rating> ratings = user_ratings.get(u);
			int count_u = 0;
			for (Rating rating : ratings) {
				// item filtering
				if (map_item_count.get(rating.itemId) < itemThreshold)	continue; 
				count_u ++;
			}
			// user filtering
			if (count_u < userThreshold)	continue;  
			// write to files
			for (Rating rating: ratings) {
				if (map_item_count.get(rating.itemId) < itemThreshold)	continue;
				// Old item id and user id
				String item = "" + rating.itemId;
				String user = "" + rating.userId;
				if (!map_item_id.containsKey(item))	{
					map_item_id.put(item, map_item_id.size());
				}
				if (!map_user_id.containsKey(user)) {
					map_user_id.put(user, map_user_id.size());
				}
				// New item id and user id
				int userId = map_user_id.get(user);
				int itemId = map_item_id.get(item);
				writer.println(userId + "\t" + itemId + "\t" + rating.score + "\t" + rating.timestamp);
				count ++;
			}
		}
		
		System.out.printf("After filtering: #user:%d, #item:%d, #rating:%d \n", 
				map_user_id.size(), map_item_id.size(), count);
		writer.close();
	}
	
	// Get some statistics about the dataset, e.g. user distribution on items
	public static void DatasetStatistics(String ratingFile) throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(ratingFile)));
		
		// Read user ratings
		int ratingCount = 0;
		ArrayList<ArrayList<Rating>> user_ratings = new ArrayList<ArrayList<Rating>>();
		String line;
		while ((line = reader.readLine()) != null) {
			Rating rating = new Rating(line);
			ratingCount ++;
			if (user_ratings.size() - 1 < rating.userId) { // create a new user
				user_ratings.add(new ArrayList<Rating>());
			}
			user_ratings.get(rating.userId).add(rating);
		}
		System.out.println("#Ratings in total: " + ratingCount);
		
		// user distribution on items
		HashMap<Integer, Integer> map_count_users = new HashMap<Integer, Integer>();
		for (ArrayList<Rating> ratings : user_ratings) {
			int count = ratings.size();
			if (!map_count_users.containsKey(ratings.size())) {
				map_count_users.put(count, 0);
			}
			map_count_users.put(count, map_count_users.get(count) + 1);
		}
		List<Integer> sortedKeys=new ArrayList<Integer>(map_count_users.keySet());
		Collections.sort(sortedKeys);
		System.out.println("#rating\t#users (percentage)");
		for (int count : sortedKeys) {
			int users = map_count_users.get(count);
			System.out.printf("%d\t %d (%.2f%%)\n", count, users, 
					(double)users / user_ratings.size() * 100 );
		}
		reader.close();
		
		// Read item ratings
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(ratingFile)));
		ArrayList<ArrayList<Rating>> item_ratings = new ArrayList<ArrayList<Rating>>();
		while ((line = reader.readLine()) != null) {
			Rating rating = new Rating(line);
			if (item_ratings.size() - 1 < rating.itemId) { // create a new user
				item_ratings.add(new ArrayList<Rating>());
			}
			item_ratings.get(rating.itemId).add(rating);
		}
		
		// item distrubution on users
		HashMap<Integer, Integer> map_count_items = new HashMap<Integer, Integer>();
		for (ArrayList<Rating> ratings : item_ratings) {
			int count = ratings.size();
			if (!map_count_items.containsKey(ratings.size())) {
				map_count_items.put(count, 0);
			}
			map_count_items.put(count, map_count_items.get(count) + 1);
		}
		sortedKeys=new ArrayList<Integer>(map_count_items.keySet());
		Collections.sort(sortedKeys);
		System.out.println("#rating\t#items (percentage)");
		for (int count : sortedKeys) {
			int items = map_count_items.get(count);
			System.out.printf("%d\t %d (%.2f%%)\n", count, items, 
					(double)items / item_ratings.size() * 100 );
		}
		reader.close();
	}
	
	// Convert the movie-len-10M input(.dat) file to rating file.
	public static void convertMLDatToRating(String ml_file) throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(ml_file)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(ml_file + ".rating"));
		
		int ratingCount = 0;
		String splitter = "::";
		HashMap<String, Integer> map_item_id = new HashMap<String, Integer>(); // id starts from 0
		HashMap<String, Integer> map_user_id = new HashMap<String, Integer>();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] arr = line.split(splitter);
			if (!map_user_id.containsKey(arr[0]))
				map_user_id.put(arr[0], map_user_id.size());
			if (!map_item_id.containsKey(arr[1]))
				map_item_id.put(arr[1], map_item_id.size());
			
			int userId = map_user_id.get(arr[0]);
			int itemId = map_item_id.get(arr[1]);
			writer.println(userId + "\t" + itemId + "\t" + arr[2] + "\t" + arr[3]);
			ratingCount ++;
		}
		
		System.out.println("Converted " + ml_file + " to .rating file");
		System.out.printf("#rating:%d, #user:%d, #item:%d \n", 
				ratingCount, map_user_id.size(), map_item_id.size());
		reader.close();
		writer.close();
	}

	// Convert the amazon review dataset (.vote) file to rating file.
	public static void convertVoteToRating(String vote_file) throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(vote_file)));
		PrintWriter writer = new PrintWriter (new FileOutputStream(vote_file + ".rating"));
		
		int ratingCount = 0;
		String splitter = " ";
		HashMap<String, Integer> map_item_id = new HashMap<String, Integer>(); // id starts from 0
		HashMap<String, Integer> map_user_id = new HashMap<String, Integer>();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] arr = line.split(splitter);
			if (!map_user_id.containsKey(arr[0]))
				map_user_id.put(arr[0], map_user_id.size());
			if (!map_item_id.containsKey(arr[1]))
				map_item_id.put(arr[1], map_item_id.size());
			
			int userId = map_user_id.get(arr[0]);
			int itemId = map_item_id.get(arr[1]);
			writer.println(userId + "\t" + itemId + "\t" + arr[2] + "\t" + arr[3]);
			ratingCount ++;
		}
		
		System.out.println("Converted " + vote_file + " to .rating file");
		System.out.printf("#rating:%d, #user:%d, #item:%d \n", 
				ratingCount, map_user_id.size(), map_item_id.size());
		reader.close();
		writer.close();
	}
	
	// Deduplicate the rating file by averaging the ratings for a (u,i) pair
	// Note: after deduplication, timestamp is removed.
	public static void deduplicate(String ratingFile) throws IOException {
		// Read user ratings.
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(ratingFile)));
		int ratingCount = 0;
		ArrayList<ArrayList<Rating>> user_ratings = new ArrayList<ArrayList<Rating>>();
		String line;
		while ((line = reader.readLine()) != null) {
			Rating rating = new Rating(line);
			ratingCount ++;
			if (user_ratings.size() - 1 < rating.userId) { // create a new user
				user_ratings.add(new ArrayList<Rating>());
			}
			user_ratings.get(rating.userId).add(rating);
		}
		System.out.println("#Ratings in total: " + ratingCount);
		reader.close();
		
		// Deduplicate and Writing to file
		PrintWriter writer = new PrintWriter (new FileOutputStream(ratingFile + ".deduplicate"));
		ratingCount = 0;
		for (int u = 0; u < user_ratings.size(); u ++) {
			ArrayList<Rating> ratings = user_ratings.get(u);
			HashMap<Integer, Double> map_item_score = new HashMap<Integer, Double>();
			HashMap<Integer, Integer> map_item_count = new HashMap<Integer, Integer>();
			for (Rating rating: ratings) {
				if (!map_item_score.containsKey(rating.itemId))	{
					map_item_score.put(rating.itemId, 0.0);
					map_item_count.put(rating.itemId, 0);
				}
				map_item_score.put(rating.itemId, map_item_score.get(rating.itemId) + rating.score);
				map_item_count.put(rating.itemId, map_item_count.get(rating.itemId) + 1);
			}
			for (int i : map_item_score.keySet()) {
				double score = map_item_score.get(i) / map_item_count.get(i);
				writer.printf("%d\t%d\t%.1f\n", u+1, i+1, score);
				ratingCount ++;
			}
		}
		writer.close();
		System.out.println("#After dedepulicate, #ratings: " + ratingCount);
	}
	
	public static void main(String[] args) throws IOException {
		String dataset ="hanwang-data/amazon_books_filter.rating";
		deduplicate(dataset);
		
		//String dataset = "data/yelp.rating";
		//ReadRatings_HoldOneOut("data/yelp.rating");
		
		//FilterRatingsWithThreshold(dataset, 10, 10);
		//DatasetStatistics(dataset);
		
		//convertVoteToRating(dataset);
		//FilterRatingsWithThreshold(dataset, 10, 10);
	}
	
	// Evaluate the model
	public static double[] evaluate_model(TopKRecommender model, String name) {
		long start = System.currentTimeMillis();
		model.buildModel();
		model.evaluate(testRatings);
		
		double[] res = new double[3];
		res[0] = model.hits.mean();
		res[1] = model.ndcgs.mean();
		res[2] = model.precs.mean();
		System.out.printf("%s\t <hr, ndcg, prec>:\t %.4f\t %.4f\t %.4f [%s]\n", 
				name, res[0], res[1], res[2],
				Printer.printTime(System.currentTimeMillis() - start));
		return res;
	}
	
	// Evaluate the model by online protocol
	public static void evaluate_model_online(TopKRecommender model, String name, int interval) {
		long start = System.currentTimeMillis();
		model.evaluateOnline(testRatings, interval);
		System.out.printf("%s\t <hr, ndcg, prec>:\t %.4f\t %.4f\t %.4f [%s]\n", 
				name, model.hits.mean(), model.ndcgs.mean(), model.precs.mean(),
				Printer.printTime(System.currentTimeMillis() - start));
	}
}

class ModelThread extends Thread {
	TopKRecommender model;
	
	public ModelThread(TopKRecommender model) {
		this.model = model;
	}
	
	public void run() {
		model.runOneIteration();
	}
}
