package fdi.games.services.model;

import java.util.HashMap;
import java.util.Map;

public class CollectionStatistics {

	private Long totalSize;

	private Long totalPlays;

	private final Map<RatingLevel, Integer> gamesByRatingLevel = new HashMap<>();

	public Long getTotalPlays() {
		return this.totalPlays;
	}

	public void setTotalPlays(Long totalPlays) {
		this.totalPlays = totalPlays;
	}

	public Long getTotalSize() {
		return this.totalSize;
	}

	public void setTotalSize(Long totalSize) {
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
