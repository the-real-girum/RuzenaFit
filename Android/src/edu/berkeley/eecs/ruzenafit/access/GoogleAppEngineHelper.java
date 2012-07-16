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
import android.preference.PreferenceManager;
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
	private static final String RETRIEVE_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/workout/getAllWorkouts"; 
	private static final String SAVE_ALL_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/workout/saveWorkoutTicks";
	
	private static String deviceID = "";
	
	// FIXME: Asynchronize this whole process.
	/**
	 *  If we have enough {@link WorkoutTick}s to send, then start attempting
	 *  to silently send this data up to GAE.
	 * 
	 *  This uses <a href="http://en.wikipedia.org/wiki/Exponential_backoff">exponential backoff</a>.
	 * @param workoutTicks
	 */
	public static void checkBatchSizeAndSendDataToGAE(WorkoutTick[] workoutTicks, Context context) {
		
		// If we don't have enough workout ticks to call this a "batch," then forget about it.
		if (workoutTicks.length < Constants.BATCH_SIZE) {
			return;
		}
		
		// Load up the SharedPreferences for this app
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		
		// Find out how many unsuccessful GAE attempts we've had so far 
		int ticksSinceLastSuccessfulUpload = preferences.getInt(Constants.TICKS_SINCE_LAST_SUCCESSFUL_UPLOAD, -1);
		
		// If this is the first ever GAE attempt, then reset the unsuccessful attempts to 0
		if (ticksSinceLastSuccessfulUpload == -1) {
			Log.d(TAG, "First ever GAE attempt -- setting ticksSinceLastSuccessfulUpload to 0");
			
			ticksSinceLastSuccessfulUpload = 0;
			editor.putInt(Constants.TICKS_SINCE_LAST_SUCCESSFUL_UPLOAD, ticksSinceLastSuccessfulUpload);
			editor.commit();
		}
		
		// FIXME: Put this back in when done debugging network code.
//		// If this is NOT the first tick since the last successful upload, then only send data
//		// once every 2^n ticks (see "Exponential Backoff").
//		if (!isPowerOfTwo(ticksSinceLastSuccessfulUpload)) {
//			Log.d(TAG, "Will not upload data on the #" + ticksSinceLastSuccessfulUpload + " upload after fail");
//			editor.putInt(Constants.TICKS_SINCE_LAST_SUCCESSFUL_UPLOAD, ++ticksSinceLastSuccessfulUpload);
//			editor.commit();
//			return;
//		}
		
		// Attempt to upload data to GAE
		int result = submitDataToGAE(workoutTicks, context);
		
		if (result != -1) {
			// Let the user know of the success
			Log.d(TAG, "Successfully uploaded workout data to GAE");
			Toast.makeText(context,
					"Successfully uploaded workout data to server.",
					Toast.LENGTH_SHORT).show();
			
			// Reset the ticks since last success back to 0, since we just had a success.
			editor.putInt(Constants.TICKS_SINCE_LAST_SUCCESSFUL_UPLOAD, 0);
			editor.commit();
			
			// TODO: Delete old data from File.
		}
		else {
			// Let the user know of the failure
			Log.e(TAG, "Error on uploading data to GAE");
			Toast.makeText(context, 
					"Couldn't upload workout data to server.  Check your Internet connection.", 
					Toast.LENGTH_SHORT).show();
			
			// Add 1 to "ticks since last successful," since we just had a tick that did NOT successfully upload.
			editor.putInt(Constants.TICKS_SINCE_LAST_SUCCESSFUL_UPLOAD, ++ticksSinceLastSuccessfulUpload);
			editor.commit();
		}
		
	}
	
	/**
	 * Tiny, fast helper function to determine if a number is a power of two.
	 * @param n
	 * @return
	 */
	private static boolean isPowerOfTwo(int n) {
		return ((n!=0) && (n&(n-1))==0);
	}	

	/**
	 * Submits data to Google App Engine (Girum's account, as set by URL).
	 * Returns the number of successful GAE submissions.
	 */
	private static int submitDataToGAE(WorkoutTick[] allWorkouts, Context context) {
		
		// Make a JSON array of "workout ticks."
		JSONArray workoutsJSONArray = new JSONArray();
		
		for (WorkoutTick workout : allWorkouts) {
			// Throw this JSONObject into the whole JSONArray.
			workoutsJSONArray.put(workout.toJSON());
		}
		
		Log.d(TAG, "Submitting the following JSON string: " + workoutsJSONArray.toString());
		
		// TODO: Send this array to GAE through the network layer.
		// Throw all of the fields of the workout into a list of nameValuePairs for
		// the POST request to use.
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
		// Retrieve the phone's IMEI from SharedPreferences  TODO: Make this helper an object and save this SharedPrefs data.
		SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(context);
		String imei = sharedPreferencesHelper.retrieveValueForString(WorkoutTick.KEY_IMEI);
		if (imei == null) {
			Log.e(TAG, "IMEI not set.");
			return -1;
		}
		
		// Throw the IMEI and the JSONArray into the POST request.
		nameValuePairs.add(new BasicNameValuePair(WorkoutTick.KEY_IMEI, imei));
		nameValuePairs.add(new BasicNameValuePair(Constants.WORKOUTS_JSON_STRING, workoutsJSONArray.toString()));
		
		// Execute the POST request
		int successfulSubmissions = executePOSTRequest(nameValuePairs);
		
		return successfulSubmissions;
	}
	
	/**
	 * Executes the POST request.
	 * 
	 * @param nameValuePairs
	 * @return The number of new workout ticks that were successfully added.
	 */
	private static int executePOSTRequest(List<NameValuePair> nameValuePairs) {
		
		// Setup the POST Request
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost request = new HttpPost(SAVE_ALL_WORKOUTS_URL);
		request.addHeader("deviceID:" , deviceID);
		
		// Execute the POST request.
		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = httpClient.execute(request);
			String responseString = EntityUtils.toString(response.getEntity());
			Log.d(TAG, "HttpResponse: " + responseString);
		} catch (Exception e) {
			Log.e(TAG, "ERROR: " + e.getMessage());
			return -1;
		}
		return 1;
	}
	
