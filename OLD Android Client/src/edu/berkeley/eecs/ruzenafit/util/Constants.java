package edu.berkeley.eecs.ruzenafit.util;

public class Constants {
	public static final String SHARED_PREFS_NAMESPACE = "RuzenaFitPrefs";
	public static final String UNDEFINED_USER_EMAIL = "__undefinedUserEmail";
	public static final String UNDEFINED_PRIVACY_SETTING = "__undefinedPrivacySetting";
	
	
	// Pseudo-constant int.  This gets set based on the privacy setting.  This is the
	// frequency between updates (for kcal, distance, pace, etc.) after starting
	// the workout. Default is 1 second
	private static int UPDATE_FREQUENCY = 3600000;

	public static int getUPDATE_FREQUENCY() {
		return UPDATE_FREQUENCY;
	}
	public static void setUPDATE_FREQUENCY(int newUpdateFrequency) {
		UPDATE_FREQUENCY = newUpdateFrequency;
	}
	
}
