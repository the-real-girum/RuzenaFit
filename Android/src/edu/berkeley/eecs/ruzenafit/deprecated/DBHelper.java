package edu.berkeley.eecs.ruzenafit.deprecated;
//package edu.berkeley.eecs.ruzenafit.legacy;
//
//import java.security.PublicKey;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;
//import android.widget.Toast;
//import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
//import edu.berkeley.eecs.ruzenafit.util.Constants;
//
///**
// * A class of static helper methods to access Android's SQLite database.
// * 
// * This is the class you want to use outside of /access to save and retrieve 
// * workout "ticks" from the phone's persistent internal storage.
// *  
// * @author gibssa
// */
//public class DBHelper {
//	private static final String TAG = DBHelper.class.getSimpleName();
//
//	/**
//	 * Inserts a single workout "tick" into Android's internal SQLite database
//	 * @param workout
//	 * @param context
//	 */
//	public static void insertWorkoutIntoDatabase(WorkoutTick workout, Context context) {
//		// Prep for DB insertion
//		DBConn dbHelper = new DBConn(context, "dbName", null, 1);
//		SQLiteDatabase database = dbHelper.getWritableDatabase();
//
//		// Throw each of this particular workout's values into the row
//		ContentValues contentValues = new ContentValues();
//		contentValues.put(WorkoutTick.KEY_IMEI, workout.getImei());
//		contentValues.put(WorkoutTick.KEY_KCALS, workout.getkCal());
//		contentValues.put(WorkoutTick.KEY_SYSTEM_TIME, workout.getSystemTime());
//		contentValues.put(WorkoutTick.KEY_LATITUDE, workout.getLatitude());
//		contentValues.put(WorkoutTick.KEY_LONGITUDE, workout.getLongitude());
//		contentValues.put(WorkoutTick.KEY_SPEED, workout.getSpeed());
//		contentValues.put(WorkoutTick.KEY_ALTITUDE, workout.getAltitude());
//		contentValues.put(WorkoutTick.KEY_HAS_ACCURACY,
//				workout.getHasAccuracy());
//		contentValues.put(WorkoutTick.KEY_ACCURACY, workout.getAccuracy());
//		contentValues.put(WorkoutTick.KEY_ACCUM_MINUTE_V, workout.getAccumMinuteV());
//		contentValues.put(WorkoutTick.KEY_ACCUM_MINUTE_H, workout.getAccumMinuteH());
//		contentValues.put(WorkoutTick.KEY_GPS_TIME, workout.getTime());
//
//		// Perform the actual insertion.
////		database.insert(Constants.WORKOUT_TABLE, null, contentValues);
//
//		// Don't forget to close your databases and cursors!
//		database.close();
//		
//		// Check for batch level, and if it's time to upload to GAE, then do it.
//		// If network access fails, consider bringing up a notification saying
//		// that you can't connect to the network.
//	}
//	
//	/**
//	 * Uses Android's built-in SQLite database to retrieve the currently
//	 * saved workout "ticks."  This method will work even when the workout tracking
//	 * is currently running.
//	 * 
//	 * @param context Use getApplicationContext() for this parameter.
//	 */
//	public static WorkoutTick[] retrieveWorkouts(Context context) {
//		
//		WorkoutTick[] workouts = null;
//		
////		// Prep for DB insertion
////		DBConn dbHelper = new DBConn(context, "dbName", null, 1);
////		SQLiteDatabase database = dbHelper.getWritableDatabase();
////
////		// Perform the actual query
////		Cursor cursor = database.query(Constants.WORKOUT_TABLE, new String[] {
////					WorkoutTick.KEY_IMEI,
////					WorkoutTick.KEY_KCALS,
////					WorkoutTick.KEY_SYSTEM_TIME,
////					WorkoutTick.KEY_LATITUDE,
////					WorkoutTick.KEY_LONGITUDE,
////					WorkoutTick.KEY_SPEED,
////					WorkoutTick.KEY_ALTITUDE,
////					WorkoutTick.KEY_HAS_ACCURACY,
////					WorkoutTick.KEY_ACCURACY,
////					WorkoutTick.KEY_ACCUM_MINUTE_V,
////					WorkoutTick.KEY_ACCUM_MINUTE_H,
////					WorkoutTick.KEY_GPS_TIME}, 
////				null, null, null, null, null);
////
////		// Instantiate the array that we're going to return, now that we know the length
////		workouts = new WorkoutTick[cursor.getCount()];
////
////		// Iterate through the returned query
////		int i = 0;
////		cursor.moveToFirst();
////		while (!cursor.isAfterLast()) {
////			
////			// Debug output.
////			Log.d(TAG, "["+i+"] Delta kCals:  " + cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_KCALS)));
////			Log.d(TAG, "["+i+"] System time: " + cursor.getString(cursor.getColumnIndex(WorkoutTick.KEY_SYSTEM_TIME)));
////			
////			// Instantiate a new workout object using the data from the query. 
////			WorkoutTick workout = new WorkoutTick(
////					cursor.getString(cursor.getColumnIndex(WorkoutTick.KEY_IMEI)), 
////					cursor.getLong(cursor.getColumnIndex(WorkoutTick.KEY_GPS_TIME)), 
////					cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_LATITUDE)), 
////					cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_LONGITUDE)), 
////					cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_ALTITUDE)), 
////					cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_SPEED)), 
////					cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_HAS_ACCURACY)), 
////					cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_ACCURACY)), 
////					cursor.getString(cursor.getColumnIndex(WorkoutTick.KEY_SYSTEM_TIME)), 
////					cursor.getFloat(cursor.getColumnIndex(WorkoutTick.KEY_KCALS)), 
////					cursor.getDouble(cursor.getColumnIndex(WorkoutTick.KEY_ACCUM_MINUTE_V)), 
////					cursor.getDouble(cursor.getColumnIndex(WorkoutTick.KEY_ACCUM_MINUTE_H)));
////
////			// Throw the new object into the array and move on
////			workouts[i++] = workout;
////			cursor.moveToNext();
////		}
////
////		// Don't forget to close cursors and databases!
////		cursor.close();
////		database.close();
//		
//		return workouts;
//	}
//}
