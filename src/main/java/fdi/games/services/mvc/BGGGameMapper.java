package fdi.games.services.mvc;

import org.springframework.stereotype.Service;

import fdi.games.services.mvc.model.BoardGame;
import fdi.games.services.mvc.model.BoardGameSource;
import fdi.games.services.ws.bgg.model.BGGGame;

@Service
public class BGGGameMapper implements Mapper<BGGGame, BoardGame> {

	@Override
	public BoardGame map(BGGGame source) {
		final BoardGame game = new BoardGame();
		game.setId(1l); // TODO calculate id
		game.setName(source.getName() == null ? source.getOriginalname() : source.getName());
		game.setSource(BoardGameSource.BOARDGAMEGEEK);
		game.setImage(source.getThumbnailUrl());
		game.setRank(5.3); // TODO calculate rank
		game.setPlays(source.getPlays());
		return game;
	}

}
