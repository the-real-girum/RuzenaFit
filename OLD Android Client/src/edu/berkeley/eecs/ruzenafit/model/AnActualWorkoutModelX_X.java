package edu.berkeley.eecs.ruzenafit.model;




/**
 * Gotta love the original code base  :)
 * 
 * @author gibssa
 *
 */
public class AnActualWorkoutModelX_X {

	private long workoutID;
	private String date;
	private String duration;
	private String totalCalories;
	private String averageSpeed;
	private String totalDistance;
	private CoordinateDataPoint[] geopoints;
	
	public AnActualWorkoutModelX_X() {
		super();
	}

	public long getWorkoutID() {
		return workoutID;
	}
	public void setWorkoutID(long workoutID) {
		this.workoutID = workoutID;
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
	public CoordinateDataPoint[] getGeopoints() {
		return geopoints;
	}
	public void setGeopoints(CoordinateDataPoint[] geopoints) {
		this.geopoints = geopoints;
	}

	@Override
	public String toString() {
		return "AnActualWorkoutModelX_X [date=" + date + ", duration="
				+ duration + ", totalCalories=" + totalCalories
				+ ", averageSpeed=" + averageSpeed + ", totalDistance="
				+ totalDistance + "]";
	}
	
	
}
