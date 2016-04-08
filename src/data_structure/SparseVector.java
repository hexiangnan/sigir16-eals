package data_structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import utils.CommonUtils;
/**
 * This class implements sparse vector, containing empty values for most space.
 * 
 * @author Joonseok Lee
 * @since 2012. 4. 20
 * @version 1.1
 */
public class SparseVector implements Serializable{
	private static final long serialVersionUID = 8002;
	
	/** The length (maximum number of items to be stored) of sparse vector. */
	private int N;
	/** Data map for <index, value> pairs. */
	private DataMap<Integer, Double> map;

	/*========================================
	 * Constructors
	 *========================================*/
	/**
	 * Construct an empty sparse vector, with capacity 0.
	 * Capacity can be reset with setLength method later.
	 */
	public SparseVector() {
		this.N = 0;
		this.map = new DataMap<Integer, Double>();
	}
	
	/**
	 * Construct a new sparse vector with size n.
	 * 
	 * @param n The capacity of new sparse vector.
	 */
	public SparseVector(int n) {
		this.N = n;
		this.map = new DataMap<Integer, Double>();
	}
	
	/**
	 * Construct an empty sparse vector, with data copied from another sparse vector.
	 * 
	 * @param sv The vector having data being copied.
	 */
	public SparseVector(SparseVector sv) {
		this.N = sv.N;
		this.map = new DataMap<Integer, Double>();
		
		for (int i = 0; i < N; i++) {
			this.setValue(i, sv.getValue(i)); 
		}
	}

	/*========================================
	 * Getter/Setter
	 *========================================*/
	/**
	 * Set a new value at the given index.
	 * 
	 * @param i The index to store new value.
	 * @param value The value to store.
	 */
	public void setValue(int i, double value) {
		if (value == 0.0)
			map.remove(i);
		else
			map.put(i, value);
	}
	
	/**
	 * Set the values of current vector as newVector
	 * @param newVector
	 */
	public void setVector(SparseVector newVector) {
		if (this.length() != newVector.length()) {
			throw new RuntimeException("Vector length disagrees.");
		}
		ArrayList<Integer> indexList = this.indexList();
		for (int i : indexList) 
			this.setValue(i, 0);
		
		indexList = newVector.indexList();
		for (int i : indexList)
			this.setValue(i, newVector.getValue(i));
	}
	
	/**
	 * Retrieve a stored value from the given index.
	 * 
	 * @param i The index to retrieve.
	 * @return The value stored at the given index.
	 */
	public double getValue(int i) {
		if (map.contains(i))
			return map.get(i);
		else
			return 0.0;
	}
	
	/**
	 * Delete a value stored at the given index.
	 * 
	 * @param i The index to delete the value in it.
	 */
	public void remove(int i) {
		if (map.contains(i))
			map.remove(i);
	}
	
	/**
	 * Copy the whole sparse vector and make a clone.
	 * 
	 * @return A clone of the current sparse vector, containing same values.
	 */
	public SparseVector copy() {
		SparseVector newVector = new SparseVector(this.N);
		
		for (int i : this.map) {
			newVector.setValue(i, this.getValue(i));
		}
		
		return newVector;
	}
	
	/**
	 * Get an Arraylist of existing indices.
	 * @return An arraylist of integer, contain indices with valid items.
	 */
	public ArrayList<Integer> indexList() {
		if (this.itemCount() == 0)
			return new ArrayList<Integer>();
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i : this.map) {
			result.add(i);
		}
		
