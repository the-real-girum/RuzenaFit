package edu.berkeley.eecs.ruzenafit.util;


public class Constants {
	
	// SharedPreferences constants
	public static final String PREFS_NAMESPACE = "edu.berkeley.eecs.ruzenafit.Preferences";
	public static final String UNDEFINED = "__undefined";
	public static final String PRIVACY_SETTING = "privacySetting";
	public static final String FACEBOOK_NAME = "facebookName";
	public static final String FACEBOOK_EMAIL = "facebookEmail";
	public static final String WORKOUTS_JSON_STRING = "workoutTicksJSONString";
	public static final String TICKS_SINCE_LAST_SUCCESSFUL_UPLOAD = "ticksSinceLastSuccessfulUpload";
	public static final String HTTP_ERROR = "Failed to upload data to server";
	public static final String TOTAL_TICKS_SENT = "totalTicksSent";
	public static final String FACEBOOK_POST_STRING = "facebookPostString";
	
	/** 
	 * The number of "workout ticks" we should have before we start attempting to
	 * send data up to GAE.
	 */
	public static final int BATCH_SIZE = 5;
	
	
	public static final String NO_INTERNET_CONNECTION_MESSAGE = 
			"WARNING:  You are not connected to the internet right now.  " +
			"You won't be able to see current rankings, or upload your data to the server.";
	
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
