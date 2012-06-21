package edu.berkeley.eecs.ruzenafit.model;

import java.util.Date;

import com.google.android.maps.GeoPoint;

public class Geopoint_Time {
	private GeoPoint geoPoint;

	private Date date;

	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public void setGeoPoint(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Geopoint_Time(GeoPoint geoPoint, Date date) {
		super();
		this.geoPoint = geoPoint;
		this.date = date;
	}

}
