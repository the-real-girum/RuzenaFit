package edu.berkeley.eecs.ruzenafit.access;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.util.Constants;

/**
 * Access layer to abstract away connections to GAE -- the Google App Engine.
 * 
 * @author gibssa
 */
public class GoogleAppEngineHelper {
	private static final String TAG = "ExternalDB";

	@SuppressWarnings("unused")
	private static final String TEST_URL = "http://ruzenafit.appspot.com/rest/workout/test";
	@SuppressWarnings("unused")
	private static final String RETRIEVE_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/workout/getAllWorkouts";
	private static final String SAVE_ALL_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/workout/saveWorkoutTicks";

	private static String deviceID = "";

	/** App-wide context (used for toasts) */
	private Context context;
	private SharedPreferencesHelper sharedPreferencesHelper;
	
	private int ticksSinceLastSuccessfulUpload;

	/**
	 * Sole constructor -- must give this class the app's context
	 * 
	 * @param context
	 */
	public GoogleAppEngineHelper(Context context) {
		this.context = context;
		
		sharedPreferencesHelper = new SharedPreferencesHelper(context);

		refreshValues();
	}
	
	private void refreshValues() {
		// Find out how many unsuccessful GAE attempts we've had so far
		ticksSinceLastSuccessfulUpload = sharedPreferencesHelper.getTicksSinceLastSuccessfulUpload();
	}

	/**
	 * If we have enough {@link WorkoutTick}s to send, then start attempting to
	 * silently send this data up to GAE.
	 * 
	 * This uses <a
	 * href="http://en.wikipedia.org/wiki/Exponential_backoff">exponential
	 * backoff</a>.
	 * 
	 * @param workoutTicks
	 */
	public void checkBatchSizeAndSendDataToGAE(WorkoutTick[] workoutTicks,
			Context context) {

		// FIXME: Change this to add batch size for every 100, 200, 300, etc.
		// If we don't have enough workout ticks to call this a "batch," then
		// forget about it.
		if (workoutTicks.length < Constants.BATCH_SIZE) {
			Log.d(TAG, "Not enough workouts to constitute a batch -- did NOT upload data to server");
			return;
		}

		ticksSinceLastSuccessfulUpload = context.getSharedPreferences(Constants.PREFS_NAMESPACE, 0).
				getInt(Constants.TICKS_SINCE_LAST_SUCCESSFUL_UPLOAD, -1);

		// If this is the first ever GAE attempt, then reset the unsuccessful
		// attempts to 0
		if (ticksSinceLastSuccessfulUpload == -1) {
			Log.d(TAG,
					"First ever GAE attempt -- setting ticksSinceLastSuccessfulUpload to 0");
			SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREFS_NAMESPACE, 0).edit();
			editor.putInt(Constants.TICKS_SINCE_LAST_SUCCESSFUL_UPLOAD, 0);
		}

		// FIXME: Put this back in when done debugging network code.
		// // If this is NOT the first tick since the last successful upload,
		// then only send data
		// // once every 2^n ticks (see "Exponential Backoff").
		// if (!isPowerOfTwo(ticksSinceLastSuccessfulUpload)) {
		// Log.d(TAG, "Will not upload data on the #" +
		// ticksSinceLastSuccessfulUpload + " upload after fail");
		// editor.putInt(Constants.TICKS_SINCE_LAST_SUCCESSFUL_UPLOAD,
		// ++ticksSinceLastSuccessfulUpload);
		// editor.commit();
		// return;
		// }

