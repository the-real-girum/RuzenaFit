package edu.berkeley.eecs.ruzenafit.shared.model;

import java.io.Serializable;

import javax.persistence.Id;


public class UserRanking implements Serializable {

	public static final String USER_NAME = "userName";
	public static final String POINT_TOTAL = "pointTotal";
	
	@Id
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

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UserRanking))
			return false;
		
		UserRanking userRanking = (UserRanking) obj;
		return userRanking.name.equals(this.name) &&
				userRanking.score == this.score;
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
