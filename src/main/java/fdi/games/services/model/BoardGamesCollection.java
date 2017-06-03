package fdi.games.services.model;

import java.time.LocalDateTime;
import java.util.Collection;

public class BoardGamesCollection {

	private final LocalDateTime lasUpdate;

	private final Collection<BoardGame> games;

	public BoardGamesCollection(LocalDateTime lasUpdate, Collection<BoardGame> games) {
		super();
		this.games = games;
		this.lasUpdate = lasUpdate;
	}

	public LocalDateTime getLasUpdate() {
		return this.lasUpdate;
	}

	public Collection<BoardGame> getGames() {
		return this.games;
	}

}
