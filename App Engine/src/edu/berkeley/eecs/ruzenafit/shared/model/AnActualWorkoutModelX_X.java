package edu.berkeley.eecs.ruzenafit.shared.model;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * Gotta love the original code base  :)
 * 
 * @author gibssa
 *
 */
@XmlRootElement(name = "workout")
public class AnActualWorkoutModelX_X {

	private String date;
	private String duration;
	private String totalCalories;
	private String averageSpeed;
	private String totalDistance;
	
	public AnActualWorkoutModelX_X() {
		super();
	}
	
	public AnActualWorkoutModelX_X(String date, String duration,
			String totalCalories, String averageSpeed, String totalDistance) {
		super();
		this.date = date;
		this.duration = duration;
		this.totalCalories = totalCalories;
		this.averageSpeed = averageSpeed;
		this.totalDistance = totalDistance;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getTotalCalories() {
		return totalCalories;
	}
	public void setTotalCalories(String totalCalories) {
		this.totalCalories = totalCalories;
	}
	public String getAverageSpeed() {
		return averageSpeed;
	}
	public void setAverageSpeed(String averageSpeed) {
		this.averageSpeed = averageSpeed;
	}
	public String getTotalDistance() {
		return totalDistance;
	}
	public void setTotalDistance(String totalDistance) {
		this.totalDistance = totalDistance;
	}
}