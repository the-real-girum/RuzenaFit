package edu.berkeley.eecs.ruzenafit.controller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.model.PrivacyPreference;

public class PreferencesActivity extends Activity {
	// Initializing variables
	RadioButton rbLow, rbMedium, rbHigh;
	TextView textOut;
	EditText getInput;

	private static String t = "Message";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);

		Log.d(t, "Preferences Screen !!!!");

		rbLow = (RadioButton) findViewById(R.id.radioButton1);
		rbMedium = (RadioButton) findViewById(R.id.radioButton2);
		rbHigh = (RadioButton) findViewById(R.id.radioButton3);

		textOut = (TextView) findViewById(R.id.textView2);
		// getInput = (EditText) findViewById(R.id.editText1);

		// Listening to button event
		rbLow.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setPrivacySetting(PrivacyPreference.lowPrivacy);
				textOut.setText("Low Preferences");
			}
		});

		// Listening to button event
		rbMedium.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setPrivacySetting(PrivacyPreference.mediumPrivacy);
				textOut.setText("Medium Preferences");
			}
		});

		// Listening to button event
		rbHigh.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setPrivacySetting(PrivacyPreference.highPrivacy);
				textOut.setText("High Preferences");
			}
		});

	}
	
	/**
	 * Saves the selected privacy setting into the phone's internal
	 * SharedPreferences storage.
	 * 
	 * @param privacyPreference
	 */
	private void setPrivacySetting(PrivacyPreference privacyPreference) {
		SharedPreferences.Editor preferences = getSharedPreferences("RuzenaFitPrefs", 0).edit();
		preferences.putString("privacySetting", privacyPreference.toString());
		preferences.commit();
		
		Toast.makeText(getApplicationContext(), "Saved privacy setting: " + privacyPreference.toString(), 3).show();
	}
}
