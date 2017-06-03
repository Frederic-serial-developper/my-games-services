package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class BGGGamePollResult {

	@XmlAttribute(name = "value")
	private String value;

	@XmlAttribute(name = "numvotes")
	private Integer numVotes;

	@XmlAttribute(name = "level")
	private Integer level;

	public String getValue() {
		return this.value;
	}

	public Integer getNumVotes() {
		return this.numVotes;
	}

	public Integer getLevel() {
		return this.level;
	}

}