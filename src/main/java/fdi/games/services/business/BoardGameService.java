package fdi.games.services.business;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fdi.games.services.model.BoardGame;
import fdi.games.services.model.CollectionStatistics;
import fdi.games.services.model.RatingLevel;
import fdi.games.services.mvc.BGGController;
import fdi.games.services.ws.bgg.BGGClient;
import fdi.games.services.ws.bgg.BGGException;
import fdi.games.services.ws.bgg.model.BGGGame;
import fdi.games.services.ws.bgg.model.BGGGameList;

@Service
public class BoardGameService {

	final static Logger logger = LoggerFactory.getLogger(BGGController.class);

	@Inject
	private BGGClient bggClient;

	@Inject
	private BGGGameMapper mapper;

	private final LoadingCache<Long, BoardGame> gamesCache = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<Long, BoardGame>() {
				@Override
				public BoardGame load(Long id) throws BoardGameServiceException {
					throw new BoardGameServiceException("not yet implemented");
				}
			});

	public Collection<BoardGame> getCollection(String username, boolean includeExpansions) throws BGGException {
		logger.info("retrieve collection for user {}, includeExpansions={}", username, includeExpansions);
		if (this.gamesCache.size() > 0) {
			logger.info("retrieve {} games from cache", this.gamesCache.size());
			return this.gamesCache.asMap().values();
		}
		logger.info("fetch collection from boardgamegeek");
		final BGGGameList result = this.bggClient.getCollection(username, includeExpansions);
		logger.debug("found {} games for user {}", result, username);
		for (final BGGGame bggGame : result.getBoardGames()) {
			final BoardGame game = this.mapper.map(bggGame);
			this.gamesCache.put(game.getId(), game);
		}
		return this.gamesCache.asMap().values();
	}

	public CollectionStatistics getStatistics(String username, boolean includeExpansions) throws BGGException {
		final CollectionStatistics stats = new CollectionStatistics();

		final Collection<BoardGame> games = getCollection(username, includeExpansions);

		logger.info("compute collection statistics for user {}, includeExpansions={}", username, includeExpansions);

		stats.setTotalSize(games.size());

		for (final BoardGame boardGame : games) {
			final Double rating = boardGame.getRating();
			final RatingLevel[] levelsAvailable = RatingLevel.values();
			for (final RatingLevel ratingLevel : levelsAvailable) {
				if (ratingLevel.match(rating)) {
					stats.incrementRatingLevel(ratingLevel);
					break;
				}
			}
		}

		return stats;
	}
}
