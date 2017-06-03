package fdi.games.services.business;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fdi.games.services.model.BoardGame;
import fdi.games.services.model.BoardGameSource;
import fdi.games.services.model.BoardGameStatus;
import fdi.games.services.model.BoardGameType;
import fdi.games.services.ws.bgg.model.BGGGame;
import fdi.games.services.ws.bgg.model.BGGGameDetail;
import fdi.games.services.ws.bgg.model.BGGGameInfo;
import fdi.games.services.ws.bgg.model.BGGGameStat;

@Service
public class BGGGameMapper implements Mapper<BGGGame, BoardGame> {

	final static Logger logger = LoggerFactory.getLogger(BGGGameMapper.class);

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

		final BGGGameDetail details = source.getDetails();
		if (details != null) {
			game.setDescription(details.getDescription());
			final List<BGGGameInfo> infos = details.getInfos();
			for (final BGGGameInfo bggGameInfo : infos) {
				final String type = bggGameInfo.getType();
				if ("boardgamecategory".equalsIgnoreCase(type)) {
					game.addCategory(bggGameInfo.getValue());
				} else if ("boardgamemechanic".equalsIgnoreCase(type)) {
					game.addMechanism(bggGameInfo.getValue());
				} else if ("boardgameexpansion".equalsIgnoreCase(type)) {
					game.addExpansion(bggGameInfo.getValue());
				} else {
					logger.trace("discard info {} for game {}", bggGameInfo, game.getName());
				}
			}
		}

		return game;
	}

}
