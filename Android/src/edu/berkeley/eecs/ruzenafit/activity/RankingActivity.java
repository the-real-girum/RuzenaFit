package edu.berkeley.eecs.ruzenafit.activity;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.access.InternalDBHelper;
import edu.berkeley.eecs.ruzenafit.util.Constants;

public class RankingActivity extends Activity {
	private static final String TAG = RankingActivity.class.getSimpleName();
	
	// Initializing variables
	Button displayWorkouts;

	private static String t = "Message";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ranking);
		Log.d(t, "Ranking Screen !!!!");
		
		displayWorkouts = (Button) findViewById(R.id.buttonSQLiteWorkouts);
		
		displayWorkouts.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				// Prep for DB insertion
				InternalDBHelper dbHelper = new InternalDBHelper(getApplicationContext(), "dbName", null, 1);
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				
				Cursor cursor = database.query(Constants.WORKOUT_TABLE, 
						new String[] {
								Constants.KEY_DELTA_KCALS,
								Constants.KEY_SYSTEM_TIME},
						null, 
						null, 
						null, 
						null, 
						null);
				
				cursor.moveToFirst();
				int i = 0;
				while (!cursor.isAfterLast()) {
					Log.d(TAG, "[" + i + "] Delta kCals:  " + 
							cursor.getFloat(cursor.getColumnIndex(Constants.KEY_DELTA_KCALS)));
					Log.d(TAG, "[" + i + "] System time: " + 
							cursor.getString(cursor.getColumnIndex(Constants.KEY_SYSTEM_TIME)));
					
					i++;
					cursor.moveToNext();
				}
				
				cursor.close();
				database.close();
			}
		});
	};
	
	
}
