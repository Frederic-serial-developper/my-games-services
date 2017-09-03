package fdi.games.services.business;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

import fdi.games.services.model.BoardGameStaticData;
import fdi.games.services.model.BoardGameStatus;
import fdi.games.services.model.BoardGameUserData;
import fdi.games.services.model.BoardGameUserFullData;
import fdi.games.services.model.BoardGamesCollection;
import fdi.games.services.model.CollectionStatistics;
import fdi.games.services.model.Play;
import fdi.games.services.model.RatingLevel;
import fdi.games.services.ws.bgg.BGGClient;
import fdi.games.services.ws.bgg.BGGException;
import fdi.games.services.ws.bgg.model.BGGGame;
import fdi.games.services.ws.bgg.model.BGGGameDetail;
import fdi.games.services.ws.bgg.model.BGGPlay;
import fdi.games.services.ws.bgg.model.BGGPlayItem;

@Service
public class BoardGameService {

	final static Logger logger = LoggerFactory.getLogger(BoardGameService.class);

	private static final long _2_HOURS = 7_200_000;
	private static final long _10_SECONDS = 10_000;

	private static final ExecutorService executor = Executors.newFixedThreadPool(1);

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

	private Cache<Long, BoardGameStaticData> gamesDataCache;

	private LoadingCache<String, Multimap<Long, Play>> playsCache;

	public Collection<BoardGameUserFullData> getCollection(String username, boolean includeExpansions,
			boolean includePreviouslyOwned) throws BoardGameServiceException {
		logger.info("retrieve collection for user {}, includeExpansions={}, includePreviouslyOwned={}", username,
				includeExpansions, includePreviouslyOwned);
		final Collection<BoardGameUserData> games = this.collectionsCache.getIfPresent(username).getGames();
		final Collection<BoardGameUserData> filteredGames = filter(games, includeExpansions, includePreviouslyOwned);

		final Collection<BoardGameUserFullData> gamesWithData = filteredGames.stream()
				.map(game -> getGameWithData(username, game)).collect(Collectors.toSet());

		return gamesWithData;
	}

	private BoardGameUserFullData getGameWithData(String username, BoardGameUserData game) {
		final BoardGameUserFullData boardGameWithData = new BoardGameUserFullData();
		boardGameWithData.setId(game.getId());
		boardGameWithData.setName(game.getName());
		boardGameWithData.setStatus(game.getStatus());
		boardGameWithData.setPlaysCount(game.getPlaysCount());
		final BoardGameStaticData data = this.gamesDataCache.getIfPresent(game.getId());
		boardGameWithData.setData(data);
		boardGameWithData.setImage(game.getImage() == null ? data.getImage() : game.getImage());
		try {
			final Multimap<Long, Play> plays = this.playsCache.get(username);
			boardGameWithData.addAllPlays(plays.get(game.getId()));
		} catch (final ExecutionException e) {
			logger.error("error while retrieving plays from cache for " + username, e);
		}
		return boardGameWithData;
	}

	private Collection<BoardGameUserData> filter(Collection<BoardGameUserData> games, boolean includeExpansions,
			boolean includePreviouslyOwned) {
		final Set<BoardGameUserData> filteredGames = new HashSet<>();
		for (final BoardGameUserData game : games) {
			final BoardGameStaticData gameData = this.gamesDataCache.getIfPresent(game.getId());
			final boolean shouldFilter = !includeExpansions && gameData.isExpansion()
					|| !includePreviouslyOwned && game.isPreviouslyOwned();
			if (!shouldFilter) {
				filteredGames.add(game);
			}

		}
		return filteredGames;
	}

