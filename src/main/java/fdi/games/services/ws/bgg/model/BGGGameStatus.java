package fdi.games.services.ws.bgg.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class BGGGameStatus {

	@XmlAttribute(name = "prevowned", required = true)
	private long prevowned;

	@XmlAttribute(name = "own", required = true)
	private long owned;

	public long getPrevowned() {
		return this.prevowned;
	}

	public void setPrevowned(long prevowned) {
		this.prevowned = prevowned;
	}

	public long getOwned() {
		return this.owned;
	}

	public void setOwned(long owned) {
		this.owned = owned;
	}

}