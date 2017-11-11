package fdi.games.services.model;

import java.util.HashMap;
import java.util.Map;

public class Statistics {

	private Long totalSize = 0l;

	private final Map<RatingLevel, Integer> gamesByRatingLevel = new HashMap<>();

	private final Map<Integer, Integer> gamesByYear = new HashMap<>();

	public Statistics() {
		super();
	}

	public void incrementRatingLevel(RatingLevel level) {
		Integer count = this.gamesByRatingLevel.get(level);
		if (count == null) {
			count = new Integer(0);
		}
		count++;
		this.gamesByRatingLevel.put(level, count);
	}

	public void incrementYear(Integer year) {
		Integer count = this.gamesByYear.get(year);
		if (count == null) {
			count = new Integer(0);
		}
		count++;
		this.gamesByYear.put(year, count);
	}

	public void incrementTotalSize() {
		this.totalSize++;
	}

	public Map<Integer, Integer> getGamesByYear() {
		return this.gamesByYear;
	}

	public Long getTotalSize() {
		return this.totalSize;
	}

	public Map<RatingLevel, Integer> getGamesByRatingLevel() {
		return this.gamesByRatingLevel;
	}

}
