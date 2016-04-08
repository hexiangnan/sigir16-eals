package data_structure;

import java.io.Serializable;
import java.util.ArrayList;
import data_structure.Pair;

/**
 * This class implements sparse matrix, containing empty values for most space.
 * 
 * @author Joonseok Lee
 * @since 2012. 4. 20
 * @version 1.1
 */
public class SparseMatrix implements Serializable{
	private static final long serialVersionUID = 8003;
	
	/** The number of rows. */
	private int M;
	/** The number of columns. */
	private int N;
	/** The array of row references. */
	private SparseVector[] rows;
	/** The array of column references. */
	private SparseVector[] cols;

	/*========================================
	 * Constructors
	 *========================================*/
	/**
	 * Construct an empty sparse matrix, with a given size.
	 * 
	 * @param m The number of rows.
	 * @param n The number of columns.
	 */
	public SparseMatrix(int m, int n) {
		this.M = m;
		this.N = n;
		rows = new SparseVector[M];
		cols = new SparseVector[N];
		
		for (int i = 0; i < M; i++) {
			rows[i] = new SparseVector(N);
		}
		for (int j = 0; j < N; j++) {
			cols[j] = new SparseVector(M);
		}
	}
	
	/**
	 * Construct an empty sparse matrix, with data copied from another sparse matrix.
	 * 
	 * @param sm The matrix having data being copied.
	 */
	public SparseMatrix(SparseMatrix sm) {
		this.M = sm.M;
		this.N = sm.N;
		rows = new SparseVector[M];
		cols = new SparseVector[N];
		
		for (int i = 0; i < M; i++) {
			rows[i] = sm.getRow(i);
		}
		for (int j = 0; j < N; j++) {
			cols[j] = sm.getCol(j);
		}
	}

	/*========================================
	 * Getter/Setter
	 *========================================*/
	/**
	 * Retrieve a stored value from the given index.
	 * 
	 * @param i The row index to retrieve.
	 * @param j The column index to retrieve.
	 * @return The value stored at the given index.
	 */
	public double getValue(int i, int j) {
		return rows[i].getValue(j);
	}
	
	/**
	 * Set a new value at the given index.
	 * 
	 * @param i The row index to store new value.
	 * @param j The column index to store new value.
	 * @param value The value to store.
	 */
	public void setValue(int i, int j, double value) {
		if (value == 0.0) {
			rows[i].remove(j);
			cols[j].remove(i);
		}
		else {
			rows[i].setValue(j, value);
			cols[j].setValue(i, value);
		}
	}
	
	/**
	 * Set a new row vector at the given row index.
	 * @param i The row index to store new vector
	 * @param newVector 
	 */
	public void setRowVector(int i, SparseVector newVector) {
		if (newVector.length() != this.N)
			throw new RuntimeException("Vector lengths disagree");
		if (i < 0 || i >= this.M)
			throw new RuntimeException("Wrong input row index.");
		// Clear the values of the current rowVector.
		if (rows[i].indexList() != null) {
			for (int j : rows[i].indexList()) {
				this.setValue(i, j, 0);
			}
		}
		// Set the new vector.
		if (newVector.indexList() != null) {
			for (int j : newVector.indexList()) {
				this.setValue(i, j, newVector.getValue(j));
			}
		}
	}
	
	/**
	 * Set a new row vector with non-negative constraint at the given row index.
	 * If the value is negative, set it as 0.
	 * 
	 * @param i The row index to store new vector
	 * @param newVector 
	 */
	public void setRowVectorNonnegative(int i, SparseVector newVector) {
		if (newVector.length() != this.N)
			throw new RuntimeException("Vector lengths disagree");
		if (i < 0 || i >= this.M)
			throw new RuntimeException("Wrong input row index.");
		// Clear the values of the current rowVector.
		if (rows[i].indexList() != null) {
			for (int j : rows[i].indexList()) {
				this.setValue(i, j, 0);
			}
		}
		// Set the new vector with nonnegative constraint.
		if (newVector.indexList() != null) {
			for (int j : newVector.indexList()) {
				double value = newVector.getValue(j);
				this.setValue(i, j, value > 0 ? value : 0);
			}
		}
	}
	
