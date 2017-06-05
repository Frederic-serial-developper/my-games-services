package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class BGGPlay {

	@XmlAttribute(name = "id", required = true)
	private long id;

	@XmlAttribute(name = "date")
	private String date;

	@XmlAttribute(name = "quantity")
	private Integer quantity;

	@XmlElement(name = "item")
	private BGGPlayItem item;

	public long getId() {
		return this.id;
	}

	public String getDate() {
		return this.date;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public BGGPlayItem getItem() {
		return this.item;
	}

}