package fdi.games.services.business;

import org.springframework.stereotype.Service;

import fdi.games.services.model.BoardGame;
import fdi.games.services.model.BoardGameSource;
import fdi.games.services.model.BoardGameStatus;
import fdi.games.services.model.BoardGameType;
import fdi.games.services.ws.bgg.model.BGGGame;
import fdi.games.services.ws.bgg.model.BGGGameStat;

@Service
public class BGGGameMapper implements Mapper<BGGGame, BoardGame> {

	@Override
	public BoardGame map(BGGGame source) {
		final BoardGame game = new BoardGame();
		game.setId(source.getBggId());
		game.setName(source.getName() == null ? source.getOriginalname() : source.getName());
		game.setSource(BoardGameSource.BOARDGAMEGEEK);
		game.setImage(source.getThumbnailUrl());

		if (source.getType().equalsIgnoreCase("boardgame")) {
			game.setType(BoardGameType.GAME);
		} else {
			game.setType(BoardGameType.EXPANSION);
		}

		if (source.getStatus().getOwned() == 1) {
			game.setStatus(BoardGameStatus.OWNED);
		} else {
			game.setStatus(BoardGameStatus.PREVIOUSLY_OWNED);
		}

		final BGGGameStat stats = source.getStats();
		if (stats != null) {
			game.setMinPlayers(stats.getMinPlayers());
			game.setMaxPlayers(stats.getMaxPlayers());
			game.setPlayingTime(stats.getPlayingTime());
			if (stats.getRating() != null && stats.getRating().getAverage() != null) {
				game.setRating(stats.getRating().getAverage().getValue());
			}
		}

		game.setPlaysCount(source.getPlays());
		game.setYear(source.getYearPublished());
		return game;
	}

}
