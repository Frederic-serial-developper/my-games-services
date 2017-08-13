package fdi.games.services.model;

public class BoardGameUserFullData extends BoardGameUserData {

	private BoardGameStaticData data;

	private String image;

	public BoardGameStaticData getData() {
		return this.data;
	}

	public void setData(BoardGameStaticData data) {
		this.data = data;
	}

	@Override
	public String getImage() {
		return this.image;
	}

	@Override
	public void setImage(String image) {
		this.image = image;
	}

}