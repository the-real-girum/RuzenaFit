package edu.berkeley.eecs.ruzenafit.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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
import edu.berkeley.eecs.ruzenafit.activity.WorkoutTrackerActivity;
import edu.berkeley.eecs.ruzenafit.util.Constants;

public class WorkoutTrackerService extends Service {
	private static Context mContext;
	protected static LocationManager mLocationManager = null;
	protected Location mLocation = null;

	// GPS updating...
	protected final static float MIN_DISTANCE = 0; // in Meters
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
	private static int samplesPerWindow;
	private static int windowtimemillisec = 2000; // 2 seconds
	private static int manysamples = 128; // should be plenty for 2 seconds
											// (DELAY_UI on G1 was ~10
											// samples/sec)
	private static double[][] data = new double[3][manysamples];
	private static PowerManager.WakeLock wl;
	private static Kcal myKcal;
	private static String imei = "0";

	private static int filenum = 0;
	private static int writen = 0;
	private static int MAXWRITEN = 10000; // start a new file for every 10,000
											// entries writen to file
	private static int MAXWRITEN2 = 1000; // start a new file for every 1,000
											// entries writen to file (for the
											// new accel detail writer that only
											// writes chunks depending on
											// windowtimemillisec)
	private static int filenumgps = 0;
	private static int writengps = 0;

	public static NotificationManager notificationManager;
	public static final int NOTIFICATION_ID = 1;

	public static long ntpdiff = 0;

	public static String accum_acceldetail = "";

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(getClass().getSimpleName(), "Service onCreate!!!");
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
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		boolean isrunningstored = settings.getBoolean("isrunning", false);
		if (isrunningstored)
			startlog();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(getClass().getSimpleName(), "Service onStartCommand!!!");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(getClass().getSimpleName(), "Service onDestroy!!!");

