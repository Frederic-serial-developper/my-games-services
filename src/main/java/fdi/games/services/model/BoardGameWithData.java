package fdi.games.services.model;

public class BoardGameWithData extends BoardGame {

	private BoardGameData data;

	public BoardGameData getData() {
		return this.data;
	}

	public void setData(BoardGameData data) {
		this.data = data;
	}

}