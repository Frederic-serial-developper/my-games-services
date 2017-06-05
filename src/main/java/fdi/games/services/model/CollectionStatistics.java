package fdi.games.services.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fdi.games.services.LocalDateTimeSerializer;

public class CollectionStatistics {

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private final LocalDateTime lasUpdate;

	private Long totalSize;

	private Long totalPlays;

	private final Map<RatingLevel, Integer> gamesByRatingLevel = new HashMap<>();

	private final Map<Integer, Integer> gamesByYear = new HashMap<>();

	private final Map<Integer, Integer> playsByYear = new HashMap<>();

	public CollectionStatistics(LocalDateTime lasUpdate) {
		super();
		this.lasUpdate = lasUpdate;
	}

	public Map<Integer, Integer> getGamesByYear() {
		return this.gamesByYear;
	}

	public Map<Integer, Integer> getPlaysByYear() {
		return this.playsByYear;
	}

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

	public void incrementYear(Integer year) {
		Integer count = this.gamesByYear.get(year);
		if (count == null) {
			count = new Integer(0);
		}
		count++;
		this.gamesByYear.put(year, count);
	}

	public void incrementPlay(Integer year, int toAdd) {
		Integer count = this.playsByYear.get(year);
		if (count == null) {
			count = new Integer(0);
		}
		count = count + toAdd;
		this.playsByYear.put(year, count);
	}

	public Map<RatingLevel, Integer> getGamesByRatingLevel() {
		return this.gamesByRatingLevel;
	}

	public LocalDateTime getLasUpdate() {
		return this.lasUpdate;
	}

}
