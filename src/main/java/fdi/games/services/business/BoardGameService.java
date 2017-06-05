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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fdi.games.services.model.BoardGame;
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
	private BGGGameMapper mapper;

	@Value("${my-games-services.cache.vip}")
	private String[] vips;

	@Value("${my-games-services.cache.expirationInMinutes}")
	private Integer cacheExpiration;

	@Value("${my-games-services.cache.maxSize}")
	private Integer cacheMaxSize;

	@Value("${my-games-services.bgg.delay}")
	private Integer bggDelay;

	private LoadingCache<String, BoardGamesCollection> gamesCache;

	private LoadingCache<String, Collection<Play>> playsCache;

	public Collection<BoardGame> getCollection(String username, boolean includeExpansions,
			boolean includePreviouslyOwned) throws BoardGameServiceException {
		logger.info("retrieve collection for user {}, includeExpansions={}, includePreviouslyOwned={}", username,
				includeExpansions, includePreviouslyOwned);
		try {
			final Collection<BoardGame> games = this.gamesCache.get(username).getGames();
			return filter(games, includeExpansions, includePreviouslyOwned);
		} catch (final ExecutionException e) {
			throw new BoardGameServiceException("Error while fetching collection", e);
		}
	}

	private Collection<BoardGame> filter(Collection<BoardGame> games, boolean includeExpansions,
			boolean includePreviouslyOwned) {
		final Set<BoardGame> filteredGames = new HashSet<>();
		for (final BoardGame game : games) {
			final boolean shouldFilter = !includeExpansions && game.isExpansion()
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
				this.gamesCache.put(vip, new BoardGamesCollection(LocalDateTime.now(), games));
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

		try {
			final BoardGamesCollection boardGamesCollection = this.gamesCache.get(username);
			final CollectionStatistics stats = new CollectionStatistics(boardGamesCollection.getLasUpdate());

			final Collection<BoardGame> games = filter(boardGamesCollection.getGames(), includeExpansions,
					includePreviouslyOwned);

			logger.info("compute collection statistics for user {}, includeExpansions={}", username, includeExpansions);

			stats.setTotalSize(new Long(games.size()));
			stats.setTotalPlays(countPlays(games));

			for (final BoardGame boardGame : games) {
				final RatingLevel ratingLevel = getRatingLevel(boardGame);
				stats.incrementRatingLevel(ratingLevel);
				stats.incrementYear(boardGame.getYear());
			}

			final Collection<Play> plays = getPlays(username);
			for (final Play play : plays) {
				final Integer year = play.getDate().getYear();
				final Integer count = play.getCount();
				stats.incrementPlay(year, count);
			}

			return stats;
		} catch (final ExecutionException e) {
			throw new BoardGameServiceException("error while getting statisctics for " + username, e);
		}
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
		logger.info("found {} games for user {}", result.size(), username);

		logger.info("fetch collection details from boardgamegeek for {}", username);
		final Set<Long> ids = result.stream().map(game -> game.getBggId()).collect(Collectors.toSet());
		final Map<Long, BGGGameDetail> detailsById = BoardGameService.this.bggClient.getDetails(ids);
		for (final BGGGame bggGame : result) {
			bggGame.setDetails(detailsById.get(bggGame.getBggId()));
		}
		logger.info("found {} games details for user {}", detailsById.size(), username);

		return result.stream().map(game -> BoardGameService.this.mapper.map(game)).collect(Collectors.toList());
	}

	@PostConstruct
	private void initialize() {
		logger.info("initialize cache: cacheMaxSize={} objects, cacheExpiration={} min", this.cacheMaxSize,
				this.cacheExpiration);
		this.gamesCache = CacheBuilder.newBuilder().maximumSize(this.cacheMaxSize)
				.expireAfterWrite(this.cacheExpiration, TimeUnit.MINUTES)
				.build(new CacheLoader<String, BoardGamesCollection>() {
					@Override
					public BoardGamesCollection load(String username) throws BoardGameServiceException, BGGException {
						final long start = System.currentTimeMillis();
						final Collection<BoardGame> games = fetchGames(username);
						final long end = System.currentTimeMillis();
						logger.info("{} games fetched for {} in {} msec", games.size(), username, end - start);
						return new BoardGamesCollection(LocalDateTime.now(), games);
					}
				});

		this.playsCache = CacheBuilder.newBuilder().maximumSize(this.cacheMaxSize)
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
