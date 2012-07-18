package edu.berkeley.eecs.ruzenafit.shared.model;

public class User {

	private String name;
	private float score;

	public User(String name, float score) {
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

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

}
