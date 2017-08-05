package fdi.games.services.model;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fdi.games.services.LocalDateSerializer;

public class Play {

	private final Long bggId;

	private final String gameName;

	@JsonSerialize(using = LocalDateSerializer.class)
	private final LocalDate date;

	private final Integer count;

	public Play(LocalDate date, Long bggId, String gameName, Integer count) {
		super();
		this.date = date;
		this.bggId = bggId;
		this.gameName = gameName;
		this.count = count;
	}

	public Long getBggId() {
		return this.bggId;
	}

	public LocalDate getDate() {
		return this.date;
	}

	public Integer getCount() {
		return this.count;
	}

	public String getGameName() {
		return this.gameName;
	}

}
