package fdi.games.services.business;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fdi.games.services.model.BoardGame;
import fdi.games.services.model.BoardGameData;
import fdi.games.services.model.BoardGameWithData;
import fdi.games.services.model.BoardGamesCollection;
import fdi.games.services.model.CollectionStatistics;
import fdi.games.services.model.Play;
import fdi.games.services.model.RatingLevel;
import fdi.games.services.ws.bgg.BGGClient;
import fdi.games.services.ws.bgg.BGGException;
import fdi.games.services.ws.bgg.model.BGGGame;
import fdi.games.services.ws.bgg.model.BGGGameDetail;
import fdi.games.services.ws.bgg.model.BGGPlay;

@Service
public class BoardGameService {

	final static Logger logger = LoggerFactory.getLogger(BoardGameService.class);

	private static final long _15_MIN = 900_000;
	private static final long _10_SECONDS = 10_000;

	@Inject
	private BGGClient bggClient;

	@Inject
	private BGGGameMapper bggGameMapper;

	@Inject
	private BGGGameDetailMapper bggGameDetailMapper;

	@Value("${my-games-services.cache.vip}")
	private String[] vips;

	@Value("${my-games-services.cache.expirationInMinutes}")
	private Integer cacheExpiration;

	@Value("${my-games-services.cache.collection.maxSize}")
	private Integer cacheCollectionMaxSize;

	@Value("${my-games-services.cache.data.maxSize}")
	private Integer cacheDataMaxSize;

	@Value("${my-games-services.cache.plays.maxSize}")
	private Integer cachePlaysMaxSize;

	@Value("${my-games-services.bgg.delay}")
	private Integer bggDelay;

	private Cache<String, BoardGamesCollection> collectionsCache;

	private Cache<Long, BoardGameData> gamesDataCache;

	private LoadingCache<String, Collection<Play>> playsCache;

	public Collection<BoardGameWithData> getCollection(String username, boolean includeExpansions,
			boolean includePreviouslyOwned) throws BoardGameServiceException {
		logger.info("retrieve collection for user {}, includeExpansions={}, includePreviouslyOwned={}", username,
				includeExpansions, includePreviouslyOwned);
		final Collection<BoardGame> games = this.collectionsCache.getIfPresent(username).getGames();
		final Collection<BoardGame> filteredGames = filter(games, includeExpansions, includePreviouslyOwned);

		final Collection<BoardGameWithData> gamesWithData = filteredGames.stream().map(game -> getGameWithData(game))
				.collect(Collectors.toSet());

		return gamesWithData;
	}

	private BoardGameWithData getGameWithData(BoardGame game) {
		final BoardGameWithData boardGameWithData = new BoardGameWithData();
		boardGameWithData.setId(game.getId());
		boardGameWithData.setName(game.getName());
		boardGameWithData.setStatus(game.getStatus());
		boardGameWithData.setPlaysCount(game.getPlaysCount());
		boardGameWithData.setData(this.gamesDataCache.getIfPresent(game.getId()));
		return boardGameWithData;
	}

	private Collection<BoardGame> filter(Collection<BoardGame> games, boolean includeExpansions,
			boolean includePreviouslyOwned) {
		final Set<BoardGame> filteredGames = new HashSet<>();
		for (final BoardGame game : games) {
			final BoardGameData gameData = this.gamesDataCache.getIfPresent(game.getId());
			final boolean shouldFilter = !includeExpansions && gameData.isExpansion()
					|| !includePreviouslyOwned && game.isPreviouslyOwned();
			if (!shouldFilter) {
				filteredGames.add(game);
			}

		}
		return filteredGames;
	}

	@Scheduled(initialDelay = _10_SECONDS, fixedDelay = _15_MIN)
	private void refreshVips() throws InterruptedException {
		for (final String vip : this.vips) {
			logger.info("refresh cache informations for {}", vip);
			try {
				delay();
				final Collection<BoardGame> games = fetchGames(vip);
				this.collectionsCache.put(vip, new BoardGamesCollection(LocalDateTime.now(), games));
				delay();
				try {
					final Multimap<LocalDate, Play> plays = fetchPlays(vip);
					this.playsCache.put(vip, plays.values());
				} catch (final BoardGameServiceException e) {
					logger.error("cannot fetch plays for {}", vip, e);
				}

			} catch (final BGGException e) {
				logger.error("error while refreshing cache informations for " + vip, e);
			}
		}
	}

	private void delay() throws InterruptedException {
		logger.debug("wait {} seconds", this.bggDelay);
		TimeUnit.SECONDS.sleep(this.bggDelay);
	}