//	/**
//	 * @deprecated -- Should send a single XML string containing all of the workouts.
//	 * 
//	 * @param workout
//	 * @param userEmail
//	 * @param privacySetting
//	 * @return
//	 */
//	private static boolean submitOneWorkout(WorkoutTick workout, String userEmail, String privacySetting) {
//		
//		if (workout == null)
//			return false;
//		
//		// Throw all of the fields of the workout into a list of nameValuePairs for
//		// the POST request to use.
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//		Log.d(TAG, "Workout: " + workout);
//		
//		// TODO: DON'T do this the terrible way of using a regular POST request.  Instead, serialize it
//		// as JSON and send ALL of the workouts at once to the server.  Do serverside JSON logic as well.
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_IMEI, 			workout.getImei()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_KCALS, 			"" + workout.getkCal()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_SYSTEM_TIME, 	"" + workout.getSystemTime()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_LATITUDE, 		"" + workout.getLatitude()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_LONGITUDE, 		"" + workout.getLongitude()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_SPEED, 			"" + workout.getSpeed()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_ALTITUDE, 		"" + workout.getAltitude()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_HAS_ACCURACY,	"" + workout.getHasAccuracy()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_ACCURACY, 		"" + workout.getAccuracy()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_ACCUM_MINUTE_V, "" + workout.getAccumMinuteV()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_ACCUM_MINUTE_H, "" + workout.getAccumMinuteH()));
//		nameValuePairs.add(new BasicNameValuePair(Constants.KEY_GPS_TIME, 		"" + workout.getTime()));
//		
//		/** START old rest api */
////		nameValuePairs.add(new BasicNameValuePair("user", 						userEmail));
////		nameValuePairs.add(new BasicNameValuePair(Constants.PRIVACY_SETTING, 	privacySetting));
////		nameValuePairs.add(new BasicNameValuePair("date", 						workout.getSystemTime()));
////		nameValuePairs.add(new BasicNameValuePair("duration", 					"someDuration"));
////		nameValuePairs.add(new BasicNameValuePair("averageSpeed", 				"" + workout.getSpeed()));
////		nameValuePairs.add(new BasicNameValuePair("totalCalories", 				"" + workout.getkCal()));
////		nameValuePairs.add(new BasicNameValuePair("totalDistance", 				"someDistance"));
////		nameValuePairs.add(new BasicNameValuePair("coordinatesXMLString", 		"someCoordinatesString, should be latitude/longitude now"));
//		/** END old rest api */
//		
//		// Setup the POST Request
//		HttpClient httpClient = new DefaultHttpClient();
//		HttpPost request = new HttpPost(SAVE_WORKOUT_URL);
//		request.addHeader("deviceID:" , deviceID);
//		
//		// Execute the POST request.
//		try {
//			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//			
//			HttpResponse response = httpClient.execute(request);
//			String responseString = EntityUtils.toString(response.getEntity());
//			Log.d(TAG, "HttpResponse: " + responseString);
//			return responseString.equals("success");
//		} catch (Exception e) {
//			Log.e(TAG, "ERROR: " + e.getMessage());
//			return false;
//		}
//	}
	
