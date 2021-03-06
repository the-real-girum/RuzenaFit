package edu.berkeley.eecs.ruzenafit.access;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.util.Constants;

public class FileAccessHelper {
	private static final String TAG = FileAccessHelper.class.getSimpleName();
	
	/**
	 * Helper method to read all workout data currently on File.
	 */
	public static WorkoutTick[] getNewWorkoutDataFromFile(Context context) {
		
		ArrayList<WorkoutTick> workoutTicks = new ArrayList<WorkoutTick>();
		
		try {
			File root = new File(Environment.getExternalStorageDirectory()
					+ "/CalFitD");
			
			// If there isn't a folder called "/CalFitD", then don't even bother reading.
			if (!root.exists()) {
				return null;
			}
			
			// Read each line from "CalFitEE.txt" as a string
			if (root.canRead()) {
				File calfitEE = new File(root, "CalFitEE.txt");
				BufferedReader bufferedReader = new BufferedReader(new FileReader(calfitEE));
				String lineOfInput = null;
				
				int totalTicksSent = context.getSharedPreferences(Constants.PREFS_NAMESPACE, 0)
						.getInt(Constants.TOTAL_TICKS_SENT, -1);
				Log.d(TAG, "totalTicksSent: " + totalTicksSent);
				
				// Parse each line of "Edmund-ish" into WorkoutTicks, doing data integrity checks as you go.
				for (int i = 0; ((lineOfInput = bufferedReader.readLine()) != null) 
						&& workoutTicks.size() < Constants.BATCH_SIZE; i++) {
					// Check to only return the WorkoutTicks pertinent to this particular batch.
					if (i > totalTicksSent && workoutTicks.size() < Constants.BATCH_SIZE) {
						// Each line will be temporarily stored in 'lineOfInput'.
						workoutTicks.add(WorkoutTick.parseEdmundish(lineOfInput));
					}
				}
				
				Log.d(TAG, "Parsed " + workoutTicks.size() + " workout ticks from File");
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Could not getAllWorkoutDataFromFile(): " + e.getMessage());
		}
		
		return workoutTicks.toArray(new WorkoutTick[workoutTicks.size()]);
	}
}
