package com.example.table.client;
        

public class RuzenaRanking {
	 private String playerId;
	  private double calories;
	  private double points;

	  public RuzenaRanking() {
	  }

	  public RuzenaRanking(String playerId, double calories, double points) {
	    this.playerId = playerId;
	    this.calories = calories;
	    this.points = points;
	  }

	  public String getplayerId() {
	    return this.playerId;
	  }

	  public double getcalories() {
	    return this.calories;
	  }

	  public double getpoints() {
	    return this.points;
	  }

	  public double getpointsPercent() {
	    return 10.0 * this.points / this.calories;
	  }

	  public void setplayerId(String playerId) {
	    this.playerId = playerId;
	  }

	  public void setcalories(double calories) {
	    this.calories = calories;
	  }

	  public void setpoints(double points) {
	    this.points = points;
	  }
	  

}
