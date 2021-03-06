package edu.berkeley.eecs.ruzenafit.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.util.AndroidUtils;
import edu.berkeley.eecs.ruzenafit.util.Constants;

// TODO: The GUI for this activity is gross.  At least make the buttons the same size.
public class MainActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = MainActivity.class.getSimpleName();

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

	@Override
	protected void onResume() {
		super.onResume();

		if (!AndroidUtils.isOnline(getApplicationContext())) {
			Toast.makeText(
					getApplicationContext(),
					Constants.NO_INTERNET_CONNECTION_MESSAGE,
					Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * Simple helper method to just set up the buttons in our main screen.
	 */
	private void setupButtons() {
		tracking = (Button) findViewById(R.id.button1);
		ranking = (Button) findViewById(R.id.button5);
		privacyPreferences = (Button) findViewById(R.id.button2);
		facebookSettings = (Button) findViewById(R.id.Button4);

		// Set each button to open up its respective Activity
		tracking.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						WorkoutTrackerActivity.class));
			}
		});
		ranking.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						RankingActivity.class));
			}
		});
		privacyPreferences.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						PrivacyPreferencesActivity.class));
			}
		});
		facebookSettings.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						FacebookSettingsActivity.class));
			}
		});
	}

}
