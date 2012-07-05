package edu.berkeley.eecs.ruzenafit.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.access.DBHelper;
import edu.berkeley.eecs.ruzenafit.access.GoogleAppEngineHelper;
import edu.berkeley.eecs.ruzenafit.access.SharedPreferencesHelper;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.util.Constants;

public class RankingActivity extends Activity {
	private static final String TAG = RankingActivity.class.getSimpleName();

	// Initializing variables
	Button displayWorkouts, explicitSend, explicitRetrieve;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ranking);
		Log.d(TAG, "Ranking Screen !!!!");

		displayWorkouts = (Button) findViewById(R.id.buttonSQLiteWorkouts);
		displayWorkouts.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Access layer.
				DBHelper.retrieveWorkouts(getApplicationContext());
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
		GoogleAppEngineHelper.submitDataToGAE(
				DBHelper.retrieveWorkouts(getApplicationContext()), 
				imei, 
				privacySetting,
				this);
	}

	private void explicitRetrieve() {
		// Grab the currently set IMEI and privacy setting
		SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
		String imei = sharedPreferencesHelper.retrieveValueForString(WorkoutTick.KEY_IMEI);
		
//		GoogleAppEngineHelper.retrieveDataFromGAE(imei, getApplicationContext());
	}
	

}
