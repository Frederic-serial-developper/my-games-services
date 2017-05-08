package fdi.games.services.business;

public class BoardGameServiceException extends Exception {

	private static final long serialVersionUID = -5399786049998217394L;

	public BoardGameServiceException(String message) {
		super(message);
	}

	public BoardGameServiceException(String message, Throwable e) {
		super(message, e);
	}

}
