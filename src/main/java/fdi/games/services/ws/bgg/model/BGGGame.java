package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class BGGGame {

	@XmlAttribute(name = "objectid", required = true)
	private long bggId;

	@XmlElement(name = "yearpublished")
	private int yearPublished;

	@XmlElement(name = "name")
	private String name;

	@XmlAttribute(name = "subtype")
	private String type;

	@XmlElement(name = "originalname")
	private String originalname;

	@XmlElement(name = "image")
	private String imageUrl;

	@XmlElement(name = "thumbnail")
	private String thumbnailUrl;

	@XmlElement(name = "numplays")
	private Long plays;

	@XmlElement(name = "status")
	private BGGGameStatus status;

	public long getPlays() {
		return this.plays;
	}

	public long getBggId() {
		return this.bggId;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getYearPublished() {
		return this.yearPublished;
	}

	public String getThumbnailUrl() {
		return this.thumbnailUrl;
	}

	public String getOriginalname() {
		return this.originalname;
	}

	public BGGGameStatus getStatus() {
		return this.status;
	}

}