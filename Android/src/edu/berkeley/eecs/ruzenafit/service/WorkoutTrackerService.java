package edu.berkeley.eecs.ruzenafit.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.access.FileAccessHelper;
import edu.berkeley.eecs.ruzenafit.access.GoogleAppEngineHelper;
import edu.berkeley.eecs.ruzenafit.access.SharedPreferencesHelper;
import edu.berkeley.eecs.ruzenafit.activity.WorkoutTrackerActivity;
import edu.berkeley.eecs.ruzenafit.model.PrivacyPreferenceEnum;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.util.Constants;
import edu.berkeley.eecs.ruzenafit.util.KCalUtils;

public class WorkoutTrackerService extends Service {
	private static final String TAG = WorkoutTrackerService.class
			.getSimpleName();

	private static Context mContext;
	protected static LocationManager mLocationManager = null;
	protected Location mLocation = null;

	// GPS updating...
	// TODO: Change this to be variable by privacy preference?
	protected final static float MIN_DISTANCE = 0; // in Meters

	// TODO: Change this tick rate to be changed by privacy preferences.
	/** The "tick rate" of each workout tick. */
	protected final static long MIN_TIME = 20000; // in Milliseconds (at least
													// every 20 secs)
	// note that kcal is only recorded once per minute.

	private static LocationListener locationListener;
	protected static SensorManager mSensorManager = null;
	private static SensorEventListener mSensorEventListener;
	private static boolean mIsRunning = false;
	private static long mMostrecent_GPS_Time = -99;
	private static float mMostrecent_GPS_Latitude = -99;
	private static float mMostrecent_GPS_Longitude = -99;
	private static float mMostrecent_GPS_Altitude = -99;
	private static float mMostrecent_GPS_Speed = -99;
	private static float mMostrecent_GPS_HasAccuracy = -99;
	private static float mMostrecent_GPS_Accuracy = -99;
	private static String mMostrecent_Provider = "-99";
	private static long mMostrecent_System_Time = -99;

	private static float mMostrecent_kCal = 0;
	private static long mMostrecent_Time = 0;
	private static long lasttime = 0;
	static long counter = 0;
	private static double accum_minute_V = 0, accum_minute_H = 0;
	private static double GRAVITY = 9.81;
	private static int EEinterval = 10; // produce an EE estimate every 10
										// seconds
	private static int samplesperwindow;
	private static int windowtimemillisec = 2000; // 2 seconds
	private static int manysamples = 128; // should be plenty for 2 seconds
											// (DELAY_UI on G1 was ~10
											// samples/sec)
	private static double[][] data = new double[3][manysamples];
	private static PowerManager.WakeLock wl;
	private static Kcal myKcal;
	private static String imei = "0";

	/** The number of files we've written so far. Updated by findFileNums(). */
	private static int fileNum = 0;
	/** Same as above, but for GPS data. */
	private static int fileNumGPS = 0;

	/** The number of lines we're written for this particular file. */
	private static int written = 0;
	/** Same as above, but for GPS data. */
	private static int writtenGPS = 0;

	/**
	 * Threshold constant for when we should start writing to a new file.
	 * "Start a new file for every 10,000 entries writen to file."
	 */
	private static int MAX_WRITTEN = 10000;

	/**
	 * Threshold constant for when we should start writing to a new file. "start
	 * a new file for every 1,000 entries writen to file (for the new accel
	 * detail writer that only writes chunks depending on windowtimemillisec"
	 */
	private static int MAX_WRITTEN_2 = 1000;

	public static NotificationManager notificationManager;
	public static final int NOTIFICATION_ID = 1;

	public static long ntpdiff = 0;

	public static String accum_acceldetail = "";

	static private String genformat = "####0.00";
	static private String geoformat = "####0.000000";

	static DecimalFormat genfmt = new DecimalFormat(genformat,
			new DecimalFormatSymbols(Locale.US));

	static DecimalFormat geofmt = new DecimalFormat(geoformat,
			new DecimalFormatSymbols(Locale.US));

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service onCreate!!!");
		mContext = this;

		// get IMEI to uniquely identify users' posts to the server
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		imei = mTelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
		if (TextUtils.isEmpty(imei)) {
			imei = "0";
		}

