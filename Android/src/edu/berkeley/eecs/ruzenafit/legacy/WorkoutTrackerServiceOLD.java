package edu.berkeley.eecs.ruzenafit.legacy;
//package edu.berkeley.eecs.ruzenafit.service;
//
//import java.text.DecimalFormat;
//import java.text.DecimalFormatSymbols;
//import java.util.Date;
//import java.util.Locale;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.os.Looper;
//import android.os.PowerManager;
//import android.os.SystemClock;
//import android.telephony.TelephonyManager;
//import android.text.TextUtils;
//import android.util.Log;
//import edu.berkeley.eecs.ruzenafit.R;
//import edu.berkeley.eecs.ruzenafit.access.DBHelper;
//import edu.berkeley.eecs.ruzenafit.activity.WorkoutTrackerActivity;
//import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
//import edu.berkeley.eecs.ruzenafit.util.Constants;
//import edu.berkeley.eecs.ruzenafit.util.KCalUtils;
//
//// TODO: Decompose this monster.
//public class WorkoutTrackerService extends Service {
//	private static final String TAG = "WorkoutTrackerService";
//	private static Context mContext;
//	protected static LocationManager mLocationManager = null;
//	protected Location mLocation = null;
//
//	/**
//	 * The minimum distance that you need to be from your previous location for
//	 * it to register as a "new location."
//	 */
//	protected final static float MIN_DISTANCE = 0; // in Meters
//
//	/** The "tick rate" of the LocationManager */
//	protected static long TICK_RATE = 500; // in Milliseconds (at least
//													// every 20 secs)
//	// note that kcal is only recorded once per minute.
//
//	private static LocationListener locationListener;
//	protected static SensorManager mSensorManager = null;
//	private static SensorEventListener mSensorEventListener;
//	private static String mMostrecent_Provider = "-99";
//	private static boolean mIsRunning = false;
//
//	private static long mMostrecent_GPS_Time = -99;
//	private static float mMostrecent_GPS_Latitude = -99;
//	private static float mMostrecent_GPS_Longitude = -99;
//	private static float mMostrecent_GPS_Altitude = -99;
//	private static float mMostrecent_GPS_Speed = -99;
//	private static float mMostrecent_GPS_HasAccuracy = -99;
//	private static float mMostrecent_GPS_Accuracy = -99;
//	private static String mMostrecent_System_Time = "unknown time";
//	// TODO: When the other instance variables are fields of the Workout model
//	// class,
//	// change this variable to be derivable from the other fields (using the
//	// Utils method) as
//	// a "getKCals()" method.
//	private static float mMostrecent_kCal = 0;
//
//	private static long lasttime = 0;
//	static long counter = 0;
//	private static double accum_minute_V = 0, accum_minute_H = 0;
//	private static double GRAVITY = 9.81;
//	private static int EEinterval = 10; // produce an EE estimate every 10
//										// seconds
//	private static int samplesPerWindow;
//	private static int windowTimeMillisec = 2000; // 2 seconds
//	private static int manySamples = 128; // should be plenty for 2 seconds
//											// (DELAY_UI on G1 was ~10
//											// samples/sec)
//	private static double[][] data = new double[3][manySamples];
//	private static PowerManager.WakeLock wl;
//	private static Kcal myKcal;
//	private static String imei = "0";
//
//	public static NotificationManager notificationManager;
//	public static final int NOTIFICATION_ID = 1;
//
//	public static long ntpDiff = 0;
//
//	public static String accumAccelDetail = "";
//
//	static private String genformat = "####0.00";
//	static private String geoformat = "####0.000000";
//
//	static DecimalFormat genfmt = new DecimalFormat(genformat,
//			new DecimalFormatSymbols(Locale.US));
//
//	static DecimalFormat geofmt = new DecimalFormat(geoformat,
//			new DecimalFormatSymbols(Locale.US));
//
//	// TODO: What's this?
//	protected void onResume() {
//		//super.onResume();
//		SharedPreferences preferences = getSharedPreferences(
//				Constants.PREFS_NAMESPACE, 0);
//
//		String notSet = "Privacy not set";
//		String lSet = "lowPrivacy";
//		String mSet = "mediumPrivacy";
//		String hSet = "highPrivacy";
//		
//		String p = preferences.getString(Constants.PRIVACY_SETTING,
//				notSet);
//
//		if (p.equals(lSet))
//		{
//			TICK_RATE = 0;
//		}
//
//		else if (p.equals(mSet))
//		{
//			TICK_RATE = 0;
//		}
//		else if (p.equals(hSet))
//		{
//			TICK_RATE = 0;
//		}
//
//	}
//	
//	@Override
//	public void onCreate() {
//		super.onCreate();
//		Log.i(getClass().getSimpleName(), "Service onCreate!!!");
//		mContext = this;
//
//		// get IMEI to uniquely identify users' posts to the server
//		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//		imei = mTelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
//		if (TextUtils.isEmpty(imei)) {
//			imei = "0";
//		}
//
//		// use shared pref to store the running state. this needs to persist in
//		// case the system kills the service and we get a restart
//		// Restore preferences
//		SharedPreferences settings = getSharedPreferences(
//				Constants.PREFS_NAMESPACE, 0);
//		boolean isrunningstored = settings.getBoolean("isrunning", false);
//		if (isrunningstored)
//			startLog();
//	}
//
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.i(getClass().getSimpleName(), "Service onStartCommand!!!");
//		return START_STICKY;
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		Log.i(getClass().getSimpleName(), "Service onDestroy!!!");
//
//		// in case the system is stopping us rather that the CalFit App, make
//		// sure to stop logging gracefully
//		if (mIsRunning)
//			stoplog();
//	}
//
//	public static int getStatus() {
//		if (mIsRunning)
//			return 1;
//		else
//			return 0;
//	}
//
//	public static void startLog() {
//		Log.i("CalFitService", "startlog!!!");
//
//		/** Legacy code */
//		// // Each time we start logging we start a new file regardless of
//		// // numwriten
//		// numTimesWritten = 0;
//		// writtenGPS = 0;
//		//
//		// fileNum = 0;
//		// fileNumGPS = 0;
//
//		// TODO: Replace with code to write to disk?
//		// FindNewFiles();
//
//		// this is needed to keep the service alive on Android 2.0+ systems
//		// setForeground(true); // this is ignored on the new systems!
//		int icon = R.drawable.star_on;
//		CharSequence tickerText = "CalFit BCN running";
//		long when = java.lang.System.currentTimeMillis();
//		Notification notification = new Notification(icon, tickerText, when);
//		CharSequence contentTitle = "CalFit BCN";
//		CharSequence contentText = "Currently running";
//		Intent notificationIntent = new Intent(mContext,
//				WorkoutTrackerActivity.class); // must use
//												// WorkoutTrackerActivity.class
//												// because
//												// this notification loads a new
//												// activity onto the stack, and
//												// if you call CalFit.class, it
//												// is hacked to auto kill it and
//												// brings about the previous
//												// activity layer, which in the
//												// case is the tabbed view
//												// personal page. you can't load
//												// perosnal page directly
//												// because it'll reset many of
//												// the global/instance/static
//												// variables that need to remain
//												// untouched for the duration of
//												// the application.
//		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
//				notificationIntent, 0);
//		notification.setLatestEventInfo(mContext, contentTitle, contentText,
//				contentIntent);
//		// notificationManager.notify(NOTIFICATION_ID, notification);
//		notification.flags |= Notification.FLAG_NO_CLEAR;
//
//		((Service) mContext).startForeground(NOTIFICATION_ID, notification);
//
//		// make sure to save our state as a pref in case we get killed by
//		// system.
//		SharedPreferences settings = mContext.getSharedPreferences(
//				Constants.PREFS_NAMESPACE, 0);
//		SharedPreferences.Editor editor = settings.edit();
//		editor.putBoolean("isrunning", true);
//		editor.commit();
//
//		myKcal = new Kcal();
//		myKcal.start();
//	}
//
//	public static void stoplog() {
//		Log.i("CalFitService", "stoplog!!!");
//		myKcal.stopGPSTracking();
//		myKcal.stopSensor();
//		// stop thread
//		if (myKcal != null) {
//			Thread dyingthread = myKcal;
//			myKcal = null;
//			dyingthread.interrupt();
//		}
//		// myKcal.stop();
//		mIsRunning = false;
//
//		// make sure to save our state as a pref in case we get killed by
//		// system.
//		SharedPreferences settings = mContext.getSharedPreferences(
//				Constants.PREFS_NAMESPACE, 0);
//		SharedPreferences.Editor editor = settings.edit();
//		editor.putBoolean("isrunning", false);
//		editor.commit();
//
//		// Make sure our notification is gone.
//		notificationManager = (NotificationManager) mContext
//				.getSystemService(Context.NOTIFICATION_SERVICE);
//		notificationManager.cancel(NOTIFICATION_ID);
//		((Service) mContext).stopForeground(true);
//
//		// release the wakelock to allow the device to sleep normally
//		wl.release();
//	}
//
//	/*************************************************************
//	 * Kcal stuff
//	 *************************************************************/
//	// TODO: Change this to a proper class
//	private static class Kcal extends Thread {
//		@Override
//		public void run() {
//
//			// super.run();
//
//			Looper.prepare();
//
//			// do not allow the CPU to go to sleep...
//			// NOTE: PARTIAL_WAKE_LOCK on Nexus One receives only location
//			// change updates
//			// but does not receive sensor change updates...
//			// hence needed to go to SCREEN_DIM_WAKE_LOCK
//			// UPDATE: as of OS 2.2 Nexus One can receive sensor updates for
//			// PARTIAL_WAKE_LOCK
//			PowerManager pm = (PowerManager) mContext
//					.getSystemService(Context.POWER_SERVICE);
//			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//					"CalFitdService");
//			wl.acquire();
//
//			lasttime = SystemClock.elapsedRealtime();
//			samplesPerWindow = 0;
//			accum_minute_V = 0;
//			accum_minute_H = 0;
//			counter = 0;
//			startSensor();
//			setupAndStartGPSTracking();
//			mIsRunning = true;
//
//			Looper.loop();
//		}
//
//		/******************************************
//		 * GPS stuff
//		 * 
//		 * Note the use of MIN_TIME and MIN_DISTANCE which are only "hints" to
//		 * the android system
//		 *********************************************/
//		private void setupAndStartGPSTracking() {
//			Log.i("CalFitService", "startGPS!!!");
//
//			// create my locationListener
//			locationListener = new MyLocationListener();
//
//			// get the LocationManager from the system
//			mLocationManager = (LocationManager) mContext
//					.getSystemService(Context.LOCATION_SERVICE);
//
//			// if GPS is not enabled, enable it.
//			if (!mLocationManager
//					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//				Log.d(getClass().getSimpleName(),
//						"GPS Provider is not enabled.");
//				Log.d(getClass().getSimpleName(), "Enabling GPS Provider...");
//				Intent enableGPS = new Intent(
//						"android.location.GPS_ENABLED_CHANGE");
//				enableGPS.putExtra("enabled", true);
//				mContext.sendBroadcast(enableGPS);
//				Log.d(getClass().getSimpleName(),
//						"GPS Provider has been enabled.");
//			}
//
//			// register for location update events to be provided to my
//			// locationListener
//			mLocationManager.requestLocationUpdates(
//					LocationManager.GPS_PROVIDER, TICK_RATE, MIN_DISTANCE,
//					locationListener);
//
//			// also ask for location updates from network location
//			// register for location update events to be provided to my
//			// locationListener
//			mLocationManager.requestLocationUpdates(
//					LocationManager.NETWORK_PROVIDER, TICK_RATE, MIN_DISTANCE,
//					locationListener);
//
//			Location loc = mLocationManager
//					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//			if (loc != null) {
//				mMostrecent_GPS_Time = loc.getTime();
//				mMostrecent_System_Time = new Date().toGMTString();
//				mMostrecent_GPS_Time = java.lang.System.currentTimeMillis();
//				mMostrecent_GPS_Latitude = (float) loc.getLatitude();
//				mMostrecent_GPS_Longitude = (float) loc.getLongitude();
//				mMostrecent_GPS_Altitude = (float) loc.getAltitude();
//				mMostrecent_GPS_Speed = loc.getSpeed();
//				if (loc.hasAccuracy())
//					mMostrecent_GPS_HasAccuracy = 1;
//				else
//					mMostrecent_GPS_HasAccuracy = 0;
//				mMostrecent_GPS_Accuracy = loc.getAccuracy();
//			} else {
//				// TODO: Change this whole service to NOT EVEN START if 
//				// location services won't work.  That is, check to make 
//				// sure that location is not null before you start this
//				// service.
//				Log.e(TAG, "ERROR: getLastKnownLocation() failed!");
//			}
//		}
//
//		private void stopGPSTracking() {
//			Log.i("CalFitService", "stopGPS!!!");
//			// unregister for location update events
//			mLocationManager.removeUpdates(locationListener);
//		}
//
//		private class MyLocationListener implements LocationListener {
//			public void onLocationChanged(Location loc) {
//				if (loc != null) {
//
//					mMostrecent_System_Time = new Date().toGMTString();
//
//					Log.i(getClass().getSimpleName(),
//							"Location : Time: " + loc.getTime() + " systime: "
//									+ mMostrecent_System_Time + " Lat: "
//									+ loc.getLatitude() + " Lng: "
//									+ loc.getLongitude() + " Alt: "
//									+ loc.getAltitude() + " Speed: "
//									+ loc.getSpeed() + " Provider: "
//									+ loc.getProvider());
//
//					// update the "most recent GPS" parameters in the
//					// CalFitdService
//					// note the DB stuff occurs in the Kcal computation
//					// associated with the
//					// onSensorChanged listener
//					// mMostrecent_GPS_Time = loc.getTime();
//					mMostrecent_GPS_Time = java.lang.System.currentTimeMillis();
//					// mMostrecent_System_Time =
//					// java.lang.System.currentTimeMillis(); // done above the
//					// Log.i()
//
//					// TODO: IF GPS IS DISABLED ON THE PHONE, then disable
//					// tracking
//					// and tell the user.
//					mMostrecent_GPS_Latitude = (float) loc.getLatitude();
//					mMostrecent_GPS_Longitude = (float) loc.getLongitude();
//					mMostrecent_GPS_Altitude = (float) loc.getAltitude();
//					mMostrecent_GPS_Speed = loc.getSpeed();
//					if (loc.hasAccuracy())
//						mMostrecent_GPS_HasAccuracy = 1;
//					else
//						mMostrecent_GPS_HasAccuracy = 0;
//					mMostrecent_GPS_Accuracy = loc.getAccuracy();
//					mMostrecent_Provider = loc.getProvider();
//
//					// Saves the data internally.  This method checks for batches.
//					saveData("Called from onLocationChanged()");
//				}
//			}
//
//			public void onProviderDisabled(String provider) {
//				Log.i(getClass().getSimpleName(), "GPS provider disabled");
//				// do nothing
//			}
//
//			public void onProviderEnabled(String provider) {
//				Log.i(getClass().getSimpleName(), "GPS provider enabled");
//				// do nothing
//			}
//
//			public void onStatusChanged(String provider, int status,
//					Bundle extras) {
//				Log.i(getClass().getSimpleName(), "GPS status changed");
//			}
//		}
//
//		/*************************************************************
//		 * ACCELEROMETER stuff
//		 * 
//		 * For Sensor.TYPE_ACCELEROMETER: All values are in SI units (m/s^2) and
//		 * measure the acceleration applied to the phone minus the force of
//		 * gravity. values[0]: Acceleration minus Gx on the x-axis values[1]:
//		 * Acceleration minus Gy on the y-axis values[2]: Acceleration minus Gz
//		 * on the z-axis
//		 * 
//		 * Examples:
//		 * 
//		 * When the device lies flat on a table and is pushed on its left side
//		 * toward the right, the x acceleration value is positive. When the
//		 * device lies flat on a table, the acceleration value is +9.81, which
//		 * correspond to the acceleration of the device (0 m/s^2) minus the
//		 * force of gravity (-9.81 m/s^2). When the device lies flat on a table
//		 * and is pushed toward the sky with an acceleration of A m/s^2, the
//		 * acceleration value is equal to A+9.81 which correspond to the
//		 * acceleration of the device (+A m/s^2) minus the force of gravity
//		 * (-9.81 m/s^2).
//		 * ****************************************************************/
//		private void startSensor() {
//			Log.i("CalFitService", "startSensor!!!");
//
//			// create my sensorListener
//			mSensorEventListener = new MySensorEventListener();
//
//			mSensorManager = (SensorManager) mContext
//					.getSystemService(Context.SENSOR_SERVICE);
//
//			/*
//			 * in production version consider using
//			 * SensorManager.SENSOR_DELAY_FASTEST, SENSOR_DELAY_UI (was a good
//			 * compromise) SENSOR_DELAY_NORMAL (probably best battery life)
//			 */
//			mSensorManager.registerListener(mSensorEventListener,
//					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//					SensorManager.SENSOR_DELAY_UI);
//		}
//
//		private void stopSensor() {
//			Log.i("CalFitService", "stopSensor!!!");
//
//			// unregister sensorListener
//			mSensorManager.unregisterListener(mSensorEventListener);
//		}
//
//		private class MySensorEventListener implements SensorEventListener {
//
//			public void onAccuracyChanged(Sensor sensor, int accuracy) {
//				// do nothing
//			}
//
//			public void onSensorChanged(SensorEvent event) {
//				if (event != null) {
//					/*
//					 * Log.i(getClass().getSimpleName(), "Sensor : X: " +
//					 * event.values[0] + " Y: " + event.values[1] + " Z: " +
//					 * event.values[2]); mMostrecent_Sensor_X = event.values[0];
//					 * mMostrecent_Sensor_Y = event.values[1];
//					 * mMostrecent_Sensor_Z = event.values[2];
//					 * 
//					 * // compute Kcal computeKcal();
//					 */
//
//					data[0][samplesPerWindow] = event.values[0];
//					data[1][samplesPerWindow] = event.values[1];
//					data[2][samplesPerWindow] = event.values[2];
//					samplesPerWindow++;
//
//					// write detail accelerometry to file
//					// code?
//					// writeFileDetailNew (System.currentTimeMillis(),
//					// event.values[0], event.values[1], event.values[2]);
//					accumAccelDetail = accumAccelDetail
//							+ System.currentTimeMillis() + ","
//							+ event.values[0] + "," + event.values[1] + ","
//							+ event.values[2] + "\n";
//
//					long now = SystemClock.elapsedRealtime();
//					// check if we have a window of data...
//					if (now >= (lasttime + windowTimeMillisec)) {
//						Log.i(getClass().getSimpleName(), "Sensor : X: "
//								+ genfmt.format(event.values[0]) + " Y: "
//								+ genfmt.format(event.values[1]) + " Z: "
//								+ genfmt.format(event.values[2]));
//
//						// write detail accelerometry to file. 
//						// TODO: Replace with code to write to disk?
//						// writeFileDetailNew2();
//
//						// calculate sample window part of kcal
//						double[] result = KCalUtils.calculateKcal(data,
//								samplesPerWindow);
//
//						finalKcalResult(result);
//
//						samplesPerWindow = 0;
//						lasttime = now;
//					}
//
//				}
//			}
//		}
//
//		// this is called after each sample window is processed; if we have a
//		// minute's
//		// worth of accumulated V and H results then generate the energy
//		// estimate
//		private void finalKcalResult(double[] VH) {
//			double EE_minute;
//
//			// Note the (samplesperwindow/(windowtimemillisec/1000)) scales back
//			// down to 60 in a min
//			if (samplesPerWindow > 0) {
//				accum_minute_V += (VH[0] / (samplesPerWindow / ((float) windowTimeMillisec / 1000)))
//						/ GRAVITY;
//				accum_minute_H += (VH[1] / (samplesPerWindow / ((float) windowTimeMillisec / 1000)))
//						/ GRAVITY;
//			}
//			counter++;
//
//			if (counter >= (EEinterval / (windowTimeMillisec / 1000))) {
//				// EEact(k) = a*H^p1 + b*V^p2
//				//
//				// assume: mass(kg) = 80 kg
//				// gender = 1 (male)
//				//
//				// a = (12.81 * mass(kg) + 843.22) / 1000 = 1.87
//				// b = (38.90 * mass(kg) - 682.44 * gender(1=male,2=female) +
//				// 692.50)/1000 = 3.12
//				// p1 = (2.66 * mass(kg) + 146.72)/1000 = 0.36
//				// p2 = (-3.85 * mass(kg) + 968.28)/1000 = 0.66
//
//				// EE in units kcal/minute --> EE_minute
//				// the 4.184 is to convert from KJ to kilocalories
//				EE_minute = (1.87 * Math.pow(accum_minute_H, 0.36) + 3.12 * Math
//						.pow(accum_minute_V, 0.66)) / 4.184;
//				mMostrecent_kCal = (float) EE_minute;
//
//				Log.i(getClass().getSimpleName(),
//						"*** EE result: " + genfmt.format(EE_minute));
//
//				// activity-based thresholding of GPS
//				/*
//				 * if ((mMostrecent_kCal < lowactivitythresholdforGPS) &&
//				 * (GPSstatus==1)) { stopGPS(); } else if ((mMostrecent_kCal >=
//				 * lowactivitythresholdforGPS) && (GPSstatus==0)) { startGPS();
//				 * }
//				 */
//
//				// Returns the current system time in milliseconds since January
//				// 1, 1970 00:00:00 UTC.
//				// this is the clock on the device as set by user/network.
//				mMostrecent_System_Time = new Date().toGMTString();
//
//				// Save data to the phone (this method will check for batch size, and
//				// upload to server if the batch is large enough).
//				saveData("Called from onSensorChanged()");
//
//				// reset the counters
//				accum_minute_V = 0;
//				accum_minute_H = 0;
//				counter = 0;
//			}
//		}
//	}
//
//	/**
//	 * Fundamental saveData() method.  This method will check if we have enough new
//	 * workout "ticks" to justify batching them up and sending the new ones to the
//	 * server for update.
//	 * 
//	 * @param methodThisMethodWasCalledFrom
//	 */
//	private static void saveData(String methodThisMethodWasCalledFrom) {
//		
//		// Debugging output
//		Log.i(TAG, "testWrite: " + methodThisMethodWasCalledFrom);
//		Log.i(TAG, "Current Workout State: ");
//		Log.i(TAG, "IMEI: " + imei);
//		Log.i(TAG, "Delta KCals burned: " + mMostrecent_kCal);
//		Log.i(TAG, "System Time: " + mMostrecent_System_Time);
//		Log.i(TAG, "GPS Latitude" + mMostrecent_GPS_Latitude);
//		Log.i(TAG, "GPS Longitude" + mMostrecent_GPS_Longitude);
//		Log.i(TAG, "Speed: " + mMostrecent_GPS_Speed);
//		Log.i(TAG, "Altitude: " + mMostrecent_GPS_Altitude);
//		Log.i(TAG, "HasAccuracy: " + mMostrecent_GPS_HasAccuracy);
//		Log.i(TAG, "Accuracy: " + mMostrecent_GPS_Accuracy);
//		Log.i(TAG, "Accum_Minute_V" + accum_minute_V);
//		Log.i(TAG, "Accum_Minute_H" + accum_minute_H);
//		Log.i(TAG, "Most Recent GPS Time: " + mMostrecent_GPS_Time);
//
//		// Instantiate a new workout model to pass data around easier.
//		WorkoutTick workout = new WorkoutTick(imei, mMostrecent_GPS_Time,
//				mMostrecent_GPS_Latitude, mMostrecent_GPS_Longitude,
//				mMostrecent_GPS_Altitude, mMostrecent_GPS_Speed,
//				mMostrecent_GPS_HasAccuracy, mMostrecent_GPS_Accuracy,
//				mMostrecent_System_Time, mMostrecent_kCal, accum_minute_V,
//				accum_minute_H);
//
//		// Go ahead and insert this "tick" into the internal SQLite database.
//		DBHelper.insertWorkoutIntoDatabase(workout, mContext);
//		
//		// TODO: Count up the number of "ticks" we've had so far.
//		
//		
//		// TODO: If we have enough unsent "ticks" to batch up, then batch up 
//		// the unsent "ticks" and send it over to the server.
//	}
//
//	@Override
//	public IBinder onBind(Intent arg0) {
//		// do nothing.
//		return null;
//	}
//
//}
