package edu.berkeley.eecs.ruzenafit.activity;

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
				textOut.setText("    With this setting you will earn a 1.8x multiplier to your points. "
						+ "You will also share the maximum amount of data about yourself possible. "
						+ "For example anyone will be able to see exactly where you are working out "
						+ "at whichever time your data is saved.");
			}
		});

		// Listening to button event
		rbMedium.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setPrivacySetting(PrivacyPreference.mediumPrivacy);
				textOut.setText("    With this setting you will earn a 1.2x multiplier to "
						+ "your points. "
						+ "You will also share a moderate amount of data about yourself. "
						+ "For example people will be able to see the area where you are working out "
						+ "but not the exact street or location "
						+ "at whichever time your data is saved.");
			}
		});

		// Listening to button event
		rbHigh.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setPrivacySetting(PrivacyPreference.highPrivacy);
				textOut.setText("    With this setting you will earn a 0.8x multiplier to your points. "
						+ "You will also share a minimal amount of data about yourself.");
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
		SharedPreferences.Editor preferences = getSharedPreferences(
				"RuzenaFitPrefs", 0).edit();
		preferences.putString("privacySetting", privacyPreference.toString());
		preferences.commit();

		Toast.makeText(getApplicationContext(),
				"Saved privacy setting: " + privacyPreference.toString(), 3)
				.show();
	}
}