		// use shared pref to store the running state. this needs to persist in
		// case the system kills the service and we get a restart
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(
				Constants.PREFS_NAMESPACE, 0);
		boolean isRunningStored = settings.getBoolean("isrunning", false);
		if (isRunningStored)
			startLog();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Service onStartCommand!!!");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service onDestroy!!!");

		// in case the system is stopping us rather that the CalFit App, make
		// sure to stop logging gracefully
		if (mIsRunning)
			stopLog();
	}

	public static int getStatus() {
		return mIsRunning ? 1 : 0;
	}

	public static void startLog() {
		Log.i(TAG, "startlog!!!");

		// Each time we start logging we start a new file regardless of
		// numwriten
		written = 0;
		writtenGPS = 0;

		fileNum = 0;
		fileNumGPS = 0;
		findFileNums();

		// this is needed to keep the service alive on Android 2.0+ systems
		// setForeground(true); // this is ignored on the new systems!
		int icon = R.drawable.star_on;
		CharSequence tickerText = "CalFit BCN running";
		long when = java.lang.System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		CharSequence contentTitle = "CalFit BCN";
		CharSequence contentText = "Currently running";
		Intent notificationIntent = new Intent(mContext,
				WorkoutTrackerActivity.class); // must use WorkoutTrackerActivity.class because
												// this notification loads a new
												// activity onto the stack, and
												// if you call WorkoutTrackerActivity.class, it
												// is hacked to auto kill it and
												// brings about the previous
												// activity layer, which in the
												// case is the tabbed view
												// personal page. you can't load
												// perosnal page directly
												// because it'll reset many of
												// the global/instance/static
												// variables that need to remain
												// untouched for the duration of
												// the application.
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(mContext, contentTitle, contentText,
				contentIntent);
		// notificationManager.notify(NOTIFICATION_ID, notification);
		notification.flags |= Notification.FLAG_NO_CLEAR;

		((Service) mContext).startForeground(NOTIFICATION_ID, notification);

		// make sure to save our state as a preference in case we get killed by
		// system.
		SharedPreferences settings = mContext.getSharedPreferences(
				Constants.PREFS_NAMESPACE, 0);
		SharedPreferences.Editor editor = settings.edit();
		// TODO: String literal.
		editor.putBoolean("isrunning", true);
		editor.commit();

		myKcal = new Kcal();
		myKcal.start();
	}

	public static void stopLog() {
		Log.i(TAG, "stoplog!!!");
		myKcal.stopGPS();
		myKcal.stopSensor();
		// stop thread
		if (myKcal != null) {
			Thread dyingthread = myKcal;
			myKcal = null;
			dyingthread.interrupt();
		}
		// myKcal.stop();
		mIsRunning = false;

		// make sure to save our state as a pref in case we get killed by
		// system.
		SharedPreferences settings = mContext.getSharedPreferences(
				Constants.PREFS_NAMESPACE, 0);
		SharedPreferences.Editor editor = settings.edit();
		// TODO: String literal.
		editor.putBoolean("isrunning", false);
		editor.commit();

		// Make sure our notification is gone.
		notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		((Service) mContext).stopForeground(true);

		// release the wakelock to allow the device to sleep normally
		wl.release();
	}

	/*************************************************************
	 * Kcal stuff
	 *************************************************************/
	static class Kcal extends Thread {
		public Handler mHandler;

		@Override
		public void run() {

			// super.run();

			Looper.prepare();

			// do not allow the CPU to go to sleep...
			// NOTE: PARTIAL_WAKE_LOCK on Nexus One receives only location
			// change updates
			// but does not receive sensor change updates...
			// hence needed to go to SCREEN_DIM_WAKE_LOCK
			// UPDATE: as of OS 2.2 Nexus One can receive sensor updates for
			// PARTIAL_WAKE_LOCK
			PowerManager pm = (PowerManager) mContext
					.getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"CalFitdService");
			wl.acquire();

			lasttime = SystemClock.elapsedRealtime();
			samplesperwindow = 0;
			accum_minute_V = 0;
			accum_minute_H = 0;
			counter = 0;
			startSensor();
			startGPS();
			mIsRunning = true;

			Looper.loop();
		}

		/******************************************
		 * Note the use of MIN_TIME and MIN_DISTANCE which are only "hints" to
		 * the android system
		 ******************************************

		/******************************************
		 * GPS stuff
		 * 
		 * Note the use of MIN_TIME and MIN_DISTANCE which are only "hints" to
		 * the android system
		 *********************************************/
		private void startGPS() {
			Log.i(TAG, "startGPS!!!");

			// create my locationListener
			locationListener = new MyLocationListener();

			// get the LocationManager from the system
			mLocationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);

			// if GPS is not enabled, enable it.
			if (!mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Log.d(TAG, "GPS Provider is not enabled.");
				Log.d(TAG, "Enabling GPS Provider...");
				Intent enableGPS = new Intent(
						"android.location.GPS_ENABLED_CHANGE");
				enableGPS.putExtra("enabled", true);
				mContext.sendBroadcast(enableGPS);
				Log.d(TAG, "GPS Provider has been enabled.");
			}
			// register for location update events to be provided to my
			// locationListener
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE,
					locationListener);

			// also ask for location updates from network location
			// register for location update events to be provided to my
			// locationListener
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE,
					locationListener);

			Location loc = mLocationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			if (loc != null) {
				// Get the SharedPreferences.Editor object we need to modify our
				// String->String preferences map.
				
				mMostrecent_GPS_Time = loc.getTime();
				mMostrecent_System_Time = java.lang.System.currentTimeMillis();
				mMostrecent_GPS_Time = java.lang.System.currentTimeMillis();
				mMostrecent_GPS_Latitude = (float) loc.getLatitude();
				mMostrecent_GPS_Longitude = (float) loc.getLongitude();
				mMostrecent_GPS_Altitude = (float) loc.getAltitude();
				mMostrecent_GPS_Speed = loc.getSpeed();
				mMostrecent_GPS_HasAccuracy = loc.hasAccuracy() ? 1 : 0;
				mMostrecent_GPS_Accuracy = loc.getAccuracy();
			}
		}

		private void stopGPS() {
			Log.i(TAG, "stopGPS!!!");
			// unregister for location update events
			mLocationManager.removeUpdates(locationListener);
		}

		private class MyLocationListener implements LocationListener {
			public void onLocationChanged(Location loc) {
				if (loc != null) {

					mMostrecent_System_Time = java.lang.System
							.currentTimeMillis();

					Log.i(TAG,
							"Location : Time: " + loc.getTime() + " systime: "
									+ mMostrecent_System_Time + " Lat: "
									+ loc.getLatitude() + " Lng: "
									+ loc.getLongitude() + " Alt: "
									+ loc.getAltitude() + " Speed: "
									+ loc.getSpeed() + " Provider: "
									+ loc.getProvider());

					// update the "most recent GPS" parameters in the
					// CalFitdService
					// note the DB stuff occurs in the Kcal computation
					// associated with the
					// onSensorChanged listener
					// mMostrecent_GPS_Time = loc.getTime();
					mMostrecent_GPS_Time = java.lang.System.currentTimeMillis();
					// mMostrecent_System_Time =
					// java.lang.System.currentTimeMillis(); // done above the
					// Log.i()
					
					mMostrecent_GPS_Latitude = (float) loc.getLatitude();
					mMostrecent_GPS_Longitude = (float) loc.getLongitude();
					mMostrecent_GPS_Altitude = (float) loc.getAltitude();
					mMostrecent_GPS_Speed = loc.getSpeed();
					mMostrecent_GPS_HasAccuracy = loc.hasAccuracy() ? 1 : 0;
					mMostrecent_GPS_Accuracy = loc.getAccuracy();
					mMostrecent_Provider = loc.getProvider();

					writeFileGPS();
				}
			}

			public void onProviderDisabled(String provider) {
				Log.i(TAG, "GPS provider disabled");
				// do nothing
			}

			public void onProviderEnabled(String provider) {
				Log.i(TAG, "GPS provider enabled");
				// do nothing
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.i(TAG, "GPS status changed");
				// do nothing
			}
		}

		/*************************************************************
		 * ACCELEROMETER stuff
		 * 
		 * For Sensor.TYPE_ACCELEROMETER: All values are in SI units (m/s^2) and
		 * measure the acceleration applied to the phone minus the force of
		 * gravity. values[0]: Acceleration minus Gx on the x-axis values[1]:
		 * Acceleration minus Gy on the y-axis values[2]: Acceleration minus Gz
		 * on the z-axis
		 * 
		 * Examples:
		 * 
		 * When the device lies flat on a table and is pushed on its left side
		 * toward the right, the x acceleration value is positive. When the
		 * device lies flat on a table, the acceleration value is +9.81, which
		 * correspond to the acceleration of the device (0 m/s^2) minus the
		 * force of gravity (-9.81 m/s^2). When the device lies flat on a table
		 * and is pushed toward the sky with an acceleration of A m/s^2, the
		 * acceleration value is equal to A+9.81 which correspond to the
		 * acceleration of the device (+A m/s^2) minus the force of gravity
		 * (-9.81 m/s^2).
		 * ****************************************************************/
		private void startSensor() {
			Log.i(TAG, "startSensor!!!");

			// create my sensorListener
			mSensorEventListener = new MySensorEventListener();

			mSensorManager = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);

			// todo: in production version consider using
			// SensorManager.SENSOR_DELAY_FASTEST,
			// SENSOR_DELAY_UI (was a good compromise)
			// SENSOR_DELAY_NORMAL (probably best battery life)
			mSensorManager.registerListener(mSensorEventListener,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_UI);
		}

		private void stopSensor() {
			Log.i(TAG, "stopSensor!!!");

			// unregister sensorListener
			mSensorManager.unregisterListener(mSensorEventListener);
		}

		private class MySensorEventListener implements SensorEventListener {

			public void onAccuracyChanged(final Sensor sensor,
					final int accuracy) {
				// do nothing
			}

			public void onSensorChanged(SensorEvent event) {
				if (event != null) {
					/*
					 * Log.i(getClass().getSimpleName(), "Sensor : X: " +
					 * event.values[0] + " Y: " + event.values[1] + " Z: " +
					 * event.values[2]); mMostrecent_Sensor_X = event.values[0];
					 * mMostrecent_Sensor_Y = event.values[1];
					 * mMostrecent_Sensor_Z = event.values[2];
					 * 
					 * // compute Kcal computeKcal();
					 */

					data[0][samplesperwindow] = event.values[0];
					data[1][samplesperwindow] = event.values[1];
					data[2][samplesperwindow] = event.values[2];
					samplesperwindow++;

					// write detail accelerometry to file
					// writeFileDetailNew (System.currentTimeMillis(),
					// event.values[0], event.values[1], event.values[2]);
					accum_acceldetail = accum_acceldetail
							+ System.currentTimeMillis() + ","
							+ event.values[0] + "," + event.values[1] + ","
							+ event.values[2] + "\n";

					long now = SystemClock.elapsedRealtime();
					// check if we have a window of data...
					if (now >= (lasttime + windowtimemillisec)) {
						Log.i(TAG,
								"Sensor : X: " + genfmt.format(event.values[0])
										+ " Y: "
										+ genfmt.format(event.values[1])
										+ " Z: "
										+ genfmt.format(event.values[2]));

						// write detail accelerometry to file
						writeFileDetailNew2();

						// calculate sample window part of kcal
						double[] result = calculateKcal();

						FinalKcalResult(result);

						samplesperwindow = 0;
						lasttime = now;
					}

				}
			}
		}

		// This is called on every sample window
		private double[] calculateKcal() {
			double res[] = new double[2]; // holds V and H result
			double history[] = new double[3];
			double d[] = new double[3];
			double p[] = new double[3];
			// double sdata[][] = new double[3][manysamples];

			// smooth the data first
			// 3-sample moving average
			/*
			 * for (int i=0; i<3; i++) { for (int j=0; j<samplesperwindow; j++)
			 * { if (j==0) { sdata[i][j] = twoprevious[i] + oneprevious[i] +
			 * data[i][j]; } else if (j==1) { sdata[i][j] = oneprevious[i] +
			 * data[i][j-1] + data[i][j]; } else { sdata[i][j] = data[i][j-2] +
			 * data[i][j-1] + data[i][j]; if (j==(samplesperwindow-2))
			 * twoprevious[i]=data[i][j]; else if (j==(samplesperwindow-1))
			 * oneprevious[i]=data[i][j]; } } }
			 */

			// this is historical average of the past samples
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < samplesperwindow; j++) {
					history[i] += data[i][j];
				}
				history[i] /= samplesperwindow;
			}

			for (int j = 0; j < samplesperwindow; j++) {

				for (int i = 0; i < 3; i++) {
					d[i] = history[i] - data[i][j];
				}

				double num = 0;
				double den = 0;
				double value = 0;
				for (int i = 0; i < 3; i++) {
					num = (d[0] * history[0] + d[1] * history[1] + d[2]
							* history[2]);
					den = (history[0] * history[0] + history[1] * history[1] + history[2]
							* history[2]);

					if (den == 0)
						den = 0.01;
					value = (num / den) * history[i];
					p[i] = value;
				}

				double pMagn = p[0] * p[0] + p[1] * p[1] + p[2] * p[2];

				res[0] += Math.sqrt(pMagn);

				res[1] += Math.sqrt((d[0] - p[0]) * (d[0] - p[0])
						+ (d[1] - p[1]) * (d[1] - p[1]) + (d[2] - p[2])
						* (d[2] - p[2]));
			}
			return res;
		}

		// this is called after each sample window is processed; if we have a
		// minute's
		// worth of accumulated V and H results then generate the energy
		// estimate
		private void FinalKcalResult(double[] VH) {
			double EE_minute;

			// Note the (samplesperwindow/(windowtimemillisec/1000)) scales back
			// down to 60 in a min
			if (samplesperwindow > 0) {
				accum_minute_V += (VH[0] / (samplesperwindow / ((float) windowtimemillisec / 1000)))
						/ GRAVITY;
				accum_minute_H += (VH[1] / (samplesperwindow / ((float) windowtimemillisec / 1000)))
						/ GRAVITY;
			}
			counter++;

			if (counter >= (EEinterval / (float) (windowtimemillisec / 1000))) {
				// EEact(k) = a*H^p1 + b*V^p2
				//
				// assume: mass(kg) = 80 kg
				// gender = 1 (male)
				//
				// a = (12.81 * mass(kg) + 843.22) / 1000 = 1.87
				// b = (38.90 * mass(kg) - 682.44 * gender(1=male,2=female) +
				// 692.50)/1000 = 3.12
				// p1 = (2.66 * mass(kg) + 146.72)/1000 = 0.36
				// p2 = (-3.85 * mass(kg) + 968.28)/1000 = 0.66

				// EE in units kcal/minute --> EE_minute
				// the 4.184 is to convert from KJ to kilocalories
				EE_minute = (1.87 * Math.pow(accum_minute_H, 0.36) + 3.12 * Math
						.pow(accum_minute_V, 0.66)) / 4.184;
				mMostrecent_kCal = (float) EE_minute;

				Log.i(TAG, "*** EE result: " + genfmt.format(EE_minute));

				// TODO: Is this where we'll put our "binning" of GPS locations?
				// activity-based thresholding of GPS
				/*
				 * if ((mMostrecent_kCal < lowactivitythresholdforGPS) &&
				 * (GPSstatus==1)) { stopGPS(); } else if ((mMostrecent_kCal >=
				 * lowactivitythresholdforGPS) && (GPSstatus==0)) { startGPS();
				 * }
				 */

				// Returns the current system time in milliseconds since January
				// 1, 1970 00:00:00 UTC.
				// this is the clock on the device as set by user/network.
				long currenttime = System.currentTimeMillis();
				mMostrecent_System_Time = currenttime;

				// write to File
				writeEEDataToFile();

				// retrieve pertinent workout ticks from File
				WorkoutTick[] workoutTicks = FileAccessHelper.getNewWorkoutDataFromFile(mContext);
				
				// attempt to silently send data up to GAE
				new GoogleAppEngineHelper(mContext).checkBatchSizeAndSendDataToGAE(workoutTicks, mContext);
				
				// reset the counters
				accum_minute_V = 0;
				accum_minute_H = 0;
				counter = 0;
			}
		}
	}
	/**
	 * This method writes true kCal data to "/CalfitD/CalFitEE.txt" 
	 */
	private static void writeEEDataToFile() {
		File myfile;
		String state = Environment.getExternalStorageState();

		// JIT: Round the GPS locations based on whatever privacy settings are currently set
		Map<String, Float> coordinates = binGPSLocations();
		float binnedLatitude = coordinates.get(WorkoutTick.KEY_LATITUDE);
		float binnedLongitude = coordinates.get(WorkoutTick.KEY_LONGITUDE);
		
		String str = imei + ","
				+ mMostrecent_GPS_Time + ","
				+ mMostrecent_System_Time + ","
				+ geofmt.format(binnedLatitude) + ","
				+ geofmt.format(binnedLongitude) + ","
				+ genfmt.format(mMostrecent_GPS_Speed) + ","
				+ genfmt.format(mMostrecent_GPS_Altitude) + ","
				+ genfmt.format(mMostrecent_GPS_HasAccuracy) + ","
				+ genfmt.format(mMostrecent_GPS_Accuracy) + ","
				+ genfmt.format(accum_minute_V) + ","
				+ genfmt.format(accum_minute_H) + ","
				+ genfmt.format(mMostrecent_kCal);
		
		// Retrieve current privacy setting
		PrivacyPreferenceEnum privacySetting = new SharedPreferencesHelper(mContext).getCurrentPrivacySetting();
		
		// Append the current privacy setting to the Edmundish string.
		switch (privacySetting) {
		case highPrivacy:
			str += ",h";
			break;
		case mediumPrivacy:
			str += ",m";
			break;
		case lowPrivacy:
			str += ",l";
			break;
		default:
			str += ",unknownPrivacySetting";
			Log.e(TAG, "Unknown privacy setting when attempting to write to file.");
			return;
		}

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media

			// write the data to file on the device's SD card
			try {
				File root = new File(Environment.getExternalStorageDirectory()
						+ "/CalFitD");
				boolean success = false;
				if (!root.exists()) {
					success = root.mkdir();
				}
				if (root.canWrite()) {
					// TODO: String literal
					myfile = new File(root, "CalFitEE.txt");
					myfile.createNewFile();
					FileWriter mywriter = new FileWriter(myfile, true);
					BufferedWriter out = new BufferedWriter(mywriter);

					// use out.write statements to write data...
					out.write(str + "\n");
					out.close();
					out = null;
					mywriter = null;
					myfile = null;
				}
			} catch (IOException e) {
				Log.e(TAG, "Could not write file " + e.getMessage());
			}
		}
	}

	/**
	 * This method writes strictly GPS data to "/CalfitD/CFgps<n>.txt"
	 */
	public static void writeFileGPS() {
		File myfile;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			// figure out which file we're up to on the SD card
			try {
				File root = new File(Environment.getExternalStorageDirectory()
						+ "/CalFitD");
				boolean success = false;
				if (!root.exists()) {
					success = root.mkdir();
				}
				if (root.canWrite()) {
					writtenGPS++;
					if (writtenGPS >= MAX_WRITTEN) { // start a new file
						writtenGPS = 0;
						fileNumGPS++;
					}
					// write the data to file on the device's SD card
					myfile = new File(root, "CFgps" + fileNumGPS + ".txt");
					myfile.createNewFile();
					FileWriter mywriter = new FileWriter(myfile, true);
					BufferedWriter out = new BufferedWriter(mywriter);

					String str = mMostrecent_GPS_Time + ","
							+ mMostrecent_System_Time + ","
							+ geofmt.format(mMostrecent_GPS_Latitude) + ","
							+ geofmt.format(mMostrecent_GPS_Longitude) + ","
							+ genfmt.format(mMostrecent_GPS_Speed) + ","
							+ genfmt.format(mMostrecent_GPS_Altitude) + ","
							+ genfmt.format(mMostrecent_GPS_HasAccuracy) + ","
							+ genfmt.format(mMostrecent_GPS_Accuracy) + ","
							+ mMostrecent_Provider + ","
							+ genfmt.format(accum_minute_V) + ","
							+ genfmt.format(accum_minute_H);

					// use out.write statements to write data...
					out.write(str + "\n");
					out.close();
					out = null;
					mywriter = null;
					myfile = null;
				}
			} catch (IOException e) {
				Log.e(TAG, "Could not write file " + e.getMessage());
			}
		}
	}

	/**
	 * This is a helper method that simply sets the variables "fileNum" and
	 * "fileNumGPS" to their correct values.
	 * 
	 * It walks through the contents of what's currently on storage to figure
	 * this out.
	 */
	public static void findFileNums() {
		File myfile, mygpsfile;

		// figure out which file we're up to on the SD card
		try {
			File root = new File(Environment.getExternalStorageDirectory()
					+ "/CalFitD");
			boolean success = false;
			if (!root.exists()) {
				success = root.mkdir();
			}
			if (root.canWrite()) {
				myfile = new File(root, "CFdet" + fileNum + ".txt");
				while (myfile.exists()) {
					fileNum++;
					myfile = new File(root, "CFdet" + fileNum + ".txt");
				}
				mygpsfile = new File(root, "CFgps" + fileNumGPS + ".txt");
				while (mygpsfile.exists()) {
					fileNumGPS++;
					mygpsfile = new File(root, "CFgps" + fileNumGPS + ".txt");
				}

			}
		} catch (Exception e) {
			Log.e(TAG, "Could not write file " + e.getMessage());
		}
	}

	/**
	 * This method writes detailed accelerometer data to "/CalFitD/CFdet.txt".
	 */
	public static void writeFileDetailNew2() {
		File myfile;

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media

			// figure out which file we're up to on the SD card
			try {
				File root = new File(Environment.getExternalStorageDirectory()
						+ "/CalFitD");
				boolean success = false;
				if (!root.exists()) {
					success = root.mkdir();
				}
				if (root.canWrite()) {
					written++;
					if (written >= MAX_WRITTEN_2) { // start a new file
						written = 0;
						fileNum++;
					}
					// write the data to file on the device's SD card
					myfile = new File(root, "CFdet" + fileNum + ".txt");
					myfile.createNewFile();
					FileWriter mywriter = new FileWriter(myfile, true);
					BufferedWriter out = new BufferedWriter(mywriter);

					// use out.write statements to write data...
					out.write(accum_acceldetail);
					out.close();
					out = null;
					mywriter = null;
					myfile = null;
					accum_acceldetail = "";
				}
			} catch (IOException e) {
				Log.e(TAG, "Could not write file " + e.getMessage());
			}
		}
	}
	

	/**
	 * Helper method to simply round the current locations that this object
	 * has on hand.
	 */
	private static Map<String, Float> binGPSLocations() {

		/** Maps from keys for the values to the values */
		Map<String, Float> coordinatesMap = new HashMap<String, Float>();
		
		// Grab the currently set privacy setting (but grab the *correct* setting).
		PrivacyPreferenceEnum privacySetting = new SharedPreferencesHelper(mContext).getCurrentPrivacySetting();
		
		// "Bin" (or "blur") the coordinate locations based on this setting.
		switch (privacySetting) {
		case highPrivacy:
			//IF Privacy is set to High: BINNS GPS Latitutde & Longitude by Multiple of 5
			coordinatesMap.put(WorkoutTick.KEY_LATITUDE, 
					(float) KCalUtils.roundDownToNearestMultipleOfFive(mMostrecent_GPS_Latitude));
			coordinatesMap.put(WorkoutTick.KEY_LONGITUDE,
					(float) KCalUtils.roundDownToNearestMultipleOfFive(mMostrecent_GPS_Longitude));
			break;
		case mediumPrivacy:
			//IF Privacy is set to Medium: BINNS GPS Latitutde & Longitude by Multiple of 3
			coordinatesMap.put(WorkoutTick.KEY_LATITUDE, 
					(float) KCalUtils.roundDownToNearestMultipleOfThree(mMostrecent_GPS_Latitude));
			coordinatesMap.put(WorkoutTick.KEY_LONGITUDE,
					(float) KCalUtils.roundDownToNearestMultipleOfThree(mMostrecent_GPS_Longitude));
			break;
		case lowPrivacy:
			// If we have low privacy, then don't bin the locations at all
			coordinatesMap.put(WorkoutTick.KEY_LATITUDE, 	mMostrecent_GPS_Latitude);
			coordinatesMap.put(WorkoutTick.KEY_LONGITUDE, 	mMostrecent_GPS_Longitude);

			break;
		default:
			Log.e(TAG, "Unknown privacy setting set");
			break;
		}
		
		return coordinatesMap;
	}
	
}
