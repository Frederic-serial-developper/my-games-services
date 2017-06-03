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
			final String url = this.bggBaseUrl + "thing?type=boardgame&id=" + idParameter;
			try {
				logger.debug("retrieve details for {} ids - {}/{}", subIds.size(), loop, partitionIds.size());
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