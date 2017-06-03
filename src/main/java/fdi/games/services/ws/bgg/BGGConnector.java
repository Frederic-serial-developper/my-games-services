package fdi.games.services.ws.bgg;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BGGConnector {

	final static Logger logger = LoggerFactory.getLogger(BGGConnector.class);

	public String executeRequest(String urlToExecute) throws BGGException {
		try {
			final URL url = new URL(urlToExecute);
			final URLConnection connection = url.openConnection();

			if (connection instanceof HttpURLConnection) {
				HttpURLConnection httpConnection = null;
				int responseCode = 0;

				do {
					if (responseCode != 0) {
						logger.trace("wait response status is 200 for {}", urlToExecute);
						Thread.sleep(5000);
					}
					httpConnection = (HttpURLConnection) url.openConnection();
					responseCode = httpConnection.getResponseCode();
					logger.trace("Response status for {} : {}", urlToExecute, responseCode);
				} while (responseCode == 202);
			}

			final String xmlResult = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
			return xmlResult;
		} catch (IOException | InterruptedException e) {
			throw new BGGException("error while retrieving data from boardgamegeek with url=" + urlToExecute, e);
		}
	}
}