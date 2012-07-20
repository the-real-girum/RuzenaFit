package edu.berkeley.eecs.ruzenafit.shared.model;

public class UserRanking {

	public static final String USER_NAME = "userName";
	public static final String POINT_TOTAL = "pointTotal";
	
	private String name;
	private float score;

	public UserRanking(String name, float score) {
		super();
		this.name = name;
		this.score = score;
	}

	public UserRanking() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public static String convertImeiToUsername(String imei) {

		// TODO: We no longer need to map the IMEI to username, since we're just using facebook name
//		if (imei.equals("99000044488456")) {
//			return "Girum";
//		}
//		return "unknownIMEI";
		return imei;
	}
	
}
