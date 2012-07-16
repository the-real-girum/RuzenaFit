package edu.berkeley.eecs.ruzenafit.model;

import android.util.Log;

/**
 * A model for each particular save point of a Workout. There is exactly one of
 * these for each "tick" of the app. This also means that there is exactly one
 * of these for each row of the internal SQLite database.
 * 
 * @author gibssa
 * 
 */
public class WorkoutTick {
	private final static String TAG = WorkoutTick.class.getSimpleName();

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

	private String imei;
	private long time;
	private float latitude;
	private float longitude;
	private float altitude;
	private float speed;
	private float hasAccuracy;
	private float accuracy;
	private long systemTime;
	private float kCal;
	private double accumMinuteV;
	private double accumMinuteH;

	public WorkoutTick(String imei, long time, float latitude, float longitude,
			float altitude, float speed, float hasAccuracy, float accuracy,
			long systemTime, float kCal, double accumMinuteV,
			double accumMinuteH) {
		super();
		this.imei = imei;
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.speed = speed;
		this.hasAccuracy = hasAccuracy;
		this.accuracy = accuracy;
		this.systemTime = systemTime;
		this.kCal = kCal;
		this.accumMinuteV = accumMinuteV;
		this.accumMinuteH = accumMinuteH;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public float getAltitude() {
		return altitude;
	}

	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getHasAccuracy() {
		return hasAccuracy;
	}

	public void setHasAccuracy(float hasAccuracy) {
		this.hasAccuracy = hasAccuracy;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public long getSystemTime() {
		return systemTime;
	}

	public void setSystemTime(long systemTime) {
		this.systemTime = systemTime;
	}

	public float getkCal() {
		return kCal;
	}

	public void setkCal(float kCal) {
		this.kCal = kCal;
	}

	public double getAccumMinuteV() {
		return accumMinuteV;
	}

	public void setAccumMinuteV(double accumMinuteV) {
		this.accumMinuteV = accumMinuteV;
	}

	public double getAccumMinuteH() {
		return accumMinuteH;
	}

	public void setAccumMinuteH(double accumMinuteH) {
		this.accumMinuteH = accumMinuteH;
	}

	@Override
	public String toString() {
		return "WorkoutPoint [imei=" + imei + ", time=" + time + ", latitude="
				+ latitude + ", longitude=" + longitude + ", altitude="
				+ altitude + ", speed=" + speed + ", hasAccuracy="
				+ hasAccuracy + ", accuracy=" + accuracy + ", systemTime="
				+ systemTime + ", kCal=" + kCal + ", accumMinuteV="
				+ accumMinuteV + ", accumMinuteH=" + accumMinuteH + "]";
	}

	/**
	 * Takes in a single line of "Edmund-ish" language data and parses it into a
	 * true WorkoutTick model object.
	 * 
	 * @param edmundishString
	 * @return
	 */
	public static WorkoutTick parseEdmundish(String edmundishString) {

		// Explode the string into pieces, delimited by commas.
		String[] explodedEdmundish = edmundishString.split(",");

		// Attempt to take all of the pieces and put them into their
		// respective data types.
		try {
			String imei = explodedEdmundish[0];
			long gpsTime = Long.parseLong(explodedEdmundish[1]);
			long systemTime = Long.parseLong(explodedEdmundish[2]);
			float latitude = Float.parseFloat(explodedEdmundish[3]);
			float longitude = Float.parseFloat(explodedEdmundish[4]);
			float altitude = Float.parseFloat(explodedEdmundish[5]);
			float speed = Float.parseFloat(explodedEdmundish[6]);
			float hasAccuracy = Float.parseFloat(explodedEdmundish[7]);
			float accuracy = Float.parseFloat(explodedEdmundish[8]);
			double accumMinuteV = Double.parseDouble(explodedEdmundish[9]);
			double accumMinuteH = Double.parseDouble(explodedEdmundish[10]);
			float kCal = Float.parseFloat(explodedEdmundish[11]);
			
			// Lastly, return a new WorkoutTick
			return new WorkoutTick(imei, gpsTime, latitude, longitude, altitude, speed, hasAccuracy, accuracy, systemTime, kCal, accumMinuteV, accumMinuteH);
		}
		catch (Exception e) {
			Log.e(TAG, "Unable to parse Edmundish: " + e.getMessage());
			return null;
		}
	}

}
