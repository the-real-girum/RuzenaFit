package edu.berkeley.eecs.ruzenafit.network;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
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
	private static final String SAVE_WORKOUT_URL = "http://ruzenafit.appspot.com/rest/workout/saveWorkout";
//	private static final String SAVE_ALL_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/workout/saveAllWorkouts";
	
	
	private static String deviceID = "";
	
	// TODO: Change this to be one POST request, as opposed to one POST request for each workout.
	/**
	 * Submits data to Google App Engine (Girum's account, as set by URL).
	 * Returns the number of successful GAE submissions.
	 */
	public static int submitDataToGAE(WorkoutTick[] allWorkouts, String userEmail, String privacySetting, Context context) {
		
		int successfulSubmissions = 0;
		for (WorkoutTick workout : allWorkouts) {
			if (submitOneWorkout(workout, userEmail, privacySetting))
				successfulSubmissions++;
		}
		
		Toast.makeText(context, "Successfully submitted " + successfulSubmissions + " workout \"ticks\"", 3).show();
		
		return successfulSubmissions;
	}
	
	/**
	 * @deprecated -- Should send a single XML string containing all of the workouts.
	 * 
	 * @param workout
	 * @param userEmail
	 * @param privacySetting
	 * @return
	 */
	private static boolean submitOneWorkout(WorkoutTick workout, String userEmail, String privacySetting) {
		
		if (workout == null)
			return false;
		
		// Throw all of the fields of the workout into a list of nameValuePairs for
		// the POST request to use.
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		Log.d(TAG, "Workout: " + workout);
		
		// TODO: Fix the REST API so that it reflects our new "tick" model.
		// TODO: Change all of these to NOT use string literals.
		nameValuePairs.add(new BasicNameValuePair("user", 						userEmail));
		nameValuePairs.add(new BasicNameValuePair(Constants.PRIVACY_SETTING, 	privacySetting));
		nameValuePairs.add(new BasicNameValuePair("date", 						workout.getSystemTime()));
		// TODO: Yank duration out of the REST API.
		nameValuePairs.add(new BasicNameValuePair("duration", 					"someDuration"));
		nameValuePairs.add(new BasicNameValuePair("averageSpeed", 				"" + workout.getSpeed()));
		nameValuePairs.add(new BasicNameValuePair("totalCalories", 				"" + workout.getkCal()));
		// TODO: Yank distance out of the REST API.
		nameValuePairs.add(new BasicNameValuePair("totalDistance", 				"someDistance"));
		// TODO: Change this to just be longitude/latitude, since each "workout" is now just a "tick."
		nameValuePairs.add(new BasicNameValuePair("coordinatesXMLString", 		"someCoordinatesString, should be latitude/longitude now"));
		
		// Setup the POST Request
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost request = new HttpPost(SAVE_WORKOUT_URL);
		request.addHeader("deviceID:" , deviceID);
		
		// Execute the POST request.
		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = httpClient.execute(request);
			String responseString = EntityUtils.toString(response.getEntity());
			Log.d(TAG, "HttpResponse: " + responseString);
			return responseString.equals("success");
		} catch (Exception e) {
			Log.e(TAG, "ERROR: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Retrieves data from Google App Engine (Girum's account, as set by URL).
	 * Returns null if there is a error in the data retrieval.
	 */
	public static WorkoutTick[] retrieveDataFromGAE(String userEmail, Context context){
		Log.d(TAG, "retrieveDataFromGAE with userEmail: " + userEmail);
		WorkoutTick[] allWorkouts = null;
		
		// Setup HTTP GET request.
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(RETRIEVE_WORKOUTS_URL + "?userName=" + userEmail);
		
		request.addHeader("deviceID:", deviceID);
		
		try {
			// Make the HTTP GET request.
			HttpResponse response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			
			// Parse the resulting XML
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document result = documentBuilder.parse(entity.getContent());
			
			NodeList workouts = result.getDocumentElement().getChildNodes();
			allWorkouts = new WorkoutTick[workouts.getLength()];
			
			// For each of the workouts...
			for (int i = 0; i < workouts.getLength(); i++) {
				
				NodeList workout = workouts.item(i).getChildNodes();
				Log.d(TAG, "WORKOUT: ");
//				allWorkouts[i] = new WorkoutTick();
				
				// For each of this workout's XML children...
				for (int j = 0; j < workout.getLength(); j++) {
					Node workoutElement = workout.item(j);
					
					Log.d(TAG, "field " + workoutElement.getNodeName() + ": " +
							workoutElement.getFirstChild().getNodeValue());
					if (workoutElement.getNodeName().equals("averageSpeed")) {
						Log.d(TAG, "averageSpeed: " + workoutElement.getFirstChild().getNodeValue());
//						allWorkouts[i].setAverageSpeed(workoutElement.getFirstChild().getNodeValue());
					}
					else if (workoutElement.getNodeName().equals("date")) {
						Log.d(TAG, "date: " + workoutElement.getFirstChild().getNodeValue());
//						allWorkouts[i].setDate(workoutElement.getFirstChild().getNodeValue());
					}
					else if (workoutElement.getNodeName().equals("duration")) {
						Log.d(TAG, "duration: " + workoutElement.getFirstChild().getNodeValue());
//						allWorkouts[i].setDuration(workoutElement.getFirstChild().getNodeValue());
					}
					else if (workoutElement.getNodeName().equals("totalCalories")) {
						Log.d(TAG, "totalCalories: " + workoutElement.getFirstChild().getNodeValue());
//						allWorkouts[i].setTotalCalories(workoutElement.getFirstChild().getNodeValue());
					}
					else if (workoutElement.getNodeName().equals("totalDistance")) {
						Log.d(TAG, "totalDistance: " + workoutElement.getFirstChild().getNodeValue());
//						allWorkouts[i].setTotalDistance(workoutElement.getFirstChild().getNodeValue());
					}
				}
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Exception occurred: " + e.getMessage());
			e.printStackTrace();
		}
		
		Toast.makeText(context, "Retrieved " + allWorkouts.length + " workout \"ticks\" from GAE.", 3).show();
		
		return allWorkouts;
	}
	
}