	@Scheduled(initialDelay = _10_SECONDS, fixedDelay = _2_HOURS)
	private void refreshVips() throws InterruptedException {
		final Runnable runnableTask = () -> {
			for (final String vip : this.vips) {
				logger.info("refresh cache informations for {}", vip);
				try {
					delay();
					final long start = System.currentTimeMillis();
					final Collection<BoardGameUserData> games = fetchGames(vip);
					this.collectionsCache.put(vip, new BoardGamesCollection(LocalDateTime.now(), games));
					delay();
					try {
						final Multimap<Long, Play> plays = fetchPlays(vip);
						this.playsCache.put(vip, plays);
					} catch (final BoardGameServiceException e) {
						logger.error("cannot fetch plays for {}", vip, e);
					}
					logger.info("{} updated in {} msec", vip, System.currentTimeMillis() - start);
				} catch (final BGGException e) {
					logger.error("error while refreshing cache informations for " + vip, e);
				}
			}
		};
		try {
			executor.execute(runnableTask);
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (final Throwable e) {
			logger.error("error while retrieve user data", e);
		}
	}

	private void delay() {
		logger.debug("wait {} seconds", this.bggDelay);
		try {
			TimeUnit.SECONDS.sleep(this.bggDelay);
		} catch (final InterruptedException e) {
			logger.error("cannot wait", e);
		}
	}

	public CollectionStatistics getStatistics(String username, boolean includeExpansions,
			boolean includePreviouslyOwned) throws BoardGameServiceException {
		final BoardGamesCollection boardGamesCollection = this.collectionsCache.getIfPresent(username);
		final CollectionStatistics stats = new CollectionStatistics(boardGamesCollection.getLasUpdate());

		final Collection<BoardGameUserData> games = filter(boardGamesCollection.getGames(), includeExpansions,
				includePreviouslyOwned);

		logger.info("compute collection statistics for user {}, includeExpansions={}", username, includeExpansions);

		stats.setTotalSize(new Long(games.size()));
		stats.setTotalPlays(countPlays(games));

		for (final BoardGameUserData boardGame : games) {
			final BoardGameStaticData gameData = this.gamesDataCache.getIfPresent(boardGame.getId());
			if (gameData != null) {
				final RatingLevel ratingLevel = getRatingLevel(gameData);
				stats.incrementRatingLevel(ratingLevel);
				stats.incrementYear(gameData.getYear());
			}
		}

		try {
			final Multimap<Long, Play> plays = this.playsCache.get(username);
			for (final Play play : plays.values()) {
				final Integer year = play.getDate().getYear();
				final Integer count = play.getCount();
				stats.incrementPlay(year, count);
			}
		} catch (final ExecutionException e) {
			logger.error("error while retrieving game plays for {}", username, e);
		}

		return stats;
	}

	private RatingLevel getRatingLevel(BoardGameStaticData game) {
		final RatingLevel[] levelsAvailable = RatingLevel.values();
		final Double rating = game.getRating();
		for (final RatingLevel ratingLevel : levelsAvailable) {
			if (ratingLevel.match(rating)) {
				return ratingLevel;
			}
		}
		return null;
	}

	private Long countPlays(Collection<BoardGameUserData> games) {
		Long total = new Long(0);
		for (final BoardGameUserData boardGame : games) {
			total = total + boardGame.getPlaysCount();
		}
		return total;
	}

	public Collection<BoardGameUserFullData> getPlays(String username) throws BoardGameServiceException {
		try {
			final Map<Long, BoardGameUserFullData> playDataByGameId = new HashMap<>();

			final BoardGamesCollection userCollection = this.collectionsCache.getIfPresent(username);

			final Collection<BoardGameUserData> games = userCollection.getGames();
			final Map<Long, BoardGameUserData> userGamesById = new HashMap<>();
			for (final BoardGameUserData boardGame : games) {
				userGamesById.put(boardGame.getId(), boardGame);
			}

			final Multimap<Long, Play> playsByGameId = this.playsCache.get(username);

			// case of plays for games that are already in user collection
			final Collection<Long> gamesInCollection = CollectionUtils.intersection(playsByGameId.keySet(),
					userGamesById.keySet());
			for (final Long bggId : gamesInCollection) {
				final BoardGameUserFullData gameWithData = getGameWithData(username, userGamesById.get(bggId));
				playDataByGameId.put(bggId, gameWithData);
			}

			// case of plays for game that are not in the user collection
			final Collection<Long> gamesNotInCollection = CollectionUtils.subtract(playsByGameId.keySet(),
					userGamesById.keySet());
			for (final Long bggId : gamesNotInCollection) {
				final BoardGameUserFullData gameWithData = new BoardGameUserFullData();
				gameWithData.setData(this.gamesDataCache.getIfPresent(bggId));
				gameWithData.setId(bggId);
				gameWithData.setStatus(BoardGameStatus.OTHER);
				gameWithData.setImage(gameWithData.getData() == null ? null : gameWithData.getData().getImage());

				final Collection<Play> somePlays = playsByGameId.get(bggId);
				for (final Play play : somePlays) {
					gameWithData.setName(play.getGameName());
					gameWithData.incrementPlayCount(play.getCount());
					gameWithData.addPlay(play);
				}
				playDataByGameId.put(bggId, gameWithData);
			}
			return playDataByGameId.values();
		} catch (final ExecutionException e) {
			throw new BoardGameServiceException("error while retrieving plays for " + username, e);
		}
	}

	private Multimap<Long, Play> fetchPlays(String username) throws BoardGameServiceException {
		try {
			logger.info("fetch plays from boardgamegeek for {}", username);
			final List<BGGPlay> plays = this.bggClient.getPlays(username);
			final Set<Long> ids = plays.stream().map(bggPlay -> bggPlay.getItem().getId()).collect(Collectors.toSet());
			fetchGameDetails(ids);
			logger.info("found {} plays for user {}", plays.size(), username);

			final Multimap<Long, Play> playsByGameId = ArrayListMultimap.create();
			for (final BGGPlay bggPlay : plays) {
				final LocalDate date = LocalDate.parse(bggPlay.getDate());
				final BGGPlayItem game = bggPlay.getItem();
				playsByGameId.put(game.getId(), new Play(date, game.getId(), game.getName(), bggPlay.getQuantity()));
			}
			return playsByGameId;
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

	private Collection<BoardGameUserData> fetchGames(String username) throws BGGException {
		logger.info("fetch collection from boardgamegeek for {}", username);
		final List<BGGGame> result = BoardGameService.this.bggClient.getCollection(username, true, true);
		final Set<BoardGameUserData> games = result.stream().map(game -> this.bggGameMapper.map(game))
				.collect(Collectors.toSet());
		this.collectionsCache.put(username, new BoardGamesCollection(LocalDateTime.now(), games));
		logger.info("found {} games for user {}", result.size(), username);

		logger.info("fetch games data from boardgamegeek for {}", username);
		final Set<Long> ids = result.stream().map(game -> game.getBggId()).collect(Collectors.toSet());
		fetchGameDetails(ids);

		return result.stream().map(game -> BoardGameService.this.bggGameMapper.map(game)).collect(Collectors.toList());
	}

	private void fetchGameDetails(Set<Long> ids) throws BGGException {
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
		logger.info("found {} games details", detailsById.size());
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
				.build(new CacheLoader<String, Multimap<Long, Play>>() {
					@Override
					public Multimap<Long, Play> load(String username) throws BoardGameServiceException, BGGException {
						final long start = System.currentTimeMillis();
						final Multimap<Long, Play> playsByGameId = fetchPlays(username);
						final long end = System.currentTimeMillis();
						logger.info("{} games fetched for {} in {} msec", playsByGameId.size(), username, end - start);
						return playsByGameId;
					}
				});
	}

	@PreDestroy
	private void destroy() {
		executor.shutdownNow();
	}
}
