package algorithms;

import java.util.ArrayList;
import java.util.HashMap;

import data_structure.Rating;
import data_structure.SparseMatrix;

public class ItemPopularity extends TopKRecommender {

	double[] item_popularity;
	public ItemPopularity(SparseMatrix trainMatrix, ArrayList<Rating> testRatings, 
			int topK, int threadNum) {
		super(trainMatrix, testRatings, topK, threadNum);
		item_popularity = new double[itemCount];
	}
	
	public void buildModel() {
		for (int i = 0; i < itemCount; i++) {
			// Measure popularity by number of reviews received.
			item_popularity[i] = trainMatrix.getColRef(i).itemCount();
		}
	}
	
	public double predict(int u, int i) {
		return item_popularity[i];
	}

	@Override
	public void updateModel(int u, int i) {
		trainMatrix.setValue(u, i, 1);
		item_popularity[i] += 1;
	}
}
