package edu.berkeley.eecs.ruzenafit.model;

import java.util.Date;

import com.google.android.maps.GeoPoint;

public class GeoPoint_Time {

	private GeoPoint geopoint;
	private String date;

	public GeoPoint_Time(GeoPoint geopoint) {
		super();
		this.geopoint = geopoint;
		this.date = new Date().toGMTString();  // set date to be current system time.
	}
	
	/**
	 * Convience constructor for reading strings from database.
	 * @param latitude
	 * @param longitude
	 * @param date
	 */
	public GeoPoint_Time(String latitude, String longitude, String date) {
		this.geopoint = new GeoPoint(Integer.parseInt(latitude), Integer.parseInt(longitude));
		this.date = date;
	}
	
	public GeoPoint getGeopoint() {
		return geopoint;
	}
	public void setGeopoint(GeoPoint geopoint) {
		this.geopoint = geopoint;
	}
	public String getDate() {
		return date;
	}
	
}
