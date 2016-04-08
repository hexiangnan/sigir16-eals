package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.AbstractMap;

import data_structure.DenseVector;

/**
 * Using PriorityQueue to implement MinHeap, for selecting topK maximum entries
 *  (by value) of a map.
 * 
 * @author HeXiangnan
 */
public class TopKPriorityQueue<K, V extends Comparable<? super V>> {
	public PriorityQueue<Map.Entry<K, V>> queue;
	private int K;  // Maximum size of the heap.
	
	private Comparator<Map.Entry<K, V>> c = new Comparator<Map.Entry<K, V>>() {
		public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	};
	
	public TopKPriorityQueue(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException();
		}
		this.K = maxSize;
		this.queue = new PriorityQueue<Map.Entry<K, V>>(maxSize, c);
	}

	public void add(Map.Entry<K, V> e) {
		if (queue.size() < K) { // The queue is not full.
			queue.add(e);
		} else { // The queue is full.
			Map.Entry<K, V> peek = queue.peek(); // Pick the top element
			if (c.compare(e, peek) > 0) { 
				queue.poll();
				queue.add(e);
			}
		}
	}
	
	public ArrayList<Map.Entry<K, V>> toList() {
		return new ArrayList<Map.Entry<K, V>>(queue);
	}

	public ArrayList<Map.Entry<K, V>> sortedList() {
		ArrayList<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(queue); 
		Collections.sort(list, c.reversed()); 
		return list;
	}
	
	ArrayList<Integer> maxPoolingIndices(DenseVector vec, int maxPooling) {
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		TopKPriorityQueue<Integer, Double> q = new TopKPriorityQueue<Integer, Double>(maxPooling);
		for (int i = 0; i < vec.size(); i ++) {
			q.add(new AbstractMap.SimpleEntry<Integer, Double>(i, vec.get(i)));
		}
		for (Map.Entry<Integer, Double> e : q.toList()) {
			indexList.add(e.getKey());
		}
		return indexList;
	}
	
	public static void main(String[] args) throws IOException {
		//Test topK selection.
		TopKPriorityQueue<Integer, Double> q = new TopKPriorityQueue(3);
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		map.put(1, 1.0);
		map.put(2, 2.0);
		map.put(3, 4.0);
		map.put(4, 3.0);
		
		for (Map.Entry<Integer, Double> e : map.entrySet()) {
			q.add(e);
		}
		q.add(new AbstractMap.SimpleEntry<Integer, Double>(6, 5.0));
		for (Map.Entry<Integer, Double> e : q.sortedList()) {
			System.out.println(e.getKey() +": " + e.getValue());
		}
		
		// Test maxPoolingIndices function.
		int[] array = {3, 1, 5, 6, 2};
		
		double[] arr = new double[10];
		Printer.printArray(arr);
		
		for (int i : CommonUtils.ArrayToArraylist(array)) {
			System.out.print(i + " ");
		}
	}
}