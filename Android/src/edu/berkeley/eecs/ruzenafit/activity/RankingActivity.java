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
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.network.GoogleAppEngineHelper;
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
		// TODO: Make this method a little more DRY with the method below it.
		// Grab the currently set IMEI and privacy setting
		SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAMESPACE, 0);
		String imei = preferences.getString(Constants.KEY_IMEI, Constants.UNDEFINED_IMEI);
		String privacySetting = preferences.getString(Constants.PRIVACY_SETTING, Constants.UNDEFINED_PRIVACY_SETTING);
		
		// Sanity checks on the strings we grabbed above
		if (imei.equals(Constants.UNDEFINED_IMEI)) {
			Toast.makeText(getApplicationContext(), "Phone's IMEI not set.", 3).show();
			return;
		}
		if (privacySetting.equals(Constants.UNDEFINED_PRIVACY_SETTING)) {
			Toast.makeText(getApplicationContext(), "Privacy setting not set.", 3).show();
			return;
		}
		
		// Network layer
		GoogleAppEngineHelper.submitDataToGAE(
				DBHelper.retrieveWorkouts(getApplicationContext()), 
				imei, 
				privacySetting,
				getApplicationContext());
	}

	private void explicitRetrieve() {
		// Grab the currently set IMEI and privacy setting
		SharedPreferences preferences = getSharedPreferences(Constants.PREFS_NAMESPACE, 0);
		String imei = preferences.getString(Constants.KEY_IMEI, Constants.UNDEFINED_IMEI);
		
		// Sanity checks on the strings we grabbed above
		if (imei.equals(Constants.UNDEFINED_IMEI)) {
			Toast.makeText(getApplicationContext(), "Phone's IMEI not set.", 3).show();
			return;
		}
		
		GoogleAppEngineHelper.retrieveDataFromGAE(imei, getApplicationContext());
		
	}
	

}
