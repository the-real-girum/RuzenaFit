package edu.berkeley.eecs.ruzenafit.access;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
	private static final String URL = "http://ruzenafit.appspot.com/rest/test";
//	private static final String URL = "http://localhost:8888/rest/hello";
	private static String deviceID = "";
	
	/**
	 * Submits data to Google App Engine (Girum's account, as set by URL).
	 * Returns false if there is a GAE connection error.
	 */
	public static boolean submitDataToGAE(AnActualWorkoutModelX_X[] allWorkouts) {
		Log.d(TAG, "submitDataToGAE: " + allWorkouts.toString());
		
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
			
//			Log.d(TAG, "Hello world string: " + result.getDocumentElement().getFirstChild().getNodeValue());
			NodeList workouts = result.getDocumentElement().getChildNodes();
			
			// For each of the workouts...
			for (int i = 0; i < workouts.getLength(); i++) {
				
				NodeList workout = workouts.item(i).getChildNodes();
				Log.d(TAG, "WORKOUT: ");
				
				// For each of this workout's XML children...
				for (int j = 0; j < workout.getLength(); j++) {
					Node workoutElement = workout.item(j);
					
					Log.d(TAG, "field " + workoutElement.getNodeName() + ": " +
							workoutElement.getFirstChild().getNodeValue());
				}
				
			}
			
//			Log.d(TAG, "Workouts: " + )
			
		}
		catch (Exception e) {
			Log.e(TAG, "Exception occurred: " + e.getMessage());
			e.printStackTrace();
		} 
		
		return allWorkouts;
	}
	
	
	
	
}
