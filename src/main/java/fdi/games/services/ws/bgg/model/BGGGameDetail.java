package fdi.games.services.ws.bgg.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class BGGGameDetail {

	@XmlAttribute(name = "id", required = true)
	private long bggId;

	@XmlAttribute(name = "type", required = true)
	private String type;

	@XmlElement(name = "description")
	private String description;

	@XmlElement(name = "thumbnail")
	private String thumbnail;

	@XmlElement(name = "yearpublished")
	private BGGValue yearpublished;

	@XmlElement(name = "minplayers")
	private BGGValue minplayers;

	@XmlElement(name = "maxplayers")
	private BGGValue maxplayers;

	@XmlElement(name = "playingtime")
	private BGGValue playingtime;

	@XmlElement(name = "minplaytime")
	private BGGValue minplaytime;

	@XmlElement(name = "maxplaytime")
	private BGGValue maxplaytime;

	@XmlElement(name = "minage")
	private BGGValue minage;

	@XmlElement(name = "image")
	private String image;

	@XmlElement(name = "link")
	private List<BGGGameInfo> infos;

	@XmlElement(name = "poll")
	private List<BGGGamePoll> polls;

	public long getBggId() {
		return this.bggId;
	}

	public String getDescription() {
		return this.description;
	}

	public List<BGGGameInfo> getInfos() {
		return this.infos;
	}

	public String getType() {
		return this.type;
	}

	public String getThumbnail() {
		return this.thumbnail;
	}

	public BGGValue getYearpublished() {
		return this.yearpublished;
	}

	public BGGValue getMinplayers() {
		return this.minplayers;
	}

	public BGGValue getMaxplayers() {
		return this.maxplayers;
	}

	public BGGValue getPlayingtime() {
		return this.playingtime;
	}

	public BGGValue getMinplaytime() {
		return this.minplaytime;
	}

	public BGGValue getMaxplaytime() {
		return this.maxplaytime;
	}

	public BGGValue getMinage() {
		return this.minage;
	}

	public String getImage() {
		return this.image;
	}

	public List<BGGGamePoll> getPolls() {
		return this.polls;
	}
}