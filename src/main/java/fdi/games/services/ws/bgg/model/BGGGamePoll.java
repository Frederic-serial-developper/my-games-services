package fdi.games.services.ws.bgg.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class BGGGamePoll {

	@XmlAttribute(name = "name", required = true)
	private String name;

	@XmlAttribute(name = "title")
	private String title;

	@XmlAttribute(name = "totalvotes")
	private Integer totalVotes;

	@XmlElement(name = "results")
	private List<BGGGamePollResults> results;

	public String getName() {
		return this.name;
	}

	public String getTitle() {
		return this.title;
	}

	public Integer getTotalVotes() {
		return this.totalVotes;
	}

	public List<BGGGamePollResults> getResults() {
		return this.results;
	}

}