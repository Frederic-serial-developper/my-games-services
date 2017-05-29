package fdi.games.services.ws.bgg;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fdi.games.services.ws.bgg.model.BGGGame;
import fdi.games.services.ws.bgg.model.BGGGameList;

@Service
public class BGGClient {

	final static Logger logger = LoggerFactory.getLogger(BGGClient.class);

	@Value("${my-games-services.bgg.baseUrl}")
	private String bggBaseUrl;

	public List<BGGGame> getCollection(final String username, boolean includeExpansions, boolean includePreviouslyOwned)
			throws BGGException {
		String url = this.bggBaseUrl + "collection?";
		url = url + "username=" + username;
		url = url + "&stats=1";
		if (includePreviouslyOwned) {
			url = url + "&prevowned=1";
		} else {
			url = url + "&own=1";
		}

		final List<BGGGame> games = getCollection(url + "&excludesubtype=boardgameexpansion");

		if (includeExpansions) {
			logger.debug("get expansions for {}, includePreviouslyOwned={}", username, includePreviouslyOwned);
			// execute a separated request to manage expansions because of a bug
			// in BGG XML2 API
			final List<BGGGame> expansions = getCollection(url + "&subtype=boardgameexpansion");
			games.addAll(expansions);
		}

		if (includePreviouslyOwned) {
			logger.debug("get previously owned for {}, includePreviouslyOwned={}", username, includePreviouslyOwned);
			// owned and previouslyOwned are exclusive, we need to run a
			// dedicated requests if we want both
			final List<BGGGame> ownedGames = getCollection(username, includeExpansions, false);
			games.addAll(ownedGames);
		}

		return games;
	}

	private List<BGGGame> getCollection(String url) throws BGGException {
		logger.debug("get collection for with url={}", url);
		try {
			final URL bggUrl = new URL(url);
			final URLConnection connection = bggUrl.openConnection();

			if (connection instanceof HttpURLConnection) {
				HttpURLConnection httpConnection = null;
				int responseCode = 0;

				do {
					if (responseCode != 0) {
						logger.debug("wait response status is 200 for {}", url);
						Thread.sleep(5000);
					}
					httpConnection = (HttpURLConnection) bggUrl.openConnection();
					responseCode = httpConnection.getResponseCode();
					logger.debug("Response status for {} : {}", url, responseCode);
				} while (responseCode == 202);
			}

			final String xmlResult = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
			final Object result = getUnmarshaller(BGGGameList.class).unmarshal(new StringReader(xmlResult));

			List<BGGGame> boardGames = ((BGGGameList) result).getBoardGames();
			if (boardGames == null) {
				boardGames = new ArrayList<>();
			}
			return boardGames;
		} catch (JAXBException | IOException | InterruptedException e) {
			throw new BGGException("error while retrieving collection from boardgamegeek with url=" + url, e);
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