	/**
	 * Set a new col vector at the given col index.
	 */
	public void setColVector(int j, SparseVector newVector) {
		if (newVector.length() != this.M)
			throw new RuntimeException("Vector lengths disagree");
		if (j < 0 || j >= this.N)
			throw new RuntimeException("Wrong input column index.");
		// Clear the values of the current colVector
		if (cols[j].indexList() != null) {
			for (int i : cols[j].indexList()) {
				this.setValue(i, j, 0);
			}
		}
		// Set the new vector.
		if (newVector.indexList() != null) {
			for (int i : newVector.indexList()) {
				this.setValue(i, j, newVector.getValue(i));
			}
		}
	}
	
	/**
	 * Set a new size of the matrix.
	 * 
	 * @param m The new row count.
	 * @param n The new column count.
	 */
	public void setSize(int m, int n) {
		this.M = m;
		this.N = n;
	}
	
	/**
	 * Return a reference of a given row.
	 * Make sure to use this method only for read-only purpose.
	 * 
	 * @param index The row index to retrieve.
	 * @return A reference to the designated row.
	 */
	public SparseVector getRowRef(int index) {
		return rows[index];
	}
	
	/**
	 * Return a copy of a given row.
	 * Use this if you do not want to affect to original data.
	 * 
	 * @param index The row index to retrieve.
	 * @return A reference to the designated row.
	 */
	public SparseVector getRow(int index) {
		SparseVector newVector = this.rows[index].copy();
		
		return newVector;
	}
	
	/**
	 * Return a reference of a given column.
	 * Make sure to use this method only for read-only purpose.
	 * 
	 * @param index The column index to retrieve.
	 * @return A reference to the designated column.
	 */
	public SparseVector getColRef(int index) {
		return cols[index];
	}
	
	/**
	 * Return a copy of a given column.
	 * Use this if you do not want to affect to original data.
	 * 
	 * @param index The column index to retrieve.
	 * @return A reference to the designated column.
	 */
	public SparseVector getCol(int index) {
		SparseVector newVector = this.cols[index].copy();
		
		return newVector;
	}
	
	/**
	 * Calculate average value for each row.
	 * 
	 * @param default_value The default average of a row if it has no values.
	 * @return A SparseVector that each value denotes the average of the row vector.
	 **/
	public SparseVector getRowAverage(double defalut_value) {
		SparseVector rowAverage = new SparseVector(this.M);
		for (int u = 0; u < this.M; u++) {
			SparseVector v = this.getRowRef(u);
			double avg = v.average();
			if (Double.isNaN(avg)) { // no rate is available: set it as median value.
				avg = defalut_value;
			}
			rowAverage.setValue(u, avg);
		}
		return rowAverage;
	}
	
	/**
	 * Calculate average value for each column.
	 * 
	 * @param default_value The default average of a column if it has no values.
	 * @return A SparseVector that each value denotes the average of the column vector.
	 */
	public SparseVector getColumnAverage(double defalut_value) {
		SparseVector columnAverage = new SparseVector(this.N);
		for (int i = 0; i < this.N; i++) {
			SparseVector j = this.getColRef(i);
			double avg = j.average();
			if (Double.isNaN(avg)) { // no rate is available: set it as median value.
				avg = defalut_value;
			}
			columnAverage.setValue(i, avg);
		}
		return columnAverage;
	}

	/*========================================
	 * Properties
	 *========================================*/
	/**
	 * Capacity of this matrix.
	 * 
	 * @return An array containing the length of this matrix.
	 * Index 0 contains row count, while index 1 column count.
	 */
	public int[] length() {
		int[] lengthArray = new int[2];
		
		lengthArray[0] = this.M;
		lengthArray[1] = this.N;
		
		return lengthArray;
	}
	
	/**
	 * Size of this matrix, M * N
	 */
	public int size() {
		return M * N;
	}
	
	/**
	 * Actual number of items in the matrix.
	 * 
	 * @return The number of items in the matrix.
	 */
	public int itemCount() { 
		int sum = 0;
		
		if (M > N) {
			for (int i = 0; i < M; i++) {
				sum += rows[i].itemCount();
			}
		}
		else {
			for (int j = 0; j < N; j++) {
				sum += cols[j].itemCount();
			}
		}
		
		return sum;
	}
	
