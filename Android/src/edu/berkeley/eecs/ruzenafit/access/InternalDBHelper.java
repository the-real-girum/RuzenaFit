package edu.berkeley.eecs.ruzenafit.access;

import android.database.Cursor;
import android.util.Log;
import edu.berkeley.eecs.ruzenafit.DBAdapter;
import edu.berkeley.eecs.ruzenafit.model.AnActualWorkoutModelX_X;

public class InternalDBHelper {
	private static final String TAG = "SQLiteHelper";
	
	/**
	 * We need to save the data for each of the workouts *before* we just throw it into the ListView,
	 * but I don't really feel like unhacking the fillData() method.  So, I'll just iterate
	 * over the total workouts ctwice.
	 */
	public static AnActualWorkoutModelX_X[] saveCurrentDataInMemory(DBAdapter mDbHelper) {
		Log.d(TAG, "saveCurrentDataInMemory, with DBAdapter: " + mDbHelper);
		
		// Grab a cursor that can iterate over the internal SQLite database
		Cursor cursor = mDbHelper.getAllUserWorkouts(1);
		
		// (re)instantiate this {model+view+controller}'s array representation
		// of said workouts
		AnActualWorkoutModelX_X[] allWorkouts = new AnActualWorkoutModelX_X[cursor.getColumnCount()];
		
		// Now step through the SQLite table and save the data in memory
		int index = 0;
		while (!cursor.isAfterLast()) {
			AnActualWorkoutModelX_X workout = new AnActualWorkoutModelX_X();
			workout.setDate(cursor.getString(cursor.getColumnIndex("date")));
			workout.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
			workout.setTotalCalories(cursor.getString(cursor.getColumnIndex("total_calories")));
			workout.setAverageSpeed(cursor.getString(cursor.getColumnIndex("average_speed")));
			workout.setTotalDistance(cursor.getString(cursor.getColumnIndex("total_distance")));
			
			allWorkouts[index++] = workout;
			cursor.moveToNext();
		}
		
		// Disconnect the cursor
		cursor.close();
		
		Log.d(TAG, "saveCurrentDataInMemory: " + allWorkouts.toString());
		
		return allWorkouts;
	}
}
