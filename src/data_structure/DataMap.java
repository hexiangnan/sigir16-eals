package data_structure;
import java.util.HashMap;
import java.util.Iterator;
import java.io.Serializable;

/**
 * This is a class implementing HashMap-based data map.
 * This data structure is used for implementing sparse vector and matrix.
 * 
 * @author Joonseok Lee
 * @since 2012. 4. 20
 * @version 1.1
 */
public class DataMap<Key extends Comparable<Key>, Val> implements Iterable<Key>, Serializable {
	private static final long serialVersionUID = 8001;
	
	/** Key-value mapping structure */
	private HashMap<Key, Val> map;

	/*========================================
	 * Constructors
	 *========================================*/
	/** Basic constructor without specifying the capacity. */
	public DataMap() {
		map = new HashMap<Key, Val>();
	}
	
	/**
	 * A constructor specifying the capacity.
	 * BE CAREFUL TO USE THIS! Never set the capacity too larger than actually needed.
	 * It will waste the memory space, reducing performance of your program.
	 */
	public DataMap(int capacity) {
		map = new HashMap<Key, Val>(capacity);
	}
	
	/*========================================
	 * Getter/Setter
	 *========================================*/
	/**
	 * Get a data value by the given key.
	 * 
	 * @param key The key to search.
	 * @return The data value associated with the given key.
	 */
	public Val get(Key key) {
		return map.get(key);
	}
	
	/**
	 * Set a data value with the given key.
	 * 
	 * @param key The key to set.
	 * @param value The data value associated with the given key.
	 */
	public void put(Key key, Val value) {
		if (value == null) {
			map.remove(key);
		}
		else {
			map.put(key, value);
		}
	}
	
	/**
	 * Remove a data element with the given key.
	 * 
	 * @param key The key to remove.
	 * @return The data value deleted with the given key.
	 */
	public Val remove(Key key) {
		return map.remove(key);
	}
	
	/**
	 * Check whether the map has a specific key inside it.
	 * 
	 * @param key The key to search.
	 * @return true if the map has the given key, false otherwise.
	 */
	public boolean contains(Key key) {
		return map.containsKey(key);
	}
	
	/**
	 * Get an iterator for the map.
	 * 
	 * @return The Iterator instance for the map.
	 */
	@Override
	public Iterator<Key> iterator() {
		return map.keySet().iterator();
	}
	
	/*========================================
	 * Properties
	 *========================================*/
	/**
	 * Count the number of elements in the map.
	 * 
	 * @return The number of items in the map.
	 */
	public int itemCount() {
		return map.size();
	}
}
