package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class BGGGameInfo {

	@XmlAttribute(name = "id", required = true)
	private long id;

	@XmlAttribute(name = "type")
	private String type;

	@XmlAttribute(name = "value")
	private String value;

	public long getId() {
		return this.id;
	}

	public String getType() {
		return this.type;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "BGGGameInfo [id=" + this.id + ", type=" + this.type + ", value=" + this.value + "]";
	}

}