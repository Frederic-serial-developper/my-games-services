package fdi.games.services.business;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fdi.games.services.model.BoardGameData;
import fdi.games.services.model.BoardGameSource;
import fdi.games.services.model.BoardGameType;
import fdi.games.services.ws.bgg.model.BGGGameDetail;
import fdi.games.services.ws.bgg.model.BGGGameInfo;

@Service
public class BGGGameDetailMapper implements Mapper<BGGGameDetail, BoardGameData> {

	final static Logger logger = LoggerFactory.getLogger(BGGGameDetailMapper.class);

	@Override
	public BoardGameData map(BGGGameDetail source) {
		final BoardGameData game = new BoardGameData();
		game.setId(source.getBggId());
		game.setSource(BoardGameSource.BOARDGAMEGEEK);
		game.setImage(source.getThumbnail());

		if (source.getType().equalsIgnoreCase("boardgame")) {
			game.setType(BoardGameType.GAME);
		} else {
			game.setType(BoardGameType.EXPANSION);
		}

		game.setMinPlayers(new Double(source.getMinplayers().getValue()).intValue());
		game.setMaxPlayers(new Double(source.getMaxplayers().getValue()).intValue());
		game.setPlayingTime(new Double(source.getPlayingtime().getValue()).intValue());

		if (source.getStats() != null) {
			game.setRating(source.getStats().getRating().getAverage().getValue());
		}

		game.setYear(new Double(source.getYearpublished().getValue()).intValue());

		game.setDescription(source.getDescription());
		final List<BGGGameInfo> infos = source.getInfos();
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

		return game;
	}

}
