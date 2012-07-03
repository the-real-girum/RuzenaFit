package edu.berkeley.eecs.ruzenafit.access;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nu.xom.Element;

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
import edu.berkeley.eecs.ruzenafit.model.CoordinateDataPoint;
import edu.berkeley.eecs.ruzenafit.model.WorkoutPoint;
import edu.berkeley.eecs.ruzenafit.util.Constants;


/**
 * Access layer to abstract away connections to GAE -- Google App Engine.
 * 
 * @author gibssa
 *
 */
public class ExternalDBHelper {
	private static final String TAG = "ExternalDB";
	
	@SuppressWarnings("unused")
	private static final String TEST_URL = "http://ruzenafit.appspot.com/rest/workout/test";
	private static final String RETRIEVE_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/workout/getAllWorkouts"; 
	private static final String SAVE_WORKOUT_URL = "http://ruzenafit.appspot.com/rest/workout/saveWorkout";
//	private static final String SAVE_ALL_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/workout/saveAllWorkouts";
	
	
	private static String deviceID = "";
	
	/**
	 * Submits data to Google App Engine (Girum's account, as set by URL).
	 * Returns the number of successful GAE submissions.
	 */
	public static int submitDataToGAE(WorkoutPoint[] allWorkouts, String userEmail, String privacySetting) {
		
		int successfulSubmissions = 0;
		for (WorkoutPoint workout : allWorkouts) {
			if (submitOneWorkout(workout, userEmail, privacySetting))
				successfulSubmissions++;
		}
		
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
	private static boolean submitOneWorkout(WorkoutPoint workout, String userEmail, String privacySetting) {
		
		if (workout == null)
			return false;
		
		// Throw all of the fields of the workout into a list of nameValuePairs for
		// the POST request to use.
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		Log.d(TAG, "Workout: " + workout);
		
		// TODO: Change all of these to string literals.
		nameValuePairs.add(new BasicNameValuePair("user", 						userEmail));
		nameValuePairs.add(new BasicNameValuePair(Constants.PRIVACY_SETTING, 	privacySetting));
		nameValuePairs.add(new BasicNameValuePair("date", 						workout.getDate()));
		nameValuePairs.add(new BasicNameValuePair("duration", 					workout.getDuration()));
		nameValuePairs.add(new BasicNameValuePair("averageSpeed", 				workout.getAverageSpeed()));
		nameValuePairs.add(new BasicNameValuePair("totalCalories", 				workout.getTotalCalories()));
		nameValuePairs.add(new BasicNameValuePair("totalDistance", 				workout.getTotalDistance()));
		nameValuePairs.add(new BasicNameValuePair("coordinatesXMLString", 
													convertGeoPointsToXMLString(workout.getGeopoints())));
		Log.d(TAG, "coordinatesXMLString: " + convertGeoPointsToXMLString(workout.getGeopoints()));
		
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
	 * Use the XML library "XOM" to setup the
	 * <coordinates></coordinates> XML section.<br /><br />
	 * 
	 * * It should look like the following:
	 * 
	 * <coordinates>
	 *    <coordinate>
	 *       <longitude>123</longitude>
	 *       <latitude>123</latitude>
	 *       <timestamp>sometimestamp</timestamp>
	 *    </coordinate>
	 *    <coordinate>
	 *       <longitude>456</longitude>
	 *       <latitude>456</latitude>
	 *       <timestamp>sometimestamp2</timestamp>
	 *    </coordinate>
	 * </coordinates>
	 * 
	 * @param geopoints
	 * @return
	 */
	private static String convertGeoPointsToXMLString(CoordinateDataPoint[] geopoints) {
		
		// Declare the root element of this XML array
		Element coordinates = new Element("coordinates");
		
		// Fill up the coordinates XML element
		for (CoordinateDataPoint geopoint : geopoints) {
			
			// <coordinates> should have several <coordinate> elements in it. 
			Element coordinate = new Element("coordinate");
			
			// Each <coordinate> needs a latitude
			Element latitude = new Element("latitude");
			latitude.appendChild("" + geopoint.getGeopoint().getLatitudeE6());
			Log.d(TAG, "Workout latitude: " + 	geopoint.getGeopoint().getLatitudeE6());
			coordinate.appendChild(latitude);
			
			// ... and a longitude
			Element longitude = new Element("longitude");
			longitude.appendChild("" + geopoint.getGeopoint().getLongitudeE6());
			Log.d(TAG, "Workout longitude: " + 	geopoint.getGeopoint().getLongitudeE6());
			coordinate.appendChild(longitude);
			
			// ... and the kcals burned for this particular point
			Element kCals = new Element("kCals");
			kCals.appendChild("" + geopoint.getkCals());
			Log.d(TAG, "KCals burned at this point: " + geopoint.getkCals());
			coordinate.appendChild(kCals);
			
			// ... and a timestamp
			Element timestamp = new Element("timestamp");
			timestamp.appendChild(geopoint.getDate());
			Log.d(TAG, "Workout date: " + 		geopoint.getDate());
			coordinate.appendChild(timestamp);
			
			// Now add this particular <coordinate> to the <coordinates> root element
			coordinates.appendChild(coordinate);
		}
		
		// Return the string of the completed XML.
		return coordinates.toXML();
	}
	
	
	/**
	 * Retrieves data from Google App Engine (Girum's account, as set by URL).
	 * Returns null if there is a error in the data retrieval.
	 */
	public static WorkoutPoint[] retrieveDataFromGAE(String userEmail){
		Log.d(TAG, "retrieveDataFromGAE with userEmail: " + userEmail);
		WorkoutPoint[] allWorkouts = null;
		
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
			allWorkouts = new WorkoutPoint[workouts.getLength()];
			
			// For each of the workouts...
			for (int i = 0; i < workouts.getLength(); i++) {
				
				NodeList workout = workouts.item(i).getChildNodes();
				Log.d(TAG, "WORKOUT: ");
				allWorkouts[i] = new WorkoutPoint();
				
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
