package edu.berkeley.eecs.ruzenafit.model;

public class User {
	
	public static final String KEY_USER = "name";
	public static final String KEY_SCORE = "score";
	
	private String name;
	private double score;

	public User(String name, double score) {
		super();
		this.name = name;
		this.score = score;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

}
