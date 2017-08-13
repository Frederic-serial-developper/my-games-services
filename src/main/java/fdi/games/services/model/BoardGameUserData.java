package fdi.games.services.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BoardGameUserData {

	private Long id;

	private String name;

	private BoardGameStatus status;

	private String image;

	private Long playsCount = 0l;

	private final List<Play> plays;

	public BoardGameUserData() {
		this.plays = new ArrayList<>();
	}

	public boolean isPreviouslyOwned() {
		return BoardGameStatus.PREVIOUSLY_OWNED.equals(this.status);
	}

	public void incrementPlayCount(Integer count) {
		this.playsCount = this.playsCount + count;
	}

	public void addPlay(Play play) {
		if (play != null) {
			this.plays.add(play);
		}
	}

	public void addAllPlays(Collection<Play> plays) {
		if (plays != null) {
			this.plays.addAll(plays);
		}
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

	public List<Play> getPlays() {
		return this.plays;
	}

}