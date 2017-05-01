package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class BGGGameStat {

	@XmlAttribute(name = "minplayers")
	private int minPlayers;

	@XmlAttribute(name = "maxplayers")
	private int maxPlayers;

	@XmlAttribute(name = "playingtime")
	private int playingTime;

	@XmlElement(name = "rating")
	private BGGGameRate rating;

	public BGGGameRate getRating() {
		return this.rating;
	}

	public int getMinPlayers() {
		return this.minPlayers;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public int getPlayingTime() {
		return this.playingTime;
	}

}
