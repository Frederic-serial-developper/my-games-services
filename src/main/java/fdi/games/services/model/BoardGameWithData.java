package fdi.games.services.model;

public class BoardGameWithData extends BoardGame {

	private BoardGameData data;

	private String image;

	public BoardGameData getData() {
		return this.data;
	}

	public void setData(BoardGameData data) {
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