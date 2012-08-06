package edu.berkeley.eecs.ruzenafit.shared.model;

import java.util.ArrayList;

import javax.persistence.Id;


public class UserData {

	@Id
	private String username;
	private ArrayList<WorkoutTick> locations;

	public UserData() {
		super();
		this.locations = new ArrayList<WorkoutTick>();
	}

	public UserData(String username, ArrayList<WorkoutTick> locations) {
		super();
		this.username = username;
		this.locations = locations;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public ArrayList<WorkoutTick> getLocations() {
		return locations;
	}

	public void setLocations(ArrayList<WorkoutTick> locations) {
		this.locations = locations;
	}
}
