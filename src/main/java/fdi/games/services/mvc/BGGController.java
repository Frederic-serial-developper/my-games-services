package fdi.games.services.mvc;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fdi.games.services.business.BoardGameService;
import fdi.games.services.business.BoardGameServiceException;
import fdi.games.services.model.BoardGameWithData;
import fdi.games.services.model.CollectionStatistics;
import fdi.games.services.model.Play;
import fdi.games.services.ws.bgg.model.BGGGameDetail;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/bgg")
public class BGGController {

	final static Logger logger = LoggerFactory.getLogger(BGGController.class);

	@Inject
	private BoardGameService service;

	@GetMapping(path = "collection/{username}", produces = "application/json; charset=UTF-8")
	public Collection<BoardGameWithData> getCollection(@PathVariable("username") String username,
			@RequestParam("includeExpansions") boolean includeExpansions,
			@RequestParam("includePreviouslyOwned") boolean includePreviouslyOwned) {
		logger.info("retrieve collection for user {}, includeExpansions={}, includePreviouslyOwned={}", username,
				includeExpansions, includePreviouslyOwned);
		try {
			final Collection<BoardGameWithData> result = this.service.getCollection(username, includeExpansions,
					includePreviouslyOwned);
			logger.debug("found {} games for user {}", result.size(), username);
			return result;
		} catch (final BoardGameServiceException e) {
			logger.error("error while retrieving collection for " + username + " : " + e.getMessage());
			return Collections.emptyList();
		}
	}

	@GetMapping(path = "collection/{username}/stats", produces = "application/json; charset=UTF-8")
	public CollectionStatistics getCollectionStats(@PathVariable("username") String username,
			@RequestParam("includeExpansions") boolean includeExpansions,
			@RequestParam("includePreviouslyOwned") boolean includePreviouslyOwned) {
		logger.info("retrieve collection statistics for user {}, includeExpansions={}, includePreviouslyOwned={}",
				username, includeExpansions, includePreviouslyOwned);
		try {
			final CollectionStatistics statistics = this.service.getStatistics(username, includeExpansions,
					includePreviouslyOwned);
			return statistics;
		} catch (final BoardGameServiceException e) {
			logger.error("error while retrieving collection statistics for " + username + " : " + e.getMessage());
			return new CollectionStatistics(LocalDateTime.now());
		}
	}

	@GetMapping(path = "collection/{username}/plays", produces = "application/json; charset=UTF-8")
	public Collection<Play> getCollectionPlays(@PathVariable("username") String username)
			throws BoardGameServiceException {
		logger.info("retrieve collection plays for user {}", username);
		return this.service.getPlays(username);
	}

	@GetMapping(path = "{bggId}", produces = "application/json; charset=UTF-8")
	public Map<Long, BGGGameDetail> getGameDetails(@PathVariable("bggId") Long bggId) throws BoardGameServiceException {
		logger.info("retrieve game detais for game {}", bggId);
		final Map<Long, BGGGameDetail> gameDetails = this.service.getGameDetails(bggId);
		return gameDetails;
	}
}
