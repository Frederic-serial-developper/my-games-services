package fdi.games.services.mvc;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fdi.games.services.mvc.model.BoardGame;
import fdi.games.services.ws.bgg.BoardGameGeekClient;
import fdi.games.services.ws.bgg.BoardGameGeekException;
import fdi.games.services.ws.bgg.model.BGGGameList;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/bgg")
public class BoardGameGeekController {

	final static Logger logger = LoggerFactory.getLogger(BoardGameGeekController.class);

	@Inject
	private BoardGameGeekClient bggClient;

	@Inject
	private BGGGameMapper mapper;

	@GetMapping(path = "collection/{username}", produces = "application/json; charset=UTF-8")
	public List<BoardGame> getCollection(@PathVariable("username") String username) throws BoardGameGeekException {
		logger.info("retrieve collection for user {}", username);
		final BGGGameList result = this.bggClient.getCollection(username);
		logger.debug("found {} games for user {}", result, username);
		return result.getBoardGames().stream().map(bggGame -> this.mapper.map(bggGame)).collect(Collectors.toList());
	}
}
