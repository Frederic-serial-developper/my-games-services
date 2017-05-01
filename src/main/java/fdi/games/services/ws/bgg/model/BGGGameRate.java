package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlElement;

public class BGGGameRate {

	@XmlElement(name = "average")
	private BGGValue average;

	public BGGValue getAverage() {
		return this.average;
	}

	@Override
	public String toString() {
		return "BGGGameRate [average=" + this.average + "]";
	}

}
