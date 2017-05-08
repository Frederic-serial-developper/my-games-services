package fdi.games.services.ws.bgg;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fdi.games.services.ws.bgg.model.BGGGameList;

@Service
public class BGGClient {

	final static Logger logger = LoggerFactory.getLogger(BGGClient.class);

	private static final String BGG_XML_API_BASE = "https://www.boardgamegeek.com/xmlapi2/";

	public BGGGameList getCollection(final String username, boolean includeExpansions) throws BGGException {
		String url = BGG_XML_API_BASE + "collection?";
		url = url + "own=1";
		url = url + "&stats=1";
		url = url + "&username=" + username;
		if (!includeExpansions) {
			url = url + "&excludesubtype=boardgameexpansion";
		}
		logger.debug("get collection for user {} with url={}", username, url);

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

			return (BGGGameList) result;
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