	/**
	 * Number of non-zero elements in the matrix.
	 * 
	 * @return The number of non-zero elements in the matrix.
	 */
	public int nonZeroCount() {
		int sum = 0;
		if (M > N) {
			for (int i = 0; i < M; i++) {
				sum += rows[i].nonZeroCount();
			}
		}
		else {
			for (int j = 0; j < N; j++) {
				sum += cols[j].nonZeroCount();
			}
		}
		
		return sum;	
	}
	
	/**
	 * Return items in the diagonal in vector form.
	 * 
	 * @return Diagonal vector from the matrix.
	 */
	public SparseVector diagonal() {
		SparseVector v = new SparseVector(Math.min(this.M, this.N));
		
		for (int i = 0; i < Math.min(this.M, this.N); i++) {
			double value = this.getValue(i, i);
			if (value > 0.0) {
				v.setValue(i, value);
			}
		}
		
		return v;
	}
	
	/**
	 * The value of maximum element in the matrix.
	 * 
	 * @return The maximum value.
	 */
	public double max() {
		double curr = Double.MIN_VALUE;
		
		for (int i = 0; i < this.M; i++) {
			SparseVector v = this.getRowRef(i);
			if (v.itemCount() > 0) {
				double rowMax = v.max();
				if (v.max() > curr) {
					curr = rowMax;
				}
			}
		}
		
		return curr;
	}
	
	/**
	 * The value of minimum element in the matrix.
	 * 
	 * @return The minimum value.
	 */
	public double min() {
		double curr = Double.MAX_VALUE;
		
		for (int i = 0; i < this.M; i++) {
			SparseVector v = this.getRowRef(i);
			if (v.itemCount() > 0) {
				double rowMin = v.min();
				if (v.min() < curr) {
					curr = rowMin;
				}
			}
		}
		
		return curr;
	}
	
	/**
	 * Sum of every element. It ignores non-existing values.
	 * 
	 * @return The sum of all elements.
	 */
	public double sum() {
		double sum = 0.0;
		
		for (int i = 0; i < this.M; i++) {
			SparseVector v = this.getRowRef(i);
			sum += v.sum();
		}
		
		return sum;
	}
	
	/**
	 * Square sum of all elements. It ignores non-existing values.
	 * 
	 * @return The square sum of all elements
	 */
	public double squareSum() {
		double sum = 0.0;
		
		for (int i = 0; i < this.M; i++) {
			SparseVector v = this.getRowRef(i);
			sum += v.squareSum();
		}
		
		return sum;
	}
	/**
	 * Average of every element. It ignores non-existing values.
	 * 
	 * @return The average value.
	 */
	public double average() {
		return this.sum() / this.itemCount();
	}
	
	/**
	 * Variance of every element. It ignores non-existing values.
	 * 
	 * @return The variance value.
	 */
	public double variance() {
		double avg = this.average();
		double sum = 0.0;
		
		for (int i = 0; i < this.M; i++) {
			ArrayList<Integer> itemList = this.getRowRef(i).indexList();
			for (int j : itemList) {
				sum += Math.pow(this.getValue(i, j) - avg, 2);
			}
		}
		
		return sum / this.itemCount();
	}
	
	/**
	 * Standard Deviation of every element. It ignores non-existing values.
	 * 
	 * @return The standard deviation value.
	 */
	public double stdev() {
		return Math.sqrt(this.variance());
	}
	
	/**
	 * Return the (non-zero) index pairs.
	 * @return
	 */
	public ArrayList<Pair<Integer, Integer>> indexPairs() {
		ArrayList<Pair<Integer, Integer>> pairs = new ArrayList<Pair<Integer, Integer>>();
		for (int i = 0; i < M; i ++) {
			for (int j : rows[i].indexList()) {
				pairs.add(new Pair<Integer, Integer>(i, j));
			}
		}
		return pairs;
	}
	
	/*========================================
	 * Matrix operations
	 *========================================*/
	/**
	 * Scalar subtraction (aX).
	 * 
	 * @param alpha The scalar value to be multiplied to this matrix.
	 * @return The resulting matrix after scaling.
	 */
	public SparseMatrix scale(double alpha) {
		SparseMatrix A = new SparseMatrix(this.M, this.N);
		
		for (int i = 0; i < A.M; i++) {
			A.rows[i] = this.getRowRef(i).scale(alpha);
		}
		for (int j = 0; j < A.N; j++) {
			A.cols[j] = this.getColRef(j).scale(alpha);
		}
		
		return A;
	}
	
