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
	private static final String TAG = PrivacyPreferencesActivity.class
			.getSimpleName();

	// String descriptions of the privacy settings.
	private final String HIGH_PRIVACY_DESCRIPTION = "    With this setting you will earn a "
			+ PrivacyPreferenceEnum.highPrivacy.getValue()
			+ "x multiplier to your points. "
			+ "You will also share the maximum amount of data about yourself possible. "
			+ "Your tracked location will be blurred slightly -- people will know where you are within"
			+ "the nearest ~5.0 square miles";

	private final String MEDIUM_PRIVACY_DESCRIPTION = "    With this setting you will earn a "
			+ PrivacyPreferenceEnum.mediumPrivacy.getValue()
			+ "x multiplier to your points. "
			+ "Your tracked location will be blurred slightly -- people will know where you are within"
			+ "the nearest ~1.5 square miles";

	private final String LOW_PRIVACY_DESCRIPTION = "    With this setting you will earn a "
			+ PrivacyPreferenceEnum.lowPrivacy.getValue()
			+ "x multiplier to your points. "
			+ "You will also share a minimal amount of data about yourself."
			+ "For example anyone will be able to see exactly where you are working out "
			+ "at whichever time your data is saved.  ";
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
				textOut.setText(LOW_PRIVACY_DESCRIPTION);
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
				textOut.setText(HIGH_PRIVACY_DESCRIPTION);
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
		SharedPreferences.Editor editor = getSharedPreferences(
				Constants.PREFS_NAMESPACE, 0).edit();

		// Now, for the string "privacySetting", we have mapped one of the
		// following strings:
		// "highPrivacy"
		// "lowPrivacy"
		// "mediumPrivacy"
		editor.putString(Constants.PRIVACY_SETTING,
				privacyPreference.toString());
		editor.commit();

		// switch (privacyPreference) {
		// case highPrivacy:
		// Constants.setUPDATE_FREQUENCY(3600000); // one hour
		// break;
		// case mediumPrivacy:
		// Constants.setUPDATE_FREQUENCY(300000); // 5 minutes
		// break;
		// case lowPrivacy:
		// Constants.setUPDATE_FREQUENCY(5000); // 5 seconds
		// break;
		// default:
		// Constants.setUPDATE_FREQUENCY(3000); // 3 seconds if undefined.
		// break;
		// }

		Toast.makeText(getApplicationContext(),
				"Saved privacy setting: " + privacyPreference.toString(), 3)
				.show();
	}

	protected void onResume() {
		super.onResume();
		SharedPreferences preferences = getSharedPreferences(
				Constants.PREFS_NAMESPACE, 0);

		String notSet = "Privacy not set";
		String lSet = "lowPrivacy";
		String mSet = "mediumPrivacy";
		String hSet = "highPrivacy";

		String p = preferences.getString(Constants.PRIVACY_SETTING, notSet);

		if (p.equals(lSet)) {
			rbLow.setChecked(true);
			textOut.setText(LOW_PRIVACY_DESCRIPTION);
		}

		else if (p.equals(mSet)) {
			rbMedium.setChecked(true);
			textOut.setText(MEDIUM_PRIVACY_DESCRIPTION);
		} else if (p.equals(hSet)) {
			rbHigh.setChecked(true);
			textOut.setText(HIGH_PRIVACY_DESCRIPTION);
		} else {
			// Toast.makeText(getApplicationContext(),
			// "Unknown privacy preference found", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Unknown privayc preference found");
		}

	}

}