//	/**
//	 * Retrieves data from Google App Engine (Girum's account, as set by URL).
//	 * Returns null if there is a error in the data retrieval.
//	 */
//	public static WorkoutTick[] retrieveDataFromGAE(String userEmail, Context context){
//		Log.d(TAG, "retrieveDataFromGAE with userEmail: " + userEmail);
//		WorkoutTick[] allWorkouts = null;
//		
//		// Setup HTTP GET request.
//		HttpClient httpClient = new DefaultHttpClient();
//		HttpGet request = new HttpGet(RETRIEVE_WORKOUTS_URL + "?userName=" + userEmail);
//		
//		request.addHeader("deviceID:", deviceID);
//		
//		try {
//			// Make the HTTP GET request.
//			HttpResponse response = httpClient.execute(request);
//			HttpEntity entity = response.getEntity();
//			
//			// Parse the resulting XML
//			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			Document result = documentBuilder.parse(entity.getContent());
//			
//			NodeList workouts = result.getDocumentElement().getChildNodes();
//			allWorkouts = new WorkoutTick[workouts.getLength()];
//			
//			// For each of the workouts...
//			for (int i = 0; i < workouts.getLength(); i++) {
//				
//				NodeList workout = workouts.item(i).getChildNodes();
//				Log.d(TAG, "WORKOUT: ");
////				allWorkouts[i] = new WorkoutTick();
//				
//				// For each of this workout's XML children...
//				for (int j = 0; j < workout.getLength(); j++) {
//					Node workoutElement = workout.item(j);
//					
//					Log.d(TAG, "field " + workoutElement.getNodeName() + ": " +
//							workoutElement.getFirstChild().getNodeValue());
//					if (workoutElement.getNodeName().equals("averageSpeed")) {
//						Log.d(TAG, "averageSpeed: " + workoutElement.getFirstChild().getNodeValue());
////						allWorkouts[i].setAverageSpeed(workoutElement.getFirstChild().getNodeValue());
//					}
//					else if (workoutElement.getNodeName().equals("date")) {
//						Log.d(TAG, "date: " + workoutElement.getFirstChild().getNodeValue());
////						allWorkouts[i].setDate(workoutElement.getFirstChild().getNodeValue());
//					}
//					else if (workoutElement.getNodeName().equals("duration")) {
//						Log.d(TAG, "duration: " + workoutElement.getFirstChild().getNodeValue());
////						allWorkouts[i].setDuration(workoutElement.getFirstChild().getNodeValue());
//					}
//					else if (workoutElement.getNodeName().equals("totalCalories")) {
//						Log.d(TAG, "totalCalories: " + workoutElement.getFirstChild().getNodeValue());
////						allWorkouts[i].setTotalCalories(workoutElement.getFirstChild().getNodeValue());
//					}
//					else if (workoutElement.getNodeName().equals("totalDistance")) {
//						Log.d(TAG, "totalDistance: " + workoutElement.getFirstChild().getNodeValue());
////						allWorkouts[i].setTotalDistance(workoutElement.getFirstChild().getNodeValue());
//					}
//				}
//			}
//		}
//		catch (Exception e) {
//			Log.e(TAG, "Exception occurred: " + e.getMessage());
//			e.printStackTrace();
//		}
//		
//		Toast.makeText(context, "Retrieved " + allWorkouts.length + " workout \"ticks\" from GAE.", 3).show();
//		
//		return allWorkouts;
//	}
	
}