	/**
	 * Scalar subtraction (aX) on the matrix itself.
	 * This is used for minimizing memory usage.
	 * 
	 * @param alpha The scalar value to be multiplied to this matrix.
	 */
	public SparseMatrix selfScale(double alpha) {
		for (int i = 0; i < this.M; i++) {
			ArrayList<Integer> itemList = this.getRowRef(i).indexList();
			for (int j : itemList) {
				this.setValue(i, j, this.getValue(i, j) * alpha);
			}
		}
		return this;
	}
	
	/**
	 * Scalar addition.
	 * @param alpha The scalar value to be added to this matrix.
	 * @return The resulting matrix after addition.
	 */
	public SparseMatrix add(double alpha) {
		SparseMatrix A = new SparseMatrix(this.M, this.N);
		
		for (int i = 0; i < A.M; i++) {
			A.rows[i] = this.getRowRef(i).add(alpha);
		}
		for (int j = 0; j < A.N; j++) {
			A.cols[j] = this.getColRef(j).add(alpha);
		}
		
		return A;
	}
	
	/**
	 * Scalar addition on the matrix itself.
	 * @param alpha The scalar value to be added to this matrix.
	 */
	public void selfAdd(double alpha) {
		for (int i = 0; i < this.M; i++) {
			ArrayList<Integer> itemList = this.getRowRef(i).indexList();
			for (int j : itemList) {
				this.setValue(i, j, this.getValue(i, j) + alpha);
			}
		}
	}
	
	/**
	 * Exponential of a given constant.
	 * 
	 * @param alpha The exponent.
	 * @return The resulting exponential matrix.
	 */
	public SparseMatrix exp(double alpha) {
		for (int i = 0; i < this.M; i++) {
			SparseVector b = this.getRowRef(i);
			ArrayList<Integer> indexList = b.indexList();
			
			for (int j : indexList) {
				this.setValue(i, j, Math.pow(alpha, this.getValue(i, j)));
			}
		}
		
		return this;
	}
	
	/**
	 * The transpose of the matrix.
	 * This is simply implemented by interchanging row and column each other. 
	 * 
	 * @return The transpose of the matrix.
	 */
	public SparseMatrix transpose() {
		SparseMatrix A = new SparseMatrix(this.N, this.M);
		
		A.cols = this.rows;
		A.rows = this.cols;
		
		return A;
	}
	
	/**
	 * Matrix-vector product (b = Ax)
	 * 
	 * @param x The vector to be multiplied to this matrix.
	 * @throws RuntimeException when dimensions disagree
	 * @return The resulting vector after multiplication.
	 */
	public SparseVector times(SparseVector x) {
		if (N != x.length())
			throw new RuntimeException("Dimensions disagree");
		
		SparseMatrix A = this;
		SparseVector b = new SparseVector(M);
		
		for (int i = 0; i < M; i++) {
			b.setValue(i, A.rows[i].innerProduct(x));
		}
		
		return b;
	}
	
