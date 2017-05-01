package fdi.games.services.mvc.model;

public class BoardGame {

	private Long id;
	private String name;
	private BoardGameSource source;
	private Double rank;
	private String image;
	private Long plays;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BoardGameSource getSource() {
		return this.source;
	}

	public void setSource(BoardGameSource source) {
		this.source = source;
	}

	public Double getRank() {
		return this.rank;
	}

	public void setRank(Double rank) {
		this.rank = rank;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Long getPlays() {
		return this.plays;
	}

	public void setPlays(Long plays) {
		this.plays = plays;
	}

}