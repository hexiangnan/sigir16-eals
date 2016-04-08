package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Random;
public class CommonUtils {
	
	/**
	 * Sort the HashMap<K, V> by its values, from Large->Small.
	 * @return List<Map.Entry<K, V>> with sorted entries.
	 */
	public static<K, V extends Comparable<? super V>> List<Map.Entry<K, V>> SortMapByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> infoIds = new ArrayList<Map.Entry<K, V>>(map.entrySet()); 
		Comparator<Map.Entry<K, V>> c = new Comparator<Map.Entry<K, V>>() { 
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) { 
				return o2.getValue().compareTo(o1.getValue());
			}};
		Collections.sort(infoIds, c); 
		return infoIds;
	}
	
	/**
	 * Get the topK keys (by its value) of a map. Does not consider the keys which are in ignoreKeys.
	 * @param map
	 * @return
	 */
	public static<K, V extends Comparable<? super V>> ArrayList<K> TopKeysByValue(Map<K, V> map, 
			int topK, ArrayList<K> ignoreKeys) {
		HashSet<K> ignoreSet;
		if (ignoreKeys == null) {
			ignoreSet = new HashSet<K>();
		} else {
			ignoreSet = new HashSet<K> (ignoreKeys);
		}
		
		TopKPriorityQueue<K, V> topQueue = new TopKPriorityQueue<K, V>(topK);
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (!ignoreSet.contains(entry.getKey())) {
				topQueue.add(entry);
			}
		}
		ArrayList<K> topKeys = new ArrayList<K>();
		for (Map.Entry<K, V> entry : topQueue.sortedList()) {
			topKeys.add(entry.getKey());
		}
		return topKeys;
		/*
		//Another implementation that first sorting.
		List<Map.Entry<K, V>> topEntities = SortMapByValue(map);
		ArrayList<K> topKeys = new ArrayList<K>();
		for (Map.Entry<K, V> entity : topEntities) {
			if (topKeys.size() >= topK)	break;
			if (!ignoreSet.contains(entity.getKey())) {
				topKeys.add(entity.getKey());
			}
		}
		return topKeys; */
	}
	
	/**
	 * Convert an int[] to ArrayList<Integer>
	 */
	public static ArrayList<Integer> ArrayToArraylist(int[] array) {
		if (array == null) {
			return new ArrayList<Integer>();
		}
		ArrayList<Integer> list = new ArrayList<Integer>(array.length);
		for (int val : array) {
			list.add(val);
		}
		return list;
	}
	
	/**
	 * Count number of matches of findStr in str.
	 * @param str
	 * @param findStr
	 * @return
	 */
	public static int CountMatchesInString(String str, String findStr) {
		int lastIndex = 0;
		int count = 0;
		while(lastIndex != -1) {
	       lastIndex = str.indexOf(findStr,lastIndex);

	       if( lastIndex != -1) {
	             count ++;
	             lastIndex+=findStr.length();
	       }
		}
		return count;
	}
	
	/**
	 * Convert a string to k-gram set.
	 * @param str
	 * @param size
	 */
	public static ArrayList<String> StringToGramSet(String str, int k) {
		ArrayList<String> grams = new ArrayList<String>();
		String[] words = str.split(" ");
		for(int i = 0; i <= words.length-k; i ++) {
			String gram = words[i];
			for (int j = 1; j < k; j++) {
				gram += " " + words[i+j];
			}
			grams.add(gram.trim());
		}
		return grams;
	}
	
	/**
	 * Randomly shuffle an int array.
	 * @param array
	 */
	public static void ShuffleArray(int[] array)
	{
	    int index, temp;
	    Random random = new Random();
	    for (int i = array.length - 1; i > 0; i--)
	    {
	        index = random.nextInt(i + 1);
	        temp = array[index];
	        array[index] = array[i];
	        array[i] = temp;
	    }
	}
}
