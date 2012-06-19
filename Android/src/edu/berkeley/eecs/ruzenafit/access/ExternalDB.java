package edu.berkeley.eecs.ruzenafit.access;

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

import android.util.Log;
import edu.berkeley.eecs.ruzenafit.model.AnActualWorkoutModelX_X;


/**
 * Access layer to abstract away connections to GAE -- Google App Engine.
 * 
 * @author gibssa
 *
 */
public class ExternalDB {
	private static final String TAG = "ExternalDB";
	
	@SuppressWarnings("unused")
	private static final String TEST_URL = "http://ruzenafit.appspot.com/rest/workout/test";
	private static final String RETRIEVE_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/workout/getAllWorkouts"; 
	private static final String SAVE_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/workout/saveWorkout";
	
	private static String deviceID = "";
	
	/**
	 * Submits data to Google App Engine (Girum's account, as set by URL).
	 * Returns false if there is a GAE connection error.
	 */
	public static int submitDataToGAE(AnActualWorkoutModelX_X[] allWorkouts, String userEmail) {
		Log.d(TAG, "submitDataToGAE: " + allWorkouts.toString());
		
		int successfulSubmissions = 0;
		for (AnActualWorkoutModelX_X workout : allWorkouts) {
			if (submitOneWorkout(workout, userEmail))
				successfulSubmissions++;
		}
		
		return successfulSubmissions;
	}
	
	private static boolean submitOneWorkout(AnActualWorkoutModelX_X workout, String userEmail) {
		// Throw all of the fields of the workout into a list of nameValuePairs for
		// the POST request to use.
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		Log.d(TAG, "Workout: " + workout);
		
		nameValuePairs.add(new BasicNameValuePair("user", 			userEmail));
		nameValuePairs.add(new BasicNameValuePair("date", 			workout.getDate()));
		nameValuePairs.add(new BasicNameValuePair("duration", 		workout.getDuration()));
		nameValuePairs.add(new BasicNameValuePair("averageSpeed", 	workout.getAverageSpeed()));
		nameValuePairs.add(new BasicNameValuePair("totalCalories", 	workout.getTotalCalories()));
		nameValuePairs.add(new BasicNameValuePair("totalDistance", 	workout.getTotalDistance()));
		
		// Setup the POST Request
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost request = new HttpPost(SAVE_WORKOUTS_URL);
		request.addHeader("deviceID:" , deviceID);
		
		// Execute the POST request.
		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = httpClient.execute(request);
			Log.d(TAG, "HttpResponse: " + EntityUtils.toString(response.getEntity()));
			
		} catch (Exception e) {
			Log.e(TAG, "ERROR: " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * Retrieves data from Google App Engine (Girum's account, as set by URL).
	 * Returns null if there is a error in the data retrieval.
	 */
	public static AnActualWorkoutModelX_X[] retrieveDataFromGAE(String userEmail){
		Log.d(TAG, "retrieveDataFromGAE with userEmail: " + userEmail);
		AnActualWorkoutModelX_X[] allWorkouts = null;
		
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
			allWorkouts = new AnActualWorkoutModelX_X[workouts.getLength()];
			
			// For each of the workouts...
			for (int i = 0; i < workouts.getLength(); i++) {
				
				NodeList workout = workouts.item(i).getChildNodes();
				Log.d(TAG, "WORKOUT: ");
				allWorkouts[i] = new AnActualWorkoutModelX_X();
				
				// For each of this workout's XML children...
				for (int j = 0; j < workout.getLength(); j++) {
					Node workoutElement = workout.item(j);
					
					Log.d(TAG, "field " + workoutElement.getNodeName() + ": " +
							workoutElement.getFirstChild().getNodeValue());
					if (workoutElement.getNodeName().equals("averageSpeed")) {
						allWorkouts[i].setAverageSpeed(workoutElement.getFirstChild().getNodeValue());
					}
					else if (workoutElement.getNodeName().equals("date")) {
						allWorkouts[i].setDate(workoutElement.getFirstChild().getNodeValue());
					}
					else if (workoutElement.getNodeName().equals("duration")) {
						allWorkouts[i].setDuration(workoutElement.getFirstChild().getNodeValue());
					}
					else if (workoutElement.getNodeName().equals("totalCalories")) {
						allWorkouts[i].setTotalCalories(workoutElement.getFirstChild().getNodeValue());
					}
					else if (workoutElement.getNodeName().equals("totalDistance")) {
						allWorkouts[i].setTotalDistance(workoutElement.getFirstChild().getNodeValue());
					}
				}
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Exception occurred: " + e.getMessage());
			e.printStackTrace();
		}
		
		return allWorkouts;
	}
	
	
	
	
}
