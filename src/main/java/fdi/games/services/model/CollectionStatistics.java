package fdi.games.services.model;

import java.util.HashMap;
import java.util.Map;

public class CollectionStatistics {

	private int totalSize;

	private final Map<RatingLevel, Integer> gamesByRatingLevel = new HashMap<>();

	public int getTotalSize() {
		return this.totalSize;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	public void incrementRatingLevel(RatingLevel level) {
		Integer count = this.gamesByRatingLevel.get(level);
		if (count == null) {
			count = new Integer(0);
		}
		count++;
		this.gamesByRatingLevel.put(level, count);
	}

	public Map<RatingLevel, Integer> getGamesByRatingLevel() {
		return this.gamesByRatingLevel;
	}

}
