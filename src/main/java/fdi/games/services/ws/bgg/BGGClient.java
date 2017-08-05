package fdi.games.services.ws.bgg;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import fdi.games.services.ws.bgg.model.BGGPlay;
import fdi.games.services.ws.bgg.model.BGGPlaysList;

@Service
public class BGGClient {

	final static Logger logger = LoggerFactory.getLogger(BGGClient.class);

	@Inject
	private BGGConnector connector;

	@Value("${my-games-services.bgg.baseUrl}")
	private String bggBaseUrl;

	@Value("${my-games-services.bgg.batchSize}")
	private Integer bggBatchSize;

	public List<BGGGame> getCollection(final String username, boolean includeExpansions, boolean includePreviouslyOwned)
			throws BGGException {
		String url = this.bggBaseUrl + "collection?";
		url = url + "username=" + username;

		final List<BGGGame> myGames = Lists.newArrayList();
		String urlToExecute;
		// get previous games
		if (includePreviouslyOwned) {
			urlToExecute = url + "&prevowned=1" + "&excludesubtype=boardgameexpansion";
			final List<BGGGame> previousGames = getCollection(urlToExecute);
			logger.info("found {} previously owned games, url={}", previousGames.size(), urlToExecute);
			myGames.addAll(previousGames);
			if (includeExpansions) {
				urlToExecute = url + "&prevowned=1" + "&subtype=boardgameexpansion";
				final List<BGGGame> previousExpansions = getCollection(urlToExecute);
				logger.info("found {} previously owned expansions, url={}", previousExpansions.size(), urlToExecute);
				myGames.addAll(previousExpansions);
			}
		}
		// get owned games
		urlToExecute = url + "&own=1" + "&excludesubtype=boardgameexpansion";
		final List<BGGGame> ownedGames = getCollection(urlToExecute);
		logger.info("found {} owned games, url={}", ownedGames.size(), urlToExecute);
		myGames.addAll(ownedGames);
		if (includeExpansions) {
			urlToExecute = url + "&own=1" + "&subtype=boardgameexpansion";
			final List<BGGGame> ownedExpansions = getCollection(urlToExecute);
			logger.info("found {} owned expansions, url={}", ownedExpansions.size(), urlToExecute);
			myGames.addAll(ownedExpansions);
		}

		return myGames;
	}

	public List<BGGPlay> getPlays(String username) throws BGGException {
		logger.info("get plays for {}", username);
		final String url = this.bggBaseUrl + "plays?username=" + username + "&page=";
		try {
			final List<BGGPlay> plays = new ArrayList<>();
			boolean shouldContinue = true;
			int page = 1;
			while (shouldContinue) {
				final String urlToExecute = url + page;
				logger.info("get plays for {}, page={}, url={}", username, page, urlToExecute);
				final String xmlResult = this.connector.executeRequest(urlToExecute);
				final BGGPlaysList result = (BGGPlaysList) getUnmarshaller(BGGPlaysList.class)
						.unmarshal(new StringReader(xmlResult));
				final List<BGGPlay> playsFromBgg = result.getPlays();
				shouldContinue = playsFromBgg != null && !playsFromBgg.isEmpty();
				if (shouldContinue) {
					plays.addAll(playsFromBgg);
					page++;
				}
			}
			return plays;
		} catch (final BGGException | JAXBException e) {
			throw new BGGException("error while getting plays from boardgamegeek with url=" + url, e);
		}
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

	public Map<Long, BGGGameDetail> getDetails(Set<Long> ids) throws BGGException {
		final Map<Long, BGGGameDetail> detailsById = new HashMap<>();

		final List<List<Long>> partitionIds = Lists.partition(Lists.newArrayList(ids), this.bggBatchSize);
		logger.debug("split ids with batch size={}", this.bggBatchSize);
		int loop = 1;
		for (final List<Long> subIds : partitionIds) {
			String idParameter = "";
			for (final Long id : subIds) {
				if (!idParameter.isEmpty()) {
					idParameter = idParameter + ",";
				}
				idParameter = idParameter + id;
			}
			String url = this.bggBaseUrl + "thing?";
			// url = url + "type=boardgame";
			url = url + "stats=1";
			url = url + "&id=" + idParameter;
			try {
				logger.info("retrieve details for {} ids - {}/{}, url={}", subIds.size(), loop, partitionIds.size(),
						url);
				final String xmlResult = this.connector.executeRequest(url);
				final BGGGameDetailsList detailsLists = (BGGGameDetailsList) getUnmarshaller(BGGGameDetailsList.class)
						.unmarshal(new StringReader(xmlResult));
				final List<BGGGameDetail> details = detailsLists.getDetailsList();
				for (final BGGGameDetail bggGameDetail : details) {
					detailsById.put(bggGameDetail.getBggId(), bggGameDetail);

				}
			} catch (final JAXBException e) {
				throw new BGGException("error while parsing detail from boardgamegeek with url=" + url, e);
			}
			loop++;
		}
		return detailsById;

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