	public CollectionStatistics getStatistics(String username, boolean includeExpansions,
			boolean includePreviouslyOwned) throws BoardGameServiceException {
		final BoardGamesCollection boardGamesCollection = this.collectionsCache.getIfPresent(username);
		final CollectionStatistics stats = new CollectionStatistics(boardGamesCollection.getLasUpdate());

		final Collection<BoardGame> games = filter(boardGamesCollection.getGames(), includeExpansions,
				includePreviouslyOwned);

		logger.info("compute collection statistics for user {}, includeExpansions={}", username, includeExpansions);

		stats.setTotalSize(new Long(games.size()));
		stats.setTotalPlays(countPlays(games));

		for (final BoardGame boardGame : games) {
			final BoardGameData gameData = this.gamesDataCache.getIfPresent(boardGame.getId());
			if (gameData != null) {
				final RatingLevel ratingLevel = getRatingLevel(gameData);
				stats.incrementRatingLevel(ratingLevel);
				stats.incrementYear(gameData.getYear());
			}
		}

		final Collection<Play> plays = getPlays(username);
		for (final Play play : plays) {
			final Integer year = play.getDate().getYear();
			final Integer count = play.getCount();
			stats.incrementPlay(year, count);
		}

		return stats;
	}

	private RatingLevel getRatingLevel(BoardGameData game) {
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

	public Collection<Play> getPlays(String username) throws BoardGameServiceException {
		try {
			return this.playsCache.get(username);
		} catch (final ExecutionException e) {
			throw new BoardGameServiceException("error while retrieving plays for " + username, e);
		}
	}

	private Multimap<LocalDate, Play> fetchPlays(String username) throws BoardGameServiceException {
		try {
			logger.info("fetch plays from boardgamegeek for {}", username);
			final List<BGGPlay> plays = this.bggClient.getPlays(username);
			logger.info("found {} plays for user {}", plays.size(), username);

			final Multimap<LocalDate, Play> playsByDate = ArrayListMultimap.create();
			for (final BGGPlay bggPlay : plays) {
				final LocalDate date = LocalDate.parse(bggPlay.getDate());
				playsByDate.put(date, new Play(date, bggPlay.getItem().getName(), bggPlay.getQuantity()));
			}
			return playsByDate;
		} catch (final BGGException e) {
			throw new BoardGameServiceException("error while retrieving plays from BGG", e);
		}
	}

	public Map<Long, BGGGameDetail> getGameDetails(Long bggId) throws BoardGameServiceException {
		try {
			return this.bggClient.getDetails(Sets.newHashSet(bggId));
		} catch (final BGGException e) {
			throw new BoardGameServiceException("error while retrieving game details from BGG", e);
		}
	}

	private Collection<BoardGame> fetchGames(String username) throws BGGException {
		logger.info("fetch collection from boardgamegeek for {}", username);
		final List<BGGGame> result = BoardGameService.this.bggClient.getCollection(username, true, true);
		final Set<BoardGame> games = result.stream().map(game -> this.bggGameMapper.map(game))
				.collect(Collectors.toSet());
		this.collectionsCache.put(username, new BoardGamesCollection(LocalDateTime.now(), games));
		logger.info("found {} games for user {}", result.size(), username);

		logger.info("fetch games data from boardgamegeek for {}", username);
		final Set<Long> ids = result.stream().map(game -> game.getBggId()).collect(Collectors.toSet());
		final Set<Long> idsToFetch = Sets.newHashSet();
		for (final Long id : ids) {
			if (this.gamesDataCache.getIfPresent(id) == null) {
				idsToFetch.add(id);
			}
		}
		final Map<Long, BGGGameDetail> detailsById = BoardGameService.this.bggClient.getDetails(idsToFetch);
		for (final BGGGameDetail detail : detailsById.values()) {
			this.gamesDataCache.put(detail.getBggId(), this.bggGameDetailMapper.map(detail));
		}
		final Collection<Long> missingDetails = CollectionUtils.subtract(idsToFetch, detailsById.keySet());
		if (CollectionUtils.isNotEmpty(missingDetails)) {
			logger.warn("cannot find details for {} : games {}", missingDetails.size(), missingDetails);
		}

		logger.info("found {} games details for user {}", detailsById.size(), username);

		return result.stream().map(game -> BoardGameService.this.bggGameMapper.map(game)).collect(Collectors.toList());
	}

	@PostConstruct
	private void initialize() {
		logger.info(
				"initialize cache: cacheDataMaxSize={} objects, cacheCollectionMaxSize={} objects, cachePlaysMaxSize={} objects, cacheExpiration={} min",
				this.cacheDataMaxSize, this.cacheCollectionMaxSize, this.cachePlaysMaxSize, this.cacheExpiration);
		this.gamesDataCache = CacheBuilder.newBuilder().maximumSize(this.cacheDataMaxSize)
				.expireAfterWrite(this.cacheExpiration, TimeUnit.MINUTES).build();

		this.collectionsCache = CacheBuilder.newBuilder().maximumSize(this.cacheCollectionMaxSize)
				.expireAfterWrite(this.cacheExpiration, TimeUnit.MINUTES).build();

		this.playsCache = CacheBuilder.newBuilder().maximumSize(this.cachePlaysMaxSize)
				.expireAfterWrite(this.cacheExpiration, TimeUnit.MINUTES)
				.build(new CacheLoader<String, Collection<Play>>() {
					@Override
					public Collection<Play> load(String username) throws BoardGameServiceException, BGGException {
						final long start = System.currentTimeMillis();
						final Multimap<LocalDate, Play> playsByDate = fetchPlays(username);
						final long end = System.currentTimeMillis();
						logger.info("{} games fetched for {} in {} msec", playsByDate.size(), username, end - start);
						return playsByDate.values();
					}
				});
	}
}
