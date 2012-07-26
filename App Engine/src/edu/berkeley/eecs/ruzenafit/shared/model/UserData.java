package edu.berkeley.eecs.ruzenafit.shared.model;

import com.google.gwt.maps.client.geom.LatLng;

public class UserData {

	private String username;
	private LatLng[] locations;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LatLng[] getLocations() {
		return locations;
	}

	public void setLocations(LatLng[] locations) {
		this.locations = locations;
	}
}
