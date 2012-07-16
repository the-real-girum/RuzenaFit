package edu.berkeley.eecs.ruzenafit.util;


public class Constants {
	
	// SharedPreferences constants
	public static final String PREFS_NAMESPACE = "edu.berkeley.eecs.ruzenafit.Preferences";
//	public static final String UNDEFINED_USER_EMAIL = "__undefinedUserEmail";
//	public static final String UNDEFINED_PRIVACY_SETTING = "__undefinedPrivacySetting";
//	public static final String UNDEFINED_IMEI = "__undefinedImei";
	public static final String UNDEFINED = "__undefined";
	public static final String PRIVACY_SETTING = "privacySetting";
	public static final String FACEBOOK_NAME = "facebookName";
	public static final String FACEBOOK_EMAIL = "facebookEmail";
	public static final String WORKOUTS_JSON_STRING = "workoutTicksJSONString";

	// SQL constants
	public static final String WORKOUT_TABLE = "workouts";
	
	
	// Pseudo-constant int.  This gets set based on the privacy setting.  This is the
	// frequency between updates (for kcal, distance, pace, etc.) after starting
	// the workout. Default is 1 second
//	private static int UPDATE_FREQUENCY = 3600000;

//	public static int getUPDATE_FREQUENCY() {
//		return UPDATE_FREQUENCY;
//	}
//	public static void setUPDATE_FREQUENCY(int newUpdateFrequency) {
//		UPDATE_FREQUENCY = newUpdateFrequency;
//	}
	
}
