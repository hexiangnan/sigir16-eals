package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import utils.CommonUtils;
import data_structure.Rating;
import data_structure.SparseMatrix;
import data_structure.SparseVector;

/**
 * Implement ItemKNN method for topK recommendation, as described in:
 * Collaborative filtering for implicit feedback datasets. 
 * By Yifan Hu , Yehuda Koren , Chris Volinsky.
 * In IEEE ICDM'2008.
 * 
 * @author xiangnanhe
 *
 */
public class ItemKNN extends TopKRecommender {

	/** Similarity matrix of item-item . */
	public SparseMatrix similarity;

	/** K neighbors to consider for each item */
	private int K = 0;
	
	/** Cache the L2 length for each item. */
	double[] lengths;
	
	public ItemKNN(SparseMatrix trainMatrix, ArrayList<Rating> testRatings, 
			int topK, int threadNum, int K) {
		super(trainMatrix, testRatings, topK, threadNum);
		this.K = K;
		this.similarity = new SparseMatrix(itemCount, itemCount);
	}

	public void buildModel() {
		// The length cache
		lengths = new double[itemCount];
		for (int i = 0; i < itemCount; i ++) {
			lengths[i] = Math.sqrt(trainMatrix.getColRef(i).squareSum());
		}
		
		// Run model multi-threads splitted by items.
		ItemKNNThread[] threads = new ItemKNNThread[threadNum];
		for (int t = 0; t < threadNum; t ++) {
			int startItem = (itemCount / threadNum) * t;
			int endItem = (t == threadNum-1) ? itemCount : 
				(itemCount / threadNum) * (t + 1);
			threads[t] = new ItemKNNThread(this, startItem, endItem);
			threads[t].start();
		}
		
		// Wait until all threads are finished.
		for (int t = 0; t < threads.length; t++) { 
		  try {
				threads[t].join();
			} catch (InterruptedException e) {
				System.err.println("InterruptException was caught: " + e.getMessage());
			}
		}
	}
	
	protected void buildModel_items(int startItem, int endItem) {
		// Build the similarity matrix for selected items.
		for (int i = startItem; i < endItem; i ++) {
			HashMap<Integer, Double> map_item_score = new HashMap<Integer, Double>();
			for (int j = 0; j < itemCount & j != i; j ++) {
				// Cosine similarity
				double score = trainMatrix.getColRef(i).innerProduct(trainMatrix.getColRef(j));
				if (score != 0) {
					score /= (lengths[i] * lengths[j]);
					map_item_score.put(j, score);
				}
			}
			if (K <= 0) {  // All neighbors
				for (int j : map_item_score.keySet()) {
					similarity.setValue(i, j, map_item_score.get(j));
				}
			} else {  // Only K nearest neighbors
				for (int j : CommonUtils.TopKeysByValue(map_item_score, K, null)) {
					similarity.setValue(i, j, map_item_score.get(j));
				}
			} // end if
		} // end for
	}
	
	public double predict(int u, int i) {
		return trainMatrix.getRowRef(u).innerProduct(similarity.getRowRef(i));
	}

	@Override
	public void updateModel(int u, int i) {
		// TODO Implement SIGMOD15 paper
		
	}
}

// Thread for building model for ItemKNN.
class ItemKNNThread extends Thread {
	ItemKNN model;
	int startItem;
	int endItem;

	public ItemKNNThread(ItemKNN model, int startItem, int endItem) {
		this.model = model;
		this.startItem = startItem;
		this.endItem = endItem;
	}
	
	public void run() {
		model.buildModel_items(startItem, endItem);
	}
}
