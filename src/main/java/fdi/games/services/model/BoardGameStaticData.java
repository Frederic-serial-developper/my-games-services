package fdi.games.services.model;

import java.util.HashSet;
import java.util.Set;

public class BoardGameStaticData {

	private Long id;

	private BoardGameType type;

	private String description;

	private String image;

	private int year;

	private int minPlayers;

	private int maxPlayers;

	private int playingTime;

	private String name;

	private BoardGameSource source;

	private Double rating;

	private final Set<String> categories;

	private final Set<String> mechanisms;

	private final Set<String> expansions;

	public BoardGameStaticData() {
		super();
		this.categories = new HashSet<>();
		this.mechanisms = new HashSet<>();
		this.expansions = new HashSet<>();
	}

	public void addCategory(String category) {
		this.categories.add(category);
	}

	public void addMechanism(String mechanism) {
		this.mechanisms.add(mechanism);
	}

	public void addExpansion(String expansion) {
		this.expansions.add(expansion);
	}

	public boolean isExpansion() {
		return BoardGameType.EXPANSION.equals(this.type);
	}

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

	public BoardGameType getType() {
		return this.type;
	}

	public void setType(BoardGameType type) {
		this.type = type;
	}

	public Set<String> getCategories() {
		return this.categories;
	}

	public Set<String> getMechanisms() {
		return this.mechanisms;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}