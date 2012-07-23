package edu.berkeley.eecs.ruzenafit.activity;

//import edu.berkeley.sph.ehs.calfitd.ICalFitdService;
import java.util.Arrays;
import java.util.Comparator;

import nu.xom.jaxen.function.ext.LowerFunction;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.model.PrivacyPreferenceEnum;
import edu.berkeley.eecs.ruzenafit.model.User;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.service.WorkoutTrackerService;
import edu.berkeley.eecs.ruzenafit.util.AndroidUtils;
import edu.berkeley.eecs.ruzenafit.util.Constants;
import edu.berkeley.eecs.ruzenafit.util.PostResultsToFacebookActivity;

public class WorkoutTrackerActivity extends Activity {
	private static final String TAG = WorkoutTrackerActivity.class
			.getSimpleName();

	private static final String RETRIEVE_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/ranking/getRankings";

	private static Context mContext;
	// The primary interface we will be calling on the service.
	// ICalFitdService mService = null;
	// notification variables
	public static NotificationManager notificationManager;
	public static final int NOTIFICATION_ID = 1;

	public double mTotal_kCal = 0.0;
	static private int MENU_ABOUT = 1;
	TextView pSetting, userName;

	private final CharSequence[] turnoffITEMS = { "resume data logging0",
			"resume data logging1", "resume data logging2",
			"resume data logging3", "resume data logging4",
			"resume data logging5", "resume data logging6",
			"resume data logging7", "resume data logging8",
			"resume data logging9", "TURN OFF data logging" };

	private static int whichitem = 0;
	private static int turnoff_log = 10;
	private ToggleButton tbutton;

	// private static int okoff = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.tracking);

		// turnoffITEMS.add("resume data logging", "TURN OFF data logging");

		mContext = this;
		// notificationManager = (NotificationManager)
		// getSystemService(Context.NOTIFICATION_SERVICE);

		// using startService allows the service to survive this activity
		// Toast.makeText(getApplicationContext(), "Starting service...",
		// Toast.LENGTH_SHORT).show();
		startService(new Intent(mContext, WorkoutTrackerService.class));
		Log.i(getClass().getSimpleName(), "startService!!!");

		// set status of the toggle
		final ToggleButton togglebutton = (ToggleButton) findViewById(R.id.togglebutton);
		tbutton = togglebutton;
		if (WorkoutTrackerService.getStatus() == 1)
			togglebutton.setChecked(true);
		else
			togglebutton.setChecked(false);

		pSetting = (TextView) findViewById(R.id.pset);
		userName = (TextView) findViewById(R.id.userValue);

		togglebutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				if (togglebutton.isChecked()
						&& (WorkoutTrackerService.getStatus() == 0)) { // turn
																		// on
																		// logging,
																		// don't
																		// need
																		// to do
																		// this
																		// if
																		// service
																		// status
																		// is
																		// already
																		// running
					
					// Set up for the dialog box that asks the user for which privacy preference he wants
					AlertDialog.Builder privacyAlertDialogBuilder = new AlertDialog.Builder(mContext);
					privacyAlertDialogBuilder.setTitle("Choose your privacy setting");
					privacyAlertDialogBuilder.setCancelable(false);
					
					// Set up the privacy selection alert dialog
					String[] privacyPreferences = new String[] { "Low Privacy", "Medium Privacy", "High Privacy" };
					privacyAlertDialogBuilder.setItems(privacyPreferences, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							
							SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFS_NAMESPACE, 0).edit();
							
							// TODO: Not very DRY
							switch (which) {
							case 0: {
								editor.putString(Constants.PRIVACY_SETTING, PrivacyPreferenceEnum.lowPrivacy.toString());
								pSetting.setText(PrivacyPreferenceEnum.lowPrivacy.getDisplayName());
								Log.d(TAG, "Saved new privacy preference: " + PrivacyPreferenceEnum.lowPrivacy.toString());
								break;
							}
							case 1: {
								editor.putString(Constants.PRIVACY_SETTING, PrivacyPreferenceEnum.mediumPrivacy.toString());
								pSetting.setText(PrivacyPreferenceEnum.mediumPrivacy.getDisplayName());
								Log.d(TAG, "Saved new privacy preference: " + PrivacyPreferenceEnum.mediumPrivacy.toString());
								break;
							}
							case 2: {
								editor.putString(Constants.PRIVACY_SETTING, PrivacyPreferenceEnum.highPrivacy.toString());
								pSetting.setText(PrivacyPreferenceEnum.highPrivacy.getDisplayName());
								Log.d(TAG, "Saved new privacy preference: " + PrivacyPreferenceEnum.highPrivacy.toString());
								break;
							}
							default: {
								Log.e(TAG, "Unknown privacy preference selection");
							}
							}
							
							// Make the privacy preference change
							editor.commit();
							
							// Start the actual datalogging now.
							Toast.makeText(getApplicationContext(),
									"Starting datalog...", Toast.LENGTH_SHORT).show();
							
							// Warns user about how to effectively use the application
							Toast.makeText(
									getApplicationContext(),
									"WARNING: kCal measurements WILL NOT BE ACCURATE if you do not have this phone strapped to your waist or in your pocket.",
									Toast.LENGTH_LONG).show();
							WorkoutTrackerService.startLog();
							Log.i(getClass().getSimpleName(), "startlog!!!");
						}
					});
					
					// Show the privacy selection dialog
					privacyAlertDialogBuilder.create().show();
					
				} else if ((togglebutton.isChecked() == false)
						&& (WorkoutTrackerService.getStatus() == 1)) { // turn
																		// off
																		// logging,
																		// don't
																		// need
																		// to do
																		// this
																		// if
																		// service
																		// status
																		// is
																		// not
																		// running
					// ask which action from list
					AlertDialog.Builder builder = new AlertDialog.Builder(
							mContext);
					builder.setTitle("Really stop?");
					builder.setCancelable(false);
					builder.setItems(turnoffITEMS,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int item) {
									whichitem = item;
									// okoff = 0;
									if (whichitem != turnoff_log) {
										// cancel... leave button checked...
										tbutton.setChecked(true);
									} else {
										// turn off logging
										// okoff = 1;
										Toast.makeText(getApplicationContext(),
												"Stopping datalog...",
												Toast.LENGTH_SHORT).show();
										WorkoutTrackerService.stopLog();
										Log.i(getClass().getSimpleName(),
												"stoplog!!!");
										// NotificationOff();
										tbutton.setChecked(false);

										// As the Tracking stops, request the user if he wants to post results to FB
										if (AndroidUtils.isOnline(getApplicationContext())) {
											startActivity(new Intent(
													getApplicationContext(),
													PostResultsToFacebookActivity.class));
										}
									}
								}

							});

					AlertDialog alert = builder.create();
					alert.show();

					/*
					 * // need this in case user hits "back" key to get out of
					 * the dialog if (okoff == 1)
					 * togglebutton.setChecked(false); else
					 * togglebutton.setChecked(true);
					 */
				}
			}
		});

		final Button button4 = (Button) findViewById(R.id.helpbutton);
		button4.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// start the About view
				startActivity(new Intent(mContext, AboutActivity.class));
			}
		});

		// get IMEI to uniquely identify users' posts to the server
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
		if (TextUtils.isEmpty(imei)) {
			imei = "0";
		}
		((TextView) findViewById(R.id.imei)).setText(imei);

		// save this IMEI to SharedPreferences to uniquely identify the phone
		SharedPreferences.Editor editor = getSharedPreferences(
				Constants.PREFS_NAMESPACE, 0).edit();
		editor.putString(WorkoutTick.KEY_IMEI, imei);
		editor.commit();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(getClass().getSimpleName(), "on stop!!!");
	}

	@Override
	protected void onDestroy() {
		// if not logging data, stop server upon exiting CalFit
		if (WorkoutTrackerService.getStatus() == 0) {
			stopService(new Intent(mContext, WorkoutTrackerService.class));
			Log.i(getClass().getSimpleName(), "stopService!!!");
		}
		super.onDestroy();
		Log.i(getClass().getSimpleName(), "on destoy!!!");
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ABOUT, 0, "About").setIcon(
				android.R.drawable.ic_menu_info_details);
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_ABOUT) {
			// start the About view
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		// set status of the toggle
		final ToggleButton toggleButton = (ToggleButton) findViewById(R.id.togglebutton);
		tbutton = toggleButton;
		toggleButton.setChecked(WorkoutTrackerService.getStatus() == 1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Retrieve current rankings from the server, and save it for facebook posting later
		if (AndroidUtils.isOnline(getApplicationContext())) {
			new FindPersonalRankingAsyncTask().execute();
		}

		pSetting = (TextView) findViewById(R.id.pset);
		userName = (TextView) findViewById(R.id.userValue);

		SharedPreferences preferences = getSharedPreferences(
				Constants.PREFS_NAMESPACE, 0);
		String facebookName = preferences.getString(Constants.FACEBOOK_NAME,
				"Name not found.");

		String p = preferences.getString(Constants.PRIVACY_SETTING,
				"Privacy not set");

		// Checks if FB Name and/or privacy setting is set.
		Log.d(TAG, "p = " + p);
		Log.d(TAG, "facebookName = " + facebookName);

		if (facebookName.equals("Name not found.")
				|| p.equals("Privacy not set")) {
			Toast.makeText(
					getApplicationContext(),
					"SORRY! You haven't logged into facebook or your privacy isn't set.",
					Toast.LENGTH_SHORT).show();
			tbutton.setEnabled(false);
		} else {
			tbutton.setEnabled(true);
		}

		// TODO: String literals.
		if (p.equals("lowPrivacy"))
			pSetting.setText("Low Privacy");
		else if (p.equals("mediumPrivacy"))
			pSetting.setText("Medium Privacy");
		else if (p.equals("highPrivacy"))
			pSetting.setText("High Privacy");
		else
			pSetting.setText("Privacy Setting Not Set");

		userName.setText(facebookName);

		// Check off the ToggleButton if the service is already running
		final ToggleButton toggleButton = (ToggleButton) findViewById(R.id.togglebutton);
		tbutton = toggleButton;
		toggleButton.setChecked(WorkoutTrackerService.getStatus() == 1);
	}

	private class FindPersonalRankingAsyncTask extends
			AsyncTask<Void, Void, User[]> {

		@Override
		protected User[] doInBackground(Void... params) {
			Log.d(TAG, "Retrieving most current rankings to post to FB later");
			
			// Setup the GET Request
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet request = new HttpGet(RETRIEVE_WORKOUTS_URL);

			String responseString = null;

			// Execute the GET request.
			try {
				HttpResponse response = httpClient.execute(request);
				responseString = EntityUtils.toString(response.getEntity());
				Log.d(TAG, "HttpResponse: " + responseString);
			} catch (Exception e) {
				Log.e(TAG, "HTTP ERROR: " + e.getMessage());
			}

			User rankings[] = null;

			try {
				// Parse the resulting JSON into the correct rankings array
				JSONArray rankingsJSONArray = new JSONArray(responseString);
				rankings = new User[rankingsJSONArray.length()];

				for (int i = 0; i < rankingsJSONArray.length(); i++) {

					JSONObject rankingJSONObject = rankingsJSONArray
							.getJSONObject(i);

					String userName = rankingJSONObject
							.getString(User.KEY_USER);
					double userScore = rankingJSONObject
							.getDouble(User.KEY_SCORE);

					rankings[i] = new User(userName, userScore);
				}
			} catch (JSONException e) {
				Log.e(TAG, "JSON exception: " + e.getMessage());
			}

			return rankings;
		}

		@Override
		protected void onPostExecute(User[] result) {
			super.onPostExecute(result);
			
			// Sanity check -- an exception could return null
			if (result == null) {
				return;
			}

			// Sort the list of users
			Arrays.sort(result, new Comparator<User>() {
				public int compare(User lhs, User rhs) {
					return ((Double) rhs.getScore()).compareTo((Double) lhs
							.getScore());
				}
			});

			// Serialize and format the results
			String[] formattedResults = new String[result.length];
			for (int i = 0; i < formattedResults.length; i++) {

				// Truncate the "user score" double value into an integer here.
				formattedResults[i] = result[i].getName() + ": "
						+ ((Double) result[i].getScore()).intValue()
						+ " points";
			}

			// Generate the facebook post string
			String facebookRankingsString = "Hey, here are the current rankings in our exercise game!  "
					+ "How do I stack up against the competition?";
			for (String userScoreString : formattedResults) {
				facebookRankingsString += "\n--";
				facebookRankingsString += userScoreString;
			}

			// Throw it into Shared Prefs
			SharedPreferences.Editor editor = getApplicationContext()
					.getSharedPreferences(Constants.PREFS_NAMESPACE, 0).edit();
			editor.putString(Constants.FACEBOOK_POST_STRING, facebookRankingsString);
			editor.commit();
			Toast.makeText(getApplicationContext(), "Retrieved workout rankings", 2).show();

		}

	}

}
