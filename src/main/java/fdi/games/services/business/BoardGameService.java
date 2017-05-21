package fdi.games.services.business;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import fdi.games.services.ws.bgg.BGGClient;
import fdi.games.services.ws.bgg.BGGException;
import fdi.games.services.ws.bgg.model.BGGGameList;

@Service
public class BoardGameService {

	final static Logger logger = LoggerFactory.getLogger(BoardGameService.class);

	@Inject
	private BGGClient bggClient;

	@Inject
	private BGGGameMapper mapper;

	private final LoadingCache<String, Collection<BoardGame>> gamesCache = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, Collection<BoardGame>>() {
				@Override
				public Collection<BoardGame> load(String usernameKey) throws BoardGameServiceException, BGGException {
					logger.info("fetch collection from boardgamegeek");
					final String[] keys = usernameKey.split("_");
					if (keys.length != 3) {
						throw new BoardGameServiceException("invalid index : " + usernameKey);
					}
					final String username = keys[0];
					final boolean includeExpansion = keys[1].equalsIgnoreCase("true");
					final boolean includePreviouslyOwned = keys[2].equalsIgnoreCase("true");
					final BGGGameList result = BoardGameService.this.bggClient.getCollection(username, includeExpansion,
							includePreviouslyOwned);
					logger.debug("found {} games for user {}",
							result.getBoardGames() == null ? 0 : result.getBoardGames().size(), usernameKey);
					if (result.getBoardGames() == null) {
						return Collections.emptyList();
					}
					return result.getBoardGames().stream().map(game -> BoardGameService.this.mapper.map(game))
							.collect(Collectors.toList());

				}
			});

	public Collection<BoardGame> getCollection(String username, boolean includeExpansions,
			boolean includePreviouslyOwned) throws BoardGameServiceException {
		logger.info("retrieve collection for user {}, includeExpansions={}, includePreviouslyOwned={}", username,
				includeExpansions, includePreviouslyOwned);
		try {
			return this.gamesCache.get(username + "_" + includeExpansions + "_" + includePreviouslyOwned);
		} catch (final ExecutionException e) {
			throw new BoardGameServiceException("Error while fetching collection", e);
		}
	}

	public CollectionStatistics getStatistics(String username, boolean includeExpansions,
			boolean includePreviouslyOwned) throws BoardGameServiceException {
		final CollectionStatistics stats = new CollectionStatistics();

		final Collection<BoardGame> games = getCollection(username, includeExpansions, includePreviouslyOwned);

		logger.info("compute collection statistics for user {}, includeExpansions={}", username, includeExpansions);

		stats.setTotalSize(new Long(games.size()));
		stats.setTotalPlays(countPlays(games));

		for (final BoardGame boardGame : games) {
			final RatingLevel ratingLevel = getRatingLevel(boardGame);
			stats.incrementRatingLevel(ratingLevel);

			stats.incrementYear(boardGame.getYear());
		}

		return stats;
	}

	private RatingLevel getRatingLevel(BoardGame game) {
		final RatingLevel[] levelsAvailable = RatingLevel.values();
		final Double rating = game.getRating();
		for (final RatingLevel ratingLevel : levelsAvailable) {
			if (ratingLevel.match(rating)) {
				return ratingLevel;
			}
		}
		return null;
	}

	private Long countPlays(Collection<BoardGame> games) {
		Long total = new Long(0);
		for (final BoardGame boardGame : games) {
			total = total + boardGame.getPlaysCount();
		}
		return total;
	}
}
