package edu.berkeley.eecs.ruzenafit.service;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.maps.GeoPoint;


public class WAVE extends Service {
	//*********************************************
	//* PUBLICLY ACCESSIBLE VARIABLES AND METHODS *
	//*********************************************
	public static float curSpeed, avgSpeed, maxSpeed, prevSpeed, prev2Speed, prev3Speed, prev4Speed, prev5Speed; // current, average, maximum, and previous speed in meters/second
	public static float distance; // total distance in km
	public static double curAltitude, avgAltitude, maxAltitude, minAltitude, prevAltitude, prev2Altitude;
	public static float curDist; // current incremental distance in km
	public static Location curLocation, prevLocation;
	public static GeoPoint curGeoPoint, prevGeoPoint;
	public static Context appContext;
	
	/**
	 * Converts milliseconds into a String form of "h:mm:ss"
	 * @param millis
	 * @return
	 */
	public static String millisToTime(long millis) {
		return null;
	}
	
	/**
	 * Set the minimum time that must pass before a location update can be requested.
	 * @param millis
	 */
	public static void setLocationUpdateMinTime(long millis) {
		minUpdateTime = millis;
	}
	
	/**
	 * Set the minimum distance that must pass before a location update can be requested.
	 * @param meters
	 */
	public static void setLocationUpdateMinDist(long meters) {
		minUpdateDist = meters;
	}
	
	/*
	public static double getKCal() {
		return Kcal.getKCal();
	}
	
	public static void startKCal() {
		Kcal.startKCal();
	}
	
	public static void stopKCal() {
		Kcal.stopKCal();
	}
	
	public static void resetKCal() {
		Kcal.setKCal(0);
	}
	
	public static void setKCal(double val) {
		Kcal.setKCal(val);
	}
	*/
	
	public static double getKCal() {
		return KCal2.getTotalKcal();
	}
	
	public static void startKCal(Context context) {
		appContext = context;
		KCal2.setContext(appContext);
		KCal2.startSensorReadings();
	}
	
	public static void pauseKCal() {
		KCal2.pauseSensorReadings();
	}
	
	public static void resumeKCal() {
		KCal2.resumeSensorReadings();
	}
	
	public static void stopKCal() {
		KCal2.stopSensorReadings();
	}
	
	//********************************************
	//* INTERNAL VARIABLES, METHODS, AND CLASSES *
	//********************************************
	private final static String TAG = "WAVE Service";
	private MyLocationListener myLocationListener;
	private static long minUpdateTime, minUpdateDist;
	private LocationManager locationManager;
	private Context myContext;
	
	/**
	 * initializes variables
	 */
	private void createWAVEService() {
		curLocation = null; prevLocation = null; curGeoPoint = null; prevGeoPoint = null;
		curSpeed = 0; avgSpeed = 0; maxSpeed = 0;
		distance = 0;
		curDist = 0;
		prevSpeed = 0; prev2Speed = 0; prev3Speed = 0; prev4Speed = 0; prev5Speed = 0;
		prevAltitude = 0; prev2Altitude = 0;
		curAltitude = 0; avgAltitude = 0; maxAltitude = 0; minAltitude = 0;
		myContext = this;
		
		// initializing default minUpdateTime and minUpdateDist
		// user can override with setLocationUpdateMinTime() or setLocationUpdateMinDist()
		minUpdateTime = 5000; minUpdateDist = 3;
		
		// intialize
		myLocationListener = new MyLocationListener();
		
		// start counting kCal
//		startService(new Intent(this, Kcal.class));
		startService(new Intent(this, KCal2.class));
	}
	
	/**
	 * starts location manager and requests updates
	 */
	private void startWAVEService() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// if GPS is not enabled, enable it.
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.d(TAG, "GPS Provider is not enabled.");
			Log.d(TAG, "Enabling GPS Provider...");
			Intent enableGPS = new Intent("android.location.GPS_ENABLED_CHANGE");
			enableGPS.putExtra("enabled", true);
			myContext.sendBroadcast(enableGPS);
			Log.d(TAG, "GPS Provider has been enabled.");
		}
