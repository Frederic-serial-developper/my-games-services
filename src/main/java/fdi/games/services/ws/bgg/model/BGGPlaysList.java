package fdi.games.services.ws.bgg.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "plays")
public class BGGPlaysList {

	@XmlElement(name = "play")
	private List<BGGPlay> plays;

	@XmlAttribute(name = "total")
	private Integer total;

	public List<BGGPlay> getPlays() {
		return this.plays;
	}

	public Integer getTotal() {
		return this.total;
	}

}