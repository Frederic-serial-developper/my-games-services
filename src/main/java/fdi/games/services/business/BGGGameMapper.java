package fdi.games.services.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fdi.games.services.model.BoardGameUserData;
import fdi.games.services.model.BoardGameStatus;
import fdi.games.services.ws.bgg.model.BGGGame;

@Service
public class BGGGameMapper implements Mapper<BGGGame, BoardGameUserData> {

	final static Logger logger = LoggerFactory.getLogger(BGGGameMapper.class);

	@Override
	public BoardGameUserData map(BGGGame source) {
		final BoardGameUserData game = new BoardGameUserData();
		game.setId(source.getBggId());
		game.setName(source.getName() == null ? source.getOriginalname() : source.getName());
		game.setImage(source.getThumbnailUrl());

		if (source.getStatus().getOwned() == 1) {
			game.setStatus(BoardGameStatus.OWNED);
		} else {
			game.setStatus(BoardGameStatus.PREVIOUSLY_OWNED);
		}

		game.setPlaysCount(source.getPlays());

		return game;
	}

}
