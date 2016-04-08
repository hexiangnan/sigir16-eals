package algorithms;

import data_structure.Rating;
import data_structure.SparseMatrix;
import data_structure.DenseVector;
import data_structure.DenseMatrix;
import data_structure.Pair;
import data_structure.SparseVector;
import happy.coding.math.Randoms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import utils.Printer;

/**
 * Coordinate descent algorithm of the KDD'15 paper:
 * Robin Devooght etc. Dynamic Matrix Factorization with Priors on Unknown Values.
 * @author xiangnanhe
 */
public class MF_CD extends TopKRecommender {
	/** Model priors to set. */
	int factors = 10; 	// number of latent factors.
	int maxIter = 100; 	// maximum iterations.
	double w0 = 0.01;	// weight for 0s
	double reg = 0.01; 	// regularization parameters
  double init_mean = 0;  // Gaussian mean for init V
  double init_stdev = 0.01; // Gaussian std-dev for init V
	
  /** Priors for line search */
	int LSMaxIter	= 10;  // max iteration of the line search. Default is 10
	double Alpha = 0.3; //	parameter of line search. In the range (0, 0.5).
	double Beta	= 0.3; //	parameter of line search. In the range (0, 1.0).
  
  /** Model parameters to learn */
  public DenseMatrix U;	// latent vectors for users
  public DenseMatrix V;	// latent vectors for items
	
  /** Caches */
  DenseMatrix SU;
  DenseMatrix SV;
  
  boolean showProgress;
  boolean showLoss;
  
  // weight for each positive instance in trainMatrix
  SparseMatrix W; 
  
  // weight of new instance in online learning
  public double w_new = 1;
  
	public MF_CD(SparseMatrix trainMatrix, ArrayList<Rating> testRatings, 
			int topK, int threadNum, int factors, int maxIter, double w0, double reg,
			double init_mean, double init_stdev, boolean showProgress, boolean showLoss) {
		super(trainMatrix, testRatings, topK, threadNum);
		this.factors = factors;
		this.maxIter = maxIter;
		this.w0 = w0 / itemCount;
		this.reg = reg;
		this.init_mean = init_mean;
		this.init_stdev = init_stdev;
		this.showProgress = showProgress;
		this.showLoss = showLoss;
		this.initialize();
		
		// By default, the weight for positive instance is uniformly 1.
		W = new SparseMatrix(userCount, itemCount);
		for (int u = 0; u < userCount; u ++)
			for (int i : trainMatrix.getRowRef(u).indexList())
				W.setValue(u, i, 1);
	}
	
	private void initialize() {
		U = new DenseMatrix(userCount, factors);
		V = new DenseMatrix(itemCount, factors);
		U.init(init_mean, init_stdev);
		V.init(init_mean, init_stdev);
		
		SU = U.transpose().mult(U);
		SV = V.transpose().mult(V);
	}
	
	public void setTrain(SparseMatrix trainMatrix) {
		this.trainMatrix = new SparseMatrix(trainMatrix);
		W = new SparseMatrix(userCount, itemCount);
		for (int u = 0; u < userCount; u ++)
			for (int i : this.trainMatrix.getRowRef(u).indexList())
				W.setValue(u, i, 1);
	}
	
	public void setLSpriors(int LSMaxIter, double Alpha, double Beta) {
		this.LSMaxIter = LSMaxIter;
		this.Alpha = Alpha;
		this.Beta = Beta;
	}
	
	// remove
	public void setUV(DenseMatrix U, DenseMatrix V) {
		this.U = U.clone();
		this.V = V.clone();
		SU = U.transpose().mult(U);
		SV = V.transpose().mult(V);
	}
	
	/**
	 * Implement the CD algorithm of the KDD'15 papers
	 */
	public void buildModel() {
		//System.out.println("Run for MF_CD.");
		
		ArrayList<Integer> shuffle_list = new ArrayList<Integer>();
		for (int i = 0; i < itemCount + userCount; i ++)
			shuffle_list.add(i);
		
		double loss_pre = Double.MAX_VALUE;
		for (int iter = 0; iter < maxIter; iter ++) {
			Long start = System.currentTimeMillis();
			Collections.shuffle(shuffle_list);
			
			for (int index : shuffle_list) {				
				if (index >= userCount)  // for an item
					update_item(index - userCount);
				else   // for a user
					update_user(index);
			}
			
			// Show progress
			if (showProgress)
				showProgress(iter, start, testRatings);
			// Show loss
			if (showLoss)
				loss_pre = showLoss(iter, start, loss_pre);

		}  // end for iter
	}
	
	// Run model for one iteration
	public void runOneIteration() {
		ArrayList<Integer> shuffle_list = new ArrayList<Integer>();
		for (int i = 0; i < itemCount + userCount; i ++)
			shuffle_list.add(i);
		Collections.shuffle(shuffle_list);
		
		for (int index : shuffle_list) {				
			if (index >= userCount)  // for an item
				update_item(index - userCount);
			else   // for a user
				update_user(index);
		}
	}
	
