package fdi.games.services.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fdi.games.services.model.BoardGame;
import fdi.games.services.model.BoardGameStatus;
import fdi.games.services.ws.bgg.model.BGGGame;

@Service
public class BGGGameMapper implements Mapper<BGGGame, BoardGame> {

	final static Logger logger = LoggerFactory.getLogger(BGGGameMapper.class);

	@Override
	public BoardGame map(BGGGame source) {
		final BoardGame game = new BoardGame();
		game.setId(source.getBggId());
		game.setName(source.getName() == null ? source.getOriginalname() : source.getName());

		if (source.getStatus().getOwned() == 1) {
			game.setStatus(BoardGameStatus.OWNED);
		} else {
			game.setStatus(BoardGameStatus.PREVIOUSLY_OWNED);
		}

		game.setPlaysCount(source.getPlays());

		return game;
	}

}
