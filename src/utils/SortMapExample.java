package utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class SortMapExample {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
		TreeMap<Integer, Double> tmap = new TreeMap<Integer, Double> ();
		tmap.put(1, 2.0);
		tmap.put(1, 3.0);
		tmap.put(1, 1.0);
		tmap.put(3, 2.0);
		tmap.put(4, 0.0);
		
		Iterator<Entry<Integer, Double>> iter = tmap.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>)iter.next();
			System.out.printf("next : %s - %s\n", entry.getKey(), entry.getValue());
		}
	}

}