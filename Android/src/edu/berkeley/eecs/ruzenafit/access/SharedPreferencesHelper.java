package edu.berkeley.eecs.ruzenafit.access;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import edu.berkeley.eecs.ruzenafit.model.PrivacyPreferenceEnum;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.util.Constants;

public class SharedPreferencesHelper {
	private static final String TAG = SharedPreferencesHelper.class.getSimpleName();
	
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private Context context;
	
	/**
	 * When using an object of this class, you'll usually use "this" activity.
	 * @param activity
	 */
	public SharedPreferencesHelper(Context context) {
		super();
		
		this.preferences = context.getSharedPreferences(Constants.PREFS_NAMESPACE, 0);
		this.editor = preferences.edit();
		this.context = context;
	}
	
	/**
	 * Queries the phone's SharedPreferences for the @param targetString.
	 * Returns null and shows an error message if it cannot be found.
	 * 
	 * @param targetString
	 * @return
	 */
	public String retrieveValueForString(String targetString) {
		String result = preferences.getString(targetString, Constants.UNDEFINED);
		
		if (result.equals(Constants.UNDEFINED)) {
			String errorMessage = "ERROR: " + targetString + " not set.";
			
			Toast.makeText(context, errorMessage, 3).show();
			Log.e(TAG, errorMessage);
			
			return null;
		}
		
		return result;
	}
	
	public PrivacyPreferenceEnum getCurrentPrivacySetting() {
		String privacyString = retrieveValueForString(Constants.PRIVACY_SETTING);
		
		if (privacyString.equals(PrivacyPreferenceEnum.highPrivacy.toString())) {
			return PrivacyPreferenceEnum.highPrivacy;
		}
		else if (privacyString.equals(PrivacyPreferenceEnum.mediumPrivacy.toString())) {
			return PrivacyPreferenceEnum.mediumPrivacy;
		}
		else if (privacyString.equals(PrivacyPreferenceEnum.lowPrivacy.toString())) {
			return PrivacyPreferenceEnum.lowPrivacy;
		}
		else {
			return null;
		}
	}
	
	
}
