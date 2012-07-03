package edu.berkeley.eecs.ruzenafit.access;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.util.Constants;

/**
 * A class of static helper methods to access Android's SQLite database.
 * Use this class to save and retrieve workout "ticks" from the phone's
 * persistent storage.
 *  
 * @author gibssa
 */
public class InternalDBHelper {
	private static final String TAG = InternalDBHelper.class.getSimpleName();

	/**
	 * Inserts a single workout "tick" into Android's internal SQLite database
	 * @param workout
	 * @param context
	 */
	public static void insertWorkoutIntoDatabase(WorkoutTick workout, Context context) {
		// Prep for DB insertion
		DBConn dbHelper = new DBConn(context, "dbName", null, 1);
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		// Throw each of this particular workout's values into the row
		ContentValues contentValues = new ContentValues();
		contentValues.put(Constants.KEY_IMEI, workout.getImei());
		contentValues.put(Constants.KEY_KCALS, workout.getkCal());
		contentValues.put(Constants.KEY_SYSTEM_TIME, workout.getSystemTime());
		contentValues.put(Constants.KEY_LATITUDE, workout.getLatitude());
		contentValues.put(Constants.KEY_LONGITUDE, workout.getLongitude());
		contentValues.put(Constants.KEY_SPEED, workout.getSpeed());
		contentValues.put(Constants.KEY_ALTITUDE, workout.getAltitude());
		contentValues.put(Constants.KEY_HAS_ACCURACY,
				workout.getHasAccuracy());
		contentValues.put(Constants.KEY_ACCURACY, workout.getAccuracy());
		contentValues.put(Constants.KEY_ACCUM_MINUTE_V, workout.getAccumMinuteV());
		contentValues.put(Constants.KEY_ACCUM_MINUTE_H, workout.getAccumMinuteH());
		contentValues.put(Constants.KEY_GPS_TIME, workout.getTime());

		// Perform the actual insertion.
		database.insert(Constants.WORKOUT_TABLE, null, contentValues);

		// Don't forget to close your databases and cursors!
		database.close();
	}
	

	/**
	 * Uses Android's built-in SQLite database to retrieve the currently
	 * saved workout "ticks."  This method will work even when the workout tracking
	 * is currently running.
	 * 
	 * @param context Use getApplicationContext() for this parameter.
	 */
	public static void retrieveWorkouts(Context context) {
		
		WorkoutTick[] workouts;
		
		// Prep for DB insertion
		DBConn dbHelper = new DBConn(context, "dbName", null, 1);
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		// Perform the actual query
		Cursor cursor = database.query(Constants.WORKOUT_TABLE, new String[] {
					Constants.KEY_IMEI,
					Constants.KEY_KCALS,
					Constants.KEY_SYSTEM_TIME,
					Constants.KEY_LATITUDE,
					Constants.KEY_LONGITUDE,
					Constants.KEY_SPEED,
					Constants.KEY_ALTITUDE,
					Constants.KEY_HAS_ACCURACY,
					Constants.KEY_ACCURACY,
					Constants.KEY_ACCUM_MINUTE_V,
					Constants.KEY_ACCUM_MINUTE_H,
					Constants.KEY_GPS_TIME}, 
				null, null, null, null, null);

		// Instantiate the array that we're going to return, now that we know the length
		workouts = new WorkoutTick[cursor.getCount()];

		// Iterate through the returned query
		int i = 0;
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			
			// Debug output.
			Log.d(TAG, "["+i+"] Delta kCals:  " + cursor.getFloat(cursor.getColumnIndex(Constants.KEY_KCALS)));
			Log.d(TAG, "["+i+"] System time: " + cursor.getString(cursor.getColumnIndex(Constants.KEY_SYSTEM_TIME)));
			
			// Instantiate a new workout object using the data from the query. 
			WorkoutTick workout = new WorkoutTick(
					cursor.getString(cursor.getColumnIndex(Constants.KEY_IMEI)), 
					cursor.getLong(cursor.getColumnIndex(Constants.KEY_GPS_TIME)), 
					cursor.getFloat(cursor.getColumnIndex(Constants.KEY_LATITUDE)), 
					cursor.getFloat(cursor.getColumnIndex(Constants.KEY_LONGITUDE)), 
					cursor.getFloat(cursor.getColumnIndex(Constants.KEY_ALTITUDE)), 
					cursor.getFloat(cursor.getColumnIndex(Constants.KEY_SPEED)), 
					cursor.getFloat(cursor.getColumnIndex(Constants.KEY_HAS_ACCURACY)), 
					cursor.getFloat(cursor.getColumnIndex(Constants.KEY_ACCURACY)), 
					cursor.getString(cursor.getColumnIndex(Constants.KEY_SYSTEM_TIME)), 
					cursor.getFloat(cursor.getColumnIndex(Constants.KEY_KCALS)), 
					cursor.getDouble(cursor.getColumnIndex(Constants.KEY_ACCUM_MINUTE_V)), 
					cursor.getDouble(cursor.getColumnIndex(Constants.KEY_ACCUM_MINUTE_H)));

			// Throw the new object into the array and move on
			workouts[i++] = workout;
			cursor.moveToNext();
		}

		// Don't forget to close cursors and databases!
		cursor.close();
		database.close();
	}
}