		return result;
	}
	
	/**
	 * Get a HashSet of existing indices.
	 * @return A hashset of integer, contain indices with valid items.
	 */
	public HashSet<Integer> indexSet() {
		if (this.itemCount() == 0)
			return new HashSet<Integer>();
		
		HashSet<Integer> result = new HashSet<Integer>();
		for (int i : this.map) {
			result.add(i);
		}
		
		return result;
	}
	
	/**
	 * Set a same value to every element.
	 * 
	 * @param value The value to assign to every element.
	 */
	public void initialize(double value) {
		for (int i = 0; i < this.N; i++) {
			this.setValue(i, value);
		}
	}
	
	/**
	 * Set same value to given indices.
	 * 
	 * @param index The list of indices, which will be assigned the new value.
	 * @param value The new value to be assigned.
	 */
	public void initialize(int[] index, double value) {
		for (int i = 0; i < index.length; i++) {
			this.setValue(index[i], value);
		}
	}
	
	/*========================================
	 * Properties
	 *========================================*/
	/**
	 * Capacity of this vector.
	 * 
	 * @return The length of sparse vector
	 */
	public int length() {
		return N;
	}

	/**
	 * Actual number of items in the vector.
	 * 
	 * @return The number of items in the vector.
	 */
	public int itemCount() {
		return map.itemCount();		
	}
	
	/**
	 * Number of non-zero elements in the vector.
	 * 
	 * @return The number of non-zero elements in the vector.
	 */
	public int nonZeroCount() {
		int count = 0;
		for (int i : map) {
			if (map.get(i) != 0)
				count ++;
		}
		return count;
	}
	
	
	/**
	 * Set a new capacity of the vector.
	 * 
	 * @param n The new capacity value.
	 */
	public void setLength(int n) {
		this.N = n;
	}
	
	/*========================================
	 * Unary Vector operations
	 *========================================*/
	/**
	 * Scalar addition operator.
	 * 
	 * @param alpha The scalar value to be added to the original vector.
	 * @return The resulting vector, added by alpha.
	 */
	public SparseVector add(double alpha) {
		SparseVector a = this;
		SparseVector c = new SparseVector(N);
		
		for (int i : a.map) {
			c.setValue(i, alpha + a.getValue(i));
		}
		
		return c;
	}
	
	/**
	 * Scalar subtraction operator.
	 * 
	 * @param alpha The scalar value to be subtracted from the original vector.
	 * @return The resulting vector, subtracted by alpha.
	 */
	public SparseVector sub(double alpha) {
		SparseVector a = this;
		SparseVector c = new SparseVector(N);
		
		for (int i : a.map) {
			c.setValue(i, a.getValue(i) - alpha);
		}
		
		return c;
	}
	
	/**
	 * Scalar multiplication operator.
	 * 
	 * @param alpha The scalar value to be multiplied to the original vector.
	 * @return The resulting vector, multiplied by alpha.
	 */
	public SparseVector scale(double alpha) {
		SparseVector a = this;
		SparseVector c = new SparseVector(N);
		
		if (alpha == 0)
			return c;
		for (int i : a.map) {
			c.setValue(i, alpha * a.getValue(i));
		}
		
		return c;
	}
	
	/**
	 * Scale multiplication operator on vector itself.
	 * @param alpha
	 * @return
	 */
	public SparseVector selfScale(double alpha) {
		SparseVector a = this;
		
		for (int i : a.map) {
			a.setValue(i, alpha * a.getValue(i));
		}
		return a;
	}
	
	/**
	 * Scalar power operator.
	 * 
	 * @param alpha The scalar value to be powered to the original vector.
	 * @return The resulting vector, powered by alpha.
	 */
	public SparseVector power(double alpha) {
		SparseVector a = this;
		SparseVector c = new SparseVector(N);
		
		for (int i : a.map) {
			c.setValue(i, Math.pow(a.getValue(i), alpha));
		}
		
		return c;
	}
	
	/**
	 * Exponential of a given constant.
	 * 
	 * @param alpha The exponent.
	 * @return The resulting exponential vector.
	 */
	public SparseVector exp(double alpha) {
		SparseVector a = this;
		SparseVector c = new SparseVector(N);
		
		for (int i : a.map) {
			c.setValue(i, Math.pow(alpha, a.getValue(i)));
		}
		
		return c;
	}
	
	public SparseVector log2() {
		SparseVector c = new SparseVector(N);
		
		for (int i : this.map) {
			c.setValue(i, 1 + log2(this.getValue(i)));
		}
		return c;
	}
	
	private double log2(double n) {
		return Math.log(n) / Math.log(2);
	}
	
	/**
	 * Return a uniform vector of size n.
	 * @param n
	 */
	public static SparseVector makeUniform(int n) {
		SparseVector v = new SparseVector(n);
		double val = 1.0 / n;
		for (int i = 0; i < n; i++) {
			v.setValue(i, val);
		}
		return v;
 	}
	
	/**
	 * Randomly generate a vector of dimension m. Each value is in the range [0,1]
	 * @param m
	 * @return
	 */
	public static SparseVector makeRandom(int m) {
		SparseVector a = new SparseVector(m);
		for (int i = 0; i < m; i++) {
			a.setValue(i, Math.random());
		}
		return a;
	}
	
	/**
	 * Calculate cosine similarity of two sparse vectors.
	 * @param a
	 * @param b
	 * @return
	 */
	public static double cosineSimilarity(SparseVector a, SparseVector b) {
		if (a.itemCount() == 0 || b.itemCount() == 0)
			return 0;
		
		double innerProduct = a.innerProduct(b);
		return innerProduct == 0 ? 0 :
			innerProduct / (Math.sqrt(a.squareSum()) * Math.sqrt(b.squareSum()));
	}

	
	/**
	 * 2-norm of the vector.
	 * 
	 * @return 2-norm value of the vector.
	 */
	public double norm() {
		SparseVector a = this;
		return Math.sqrt(a.innerProduct(a));
	}
	
	/**
	 * L1 norm (sum of elements is 1) of the vector.
	 * @return L1-norm of the vector. 
	 */
	public SparseVector L1_norm() {
		double sum = this.sum();
		return this.scale(1.0 / sum);
	}
	
	/**
	 * Sum of every element in the vector.
	 * 
	 * @return Sum value of every element.
	 */
	public double sum() {
		SparseVector a = this;
		
		double sum = 0.0;
		for (int i : a.map) {
			sum += a.getValue(i);
		}
		
		return sum;
	}
	
	/**
	 * Square sum of all elements in the vector.
	 * 
	 * @return Square sum of all elements.
	 */
	public double squareSum() {
		return this.innerProduct(this);
	}
	
	/**
	 * The value of maximum element in the vector.
	 * 
	 * @return Maximum value in the vector.
	 */
	public double max() {
		SparseVector a = this;
		
		double curr = Double.MIN_VALUE;
		for (int i : a.map) {
			if (a.getValue(i) > curr) {
				curr = a.getValue(i);
			}
		}
		
		return curr;
	}
	
	/**
	 * The value of minimum element in the vector.
	 * 
	 * @return Minimum value in the vector.
	 */
	public double min() {
		SparseVector a = this;
		
		double curr = Double.MAX_VALUE;
		for (int i : a.map) {
			if (a.getValue(i) < curr) {
				curr = a.getValue(i);
			}
		}
		
		return curr;
	}
	
	/**
	 * Sum of absolute value of every element in the vector.
	 * 
	 * @return Sum of absolute value of every element.
	 */
	public double absoluteSum() {
		SparseVector a = this;
		
		double sum = 0.0;
		for (int i : a.map) {
			sum += Math.abs(a.getValue(i));
		}
		
		return sum;
	}
	
	/**
	 * Average of every element. It ignores non-existing values.
	 * 
	 * @return The average value.
	 */
	public double average() {
		SparseVector a = this;
		
		return a.sum() / (double) a.itemCount();
	}
	
	/**
	 * Variance of every element. It ignores non-existing values.
	 * 
	 * @return The variance value.
	 */
	public double variance() {
		double avg = this.average();
		double sum = 0.0;
		
		for (int i : this.map) {
			sum += Math.pow(this.getValue(i) - avg, 2);
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
	
	/*========================================
	 * Binary Vector operations
	 *========================================*/
	/**
	 * Vector sum (a + b)
	 * 
	 * @param b The vector to be added to this vector.
	 * @return The resulting vector after summation.
	 */
	public SparseVector plus(SparseVector b) {
		SparseVector a = this;
		if (a.N != b.N)
			throw new RuntimeException("Vector lengths disagree");
		
		SparseVector c = new SparseVector(N);
		for (int i : a.map)
			c.setValue(i, a.getValue(i));  // c = a
		for (int i : b.map)
			c.setValue(i, b.getValue(i) + c.getValue(i)); // c = c + b
		
		return c;
	}
	
	/**
	 * Vector sum on itself (a + b)
	 * @param b
	 * @return
	 */
	public SparseVector selfPlus(SparseVector b) {
		SparseVector a = this;
		if (a.N != b.N)
			throw new RuntimeException("Vector lengths disagree");
		
		for (int i : b.map) {
			a.setValue(i, a.getValue(i) + b.getValue(i));
		}
		return a;
	}
	
	/**
	 * Vector subtraction (a - b)
	 * 
	 * @param b The vector to be subtracted from this vector.
	 * @return The resulting vector after subtraction.
	 */
	public SparseVector minus(SparseVector b) {
		SparseVector a = this;
		if (a.N != b.N)
			throw new RuntimeException("Vector lengths disagree");
		
		SparseVector c = new SparseVector(N);
		for (int i : a.map)
			c.setValue(i, a.getValue(i));  // c = a
		for (int i : b.map)
			c.setValue(i, c.getValue(i) - b.getValue(i)); // c = c - b
		
		return c;
	}
	
	/**
	 * Vector subtraction on itself (a - b)
	 * @param b
	 * @return
	 */
	public SparseVector selfMinus(SparseVector b) {
		SparseVector a = this;
		if (a.N != b.N)
			throw new RuntimeException("Vector lengths disagree");
		
		for (int i : b.map) {
			a.setValue(i, a.getValue(i) - b.getValue(i));
		}
		return a;
	}
	
	/**
	 * Vector subtraction (a - b), for only existing values.
	 * The resulting vector can have a non-zero value only if both vectors have a value at the index.
	 * 
	 * @param b The vector to be subtracted from this vector.
	 * @return The resulting vector after subtraction.
	 */
	public SparseVector commonMinus(SparseVector b) {
		SparseVector a = this;
//		if (a.N != b.N)
//			throw new RuntimeException("Vector lengths disagree");
		
		SparseVector c = new SparseVector(N);
		if (a.itemCount() <= b.itemCount()) {
			for (int i : a.map) {
				if (b.map.contains(i)) c.setValue(i, a.getValue(i) - b.getValue(i));
			}
		}
		else {
			for (int i : b.map) {
				if (a.map.contains(i)) c.setValue(i, a.getValue(i) - b.getValue(i));
			}
		}
		
		return c;
	}
	
	/**
	 * Inner product of two vectors.
	 * 
	 * @param b The vector to be inner-producted with this vector.
	 * @return The inner-product value.
	 */
	public double innerProduct(SparseVector b) {
		SparseVector a = this;
		double sum = 0.0;
		
		if (a.N != b.N)
			throw new RuntimeException("Vector lengths disagree");
		
		// iterate over the vector with the fewer items
		if (a.itemCount() <= b.itemCount()) {
			for (int i : a.map) {
				if (b.map.contains(i)) sum += a.getValue(i) * b.getValue(i);
			}
		}
		else {
			for (int i : b.map) {
				if (a.map.contains(i)) sum += a.getValue(i) * b.getValue(i);
			}
		}
		
		return sum;
	}
	
	/**
	 * Outer product of two vectors.
	 * 
	 * @param b The vector to be outer-producted with this vector.
	 * @return The resulting outer-product matrix. 
	 */
	public SparseMatrix outerProduct(SparseVector b) {
		SparseMatrix A = new SparseMatrix(this.N, b.N);
		
		for (int i = 0; i < this.N; i++) {
			for (int j = 0; j < b.N; j++) {
				A.setValue(i, j, this.getValue(i) * b.getValue(j));
			}
		}
		
		return A;
	}
	
	/**
	 * Dot product of two vectors (c_i = a_i * b_i)
	 * @param b
	 * @return The resulting doc-product vector.
	 */
	public SparseVector dotProduct(SparseVector b) {
		if (N != b.N)
			throw new RuntimeException("dotProduct Error - Vector lengths disagree");
		
		SparseVector c = new SparseVector(N);
		for (int i : map) {
			if (getValue(i) != 0 && b.getValue(i)!= 0)
				c.setValue(i, getValue(i) * b.getValue(i));
		}
		return c;
	}
	
	/*========================================
	 * Binary Vector operations (partial)
	 *========================================*/
	/**
	 * Vector sum (a + b) for indices only in the given indices.
	 * 
	 * @param b The vector to be added to this vector.
	 * @param indexList The list of indices to be applied summation.
	 * @return The resulting vector after summation.
	 */
	public SparseVector partPlus(SparseVector b, int[] indexList) {
		if (indexList == null)
			return this;
		
		if (this.N != b.N)
			throw new RuntimeException("Vector lengths disagree");
		
		for (int i : indexList)
			this.setValue(i, this.getValue(i) + b.getValue(i)); // c = c + b
		
		return this;
	}
	
	/**
	 * Vector subtraction (a - b) for indices only in the given indices.
	 * 
	 * @param b The vector to be subtracted from this vector.
	 * @param indexList The list of indices to be applied subtraction.
	 * @return The resulting vector after subtraction.
	 */
	public SparseVector partMinus(SparseVector b, int[] indexList) {
		if (indexList == null)
			return this;
		
		if (this.N != b.N)
			throw new RuntimeException("Vector lengths disagree");
		
		for (int i : indexList)
			this.setValue(i, this.getValue(i) - b.getValue(i)); // c = c - b
		
		return this;
	}
	
	/**
	 * Inner-product for indices only in the given indices.
	 * 
	 * @param b The vector to be inner-producted with this vector.
	 * @param indexList The list of indices to be applied inner-product.
	 * @return The inner-product value.
	 */
	public double partInnerProduct(SparseVector b, int[] indexList) {
		double sum = 0.0;
		
		if (indexList != null) {
			for (int i : indexList) {
				sum += this.getValue(i) * b.getValue(i);
			}
		}
		
		return sum;
	}
	
	/**
	 * Outer-product for indices only in the given indices.
	 * 
	 * @param b The vector to be outer-producted with this vector.
	 * @param indexList The list of indices to be applied outer-product.
	 * @return The outer-product value.
	 */
	public SparseMatrix partOuterProduct(SparseVector b, int[] indexList) {
		if (indexList == null)
			return null;
		
		SparseMatrix A = new SparseMatrix(b.length(), b.length());
		
		for (int i : indexList) {
			for (int j : indexList) {
				A.setValue(i, j, this.getValue(i) * b.getValue(j));
			}
		}
		
		return A;
	}
	
	/**
	 * Get the topK indices with largest values. 
	 * @param topK
	 * @param igonoreIndices Indices to ignore. 
	 * @return
	 */
	public ArrayList<Integer> topIndicesByValue(int topK, ArrayList<Integer> ignoreIndices) {
		HashMap<Integer, Double> hashmap = new HashMap<Integer, Double>();
		for (int j : this.indexList()) {
			hashmap.put(j, this.getValue(j));
		}
		return CommonUtils.TopKeysByValue(hashmap, topK, ignoreIndices);
	}
	
	/**
	 * Convert the vector to a printable string.
	 * 
	 * @return The resulted string in the form of "(1: 5.0) (2: 4.5)"
	 */
	@Override
	public String toString() {
        String s = "";
        for (int i : this.map) {
        	s += String.format("(%d:\t%.6f) ", i, map.get(i));
            // s += "(" + i + ": " + map.get(i) + ") ";
        }
        return s;
    }
	
	public String KeysToString() {
		String s = "[";
		for (int i : this.map) {
			s += i + ", ";
		}
		s += "]";
		return s;
	}
}
