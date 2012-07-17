package edu.berkeley.eecs.ruzenafit.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.access.SharedPreferencesHelper;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.util.Constants;

// FIXME: Change this whole activity to be a programmatic display of current rankings.
public class RankingActivity extends Activity {
	private static final String TAG = RankingActivity.class.getSimpleName();

	// Initializing variables
	Button displayWorkouts, explicitSend, explicitRetrieve;
	ProgressBar progressBar;
	
	// Move this out of the controller, and into a network or DB layer
	// Change this one workout task to be used for all of the different network buttonss
	private class RetrieveWorkoutsTask extends AsyncTask<Void, Integer, WorkoutTick[]> {

		@Override
		protected WorkoutTick[] doInBackground(Void... params) {
			
			WorkoutTick[] workouts = null;
			
//			// Prep for DB insertion
////			DBConn dbHelper = new DBConn(getApplicationContext(), "dbName", null, 1);
////			SQLiteDatabase database = dbHelper.getWritableDatabase();
//
//			// Perform the actual query
//			Cursor cursor = null; 
////					database.query(Constants.WORKOUT_TABLE, new String[] {
////						WorkoutTick.KEY_IMEI,
////						WorkoutTick.KEY_KCALS,
////						WorkoutTick.KEY_SYSTEM_TIME,
////						WorkoutTick.KEY_LATITUDE,
////						WorkoutTick.KEY_LONGITUDE,
////						WorkoutTick.KEY_SPEED,
////						WorkoutTick.KEY_ALTITUDE,
////						WorkoutTick.KEY_HAS_ACCURACY,
////						WorkoutTick.KEY_ACCURACY,
////						WorkoutTick.KEY_ACCUM_MINUTE_V,
////						WorkoutTick.KEY_ACCUM_MINUTE_H,
////						WorkoutTick.KEY_GPS_TIME}, 
////					null, null, null, null, null);
//
//			// Instantiate the array that we're going to return, now that we know the length
//			int length = cursor.getCount();
//			workouts = new WorkoutTick[length];
//
//			// Iterate through the returned query
//			int i = 0;
//			cursor.moveToFirst();
//			while (!cursor.isAfterLast()) {
//				
//				// Debug output.
//				Log.d(TAG, "["+i+"] Delta kCals:  " + cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_KCALS)));
//				Log.d(TAG, "["+i+"] System time: " + cursor.getString(cursor.getColumnIndex(WorkoutTick.KEY_SYSTEM_TIME)));
//				
//				// Instantiate a new workout object using the data from the query. 
//				WorkoutTick workout = new WorkoutTick(
//						cursor.getString(cursor.getColumnIndex(WorkoutTick.KEY_IMEI)), 
//						cursor.getLong(cursor.getColumnIndex(WorkoutTick.KEY_GPS_TIME)), 
//						cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_LATITUDE)), 
//						cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_LONGITUDE)), 
//						cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_ALTITUDE)), 
//						cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_SPEED)), 
//						cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_HAS_ACCURACY)), 
//						cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_ACCURACY)), 
//						cursor.getLong(cursor.getColumnIndex(WorkoutTick.KEY_SYSTEM_TIME)), 
//						cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_KCALS)), 
//						cursor.getDouble(cursor.getColumnIndex(WorkoutTick.KEY_ACCUM_MINUTE_V)), 
//						cursor.getDouble(cursor.getColumnIndex(WorkoutTick.KEY_ACCUM_MINUTE_H)));
//
//				publishProgress((int) ((i / (float)length ) * 100));
//				
//				// Throw the new object into the array and move on
//				workouts[i++] = workout;
//				cursor.moveToNext();
//			}
//
//			// Don't forget to close cursors and databases!
//			cursor.close();
////			database.close();
			
			return workouts;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressBar.setProgress(values[0]);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			setProgressBarVisibility(false);
		}

		@Override
		protected void onPostExecute(WorkoutTick[] result) {
			super.onPostExecute(result);

			// Let the user know what's up.
			Toast.makeText(
					getApplicationContext(), 
					"Retrieved " + result.length + " workout \"ticks\" from internal SQLite", 
					Toast.LENGTH_SHORT).show();
			
			setProgressBarVisibility(false);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ranking);
		Log.d(TAG, "Ranking Screen !!!!");

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		displayWorkouts = (Button) findViewById(R.id.buttonSQLiteWorkouts);
		displayWorkouts.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Access layer.
				new RetrieveWorkoutsTask().execute();
			}
		});

		explicitSend = (Button) findViewById(R.id.buttonExplcitSend);
		explicitSend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				explicitSend();				
			}
		});
		
		explicitRetrieve = (Button) findViewById(R.id.buttonExplicitRetrieve);
		explicitRetrieve.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				explicitRetrieve();
			}
		});
	};
	
	private void explicitSend() {
		
		// Grab the currently set imei and privacySetting
		SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
		String imei = sharedPreferencesHelper.retrieveValueForString(WorkoutTick.KEY_IMEI);
		String privacySetting = sharedPreferencesHelper.retrieveValueForString(Constants.PRIVACY_SETTING);
		
		// Network layer
//		GoogleAppEngineHelper.submitDataToGAE(
//				DBHelper.retrieveWorkouts(getApplicationContext()), 
//				imei, 
//				privacySetting,
//				this);
	}

	private void explicitRetrieve() {
		// Grab the currently set IMEI and privacy setting
		SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
		String imei = sharedPreferencesHelper.retrieveValueForString(WorkoutTick.KEY_IMEI);
		
//		GoogleAppEngineHelper.retrieveDataFromGAE(imei, getApplicationContext());
	}
	

}
