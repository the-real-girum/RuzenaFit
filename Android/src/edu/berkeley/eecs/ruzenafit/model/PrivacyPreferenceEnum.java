package edu.berkeley.eecs.ruzenafit.model;

/**
 * Basic enum representing the different privacy settings we have.
 * @author gibssa
 *
 */
public enum PrivacyPreferenceEnum {
	
	highPrivacy("High Privacy", 0.8),
	mediumPrivacy("Medium Privacy", 1.2),
	lowPrivacy("Low Privacy", 1.8);
	
	private PrivacyPreferenceEnum(String displayName, double value) {
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
