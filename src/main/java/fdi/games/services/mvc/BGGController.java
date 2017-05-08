package fdi.games.services.mvc;

import java.util.Collection;
import java.util.Collections;

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
import fdi.games.services.model.BoardGame;
import fdi.games.services.model.CollectionStatistics;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/bgg")
public class BGGController {

	final static Logger logger = LoggerFactory.getLogger(BGGController.class);

	@Inject
	private BoardGameService service;

	@GetMapping(path = "collection/{username}", produces = "application/json; charset=UTF-8")
	public Collection<BoardGame> getCollection(@PathVariable("username") String username,
			@RequestParam("includeExpansions") boolean includeExpansions) {
		logger.info("retrieve collection for user {}, includeExpansions={}", username, includeExpansions);
		try {
			final Collection<BoardGame> result = this.service.getCollection(username, includeExpansions);
			logger.debug("found {} games for user {}", result.size(), username);
			return result;
		} catch (final BoardGameServiceException e) {
			logger.error("error while retrieving collection for " + username + " : " + e.getMessage());
			return Collections.emptyList();
		}
	}

	@GetMapping(path = "collection/{username}/stats", produces = "application/json; charset=UTF-8")
	public CollectionStatistics getCollectionStats(@PathVariable("username") String username,
			@RequestParam("includeExpansions") boolean includeExpansions) {
		logger.info("retrieve collection statistics for user {}, includeExpansions={}", username, includeExpansions);
		try {
			final CollectionStatistics statistics = this.service.getStatistics(username, includeExpansions);
			return statistics;
		} catch (final BoardGameServiceException e) {
			logger.error("error while retrieving collection statistics for " + username + " : " + e.getMessage());
			return new CollectionStatistics();
		}
	}
}
