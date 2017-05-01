package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlAttribute;

public class BGGValue {

	@XmlAttribute(name = "value")
	private Double value;

	public Double getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "BGGValue [value=" + this.value + "]";
	}

}
