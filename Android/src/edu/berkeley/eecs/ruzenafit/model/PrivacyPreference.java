package edu.berkeley.eecs.ruzenafit.model;

/**
 * Basic enum representing the different privacy settings we have.
 * @author gibssa
 *
 */
public enum PrivacyPreference {
	
	highPrivacy("High Privacy", 0.8),
	mediumPrivacy("Medium Privacy", 1.0),
	lowPrivacy("Low Privacy", 1.2);
	
	private PrivacyPreference(String displayName, double value) {
		this.displayName = displayName;
		this.value = value;
	}
	
	private String displayName;
	private double value;
	
	// Getters/setters
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
}
