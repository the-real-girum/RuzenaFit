package edu.berkeley.eecs.ruzenafit.access;

import java.util.ArrayList;

import android.database.Cursor;
import android.util.Log;
import edu.berkeley.eecs.ruzenafit.model.AnActualWorkoutModelX_X;
import edu.berkeley.eecs.ruzenafit.model.GeoPoint_Time;

public class InternalDBHelper {
	private static final String TAG = "InternalDBHelper";
	
	/**
	 * We need to save the data for each of the workouts *before* we just throw it into the ListView,
	 * but I don't really feel like unhacking the fillData() method.  So, I'll just iterate
	 * over the total workouts twice.
	 */
	public static AnActualWorkoutModelX_X[] loadSQLiteWorkoutDataIntoMemory(CalFitDBAdapter mDbHelper) {
		Log.d(TAG, "saveCurrentDataInMemory, with DBAdapter: " + mDbHelper);
		
		
		
		// Grab a cursor that can iterate over the internal SQLite database
		Cursor cursor = mDbHelper.getAllUserWorkouts(1);
		cursor.moveToFirst();
		
		// (re)instantiate this {model+view+controller}'s array representation
		// of said workouts
		AnActualWorkoutModelX_X[] allWorkouts = new AnActualWorkoutModelX_X[cursor.getColumnCount()];
		
		// Now step through the SQLite table and save the data in memory
		for (int i = 0; !cursor.isAfterLast(); i++) {
			AnActualWorkoutModelX_X workout = new AnActualWorkoutModelX_X();
			workout.setWorkoutID(Long.parseLong(cursor.getString(cursor.getColumnIndex("_id"))));
			workout.setDate(cursor.getString(cursor.getColumnIndex("date")));
			workout.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
			workout.setTotalCalories(cursor.getString(cursor.getColumnIndex("total_calories")));
			workout.setAverageSpeed(cursor.getString(cursor.getColumnIndex("average_speed")));
			workout.setTotalDistance(cursor.getString(cursor.getColumnIndex("total_distance")));
			
			ArrayList<GeoPoint_Time> geopoints = new ArrayList<GeoPoint_Time>();
			Cursor dataSampleCursor = mDbHelper.getWorkoutSampledata(workout.getWorkoutID());
			dataSampleCursor.moveToFirst();
			
			while (!dataSampleCursor.isAfterLast()) {
//				Log.d(TAG, "geopoint_lat column index )
				String latitude = dataSampleCursor.getString(dataSampleCursor.getColumnIndex("geopoint_lat"));
				String longitude = dataSampleCursor.getString(dataSampleCursor.getColumnIndex("geopoint_long"));
				String geopointDate = dataSampleCursor.getString(dataSampleCursor.getColumnIndex("geopoint_date"));

				geopoints.add(new GeoPoint_Time(latitude, longitude, geopointDate));
				
				dataSampleCursor.moveToNext();
			}
			dataSampleCursor.close();
			
			GeoPoint_Time[] geopointsArray = geopoints.toArray(new GeoPoint_Time[geopoints.size()]);
			workout.setGeopoints(geopointsArray);
			
			allWorkouts[i++] = workout;
			cursor.moveToNext();
		}
		
		// Disconnect the cursor
		cursor.close();
		
		Log.d(TAG, "saveCurrentDataInMemory: " + allWorkouts.toString());
		
		return allWorkouts;
	}
}
