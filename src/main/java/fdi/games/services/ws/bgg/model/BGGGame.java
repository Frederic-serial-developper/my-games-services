package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class BGGGame {

	@XmlAttribute(name = "objectid", required = true)
	private long bggId;

	@XmlElement(name = "yearpublished")
	private int yearPublished;

	@XmlElement(name = "name")
	private String name;

	@XmlElement(name = "originalname")
	private String originalname;

	@XmlElement(name = "image")
	private String imageUrl;

	@XmlElement(name = "thumbnail")
	private String thumbnailUrl;

	@XmlElement(name = "numplays")
	private Long plays;

	@XmlElement(name = "stats")
	private BGGGameStat stats;

	public BGGGameStat getStats() {
		return this.stats;
	}

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

	public int getYearPublished() {
		return this.yearPublished;
	}

	public String getThumbnailUrl() {
		return this.thumbnailUrl;
	}

	public String getOriginalname() {
		return this.originalname;
	}

	@Override
	public String toString() {
		return "BoardGame [bggId=" + this.bggId + ", name=" + this.name + "]";
	}

}