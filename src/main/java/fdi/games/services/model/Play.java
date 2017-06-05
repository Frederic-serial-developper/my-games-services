package fdi.games.services.model;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fdi.games.services.LocalDateSerializer;

public class Play {

	private final String boardGame;

	@JsonSerialize(using = LocalDateSerializer.class)
	private final LocalDate date;

	private final Integer count;

	public Play(LocalDate date, String boardGame, Integer count) {
		super();
		this.date = date;
		this.boardGame = boardGame;
		this.count = count;
	}

	public String getBoardGame() {
		return this.boardGame;
	}

	public LocalDate getDate() {
		return this.date;
	}

	public Integer getCount() {
		return this.count;
	}

}
