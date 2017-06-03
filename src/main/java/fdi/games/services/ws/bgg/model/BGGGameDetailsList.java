package fdi.games.services.ws.bgg.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "items")
public class BGGGameDetailsList {

	@XmlElement(name = "item")
	private List<BGGGameDetail> detailsList;

	public List<BGGGameDetail> getDetailsList() {
		return this.detailsList;
	}

	@Override
	public String toString() {
		return "BGGGameList [detailsList=" + this.detailsList + "]";
	}

}