	/**
	 * Matrix-matrix product (C = AB)
	 * 
	 * @param B The matrix to be multiplied to this matrix.
	 * @throws RuntimeException when dimensions disagree
	 * @return The resulting matrix after multiplication.
	 */
	public SparseMatrix times(SparseMatrix B) {
		// original implementation
		if (N != (B.length())[0])
			throw new RuntimeException("Dimensions disagree");
		
		SparseMatrix A = this;
		SparseMatrix C = new SparseMatrix(M, (B.length())[1]);
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < (B.length())[1]; j++) {
				SparseVector x = A.getRowRef(i);
				SparseVector y = B.getColRef(j);
				
				if (x != null && y != null)
					C.setValue(i, j, x.innerProduct(y));
				else
					C.setValue(i, j, 0.0);
			}
		}
		
		return C;
	}
	
	/**
	 * Element-wise matrix product (C_ij = A_ij * B_ij)
	 * @param B
	 * @return
	 */
	public SparseMatrix dotTimes(SparseMatrix B) {
		if (M != B.M || N != B.N) {
			throw new RuntimeException("dotTimes: Matrices are not of the same size!");
		}
		SparseMatrix C = new SparseMatrix(M, N);
		for (int i = 0; i < M; i ++) {
			ArrayList<Integer> wordList = this.rows[i].indexList();
			for (int j : wordList) {
				double A_ij = getValue(i, j);
				double B_ij = B.getValue(i, j);
				if (A_ij != 0 && B_ij != 0) {
					C.setValue(i, j, A_ij * B_ij);
				}
			}
		}
		return C;
	}
	
	/**
	 * Element-wise matrix division (C_ij = A_ij / B_ij)
	 * It ignore 0 elements.
	 * @param B
	 * @return
	 */
	public SparseMatrix dotDivide(SparseMatrix B) {
		if (M != B.M || N != B.N) {
			throw new RuntimeException("dotDivide: Matrices are not of the same size!");
		}
		SparseMatrix C = new SparseMatrix(M, N);
		for (int i = 0; i < M; i ++) {
			ArrayList<Integer> wordList = this.rows[i].indexList();
			for (int j : wordList) {
				double A_ij = getValue(i, j);
				double B_ij = B.getValue(i, j);
				if (A_ij != 0 && B_ij != 0) {
					C.setValue(i, j, A_ij / B_ij);
				}
			}
		}		
		return C;
	}
	
	/** TF-IDF term weighting on an itemWords Matrix (row denotes item, column denotes word, each value is an integer).
	 *  
	 * @return TF-IDF term weighted matrix.
	 */
	public SparseMatrix tfidf() {
		SparseMatrix C = new SparseMatrix(M, N);
		for (int i = 0; i < M; i++) { // row represents a doc
			ArrayList<Integer> wordList = rows[i].indexList();
			for (int j : wordList) { // col represent a word
				if (this.getValue(i, j) != 0) {
					double TF = 1 + log2(getValue(i, j));
					double IDF = log2((double)M / cols[j].itemCount());
					C.setValue(i, j, TF * IDF);
				}
				
			}
		}
		return C;
	}
	
	/**
	 * IDF term weighting on an itemWords matrix.
	 * @return
	 */
	public SparseMatrix idf() {
		SparseMatrix C = new SparseMatrix(M, N);
		for (int i = 0; i < M; i++) { // row represents a doc
			ArrayList<Integer> wordList = rows[i].indexList();
			for (int j : wordList) { // col represent a word
				if (this.getValue(i, j) != 0) {
					double TF = 1;
					double IDF = log2((double)M / cols[j].itemCount());
					C.setValue(i, j, TF * IDF);
				}
				
			}
		}
		return C;
	}
	
	/**
	 * TF term weighting on an itemWords Matrix (row denotes item, column denotes word, each value is an integer).
	 * @return TF term weighted matrix.
	 */
	public SparseMatrix tf() {
		SparseMatrix C = new SparseMatrix(M, N);
		for (int i = 0; i < M; i++) { // row represents a doc
			ArrayList<Integer> wordList = rows[i].indexList();
			for (int j : wordList) { // col represent a word
				if (this.getValue(i, j) != 0) {
					double TF = 1 + log2(getValue(i, j));
					C.setValue(i, j, TF);
				}
				
			}
		}
		return C;
	}
	
	public SparseMatrix log2() {
		SparseMatrix C = new SparseMatrix(M, N);
		for (int i = 0; i < M; i++) {
			ArrayList<Integer> indexList = this.getRowRef(i).indexList();
			for (int j : indexList) {
				C.setValue(i, j, 1 + log2(this.getValue(i, j)));
			}
		}
		return C;
	}
	
	private double log2(double n) {
		return Math.log(n) / Math.log(2);
	}
	
	
	/** Convert a non-negative matrix to a row stochastic matrix (i.e. sum of a row is 1).
	 *  It ignores 0 row vector.
	 *  
	 * @return Row stochastic matrix
	 */
	public SparseMatrix rowStochastic() {
		SparseMatrix C = new SparseMatrix(M, N); 
		for (int i = 0; i < M; i++) {
			double sum  = rows[i].sum();
			if (sum != 0) {
				for (int j : this.rows[i].indexList()) {
					C.setValue(i, j, getValue(i, j) / sum);
				}
			}
		}
		return C;
	}
	
	/**
	 * Apply L2 norm on each row vector.
	 * It ignores 0 row vector.
	 * @return
	 */
	public SparseMatrix rowL2Norm() {
		SparseMatrix C = new SparseMatrix(M, N);
		for (int i = 0; i < M; i++) {
			double squareSum = rows[i].squareSum();
			if (squareSum != 0) {
				double l2_norm = Math.sqrt(squareSum);
				for (int j : rows[i].indexList()) {
					C.setValue(i, j, getValue(i, j) / l2_norm);
				}
			}
		}
		
		return C;
	}
	
	/** Convert a non-negative matrix to a column stochastic matrix (i.e. sum of a column is 1).
	 *  It ignores 0 column vector.
	 *  
	 * @return Column stochastic matrix.
	 */
	public SparseMatrix colStochastic() {
		SparseMatrix C = new SparseMatrix(this.M, this.N);
		for (int j = 0; j < this.N; j++) {
			double sum = this.cols[j].sum();
			if (sum != 0) {
				for (int i : this.cols[j].indexList()) {
					C.setValue(i, j, this.getValue(i, j) / sum);
				}
			}
		}
		return C;
	}

	/**
	 * Matrix-matrix product (A = AB), without using extra memory.
	 * 
	 * @param B The matrix to be multiplied to this matrix.
	 * @throws RuntimeException when dimensions disagree
	 */
	public void selfTimes(SparseMatrix B) {
		// original implementation
		if (N != (B.length())[0])
			throw new RuntimeException("Dimensions disagree");
		
		for (int i = 0; i < M; i++) {
			SparseVector tmp = new SparseVector(N);
			for (int j = 0; j < (B.length())[1]; j++) {
				SparseVector x = this.getRowRef(i);
				SparseVector y = B.getColRef(j);
				
				if (x != null && y != null)
					tmp.setValue(j, x.innerProduct(y));
				else
					tmp.setValue(j, 0.0);
			}
			
			for (int j = 0; j < (B.length())[1]; j++) {
				this.setValue(i, j, tmp.getValue(j));
			}
		}
	}

	/**
	 * Matrix-matrix sum (C = A + B)
	 * 
	 * @param B The matrix to be added to this matrix.
	 * @throws RuntimeException when dimensions disagree
	 * @return The resulting matrix after summation.
	 */
	public SparseMatrix plus(SparseMatrix B) {
		SparseMatrix A = this;
		if (A.M != B.M || A.N != B.N)
			throw new RuntimeException("Dimensions disagree");
		
		SparseMatrix C = new SparseMatrix(M, N);
		for (int i = 0; i < M; i++) {
			C.rows[i] = A.rows[i].plus(B.rows[i]);
		}
		for (int j = 0; j < N; j++) {
			C.cols[j] = A.cols[j].plus(B.cols[j]);
		}
		
		return C;
	}
	
	/**
	 * Matrix-matrix minus (C = A - B)
	 * 
	 * @param B The matrix to be deducted to this matrix.
	 * @throws RuntimeException when dimensions disagree
	 * @return The resulting matrix after minus.
	 */
	public SparseMatrix minus(SparseMatrix B) {
		SparseMatrix A = this;
		if (A.M != B.M || A.N != B.N)
			throw new RuntimeException("Dimensions disagree");
		
		SparseMatrix C = new SparseMatrix(M, N);
		for (int i = 0; i < M; i++) {
			C.rows[i] = A.rows[i].minus(B.rows[i]);
		}
		for (int j = 0; j < N; j++) {
			C.cols[j] = A.cols[j].minus(B.cols[j]);
		}
		
		return C;
	}
	
	/**
	 * Generate an identity matrix with the given size.
	 * 
	 * @param n The size of requested identity matrix.
	 * @return An identity matrix with the size of n by n. 
	 */
	public static SparseMatrix makeIdentity(int n) {
		SparseMatrix m = new SparseMatrix(n, n);
		for (int i = 0; i < n; i++) {
			m.setValue(i, i, 1.0);
		}
		
		return m;
	}
		
	/**
	 * Generate a uniform matrix with the given size.
	 * The sum of each row is 1.
	 * @param m
	 * @param n
	 * @return
	 */
	public static SparseMatrix makeUniform(int M, int N) {
		SparseMatrix m = new SparseMatrix(M, N);
		for (int i = 0; i < M; i ++) {
			for (int j = 0; j < N; j++) {
				m.setValue(i, j, 1.0 / N);
			}
		}
		return m;
	}
	
	/**
	 * Generate a random matrix with the given size and sparseRate.
	 * Each entry is in the range [0,1]
	 * @param M
	 * @param N
	 * @param sparseRate
	 * @return
	 */
	public static SparseMatrix makeRandom(int M, int N, double sparseRate) {
		if (sparseRate <=0 || sparseRate >1) {
			throw new RuntimeException("SparseRate input error!");
		}
		
		SparseMatrix m = new SparseMatrix(M, N);
		for (int i = 0; i < M; i ++) {
			for (int j = 0; j < N; j ++) {
				double random = Math.random();
				if (random < sparseRate) {
					m.setValue(i, j, Math.random());
				}
			}
		}
		return m;
	}
	
	/**
	 * Calculate inverse matrix.
	 * 
	 * @throws RuntimeException when dimensions disagree.
	 * @return The inverse of current matrix.
	 */
	public SparseMatrix inverse() {
		if (this.M != this.N)
			throw new RuntimeException("Dimensions disagree");
		
		SparseMatrix original = this;
		SparseMatrix newMatrix = makeIdentity(this.M);
		
		int n = this.M;
		
		if (n == 1) {
			newMatrix.setValue(0, 0, 1 / original.getValue(0, 0));
			return newMatrix;
		}

		SparseMatrix b = new SparseMatrix(original);
		
		for (int i = 0; i < n; i++) {
			// find pivot:
			double mag = 0;
			int pivot = -1;

			for (int j = i; j < n; j++) {
				double mag2 = Math.abs(b.getValue(j, i));
				if (mag2 > mag) {
					mag = mag2;
					pivot = j;
				}
			}

			// no pivot (error):
			if (pivot == -1 || mag == 0) {
				return newMatrix;
			}

			// move pivot row into position:
			if (pivot != i) {
				double temp;
				for (int j = i; j < n; j++) {
					temp = b.getValue(i, j);
					b.setValue(i, j, b.getValue(pivot, j));
					b.setValue(pivot, j, temp);
				}

				for (int j = 0; j < n; j++) {
					temp = newMatrix.getValue(i, j);
					newMatrix.setValue(i, j, newMatrix.getValue(pivot, j));
					newMatrix.setValue(pivot, j, temp);
				}
			}

			// normalize pivot row:
			mag = b.getValue(i, i);
			for (int j = i; j < n; j ++) {
				b.setValue(i, j, b.getValue(i, j) / mag);
			}
			for (int j = 0; j < n; j ++) {
				newMatrix.setValue(i, j, newMatrix.getValue(i, j) / mag);
			}

			// eliminate pivot row component from other rows:
			for (int k = 0; k < n; k ++) {
				if (k == i)
					continue;
				
				double mag2 = b.getValue(k, i);

				for (int j = i; j < n; j ++) {
					b.setValue(k, j, b.getValue(k, j) - mag2 * b.getValue(i, j));
				}
				for (int j = 0; j < n; j ++) {
					newMatrix.setValue(k, j, newMatrix.getValue(k, j) - mag2 * newMatrix.getValue(i, j));
				}
			}
		}
		
		return newMatrix;
	}
	
	/**
	 * Calculate Cholesky decomposition of the matrix.
	 * 
	 * @throws RuntimeException when matrix is not square.
	 * @return The Cholesky decomposition result.
	 */
	public SparseMatrix cholesky() {
		if (this.M != this.N)
			throw new RuntimeException("Matrix is not square");
		
		SparseMatrix A = this;
		
		int n = A.M;
		SparseMatrix L = new SparseMatrix(n, n);

		for (int i = 0; i < n; i++)  {
			for (int j = 0; j <= i; j++) {
				double sum = 0.0;
				for (int k = 0; k < j; k++) {
					sum += L.getValue(i, k) * L.getValue(j, k);
				}
				if (i == j) {
					L.setValue(i, i, Math.sqrt(A.getValue(i, i) - sum));
				}
				else {
					L.setValue(i, j, 1.0 / L.getValue(j, j) * (A.getValue(i, j) - sum));
				}
			}
			if (Double.isNaN(L.getValue(i, i))) {
				//throw new RuntimeException("Matrix not positive definite: (" + i + ", " + i + ")");
				return null;
			}
		}
		
		return L.transpose();
	}
	
	/**
	 * Generate a covariance matrix of the current matrix.
	 * 
	 * @return The covariance matrix of the current matrix.
	 */
	public SparseMatrix covariance() {
		int columnSize = this.N;
		SparseMatrix cov = new SparseMatrix(columnSize, columnSize);
		
		for (int i = 0; i < columnSize; i++) {
			for (int j = i; j < columnSize; j++) {
				SparseVector data1 = this.getCol(i);
				SparseVector data2 = this.getCol(j);
				double avg1 = data1.average();
				double avg2 = data2.average();
				
				double value = data1.sub(avg1).innerProduct(data2.sub(avg2)) / (data1.length()-1);
				cov.setValue(i, j, value);
				cov.setValue(j, i, value);
			}
		}
		
		return cov;
	}
	
	/*========================================
	 * Matrix operations (partial)
	 *========================================*/
	/**
	 * Scalar Multiplication only with indices in indexList.
	 * 
	 * @param alpha The scalar to be multiplied to this matrix.
	 * @param indexList The list of indices to be applied summation.
	 * @return The resulting matrix after scaling.
	 */
	public SparseMatrix partScale(double alpha, int[] indexList) {
		if (indexList != null) {
			for (int i : indexList) {
				for (int j : indexList) {
					this.setValue(i, j, this.getValue(i, j) * alpha);
				}
			}
		}
		
		return this;
	}
	
	/**
	 * Matrix summation (A = A + B) only with indices in indexList.
	 * 
	 * @param B The matrix to be added to this matrix.
	 * @param indexList The list of indices to be applied summation.
	 * @throws RuntimeException when dimensions disagree.
	 * @return The resulting matrix after summation.
	 */
	public SparseMatrix partPlus(SparseMatrix B, int[] indexList) {
		if (indexList != null) {
			if (this.M != B.M || this.N != B.N)
				throw new RuntimeException("Dimensions disagree");
			
			for (int i : indexList) {
				this.rows[i].partPlus(B.rows[i], indexList);
			}
			for (int j : indexList) {
				this.cols[j].partPlus(B.cols[j], indexList);
			}
		}
		
		return this;
	}
	
	/**
	 * Matrix subtraction (A = A - B) only with indices in indexList.
	 * 
	 * @param B The matrix to be subtracted from this matrix.
	 * @param indexList The list of indices to be applied subtraction.
	 * @throws RuntimeException when dimensions disagree.
	 * @return The resulting matrix after subtraction.
	 */
	public SparseMatrix partMinus(SparseMatrix B, int[] indexList) {
		if (indexList != null) {
			if (this.M != B.M || this.N != B.N)
				throw new RuntimeException("Dimensions disagree");
			
			for (int i : indexList) {
				this.rows[i].partMinus(B.rows[i], indexList);
			}
			for (int j : indexList) {
				this.cols[j].partMinus(B.cols[j], indexList);
			}
		}
		
		return this;
	}
	
	/**
	 * Matrix-vector product (b = Ax) only with indices in indexList.
	 * 
	 * @param x The vector to be multiplied to this matrix.
	 * @param indexList The list of indices to be applied multiplication.
	 * @return The resulting vector after multiplication.
	 */
	public SparseVector partTimes(SparseVector x, int[] indexList) {
		if (indexList == null)
			return x;
		
		SparseVector b = new SparseVector(M);
		
		for (int i : indexList) {
			b.setValue(i, this.rows[i].partInnerProduct(x, indexList));
		}
		
		return b;
	}
	
	/**
	 * Convert the matrix to a printable string.
	 * 
	 * @return The resulted string in the form of "(1, 2: 5.0) (2, 4: 4.5)"
	 */
	@Override
	public String toString() {
        String s = "";
        
        for (int i = 0; i < this.M; i++) {
        	SparseVector row = this.getRowRef(i);
        	if (row.itemCount() == 0)	continue;
        	for (int j : row.indexList()) {
        		s += "(" + i + ", " + j + ": " + this.getValue(i, j) + ") ";
        	}
        	s += "\r\n";
        }
        
        return s;
    }
}
