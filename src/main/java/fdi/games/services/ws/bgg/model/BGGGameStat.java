package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlElement;

public class BGGGameStat {

	@XmlElement(name = "ratings")
	private BGGGameRate rating;

	public BGGGameRate getRating() {
		return this.rating;
	}

}
