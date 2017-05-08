package fdi.games.services.model;

public enum RatingLevel {

	LEVEL_0(0, 1), LEVEL_1(1, 2), LEVEL_2(2, 3), LEVEL_3(3, 4), LEVEL_4(4, 5), LEVEL_5(5, 6), LEVEL_6(6, 7), LEVEL_7(7,
			8), LEVEL_8(8, 9), LEVEL_9(9, 10);

	private int min;
	private int max;

	private RatingLevel(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public boolean match(Double value) {
		if (value == null) {
			return false;
		}
		// case where 10<=rating
		if (10 <= value && this == RatingLevel.LEVEL_9) {
			return true;
		}
		return this.min <= value && value < this.max;
	}
}
