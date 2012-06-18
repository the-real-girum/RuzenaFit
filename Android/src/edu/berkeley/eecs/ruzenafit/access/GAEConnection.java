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
public class GAEConnection {
	private static final String TAG = "GAEConnection";
	
//	/** The username for the GAE account */
//	private static final String username = "ibssagirum";
	private static final String URL = "http://ruzenafit.appspot.com/rest/workout/test";
//	private static final String URL = "http://localhost:8888/rest/hello";
	private static String deviceID = "";
	
	/**
	 * Submits data to Google App Engine (Girum's account, as set by URL).
	 * Returns false if there is a GAE connection error.
	 */
	public static boolean submitDataToGAE(AnActualWorkoutModelX_X[] allWorkouts) {
		Log.d(TAG, "submitDataToGAE: " + allWorkouts.toString());
		
		// Throw all of the fields of the workout into a list of nameValuePairs for
		// the POST request to use.
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		
//		AnActualWorkoutModelX_X workout = allWorkouts[0];
		
		// FIXME: I HATE HACKSSSSS.  String concatenation of array index here
		for (int i = 0; i < allWorkouts.length; i++) {
			nameValuePairs.add(new BasicNameValuePair("date" + "["+i+"]", 			allWorkouts[i].getDate()));
			nameValuePairs.add(new BasicNameValuePair("duration" + "["+i+"]", 		allWorkouts[i].getDuration()));
			nameValuePairs.add(new BasicNameValuePair("averageSpeed" + "["+i+"]", 	allWorkouts[i].getAverageSpeed()));
			nameValuePairs.add(new BasicNameValuePair("totalCalories" + "["+i+"]", 	allWorkouts[i].getTotalCalories()));
			nameValuePairs.add(new BasicNameValuePair("totalDistance" + "["+i+"]", 	allWorkouts[i].getTotalDistance()));
		}
		
		
		// Setup the POST Request
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost request = new HttpPost("http://ruzenafit.appspot.com/rest/workout/saveWorkout");
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
	public static AnActualWorkoutModelX_X[] retrieveDataFromGAE(){
		Log.d(TAG, "retrieveDataFromGAE");
		AnActualWorkoutModelX_X[] allWorkouts = null;
		
		// Setup HTTP GET request.
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(URL);
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
