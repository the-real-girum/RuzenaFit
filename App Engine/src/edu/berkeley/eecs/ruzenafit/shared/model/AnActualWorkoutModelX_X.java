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

	private String privacySetting;
	private String date;
	private String duration;
	private String totalCalories;
	private String averageSpeed;
	private String totalDistance;
	
	public AnActualWorkoutModelX_X() {
		super();
	}
	
	public AnActualWorkoutModelX_X(String privacySetting, String date, String duration,
			String totalCalories, String averageSpeed, String totalDistance) {
		super();
		this.privacySetting = privacySetting;
		this.date = date;
		this.duration = duration;
		this.totalCalories = totalCalories;
		this.averageSpeed = averageSpeed;
		this.totalDistance = totalDistance;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof AnActualWorkoutModelX_X))
			return false;
		AnActualWorkoutModelX_X workout = (AnActualWorkoutModelX_X)arg0;
		
		return (this.averageSpeed.equals(	workout.getAverageSpeed()) 	&&
				this.date.equals(			workout.getDate()) 			&&
				this.duration.equals(		workout.getDuration()) 		&&
				this.totalCalories.equals(	workout.getTotalCalories()) &&
				this.totalDistance.equals(	workout.getTotalDistance()));
	}

	
	public String getPrivacySetting() {
		return privacySetting;
	}
	public void setPrivacySetting(String privacySetting) {
		this.privacySetting = privacySetting;
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
