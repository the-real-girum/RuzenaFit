/** KCal2.java
 * 
 * Created on April 1, 2010
 * 
 * @author: Irving Lin
 */

package edu.berkeley.eecs.ruzenafit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class KCal2 extends Service {
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *  ACCELEROMETER stuff 
	 *  
	 *  For Sensor.TYPE_ACCELEROMETER:
	 *  All values are in SI units (m/s^2) and measure the acceleration applied to the phone minus the force of gravity.
	 *  values[0]: Acceleration minus Gx on the x-axis 
	 *  values[1]: Acceleration minus Gy on the y-axis
	 *  values[2]: Acceleration minus Gz on the z-axis
	 *  
	 *  Examples:
	 *  
	 *  When the device lies flat on a table and is pushed on its left side toward the right, the x acceleration value is positive.
	 *  When the device lies flat on a table, the acceleration value is +9.81, which correspond to the acceleration of the device (0 m/s^2) minus the force of gravity (-9.81 m/s^2).
	 *  When the device lies flat on a table and is pushed toward the sky with an acceleration of A m/s^2, the acceleration value is equal to A+9.81 which correspond to the acceleration of the device (+A m/s^2) minus the force of gravity (-9.81 m/s^2).
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	public static final String TAG = "KCal2 Service";
	private static Context mContext;
	protected static SensorManager mSensorManager = null;
    private static SensorEventListener mSensorEventListener;
    private static long lasttime = 0; static long counter = 0;
	private static double accum_minute_V = 0, accum_minute_H = 0;
	private static double GRAVITY = 9.81;
	private static int samplesperwindow, secondsprocessed;
	private static final int WINDOWTIMEMILLISEC = 1000;	// 1 seconds
	private static int manysamples = 128;  // should be plenty for 2 seconds (DELAY_UI on G1 was ~10 samples/sec)
	private static double[][] data = new double[3][manysamples];
	private static double[] twoprevious = new double[3];
	private static double[] oneprevious = new double[3];
	private static double interim_V = 0;
	private static float mTotal_kCal = 0;
	private static double localV;
	private static double localH;
    private static float mMostrecent_Sensor_X = 0;
    private static float mMostrecent_Sensor_Y = 0;
    private static float mMostrecent_Sensor_Z = 0;
    private static float mMostrecent_kCal = 0;
    private static int mMostrecent_Time = 0;
    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI; 	// TODO: in production version consider using SensorManager.SENSOR_DELAY_FASTEST
	private static String genformat = "####0.00";
	private static DecimalFormat genfmt = new DecimalFormat( genformat, new DecimalFormatSymbols(Locale.US));
	private static boolean contextSet = false;
	public static PowerManager.WakeLock wl;
	public static final short RUN_STATE = 0, PAUSE_STATE = 1, END_STATE = 2;
	public static short state = END_STATE;
	
	public static void startSensorReadings() {
		if (state == END_STATE) {
			Log.d(TAG, "STARTING SENSOR READINGS...");
			state = RUN_STATE;
		
			// create my sensorListener
			mSensorEventListener = new MySensorEventListener();
			resetData();
			
			PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			
			// get sensor manager and register sensorListener
			registerListener(true);
			
			if (wl != null && !wl.isHeld()) {
				wl.acquire();
			}
		} else {
			Log.d(TAG, "STARTING SENSOR READINGS FROM RUN OR PAUSE STATE (NOT SUPPOSED TO HAPPEN)...");
		}
	}
	
	public static void pauseSensorReadings() {
		if (state == RUN_STATE) {
			Log.d(TAG, "PAUSING SENSOR READINGS...");
			state = PAUSE_STATE;
			// unregister sensorListener
			unregisterListener();
	
			if (wl != null && wl.isHeld()) {
				wl.release();
			}
		} else {
			Log.d(TAG, "PAUSING SENSOR READINGS FROM PAUSE OR END STATE (NOT SUPPOSED TO HAPPEN)...");
		}
	}
	
	public static void resumeSensorReadings() {
		if (state == PAUSE_STATE) {
			Log.d(TAG, "RESUMING SENSOR READINGS...");
			state = RUN_STATE;
			// re-register sensorListener
			registerListener(false);
			if (wl != null && !wl.isHeld()) {
				wl.acquire();
			}	
		} else {
			Log.d(TAG, "RESUMING SENSOR READINGS FROM RUN OR END STATE (NOT SUPPOSED TO HAPPEN)...");
		}
	}
	
	public static void stopSensorReadings() {
		// unregister sensorListener
		unregisterListener();
		state = END_STATE;
		
		resetData();
		
		if (wl != null && wl.isHeld()) {
			wl.release();
		}
	}
	
	private static void registerListener(boolean start) {
		try {
			if (start) {
				mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
			}
			mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
			Log.d(TAG, "REGISTERED LISTENER FOR SENSOR MANAGER.");
			contextSet = true;
		} catch (Exception e) {
			Log.d(TAG, "context not set... please set context in application through WAVE service by calling setContext(Context context)");
			contextSet = false;
		}
	}
	
	private static void unregisterListener() {
		if (contextSet) {
			mSensorManager.unregisterListener(mSensorEventListener);
		}
	}
	
	public static void setContext(Context context) {
		  mContext = context;
	}
	
	public static double getTotalKcal() {
		return mTotal_kCal;
	}
	
	private static void resetData() {
		lasttime = SystemClock.elapsedRealtime();
		contextSet = false;
		samplesperwindow = 0;
		secondsprocessed = 0;
		accum_minute_V = 0;
		accum_minute_H = 0;
		mTotal_kCal = 0;
		counter = 0;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private static class MySensorEventListener implements SensorEventListener {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// do nothing
		}

		public void onSensorChanged(SensorEvent event) {
			if (event != null) {
//	 			Log.i(TAG, "Sensor values: " + event.values);

/*
                mMostrecent_Sensor_X = event.values[0];
                mMostrecent_Sensor_Y = event.values[1];
                mMostrecent_Sensor_Z = event.values[2];

				// compute Kcal 
	 			computeKcal();
*/
				
				int col = samplesperwindow%manysamples;
				data[0][col] = event.values[0];
				data[1][col] = event.values[1];
				data[2][col] = event.values[2];				
				samplesperwindow++;

				long now = SystemClock.elapsedRealtime();
				// check if we have a window of data...
				if ( now >= (lasttime + WINDOWTIMEMILLISEC) ) {
//					Log.i(TAG, 
//							"Sensor : X: " + genfmt.format(event.values[0]) +
//							" Y: " + genfmt.format(event.values[1]) + 
//							" Z: " + genfmt.format(event.values[2]));

					// calculate sample window part of kcal 
					double [] result = calculateKcal(data);
					finalKcalResult(result);
					
					samplesperwindow = 0;
					lasttime = now;
				}
			}
		}
	}
	
	// This is called on every sample window 
	private static double[] calculateKcal(double[][] data) {
		double res[] = new double[2];	// holds V and H result
		double history[] = new double[3];
		double d[] = new double[3];
		double p[] = new double[3];
//		double sdata[][] = new double[3][manysamples];

/*		
		// smooth the data first
		// 3-sample moving average 
		for (int i=0; i<3; i++) {
			for (int j=0; j<samplesperwindow; j++) {
				if (j==0) {
					sdata[i][j] = twoprevious[i] + oneprevious[i] + data[i][j];
				} else if (j==1) {
					sdata[i][j] = oneprevious[i] + data[i][j-1] + data[i][j];
				} else {
					sdata[i][j] = data[i][j-2] + data[i][j-1] + data[i][j];
					if (j==(samplesperwindow-2))
						twoprevious[i]=data[i][j];
					else if (j==(samplesperwindow-1))
						oneprevious[i]=data[i][j];
				}
			}
		}
*/
		
	    // this is historical average of the past samples
		if (samplesperwindow > 0) { // sanity check just to be safe, otherwise, will result in history[i] being infinity
			for (int i=0; i<3; i++) {
				for (int j=0; j<samplesperwindow; j++) {
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
					num = (d[0] * history[0] + d[1] * history[1] + d[2] * history[2]);
					den = (history[0] * history[0] + history[1] * history[1] + history[2] * history[2]);
	
					if (den==0)
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
		}
		return res;
	}

	// this is called after each sample window is processed; if we have a minute's
	// worth of accumulated V and H results then generate the energy estimate
	private static void finalKcalResult(double [] VH) {
		double EE_minute;
		
		// Note the (samplesperwindow/(windowtimemillisec/1000)) scales back down to 60 in a min
		if (samplesperwindow > 0) { // sanity check just to be safe
			accum_minute_V += (VH[0]/(samplesperwindow/(((float)WINDOWTIMEMILLISEC)/1000)))/GRAVITY; // need to cast to float to prevent divide by 0.
			accum_minute_H += (VH[1]/(samplesperwindow/(((float)WINDOWTIMEMILLISEC)/1000)))/GRAVITY; // need to cast to float to prevent divide by 0.
		}
		counter++;
		long seconds = counter * WINDOWTIMEMILLISEC/1000;
		
		interim_V = accum_minute_V/counter;
		
		if (seconds >= 1) {
			// EEact(k) = a*H^p1 + b*V^p2
			//
			// assume:  mass(kg) = 80 kg
			// 			gender = 1 (male)
			//
			// a = (12.81 * mass(kg) + 843.22) / 1000 = 1.87
			// b = (38.90 * mass(kg) - 682.44 * gender(1=male,2=female) + 692.50)/1000 = 3.12
			// p1 = (2.66 * mass(kg) + 146.72)/1000 = 0.36
			// p2 = (-3.85 * mass(kg) + 968.28)/1000 = 0.66
			
			// EE in units kcal/minute --> EE_minute 
			// the 4.184 is to convert from KJ to kilocalories
			EE_minute = (1.87 * Math.pow(accum_minute_H, 0.36) + 3.12 * Math.pow(accum_minute_V, 0.66)) / 4.184;   
			mMostrecent_kCal = (float) EE_minute;
			if (mMostrecent_kCal >= .1) {
				mTotal_kCal += mMostrecent_kCal;
			}
			Log.i(TAG, "*** EE result: " + genfmt.format(EE_minute));
	
			// Returns the current system time in milliseconds since January 1, 1970 00:00:00 UTC.
			// this is the clock on the device as set by user/network.
			long currenttime = System.currentTimeMillis();
			mMostrecent_Time = (int) ((currenttime) / 1000);
			localV = accum_minute_V;
			localH = accum_minute_H;

/* commented out.. should be in workout.java, not here.			
			// write to website server with POST request...
		    // Create a new HttpClient and Post Header   
			Thread httpthread = new Thread() {
        	public void run() {
        		
				HttpClient httpclient = new DefaultHttpClient();   
			    HttpPost httppost = new HttpPost("http://calfitd.dyndns.org/PHAST/CalFitd/index.php");   
			  
			    try {   
			        // Add your data   
			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);   
			        nameValuePairs.add(new BasicNameValuePair("time", ((Integer)(mMostrecent_Time)).toString()));   
			        nameValuePairs.add(new BasicNameValuePair("lat", geofmt.format(mMostrecent_GPS_Latitude)));   
			        nameValuePairs.add(new BasicNameValuePair("lon", geofmt.format(mMostrecent_GPS_Longitude)));   
			        nameValuePairs.add(new BasicNameValuePair("speed", genfmt.format(mMostrecent_GPS_Speed)));   
			        nameValuePairs.add(new BasicNameValuePair("alt", genfmt.format(mMostrecent_GPS_Altitude)));   
			        nameValuePairs.add(new BasicNameValuePair("v", genfmt.format(localV)));   
			        nameValuePairs.add(new BasicNameValuePair("h", genfmt.format(localH)));   
			        nameValuePairs.add(new BasicNameValuePair("kcal", genfmt.format(mMostrecent_kCal)));   
			        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));   
			  
			        // Execute HTTP Post Request   
			        ResponseHandler<String> responseHandler = new BasicResponseHandler();
			        String responseBody = httpclient.execute(httppost, responseHandler);
//			        HttpResponse response = httpclient.execute(httppost);   
			           
			    } catch (Exception e) {   
					Log.e(getClass().getSimpleName(), "HTTP error: " + e.getMessage());   
			    }   		        		
        	}
        };
        httpthread.start();
			
		String str =  ((Integer)(mMostrecent_Time)).toString() + "," +
		geofmt.format(mMostrecent_GPS_Latitude) + "," +
		geofmt.format(mMostrecent_GPS_Longitude) + "," + 			
		genfmt.format(mMostrecent_GPS_Speed) + "," +
		genfmt.format(mMostrecent_GPS_Altitude) + "," +
		genfmt.format(accum_minute_V) + "," +
		genfmt.format(accum_minute_H) + "," +
		genfmt.format(mMostrecent_kCal) + "\n";
        
		
		// write the data to file on the device's SD card
		try {
		    File root = Environment.getExternalStorageDirectory();
		    if (root.canWrite()){
		    	File myfile = new File(root, "CalFitdatalog.txt");
			    myfile.createNewFile();
		        FileWriter mywriter = new FileWriter(myfile, true);
		        BufferedWriter out = new BufferedWriter(mywriter);

		        // use out.write statements to write data...
		       
		        out.write(str);
		        out.close();
		    }
		} catch (IOException e) {
		    Log.e(getClass().getSimpleName(), "Could not write file " + e.getMessage());
		}
*/

/*			
		try {
			// Construct data
			String data = URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(((Integer)(mMostrecent_Time)).toString(), "UTF-8");
			data += "&" + URLEncoder.encode("lat", "UTF-8") + "=" + URLEncoder.encode(geofmt.format(mMostrecent_GPS_Latitude), "UTF-8");
			data += "&" + URLEncoder.encode("lon", "UTF-8") + "=" + URLEncoder.encode(geofmt.format(mMostrecent_GPS_Longitude), "UTF-8");
			data += "&" + URLEncoder.encode("speed", "UTF-8") + "=" + URLEncoder.encode(genfmt.format(mMostrecent_GPS_Speed), "UTF-8");
			data += "&" + URLEncoder.encode("alt", "UTF-8") + "=" + URLEncoder.encode(genfmt.format(mMostrecent_GPS_Altitude), "UTF-8");
			data += "&" + URLEncoder.encode("v", "UTF-8") + "=" + URLEncoder.encode(genfmt.format(accum_minute_V), "UTF-8");
			data += "&" + URLEncoder.encode("h", "UTF-8") + "=" + URLEncoder.encode(genfmt.format(accum_minute_H), "UTF-8");
			data += "&" + URLEncoder.encode("kcal", "UTF-8") + "=" + URLEncoder.encode(genfmt.format(mMostrecent_kCal), "UTF-8");				

			// Send data
			URL url = new URL("http://calfitd.dyndns.org/PHAST/CalFitd/index.php");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
//			wr.flush();
			wr.close();		
*/
		
			// Get the response
/*				
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				// Process line...
//				Log.i(getClass().getSimpleName(), "HTTP response: " + line);
			}
*/

//			rd.close();
/*			    
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), "HTTP error: " + e.getMessage());
		    }
*/
			
			// reset the counters
			accum_minute_V = 0;
			accum_minute_H = 0;
			counter = 0;
		}
	}
}