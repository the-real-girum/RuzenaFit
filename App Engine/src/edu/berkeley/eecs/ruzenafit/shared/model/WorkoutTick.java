package edu.berkeley.eecs.ruzenafit.shared.model;

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

	public static final String KEY_KCALS = "delta_kcals";				//
	public static final String KEY_SYSTEM_TIME = "system_time";			//
	public static final String KEY_LATITUDE = "latitude";				//
	public static final String KEY_LONGITUDE = "longitude";				//
	public static final String KEY_SPEED = "speed";						//
	public static final String KEY_ALTITUDE = "altitude";				//
	public static final String KEY_HAS_ACCURACY = "has_accuracy";		//
	public static final String KEY_ACCURACY = "accuracy";				//
	public static final String KEY_ACCUM_MINUTE_V = "accum_minute_v";  	//
	public static final String KEY_ACCUM_MINUTE_H = "accum_minute_h";  	//
	public static final String KEY_GPS_TIME = "gps_time";				//
	public static final String KEY_PRIVACY_SETTING = "privacy_setting";	//

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
	private String privacySetting;

	public WorkoutTick() {
		super();
	}
	
	public WorkoutTick(long time, float latitude, float longitude,
			float altitude, float speed, float hasAccuracy, float accuracy,
			long systemTime, float kCal, double accumMinuteV,
			double accumMinuteH, String privacySetting) {
		super();
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
		this.privacySetting = privacySetting;
	}
	
//	public WorkoutTick(long time, float latitude, float longitude,
//			float altitude, float speed, float hasAccuracy, float accuracy,
//			long systemTime, float kCal, double accumMinuteV,
//			double accumMinuteH, String privacySetting) {
//		super();
//		this.time = time;
//		this.latitude = latitude;
//		this.longitude = longitude;
//		this.altitude = altitude;
//		this.speed = speed;
//		this.hasAccuracy = hasAccuracy;
//		this.accuracy = accuracy;
//		this.systemTime = systemTime;
//		this.kCal = kCal;
//		this.accumMinuteV = accumMinuteV;
//		this.accumMinuteH = accumMinuteH;
//		this.privacySetting = findPrivacyEnumType(privacySetting);
//	}

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
	
	public String getPrivacySetting() {
		return privacySetting;
	}

	public void setPrivacySetting(String privacySetting) {
		this.privacySetting = privacySetting;
	}

	@Override
	public String toString() {
		return "WorkoutTick [time=" + time + ", latitude="
				+ latitude + ", longitude=" + longitude + ", altitude="
				+ altitude + ", speed=" + speed + ", hasAccuracy="
				+ hasAccuracy + ", accuracy=" + accuracy + ", systemTime="
				+ systemTime + ", kCal=" + kCal + ", accumMinuteV="
				+ accumMinuteV + ", accumMinuteH=" + accumMinuteH
				+ ", privacySetting=" + privacySetting + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WorkoutTick)) {
			return false;
		}
		
		WorkoutTick target = (WorkoutTick) obj;
		
		return this.accumMinuteH == target.accumMinuteH &&
				this.accumMinuteV == target.accumMinuteV &&
				this.accuracy == target.accuracy &&
				this.altitude == target.altitude &&
				this.hasAccuracy == target.hasAccuracy &&
				this.kCal == target.kCal &&
				this.latitude == target.latitude &&
				this.longitude == target.longitude &&
				this.privacySetting.equals(target.privacySetting) &&
				this.speed == target.speed &&
				this.systemTime == target.systemTime &&
				this.time == target.time;
	}

//	private PrivacyPreferenceEnum findPrivacyEnumType(String privacyString) {
//		if (privacySetting.equals("h") || privacySetting.equals("highPrivacy")) {
//			return PrivacyPreferenceEnum.highPrivacy;
//		}
//		else if (privacySetting.equals("m") || privacySetting.equals("mediumPrivacy")) {
//			return PrivacyPreferenceEnum.mediumPrivacy;
//		}
//		else if (privacySetting.equals("l") || privacySetting.equals("lowPrivacy")) {
//			return PrivacyPreferenceEnum.lowPrivacy;
//		}
//		else {
//			return null;
//		}
//	}
	

}