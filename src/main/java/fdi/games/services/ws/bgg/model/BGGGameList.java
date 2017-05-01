package fdi.games.services.ws.bgg.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "items")
public class BGGGameList {

	@XmlElement(name = "item")
	private List<BGGGame> boardGames;

	public List<BGGGame> getBoardGames() {
		return this.boardGames;
	}

	@Override
	public String toString() {
		return "BoardGameList [boardGames=" + this.boardGames.size() + "]";
	}

}