		// Attempt to upload data to GAE
		submitDataToGAE(workoutTicks, context);
	}

	/**
	 * Submits data to Google App Engine (Girum's account, as set by URL).
	 * Returns the number of successful GAE submissions.
	 */
	@SuppressWarnings("unchecked")
	private void submitDataToGAE(WorkoutTick[] allWorkouts, Context context) {

		// Make a JSON array of "workout ticks."
		JSONArray workoutsJSONArray = new JSONArray();

		for (WorkoutTick workout : allWorkouts) {
			// Throw this JSONObject into the whole JSONArray.
			workoutsJSONArray.put(workout.toJSON());
		}

		Log.d(TAG, "Asynchronously submitting the following JSON string to server: "
						+ workoutsJSONArray.toString());

		// TODO: Send this array to GAE through the network layer.
		// Throw all of the fields of the workout into a list of nameValuePairs
		// for
		// the POST request to use.
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		// Retrieve the phone's IMEI from SharedPreferences TODO: Make this
		// helper an object and save this SharedPrefs data.
		SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(
				context);
		String imei = sharedPreferencesHelper
				.retrieveValueForString(WorkoutTick.KEY_IMEI);
		if (imei == null) {
			Log.e(TAG, "IMEI not set.");
		}

		// Throw the IMEI and the JSONArray into the POST request.
		nameValuePairs.add(new BasicNameValuePair(WorkoutTick.KEY_IMEI, imei));
		nameValuePairs.add(new BasicNameValuePair(
				Constants.WORKOUTS_JSON_STRING, workoutsJSONArray.toString()));

		// Execute the POST request asynchronously
		new GAEAsyncTaskRunner().execute(nameValuePairs);
	}

	/**
	 * Private inner class that runs the Networking asynchronously.
	 * 
	 * @author gibssa
	 */
	private class GAEAsyncTaskRunner extends
			AsyncTask<List<NameValuePair>, Void, String> {

		@Override
		protected String doInBackground(List<NameValuePair>... nameValuePairs) {
			// Setup the POST Request
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost request = new HttpPost(SAVE_ALL_WORKOUTS_URL);
			request.addHeader("deviceID:", deviceID);

			String responseString = null;

			// Execute the POST request.
			try {
				request.setEntity(new UrlEncodedFormEntity(nameValuePairs[0]));

				HttpResponse response = httpClient.execute(request);
				responseString = EntityUtils.toString(response.getEntity());
				Log.d(TAG, "HttpResponse: " + responseString);
			} catch (Exception e) {
				Log.e(TAG, "HTTP ERROR: " + e.getMessage());
				return Constants.HTTP_ERROR + ": " + e.getMessage();
			}

			return responseString;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (result.contains(Constants.HTTP_ERROR)) {
				Toast.makeText(context, result, Toast.LENGTH_LONG).show();
				sharedPreferencesHelper.setTicksSinceLastSuccessfulUpload(
						sharedPreferencesHelper.getTicksSinceLastSuccessfulUpload()+1);
				return;
			}

			// This should only "succeed" after we check the response
			// string to be OK.
			if (!result.contains("Saved")) {
				Toast.makeText(
						context,
						"Server-side error -- GAE's quota was probably exceeded",
						Toast.LENGTH_LONG).show();
				return;
			}

			Toast.makeText(context, "Successfully uploaded data to server.",
					Toast.LENGTH_LONG).show();
			
			// Update the totalTicksSent to reflect that we've succeeded
			SharedPreferences preferences = context.getSharedPreferences(Constants.PREFS_NAMESPACE, 0);
			int totalTicksSent = preferences.getInt(Constants.TOTAL_TICKS_SENT, -1);
			
			// Calculate new totalTicksSent
			int newTotalTickSent = (totalTicksSent == -1 ? 0 : totalTicksSent) + Constants.BATCH_SIZE;
			
			// Set new totalTicksSent
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt(Constants.TOTAL_TICKS_SENT, newTotalTickSent);
			editor.commit();
			
			Log.d(TAG, "Updated totalTicksSent to " + newTotalTickSent);
		}

	}

	/**
	 * Tiny, fast helper function to determine if a number is a power of two.
	 * 
	 * @param n
	 * @return
	 */
	private static boolean isPowerOfTwo(int n) {
		return ((n != 0) && (n & (n - 1)) == 0);
	}

}