//		curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//		if (curLocation != null) {
//			curGeoPoint = new GeoPoint((int) (curLocation.getLatitude() * 1E6), (int) (curLocation.getLongitude() * 1E6));
//		}
//		LocationProvider locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateTime, minUpdateDist, myLocationListener);
	}
	
	/**
	 * MyLocationListener overrides onLocationchanges to update current and previous locations and geopoints.
	 * @author Irving
	 */
	private class MyLocationListener implements LocationListener {
		public void onLocationChanged(Location loc) {
			Log.d(TAG, "Location Changed...");
			if (loc != null) { // sanity check, shouldn't be the case though.
				if (curLocation != null) {
					// store previous location
					prevLocation = curLocation;
					prevGeoPoint = curGeoPoint;
					Log.d(TAG, "Setting prev geopoint to: " + prevGeoPoint.getLatitudeE6() + "," + prevGeoPoint.getLongitudeE6());
				}
				
				// set new location and geopoint to be the current one
				curLocation = loc;
				curGeoPoint = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
				Log.d(TAG, "Setting cur geopoint to: " + curGeoPoint.getLatitudeE6() + "," + curGeoPoint.getLongitudeE6());

				// update/calculate/get relevant information such as distance, speed, and altitude
				if (prevGeoPoint != null) {
					//distance += getDisplacement(prevGeoPoint, curGeoPoint);
					curDist = curLocation.distanceTo (prevLocation) / 1000;
					distance += curDist;

					// need to compute my own speed, because speed from GPS is unstable
					// to convert from km/millisec to m/s multiply by 1000000.
					// use a little moving average for smoothing.
					curSpeed = (float) ((((curDist / (curLocation.getTime() - prevLocation.getTime())) * 1000000) + prevSpeed + prev2Speed) / 3.0);
					prev2Speed = prevSpeed;
					prevSpeed = curSpeed;
				}
				Log.d(TAG, "Setting new distance to: " + distance);

/*				if (loc.hasSpeed()) {
					// applies a little moving average to remove noise
					curSpeed = (float)((0.1*loc.getSpeed() + 0.1*prevSpeed + 0.1*prev2Speed +
							0.2*prev3Speed + 0.2*prev4Speed + 0.3*prev5Speed));
					prev5Speed = prev4Speed;
					prev4Speed = prev3Speed;
					prev3Speed = prev2Speed;
					prev2Speed = prevSpeed;
					prevSpeed = curSpeed;
				}
				*/
				Log.d(TAG, "Setting new curSpeed to: " + curSpeed);
				
				if (loc.hasAltitude()) {
					// applies a little moving average to remove noise
					curAltitude = (float) ((loc.getAltitude() + prevAltitude + prev2Altitude) / 3.0);
					prev2Altitude = prevAltitude;
					prevAltitude = curAltitude;
					Log.d(TAG, "Setting new altitude to: " + curAltitude);
				}
			}
		}
		
		public void onProviderDisabled(String provider) {
		}
		
		public void onProviderEnabled(String provider) {
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	/**
	 * This method takes in an array of GPS positions and finds the Google Map Distance from one to the next, summing
	 * them all up into one final distance.
	 * 
	 * @param initGPSPos
	 *            GPS Coordinates in "xxx.xxxxx,yyy.yyyyy, zzz.zzzz"
	 * @return float
	 */
	public static double getDistance(String[] GPSPos) {
		double distance = 0;
		String currPos = GPSPos[0];
		for (int i = 1; i < GPSPos.length; i++) {
			if (GPSPos[i] != null) {
				distance += getDisplacement(currPos, GPSPos[i]);
				currPos = GPSPos[i];
			}
		}
		return distance;
	}
	
	public static double getDistance(ArrayList<GeoPoint> GPSPos) {
		double distance = 0;
		GeoPoint curPos = GPSPos.get(0);
		for (int i = 1; i < GPSPos.size(); i++) {
			GeoPoint nextPos = GPSPos.get(i);
			if (nextPos != null) {
				distance += getDisplacement(curPos, nextPos);
				curPos = nextPos;
			}
		}
		return distance;
	}
	
	/**
	 * Parses a GPS position into its constituent components and puts the results into a String[]
	 * @param String pos "xxx.xxxxx, yyy.yyyyy, zzz.zzzzz"
	 * @return String[] of latitude, longitude, and altitude of the input position, respectively
	 */
	private static String[] parseGPS(String pos) {
		int count = 0; //Keep track of index in parsedPos
		char[] charArray = pos.toCharArray();
		String[] parsedPos = new String[3];
		for (int i = 0; i < parsedPos.length; i++)
			parsedPos[i] = "";
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i] == ',')
				count++;
			else if (charArray[i] != ' ' && charArray[i] != ',')
				parsedPos[count] += Character.toString(charArray[i]);
		}
		return parsedPos;
	}

	public static double getDisplacement(GeoPoint start, GeoPoint stop) {
		double million = 1000000.0;
		
		String startString = start.getLatitudeE6()/million + "," + start.getLongitudeE6()/million + "," + "000.00000";
		String stopString = stop.getLatitudeE6()/million + "," + stop.getLongitudeE6()/million + "," + "000.00000";
		return getDisplacement(startString, stopString);
	}
	
	/**
	 * This method returns the displacement between the initial supplied GPS
	 * position and secondary supplied GPS position.
	 * 
	 * @param initGPSPos
	 * @param finalGPSPos
	 * @return float
	 */
	public static double getDisplacement(String initGPSPos, String finalGPSPos) {
		int radius = 6370000; //Value is given in meters
		String[] initGPSArray = parseGPS(initGPSPos);
		String[] finalGPSArray = parseGPS(finalGPSPos);
		
		//---These are in polar coordinates for initGPSPos---
		//AddAccuracy must be added to the altitude readings to take into account that
		//the distance formula have a reference point as the center of the earth,
		//whereas the GPS reports values above sea level. To fix this, simply add the distance
		//between sea level and the center of the earth, which is about 6370km
		double latitude0 = Double.parseDouble(initGPSArray[0]);
		double longitude0 = Double.parseDouble(initGPSArray[1]);
		//double altitude0 = Double.parseDouble(initGPSArray[2]) + AddAccuracy;
		
		//---These are in polar coordinates for final GPSPos---
		double latitude1 = Double.parseDouble(finalGPSArray[0]);
		double longitude1 = Double.parseDouble(finalGPSArray[1]);
		//double altitude1 = Double.parseDouble(finalGPSArray[2]) + AddAccuracy;
		
	    double A = latitude0/57.29577951;
//	    double B = longitude0/57.29577951;
	    double C = latitude1/57.29577951;
//	    double D = longitude1/57.29577951; //(all converted to radians: degree/57.29577951)
	    double delta = (longitude1 - longitude0)/57.29577951;
	    double displacement = 0;

	    displacement = Math.acos(Math.sin(A) * Math.sin(C) + Math.cos(A) * Math.cos(C) * Math.cos(delta)) * radius / 1000.0;

		Log.d(TAG, "computing displacement..." + displacement);

	    return displacement;
	}
	
	//******************************
	//* SERVICE LOGISTICAL METHODS *
	//******************************
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "WAVE Service is being created.");
		createWAVEService();
	}
	
//	@Override
//	public int onStartCommand (Intent intent, int flags, int startId) {
//		super.onStartCommand(intent, flags, startId);
//		
//		Log.d(TAG, "WAVE Service is being started.");
//		startWAVEService();
//		return Service.START_STICKY;
//	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		startWAVEService();
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "WAVE Service is being destroyed.");
		super.onDestroy();
		
//		stopService(new Intent(this, Kcal.class));
		stopService(new Intent(this, KCal2.class));
		
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.d(TAG, "GPS Provider is enabled. Disabling GPS Provider...");
			Intent enableGPS = new Intent("android.location.GPS_DISABLED_CHANGE");
			enableGPS.putExtra("disabled", true);
			myContext.sendBroadcast(enableGPS);
			Log.d(TAG, "GPS Provider has been disabled.");
		}
		
		locationManager.removeUpdates(myLocationListener);
		appContext = null;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}