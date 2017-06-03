package fdi.games.services.ws.bgg.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class BGGGamePollResults {

	@XmlAttribute(name = "numplayers")
	private Integer numplayers;

	@XmlElement(name = "result")
	private List<BGGGamePollResult> results;

	public Integer getNumplayers() {
		return this.numplayers;
	}

	public List<BGGGamePollResult> getResults() {
		return this.results;
	}

}