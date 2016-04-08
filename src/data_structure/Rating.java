package data_structure;

public class Rating {
	public int userId; // user id, starts from 0
	public int itemId; // item id, starts from 0
	public float score;
	public long timestamp;
	
	public Rating(int userId, int itemId, float score, long timestamp) {
		this.userId = userId;
		this.itemId = itemId;
		this.score = score;
		this.timestamp = timestamp;
	}
	
	public Rating(String line) {
		String[] arr = line.split("\t");
		userId = Integer.parseInt(arr[0]);
		itemId = Integer.parseInt(arr[1]);
		score = Float.parseFloat(arr[2]);
		if (arr.length > 3)	timestamp = Long.parseLong(arr[3]);
	}
	
	public String toString() {
		return "<" + userId + "," + itemId + "," + score + "," + timestamp + ">";
	}
}