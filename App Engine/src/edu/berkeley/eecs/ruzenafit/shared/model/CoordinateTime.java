package edu.berkeley.eecs.ruzenafit.shared.model;

import java.util.Date;

public class CoordinateTime {

	private long longitude;
	private long latitude;
	private Date date;
	
	public CoordinateTime(long longitude, long latitude, Date date) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.date = date;
	}
	
	public long getLongitude() {
		return longitude;
	}
	public void setLongitude(long longitude) {
		this.longitude = longitude;
	}
	public long getLatitude() {
		return latitude;
	}
	public void setLatitude(long latitude) {
		this.latitude = latitude;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
}
