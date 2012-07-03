package edu.berkeley.eecs.ruzenafit.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.berkeley.eecs.ruzenafit.R;

public class MainActivity extends Activity {

	Button tracking;
	Button ranking;
	Button privacyPreferences;
	Button facebookSettings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setupButtons();
	}

	/**
	 * Simple helper method to just set up the buttons in our main
	 * screen.
	 */
	private void setupButtons() {
		tracking = (Button) findViewById(R.id.button1);
		ranking = (Button) findViewById(R.id.button5);
		privacyPreferences = (Button) findViewById(R.id.button2);
		facebookSettings = (Button) findViewById(R.id.Button4);
		
		// Set each button to open up its respective Activity
		tracking.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), WorkoutTrackerActivity.class));
			}
		});
		ranking.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), RankingActivity.class));
			}
		});
		privacyPreferences.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), PrivacyPreferencesActivity.class));
			}
		});
		facebookSettings.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), FacebookSettingsActivity.class));
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	
}
