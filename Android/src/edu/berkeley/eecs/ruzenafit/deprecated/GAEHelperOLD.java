package edu.berkeley.eecs.ruzenafit.deprecated;

public class GAEHelperOLD {
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
//		// DON'T do this the terrible way of using a regular POST request.  Instead, serialize it
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
