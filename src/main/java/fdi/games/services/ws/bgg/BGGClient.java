package fdi.games.services.ws.bgg;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import fdi.games.services.ws.bgg.model.BGGGame;
import fdi.games.services.ws.bgg.model.BGGGameDetail;
import fdi.games.services.ws.bgg.model.BGGGameDetailsList;
import fdi.games.services.ws.bgg.model.BGGGameList;

@Service
public class BGGClient {

	final static Logger logger = LoggerFactory.getLogger(BGGClient.class);

	@Inject
	private BGGConnector connector;

	@Value("${my-games-services.bgg.baseUrl}")
	private String bggBaseUrl;

	public List<BGGGame> getCollection(final String username, boolean includeExpansions, boolean includePreviouslyOwned)
			throws BGGException {
		String url = this.bggBaseUrl + "collection?";
		url = url + "username=" + username;
		url = url + "&stats=1";

		final List<BGGGame> myGames = Lists.newArrayList();

		// get previous games
		if (includePreviouslyOwned) {
			final List<BGGGame> previousGames = getCollection(
					url + "&prevowned=1" + "&excludesubtype=boardgameexpansion");
			logger.info("found {} previously owned games for {}", previousGames.size(), username);
			myGames.addAll(previousGames);
			if (includeExpansions) {
				final List<BGGGame> previousExpansions = getCollection(
						url + "&prevowned=1" + "&subtype=boardgameexpansion");
				logger.info("found {} previously owned expansions for {}", previousExpansions.size(), username);
				myGames.addAll(previousExpansions);
			}
		}
		// get owned games
		final List<BGGGame> ownedGames = getCollection(url + "&own=1" + "&excludesubtype=boardgameexpansion");
		logger.info("found {} owned games for {}", ownedGames.size(), username);
		myGames.addAll(ownedGames);
		if (includeExpansions) {
			final List<BGGGame> ownedExpansions = getCollection(url + "&own=1" + "&subtype=boardgameexpansion");
			logger.info("found {} owned expansions for {}", ownedExpansions.size(), username);
			myGames.addAll(ownedExpansions);
		}

		return myGames;
	}

	private List<BGGGame> getCollection(String url) throws BGGException {
		logger.trace("get collection using url={}", url);
		try {

			final String xmlResult = this.connector.executeRequest(url);
			final Object result = getUnmarshaller(BGGGameList.class).unmarshal(new StringReader(xmlResult));
			List<BGGGame> boardGames = ((BGGGameList) result).getBoardGames();
			if (boardGames == null) {
				boardGames = new ArrayList<>();
			}
			return boardGames;
		} catch (final JAXBException e) {
			throw new BGGException("error while parsing collection from boardgamegeek with url=" + url, e);
		}
	}

	public BGGGameDetail getDetails(Long bggId) throws BGGException {
		// https://www.boardgamegeek.com/xmlapi2/thing?type=boardgame&id=143519
		final String url = this.bggBaseUrl + "thing?type=boardgame&id=" + bggId;
		try {
			final String xmlResult = this.connector.executeRequest(url);
			final Object result = getUnmarshaller(BGGGameDetailsList.class).unmarshal(new StringReader(xmlResult));
			return ((BGGGameDetailsList) result).getDetailsList().get(0);
		} catch (final JAXBException e) {
			throw new BGGException("error while parsing detail from boardgamegeek with url=" + url, e);
		}
	}

	private Unmarshaller getUnmarshaller(final Class<?> payloadClass) {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(payloadClass);
			return jaxbContext.createUnmarshaller();
		} catch (final JAXBException e) {
			throw new IllegalStateException(e);
		}
	}

}