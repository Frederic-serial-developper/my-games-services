package fdi.games.services.mvc.model;

public class BoardGame {

	private Long id;

	private String name;

	private BoardGameSource source;

	private Double rating;

	private String image;

	private Long playsCount;

	private int year;

	private int minPlayers;

	private int maxPlayers;

	private int playingTime;

	public int getPlayingTime() {
		return this.playingTime;
	}

	public void setPlayingTime(int playingTime) {
		this.playingTime = playingTime;
	}

	public int getMinPlayers() {
		return this.minPlayers;
	}

	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public int getYear() {
		return this.year;
	}

	public void setYear(int year) {
		this.year = year;
	}

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

	public Double getRating() {
		return this.rating;
	}

	public void setRating(Double rank) {
		this.rating = rank;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Long getPlaysCount() {
		return this.playsCount;
	}

	public void setPlaysCount(Long plays) {
		this.playsCount = plays;
	}

}