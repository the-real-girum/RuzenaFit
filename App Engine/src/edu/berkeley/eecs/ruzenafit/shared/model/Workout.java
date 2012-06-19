package edu.berkeley.eecs.ruzenafit.shared.model;

import java.text.DateFormat;
import java.util.Date;


/**
 * Model class for what a timed "Workout" is.
 *  
 * @author gibssa
 *
 */
public class Workout {
	
	private Date date;
	private long duration;
	private float totalCalories;
	private float averageSpeed;
	private float totalDistance;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public float getTotalCalories() {
		return totalCalories;
	}
	public void setTotalCalories(float totalCalories) {
		this.totalCalories = totalCalories;
	}
	public float getAverageSpeed() {
		return averageSpeed;
	}
	public void setAverageSpeed(float averageSpeed) {
		this.averageSpeed = averageSpeed;
	}
	public float getTotalDistance() {
		return totalDistance;
	}
	public void setTotalDistance(float totalDistance) {
		this.totalDistance = totalDistance;
	}
	
	
	
}
