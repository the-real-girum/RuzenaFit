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
import edu.berkeley.eecs.ruzenafit.model.PrivacyPreferenceEnum;
import edu.berkeley.eecs.ruzenafit.util.Constants;

public class PrivacyPreferencesActivity extends Activity {

	// String descriptions of the privacy settings.
	private final String HIGH_PRIVACY_DESCRIPTION = "    With this setting you will earn a " 
			+ PrivacyPreferenceEnum.highPrivacy.getValue()
			+ "x multiplier to your points. "
			+ "You will also share the maximum amount of data about yourself possible. "
			+ "For example anyone will be able to see exactly where you are working out "
			+ "at whichever time your data is saved.  "
			+ "\n\n"
			+ "With this preference, your data will update ONCE EVERY 5 SECONDS.";
	
	private final String MEDIUM_PRIVACY_DESCRIPTION = "    With this setting you will earn a "
			+ PrivacyPreferenceEnum.mediumPrivacy.getValue()
			+ "x multiplier to your points. "
			+ "You will also share a minimal amount of data about yourself."
			+ "\n\n"
			+ "With this preference, your data will update ONCE EVERY HOUR.";
	
	private final String LOW_PRIVACY_DESCRIPTION = "    With this setting you will earn a "
			+ PrivacyPreferenceEnum.lowPrivacy.getValue()
			+ "x multiplier to your points. "
			+ "You will also share a minimal amount of data about yourself."
			+ "\n\n"
			+ "With this preference, your data will update ONCE EVERY HOUR.";
	
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

		// Listening to button event
		rbLow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setPrivacySetting(PrivacyPreferenceEnum.lowPrivacy);
				textOut.setText(HIGH_PRIVACY_DESCRIPTION);
			}
		});

		// Listening to button event
		rbMedium.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setPrivacySetting(PrivacyPreferenceEnum.mediumPrivacy);
				textOut.setText(MEDIUM_PRIVACY_DESCRIPTION);
			}
		});

		// Listening to button event
		rbHigh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setPrivacySetting(PrivacyPreferenceEnum.highPrivacy);
				textOut.setText(LOW_PRIVACY_DESCRIPTION);
			}
		});

	}

	/**
	 * Saves the selected privacy setting into the phone's internal
	 * SharedPreferences storage.
	 * 
	 * @param privacyPreference
	 */
	private void setPrivacySetting(PrivacyPreferenceEnum privacyPreference) {

		// Get the SharedPreferences.Editor object we need to modify our
		// String->String preferences map.
		SharedPreferences.Editor preferences = getSharedPreferences(
				Constants.PREFS_NAMESPACE, 0).edit();

		// Now, for the string "privacySetting", we have mapped one of the
		// following strings:
		// "highPrivacy"
		// "lowPrivacy"
		// "mediumPrivacy"
		preferences.putString(Constants.PRIVACY_SETTING,
				privacyPreference.toString());
		preferences.commit();

		switch (privacyPreference) {
		case highPrivacy:
			Constants.setUPDATE_FREQUENCY(3600000); // one hour
			break;
		case mediumPrivacy:
			Constants.setUPDATE_FREQUENCY(300000); // 5 minutes
			break;
		case lowPrivacy:
			Constants.setUPDATE_FREQUENCY(5000); // 5 seconds
			break;
		default:
			Constants.setUPDATE_FREQUENCY(3000); // 3 seconds if undefined.
			break;
		}

		Toast.makeText(getApplicationContext(),
				"Saved privacy setting: " + privacyPreference.toString(), 3)
				.show();
	}

	protected void onResume() {
		super.onResume();
		SharedPreferences preferences = getSharedPreferences(
				Constants.PREFS_NAMESPACE, 0);

		// TODO: Change this so that it doesn't use string literals
		String p = preferences.getString(Constants.PRIVACY_SETTING,
				"Privacy not set");

		if (p.equals("lowPrivacy"))
		{
			rbLow.setChecked(true);
			textOut.setText(LOW_PRIVACY_DESCRIPTION);
		}

		else if (p.equals("mediumPrivacy"))
		{
			rbMedium.setChecked(true);
			textOut.setText(MEDIUM_PRIVACY_DESCRIPTION);
		}
		else if (p.equals("highPrivacy"))
		{
			rbHigh.setChecked(true);
			textOut.setText(HIGH_PRIVACY_DESCRIPTION);
		}

	}

}
