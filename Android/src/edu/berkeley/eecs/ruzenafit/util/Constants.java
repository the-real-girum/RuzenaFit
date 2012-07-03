package edu.berkeley.eecs.ruzenafit.util;


public class Constants {
	
	// SharedPreferences constants
	public static final String PREFS_NAMESPACE = "edu.berkeley.eecs.ruzenafit.Preferences";
	public static final String UNDEFINED_USER_EMAIL = "__undefinedUserEmail";
	public static final String UNDEFINED_PRIVACY_SETTING = "__undefinedPrivacySetting";
	public static final String UNDEFINED_IMEI = "__undefinedImei";
	public static final String PRIVACY_SETTING = "privacySetting";
	public static final String FACEBOOK_NAME = "facebookName";
	public static final String FACEBOOK_EMAIL = "facebookEmail";

	// SQL constants
	public static final String WORKOUT_TABLE = "workouts";
	
	// SQL column keys
	public static final String KEY_IMEI = "imei";
	public static final String KEY_KCALS = "delta_kcals";
	public static final String KEY_SYSTEM_TIME = "system_time";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_SPEED = "speed";
	public static final String KEY_ALTITUDE = "altitude";
	public static final String KEY_HAS_ACCURACY = "has_accuracy";
	public static final String KEY_ACCURACY = "accuracy";
	public static final String KEY_ACCUM_MINUTE_V = "accum_minute_v";
	public static final String KEY_ACCUM_MINUTE_H = "accum_minute_h";
	public static final String KEY_GPS_TIME = "gps_time";
	
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