		// in case the system is stopping us rather that the CalFit App, make
		// sure to stop logging gracefully
		if (mIsRunning)
			stoplog();
	}

	public static int getStatus() {
		if (mIsRunning)
			return 1;
		else
			return 0;
	}

	public static void startlog() {
		Log.i("CalFitService", "startlog!!!");

		// Each time we start logging we start a new file regardless of
		// numwriten
		writen = 0;
		writengps = 0;

		filenum = 0;
		filenumgps = 0;
		FindNewFiles();

		// this is needed to keep the service alive on Android 2.0+ systems
		// setForeground(true); // this is ignored on the new systems!
		int icon = R.drawable.star_on;
		CharSequence tickerText = "CalFit BCN running";
		long when = java.lang.System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		CharSequence contentTitle = "CalFit BCN";
		CharSequence contentText = "Currently running";
		Intent notificationIntent = new Intent(mContext,
				WorkoutTrackerActivity.class); // must use CalFit.class because
												// this notification loads a new
												// activity onto the stack, and
												// if you call CalFit.class, it
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

		// make sure to save our state as a pref in case we get killed by
		// system.
		SharedPreferences settings = mContext.getSharedPreferences(
				Constants.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("isrunning", true);
		editor.commit();

		myKcal = new Kcal();
		myKcal.start();
	}

	public static void stoplog() {
		Log.i("CalFitService", "stoplog!!!");
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
				Constants.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
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
	private static class Kcal extends Thread {
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
			samplesPerWindow = 0;
			accum_minute_V = 0;
			accum_minute_H = 0;
			counter = 0;
			startSensor();
			startGPS();
			mIsRunning = true;

			Looper.loop();
		}

		/******************************************
		 * GPS stuff
		 * 
		 * Note the use of MIN_TIME and MIN_DISTANCE which are only "hints" to
		 * the android system
		 *********************************************/
		private void startGPS() {
			Log.i("CalFitService", "startGPS!!!");

			// create my locationListener
			locationListener = new MyLocationListener();

			// get the LocationManager from the system
			mLocationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);

			// if GPS is not enabled, enable it.
			if (!mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Log.d(getClass().getSimpleName(),
						"GPS Provider is not enabled.");
				Log.d(getClass().getSimpleName(), "Enabling GPS Provider...");
				Intent enableGPS = new Intent(
						"android.location.GPS_ENABLED_CHANGE");
				enableGPS.putExtra("enabled", true);
				mContext.sendBroadcast(enableGPS);
				Log.d(getClass().getSimpleName(),
						"GPS Provider has been enabled.");
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
				mMostrecent_GPS_Time = loc.getTime();
				mMostrecent_System_Time = java.lang.System.currentTimeMillis();
				mMostrecent_GPS_Time = java.lang.System.currentTimeMillis();
				mMostrecent_GPS_Latitude = (float) loc.getLatitude();
				mMostrecent_GPS_Longitude = (float) loc.getLongitude();
				mMostrecent_GPS_Altitude = (float) loc.getAltitude();
				mMostrecent_GPS_Speed = loc.getSpeed();
				if (loc.hasAccuracy())
					mMostrecent_GPS_HasAccuracy = 1;
				else
					mMostrecent_GPS_HasAccuracy = 0;
				mMostrecent_GPS_Accuracy = loc.getAccuracy();
			}
		}

		private void stopGPS() {
			Log.i("CalFitService", "stopGPS!!!");
			// unregister for location update events
			mLocationManager.removeUpdates(locationListener);
		}

		private class MyLocationListener implements LocationListener {
			@Override
			public void onLocationChanged(Location loc) {
				if (loc != null) {

					mMostrecent_System_Time = java.lang.System
							.currentTimeMillis();

					Log.i(getClass().getSimpleName(),
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
					if (loc.hasAccuracy())
						mMostrecent_GPS_HasAccuracy = 1;
					else
						mMostrecent_GPS_HasAccuracy = 0;
					mMostrecent_GPS_Accuracy = loc.getAccuracy();
					mMostrecent_Provider = loc.getProvider();

					writeFileGPS();
				}
			}

			@Override
			public void onProviderDisabled(String provider) {
				Log.i(getClass().getSimpleName(), "GPS provider disabled");
				// do nothing
			}

			@Override
			public void onProviderEnabled(String provider) {
				Log.i(getClass().getSimpleName(), "GPS provider enabled");
				// do nothing
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.i(getClass().getSimpleName(), "GPS status changed");
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
			Log.i("CalFitService", "startSensor!!!");

			// create my sensorListener
			mSensorEventListener = new MySensorEventListener();

			mSensorManager = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);

			// TODO: in production version consider using
			// SensorManager.SENSOR_DELAY_FASTEST,
			// SENSOR_DELAY_UI (was a good compromise)
			// SENSOR_DELAY_NORMAL (probably best battery life)
			mSensorManager.registerListener(mSensorEventListener,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_UI);
		}

		private void stopSensor() {
			Log.i("CalFitService", "stopSensor!!!");

			// unregister sensorListener
			mSensorManager.unregisterListener(mSensorEventListener);
		}

		private class MySensorEventListener implements SensorEventListener {

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// do nothing
			}

			@Override
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

					data[0][samplesPerWindow] = event.values[0];
					data[1][samplesPerWindow] = event.values[1];
					data[2][samplesPerWindow] = event.values[2];
					samplesPerWindow++;

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
						Log.i(getClass().getSimpleName(), "Sensor : X: "
								+ genfmt.format(event.values[0]) + " Y: "
								+ genfmt.format(event.values[1]) + " Z: "
								+ genfmt.format(event.values[2]));

						// write detail accelerometry to file
						writeFileDetailNew2();

						// calculate sample window part of kcal
						double[] result = calculateKcal();

						FinalKcalResult(result);

						samplesPerWindow = 0;
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
				for (int j = 0; j < samplesPerWindow; j++) {
					history[i] += data[i][j];
				}
				history[i] /= samplesPerWindow;
			}

			for (int j = 0; j < samplesPerWindow; j++) {

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
			if (samplesPerWindow > 0) {
				accum_minute_V += (VH[0] / (samplesPerWindow / ((float) windowtimemillisec / 1000)))
						/ GRAVITY;
				accum_minute_H += (VH[1] / (samplesPerWindow / ((float) windowtimemillisec / 1000)))
						/ GRAVITY;
			}
			counter++;

			if (counter >= (EEinterval / (windowtimemillisec / 1000))) {
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

				Log.i(getClass().getSimpleName(),
						"*** EE result: " + genfmt.format(EE_minute));

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

				// write to file
				writeFile();

				/*
				 * // post to server only on certain intervals if (postcounter >
				 * POSTINTERVAL) { // write to website server with POST
				 * request... // Create a new HttpClient and Post Header Thread
				 * httpthread = new Thread() { public void run() {
				 * 
				 * HttpClient httpclient = new DefaultHttpClient(); HttpPost
				 * httppost = new
				 * HttpPost("http://calfitd.dyndns.org/PHAST/CalFitd/index.php"
				 * );
				 * 
				 * try { // Add your data List<NameValuePair> nameValuePairs =
				 * new ArrayList<NameValuePair>(2); nameValuePairs.add(new
				 * BasicNameValuePair("imei", imei)); nameValuePairs.add(new
				 * BasicNameValuePair("time",
				 * ((Integer)(mMostrecent_Time)).toString()));
				 * nameValuePairs.add(new BasicNameValuePair("lat",
				 * geofmt.format(mMostrecent_GPS_Latitude)));
				 * nameValuePairs.add(new BasicNameValuePair("lon",
				 * geofmt.format(mMostrecent_GPS_Longitude)));
				 * nameValuePairs.add(new BasicNameValuePair("speed",
				 * genfmt.format(mMostrecent_GPS_Speed)));
				 * nameValuePairs.add(new BasicNameValuePair("alt",
				 * genfmt.format(mMostrecent_GPS_Altitude)));
				 * nameValuePairs.add(new BasicNameValuePair("v",
				 * genfmt.format(localV))); nameValuePairs.add(new
				 * BasicNameValuePair("h", genfmt.format(localH)));
				 * nameValuePairs.add(new BasicNameValuePair("kcal",
				 * genfmt.format(mMostrecent_kCal))); httppost.setEntity(new
				 * UrlEncodedFormEntity(nameValuePairs));
				 * 
				 * // Execute HTTP Post Request ResponseHandler<String>
				 * responseHandler = new BasicResponseHandler(); String
				 * responseBody = httpclient.execute(httppost, responseHandler);
				 * // HttpResponse response = httpclient.execute(httppost);
				 * 
				 * } catch (Exception e) { Log.e(getClass().getSimpleName(),
				 * "HTTP error: " + e.getMessage()); } } }; httpthread.start();
				 * postcounter = 0; } else { postcounter++; }
				 */

				// reset the counters
				accum_minute_V = 0;
				accum_minute_H = 0;
				counter = 0;
			}
		}
	}

	private static void writeFile() {

		String state = Environment.getExternalStorageState();

		String str = imei + "," + mMostrecent_GPS_Time + ","
				+ mMostrecent_System_Time + ","
				+ geofmt.format(mMostrecent_GPS_Latitude) + ","
				+ geofmt.format(mMostrecent_GPS_Longitude) + ","
				+ genfmt.format(mMostrecent_GPS_Speed) + ","
				+ genfmt.format(mMostrecent_GPS_Altitude) + ","
				+ genfmt.format(mMostrecent_GPS_HasAccuracy) + ","
				+ genfmt.format(mMostrecent_GPS_Accuracy) + ","
				+ genfmt.format(accum_minute_V) + ","
				+ genfmt.format(accum_minute_H) + ","
				+ genfmt.format(mMostrecent_kCal);

		/*
		 * // encrypt line String encstr = null; try { encstr =
		 * SimpleCrypto.toHex( SimpleCrypto.encrypt(SimpleCrypto.toByte(ENCKEY),
		 * str.getBytes())); } catch (Exception e) { encstr = ""; }
		 */

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
					File myfile = new File(root, "CalFitEE.txt");
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
				Log.e("CalFitService", "Could not write file " + e.getMessage());
			}
		}
	}

	public static void FindNewFiles() {
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
				myfile = new File(root, "CFdet" + filenum + ".txt");
				while (myfile.exists()) {
					filenum++;
					myfile = new File(root, "CFdet" + filenum + ".txt");
				}
				mygpsfile = new File(root, "CFgps" + filenumgps + ".txt");
				while (mygpsfile.exists()) {
					filenumgps++;
					mygpsfile = new File(root, "CFgps" + filenumgps + ".txt");
				}

			}
		} catch (Exception e) {
			Log.e("CalFitService", "Could not write file " + e.getMessage());
		}
	}

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
					writen++;
					if (writen >= MAXWRITEN2) { // start a new file
						writen = 0;
						filenum++;
					}
					// write the data to file on the device's SD card
					myfile = new File(root, "CFdet" + filenum + ".txt");
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
				Log.e("CalFitService", "Could not write file " + e.getMessage());
			}
		}
	}

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
					writengps++;
					if (writengps >= MAXWRITEN) { // start a new file
						writengps = 0;
						filenumgps++;
					}
					// write the data to file on the device's SD card
					myfile = new File(root, "CFgps" + filenumgps + ".txt");
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

					/*
					 * // encrypt line String encstr = null; try { encstr =
					 * SimpleCrypto.toHex(
					 * SimpleCrypto.encrypt(SimpleCrypto.toByte(ENCKEY),
					 * str.getBytes())); } catch (Exception e) { encstr = ""; }
					 */
					// use out.write statements to write data...
					out.write(str + "\n");
					out.close();
					out = null;
					mywriter = null;
					myfile = null;
				}
			} catch (IOException e) {
				Log.e("CalFitService", "Could not write file " + e.getMessage());
			}

		}
	}

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

}