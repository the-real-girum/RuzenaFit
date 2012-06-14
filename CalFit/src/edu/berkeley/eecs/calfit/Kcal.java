package edu.berkeley.eecs.calfit;

import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class Kcal extends Service implements SensorEventListener {
	private static final String TAG = "Kcal Activity";
	private static SensorManager m = null;
	private static int counter1 = 1;
	private static int samples = 0;
	private static double accum_minute_V = 0;
	private static double accum_minute_H = 0;
	private static int ValueCount = 0;
	private static int totSamples = 30;
	private static int[][] data1 = new int[3][totSamples];
	public static Vector<Double> results = new Vector<Double>();
	public static double curResult = 0;
	private static double curTotal = 0;
	public static int resultIndex = 0;
	private static Sensor sensor = null;
	private static Kcal cal = null;

	/** Called when the activity is first created. */
	public void onCreate() {
		cal = new Kcal();
		m = (SensorManager) getSystemService(SENSOR_SERVICE);
        // need to ensure that this sensor is the right one.
        sensor = m.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        Log.d(TAG, "Accelerometers: " + sensor.toString());
//        Log.d(TAG, "registering listener");
//      m.registerListener(this,SensorManager.SENSOR_ORIENTATION |SensorManager.SENSOR_ACCELEROMETER,SensorManager.SENSOR_DELAY_FASTEST);
//        m.registerListener(cal, sensor, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	public void onStart(Intent intent, int startId) {
        // need to ensure that this sensor is the right one.
//        Sensor sensor = m.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
//        Log.d(TAG, "registering listener");
//        m.registerListener(this,SensorManager.SENSOR_ORIENTATION |SensorManager.SENSOR_ACCELEROMETER,SensorManager.SENSOR_DELAY_FASTEST);
//        m.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

	@Override
	public void onDestroy() {
        Log.d(TAG, "unregistering listener");
		m.unregisterListener(cal);
	}
	
	public static double getKCal() {
		return curTotal;
	}
	
	public static void setKCal(double val) {
		curTotal = val;
	}
	
	public static void startKCal() {
		try {
			Log.d(TAG, "registering listener and starting/resuming kcal count");
			m.registerListener(cal, sensor, SensorManager.SENSOR_DELAY_FASTEST);
		} catch (Exception e) {
			Log.d(TAG, "failed to register listener");
		}
	}
	
	public static void stopKCal() {
		try {
			Log.d(TAG, "unregistering listener and pausing kcal count");
			m.unregisterListener(cal);
		} catch (Exception e) {
			Log.d(TAG, "unable to unregister kcal accelerometer listener");
		}
	}
	
	public void dataReceived(int ch1, int ch2) {

		double EE_minute = 0;
		double FinalResult = 0;

		accum_minute_V = (((double) ch1) / 30.0) / 1024.0;
		accum_minute_H = (((double) ch2) / 30.0) / 1024.0;

		EE_minute = 1.87 * java.lang.Math.pow(accum_minute_H, 0.36) + 3.12
				* java.lang.Math.pow(accum_minute_V, 0.66);

		FinalResult = EE_minute / 4.184;
		if (FinalResult < 0 || (FinalResult - curResult < .0001 && FinalResult < .05)) {
			curTotal += 0;
			Log.i(TAG, "result: " + 0.0);
		} else {
			curTotal += FinalResult;
			curResult = FinalResult;
			Log.i(TAG, "result: " + FinalResult);
		}
		results.addElement(new Double(FinalResult));
	}

	public void loadData(float AccelX, float AccelY, float AccelZ) {
		data1[0][ValueCount] = (int) AccelX;
		data1[1][ValueCount] = (int) AccelY;
		data1[2][ValueCount] = (int) AccelZ;
		ValueCount++;
	}

	public void runKcalFeature(int[][] dataAll) {

		int[][] data = new int[3][30];
		int counter = 0;
		int[] sumResult = new int[2];
		for (int j = 0; j < dataAll[0].length; j++) {
			if (((counter + 1) % 30) != 0) {
				data[0][counter] = dataAll[0][j];
				data[1][counter] = dataAll[1][j];
				data[2][counter++] = dataAll[2][j];
			} else {
				j--;
				counter = 0;
				int[] result = cal.calculateKcal(data);

				sumResult[0] += result[0];
				sumResult[1] += result[1];

				if (counter1++ >= 5) {
					cal.dataReceived(sumResult[0], sumResult[1]);
					counter1 = 1;
					sumResult[0] = 0;
					sumResult[1] = 0;
				}
			}
		}
	}

	public int[] calculateKcal(int[][] data) {
		int history[] = new int[3];

		int res[] = new int[2];
		int d[] = new int[3];
		int p[] = new int[3];

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < data[0].length; j++) {
				history[i] += data[i][j];
			}
			history[i] /= 30;
		}

		for (int j = 0; j < data[0].length; j++) {

			for (int i = 0; i < 3; i++) {
				d[i] = history[i] - data[i][j];
			}

			int num = 0;
			int den = 0;
			int value = 0;
			for (int i = 0; i < 3; i++) {
				num = (d[0] * history[0] + d[1] * history[1] + d[2]
						* history[2]);
				den = (history[0] * history[0] + history[1] * history[1] + history[2]
						* history[2]);
				
				try {
					value = ((num * 1024) / den) * history[i];
				} catch (Exception e) {
					e.printStackTrace();
					value = 0;
				}
				p[i] = value / 1024;
			}

			int pMagn = p[0] * p[0] + p[1] * p[1] + p[2] * p[2];

			res[0] += SquareRoot.sqrt(pMagn);

			res[1] += SquareRoot.sqrt((d[0] - p[0]) * (d[0] - p[0])
					+ (d[1] - p[1]) * (d[1] - p[1]) + (d[2] - p[2])
					* (d[2] - p[2]));
		}
		return res;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		float[] values = event.values;
		//Log.d("kCal","RECEIVED DATA!!!!!");
		synchronized (cal) {
			if (samples != totSamples) {
				cal.loadData(Math.abs(values[0]), Math.abs(values[1]), Math.abs(values[2]));
				samples++;
			}
			if (samples == totSamples) {
				runKcalFeature(data1);
				samples = 0;
				ValueCount = 0;
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/* @deprecated
	public void onSensorChanged(int sensor, float[] values) {
		synchronized (this) {

			if (samples != totSamples) {
				loadData(values[0], values[1], values[2]);
				samples++;
			}
			if (samples == totSamples) {
				runKcalFeature(data1);
				samples = 0;
				ValueCount = 0;
			}

			if (sensor == SensorManager.SENSOR_ORIENTATION) {
				// alfa.setText("Most recent EE in kcal: " +
				// CurrentValue(results));
				// omega.setText("Number of Samples: " + samples);
				// gamma.setText("Total EE in kcal: " + AddValues(results));
			}
			if (sensor == SensorManager.SENSOR_ACCELEROMETER) {

				// x.setText("sumCounter: " + counter1);

			}
		}
	}
	*/
	
	/* @deprecated
	public double AddValues(Vector<Double> results) {
//		double TotalEE = 0;
//		for (int i = 0; i < results.size(); i++)
//			TotalEE = TotalEE + CurrentValue(results);

		for (int i = resultIndex; i < results.size(); i++) {
			curResult += results.get(i);
		}
		resultIndex = results.size();
		return curResult;
	}
	*/

	/* @deprecated
//	public double CurrentValue(Vector<Double> results) {
//		double CurrentValue = 0;
//		for (int i = 0; i < results.size(); i++)
//			CurrentValue = results.elementAt(results.size() - 1);
//		return CurrentValue;
//	}
	*/

	/* @deprecated
	public void onAccuracyChanged(int sensor, int accuracy) {
	}
	*/
}