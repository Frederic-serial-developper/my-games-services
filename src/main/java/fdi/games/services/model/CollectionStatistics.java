package fdi.games.services.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fdi.games.services.LocalDateTimeSerializer;

public class CollectionStatistics {

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private final LocalDateTime lasUpdate;

	private final Statistics gamesStats;
	private final Statistics previousGamesStats;
	private final Statistics expansionsStats;
	private final Statistics previousExpansionsStats;

	private final Map<Integer, Integer> playsByYear = new HashMap<>();
	private Long totalPlays = 0l;

	public CollectionStatistics(LocalDateTime lasUpdate) {
		super();
		this.lasUpdate = lasUpdate;
		this.gamesStats = new Statistics();
		this.previousGamesStats = new Statistics();
		this.expansionsStats = new Statistics();
		this.previousExpansionsStats = new Statistics();
	}

	public void incrementPlay(Integer year, int toAdd) {
		Integer count = this.playsByYear.get(year);
		if (count == null) {
			count = new Integer(0);
		}
		count = count + toAdd;
		this.playsByYear.put(year, count);
	}

	public Map<Integer, Integer> getPlaysByYear() {
		return this.playsByYear;
	}

	public LocalDateTime getLasUpdate() {
		return this.lasUpdate;
	}

	public Statistics getGamesStats() {
		return this.gamesStats;
	}

	public Statistics getPreviousGamesStats() {
		return this.previousGamesStats;
	}

	public Statistics getExpansionsStats() {
		return this.expansionsStats;
	}

	public Statistics getPreviousExpansionsStats() {
		return this.previousExpansionsStats;
	}

	public Long getTotalPlays() {
		return this.totalPlays;
	}

	public void setTotalPlays(Long totalPlays) {
		this.totalPlays = totalPlays;
	}

}
