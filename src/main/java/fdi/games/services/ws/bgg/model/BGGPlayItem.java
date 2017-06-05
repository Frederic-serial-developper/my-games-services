package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class BGGPlayItem {

	@XmlAttribute(name = "objectid", required = true)
	private long id;

	@XmlAttribute(name = "name")
	private String name;

	public long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

}