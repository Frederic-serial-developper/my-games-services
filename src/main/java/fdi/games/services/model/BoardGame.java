package fdi.games.services.model;

public class BoardGame {

	private Long id;

	private String name;

	private BoardGameStatus status;

	private String image;

	private Long playsCount = 0l;

	public boolean isPreviouslyOwned() {
		return BoardGameStatus.PREVIOUSLY_OWNED.equals(this.status);
	}

	public void incrementPlayCount(Integer count) {
		this.playsCount = this.playsCount + count;
	}

	public Long getPlaysCount() {
		return this.playsCount;
	}

	public void setPlaysCount(Long plays) {
		this.playsCount = plays;
	}

	public BoardGameStatus getStatus() {
		return this.status;
	}

	public void setStatus(BoardGameStatus status) {
		this.status = status;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String image) {
		this.image = image;
	}

}