	// Line search (book, Convex Optimization) for the best step size.
	private double linesearch(int index, DenseVector embedding, 
			DenseVector gradient, int LSMaxIter, double Alpha, double Beta) {
		double step_size = 1.0;
		double init_error = error_row(index, embedding);
		
		for (int iter = 0; iter < LSMaxIter; iter ++) {
			// Build new features (ie embedding) with current step size
			DenseVector newEmbedding = embedding.minus(gradient.scale(step_size));
			
			// Check if new features are good enough. If not reduce step size
			double new_error = error_row(index, newEmbedding);
			if (new_error > init_error - Alpha * step_size * gradient.squaredSum())
				step_size *= Beta;
			else
				break;
			
			// Too many iterations, return step_size = 0
			if (iter == LSMaxIter - 1) {
				step_size = 0;
				break;
			}
		}	
		return step_size;
	}
	
	private double error_row(int index, DenseVector embedding) {
		double err = 0;
		if (index >= userCount) {  // for an item
			int i = index - userCount;
			for (int u : trainMatrix.getColRef(i).indexList()) {
				double prediction = U.row(u, false).inner(embedding);
				err += W.getValue(u, i) * Math.pow(trainMatrix.getValue(u, i) - prediction, 2);
			}
			err *= (1 - w0);
			err += w0 * SU.mult(embedding).inner(embedding);
			err += reg * embedding.squaredSum();
			
		} else {  // for a user
			int u = index;
			for (int i : trainMatrix.getRowRef(u).indexList()) {
				double prediction = V.row(i, false).inner(embedding);
				err += W.getValue(u, i) * Math.pow(trainMatrix.getValue(u, i) - prediction, 2);
			}
			err *= (1 - w0);
			err += w0 * SV.mult(embedding).inner(embedding);
			err += reg * embedding.squaredSum();
		}
		return err;
	}
	
	private void update_user(int u) {
		DenseVector embedding = U.row(u, false);
		// Calculate the gradient
		DenseVector gradient = SV.mult(embedding).scale(w0);
		for (int i : trainMatrix.getRowRef(u).indexList()) {
			double mul = W.getValue(u, i) * (predict(u, i) * (1 - w0) - trainMatrix.getValue(u, i));
			gradient.selfAdd(V.row(i, false).scale(mul));
		}
		gradient.selfAdd(embedding.scale(reg));  // with regularizer
		
		// Line search for learning rate
		double lr = linesearch(u, embedding, gradient, LSMaxIter, Alpha, Beta);
		
		// Update S cache before updating parameters
		DenseVector new_embedding = embedding.minus(gradient.scale(lr));
		for (int f = 0; f < factors; f ++) {
			for (int k = 0; k <= f; k ++) {
				double val = SU.get(f, k) - embedding.get(f) * embedding.get(k)
						+ new_embedding.get(f) * new_embedding.get(k);
				SU.set(f, k, val);
				SU.set(k, f, val);
			}
		}
		
		// Parameter update
		for (int f = 0; f < factors; f ++)
			embedding.set(f, new_embedding.get(f));
	}
	
	private void update_item(int i) {
		DenseVector embedding = V.row(i, false);
		// Calculate the gradient
		DenseVector gradient = SU.mult(embedding).scale(w0);
		for (int u : trainMatrix.getColRef(i).indexList()) {
			double mul = W.getValue(u, i) * (predict(u, i) * (1 - w0) - trainMatrix.getValue(u, i));
			gradient.selfAdd(U.row(u, false).scale(mul));
		}
		gradient.selfAdd(embedding.scale(reg));  // with regularizer
		
		// Line search for learning rate
		double lr = linesearch(userCount + i, embedding, gradient, LSMaxIter, Alpha, Beta);
		
		// Update SV cache
		DenseVector new_embedding = embedding.minus(gradient.scale(lr));
		for (int f = 0; f < factors; f ++) {
			for (int k = 0; k <= f; k ++) {
				double val = SV.get(f, k) - embedding.get(f) * embedding.get(k)
						+ new_embedding.get(f) * new_embedding.get(k);
				SV.set(f, k, val);
				SV.set(k, f, val);
			}
		}
		
		// Parameter update
		for (int f = 0; f < factors; f ++)
			embedding.set(f, new_embedding.get(f));
	}
	
	public double showLoss(int iter, long start, double loss_pre) {
		long start1 = System.currentTimeMillis();
		double loss_cur = loss();
		String symbol = loss_pre >= loss_cur ? "-" : "+";
		System.out.printf("Iter=%d [%s]\t [%s]loss: %.4f [%s]\n", iter, 
				Printer.printTime(start1 - start), symbol, loss_cur, 
				Printer.printTime(System.currentTimeMillis() - start1));
		return loss_cur;
	}
	
	// Fast way to calculate the loss function
	public double loss() {
		double L = reg * (U.squaredSum() + V.squaredSum());
		for (int u = 0; u < userCount; u ++) {
			double l = 0;
			for (int i : trainMatrix.getRowRef(u).indexList()) {
				l +=  W.getValue(u, i) * Math.pow(trainMatrix.getValue(u, i) - predict(u, i), 2);
			}
			l *= (1 - w0);
			l += w0 * SV.mult(U.row(u, false)).inner(U.row(u, false));
			L += l;
		}
		
		return L;
	}
	
	@Override
	public double predict(int u, int i) {
		return U.row(u, false).inner(V.row(i, false));
	}

	@Override
	public void updateModel(int u, int i) {
		trainMatrix.setValue(u, i, 1);
		W.setValue(u, i, w_new);
		
		for (int iter = 0; iter < maxIterOnline; iter ++) {
			update_user(u);
			update_item(i);
		}
	}
}
