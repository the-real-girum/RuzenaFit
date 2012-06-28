package edu.berkeley.eecs.ruzenafit.model;

import java.util.Date;

import com.google.android.maps.GeoPoint;

public class CoordinateDataPoint {

	private GeoPoint geopoint;
	private String date;
	private float kCals;

	public CoordinateDataPoint(GeoPoint geopoint) {
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
	public CoordinateDataPoint(String latitude, String longitude, String date, float kCals) {
		this.geopoint = new GeoPoint(Integer.parseInt(latitude), Integer.parseInt(longitude));
		this.date = date;
		this.kCals = kCals;
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
	public float getkCals() {
		return kCals;
	}
	public void setkCals(float kCals) {
		this.kCals = kCals;
	}
	
}
