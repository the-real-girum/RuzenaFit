package edu.berkeley.eecs.ruzenafit.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.berkeley.eecs.ruzenafit.shared.model.PrivacyPreferenceEnum;
import edu.berkeley.eecs.ruzenafit.shared.model.UserRanking;
import edu.berkeley.eecs.ruzenafit.shared.model.WorkoutTick;

@Path("/workout")
public class WorkoutServlet {
	private static final Logger log = Logger.getLogger(WorkoutServlet.class.getName());
	
	/**
	 * Returns all WorkoutTick data for a particular user.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getAllWorkoutTicks")
	public WorkoutTick[] getAllWorkoutTicks(
			@QueryParam("imei") String imei) {

		// Prepare to make the query for all workout ticks
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key key = KeyFactory.createKey("WorkoutTick User", imei);
		Query query = new Query("WorkoutTick", key);

		// Execute the actual query
		List<Entity> workoutEntities = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());
		
		// Convert the List into a Java array of model objects: declare the array
		WorkoutTick[] workoutTicks = new WorkoutTick[workoutEntities.size()];
		int i = 0;
		for (Entity workoutEntity : workoutEntities) {
			
			// Iterate through the Google-style entities, converting each one to a POJO
			WorkoutTick workoutTick = new WorkoutTick();
			workoutTick.setAccumMinuteH((Double)workoutEntity.getProperty(WorkoutTick.KEY_ACCUM_MINUTE_H));
			workoutTick.setAccumMinuteV((Double)workoutEntity.getProperty(WorkoutTick.KEY_ACCUM_MINUTE_V));
			workoutTick.setAccuracy(((Double)workoutEntity.getProperty(WorkoutTick.KEY_ACCURACY)).floatValue());
			workoutTick.setAltitude(((Double)workoutEntity.getProperty(WorkoutTick.KEY_ALTITUDE)).floatValue());
			workoutTick.setHasAccuracy(((Double)workoutEntity.getProperty(WorkoutTick.KEY_HAS_ACCURACY)).floatValue());
			workoutTick.setkCal(((Double)workoutEntity.getProperty(WorkoutTick.KEY_KCALS)).floatValue());
			workoutTick.setLatitude(((Double) workoutEntity.getProperty(WorkoutTick.KEY_LATITUDE)).floatValue());
			workoutTick.setLongitude(((Double) workoutEntity.getProperty(WorkoutTick.KEY_LONGITUDE)).floatValue());
			workoutTick.setPrivacySetting((String)workoutEntity.getProperty(WorkoutTick.KEY_PRIVACY_SETTING));
			workoutTick.setSpeed(((Double)workoutEntity.getProperty(WorkoutTick.KEY_SPEED)).floatValue());
			workoutTick.setSystemTime((Long)workoutEntity.getProperty(WorkoutTick.KEY_SYSTEM_TIME));
			workoutTick.setTime((Long)workoutEntity.getProperty(WorkoutTick.KEY_SYSTEM_TIME));
			
			workoutTicks[i++] = workoutTick;
		}
		
		// Return the Jersey-enhanced version of the POJO (Jersey can serialize POJOs for us)
		return workoutTicks;
	}

	/**
	 * Saves a bunch of WorkoutTicks for a particular user.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/saveWorkoutTicks")
	public String saveWorkoutTicks(@FormParam("imei") String imei,
			@FormParam("workoutTicksJSONString") String workoutTicksJSONString) {

		/** Counter for new, unique workouts */
		int newWorkoutsSaved = 0;
		
		// Parse the inputted JSON into an array of WorkoutTicks.
		JsonParser jsonParser = new JsonParser();
		JsonArray workoutTicksJSONArray = jsonParser.parse(workoutTicksJSONString).getAsJsonArray();
		
		/** Used later to calculate point value of ticks */
		ArrayList<WorkoutTick> workoutTicks = new ArrayList<WorkoutTick>();
		
		// For each workout tick JSON element, parse this 
		for (JsonElement workoutElement : workoutTicksJSONArray) {
			JsonObject workoutObject = workoutElement.getAsJsonObject();

			// Step through each of the 
			WorkoutTick workoutTick = new WorkoutTick(
					workoutObject.get(WorkoutTick.KEY_GPS_TIME).getAsLong(), 
					workoutObject.get(WorkoutTick.KEY_LATITUDE).getAsFloat(), 
					workoutObject.get(WorkoutTick.KEY_LONGITUDE).getAsFloat(), 
					workoutObject.get(WorkoutTick.KEY_ALTITUDE).getAsFloat(), 
					workoutObject.get(WorkoutTick.KEY_SPEED).getAsFloat(), 
					workoutObject.get(WorkoutTick.KEY_HAS_ACCURACY).getAsFloat(), 
					workoutObject.get(WorkoutTick.KEY_ACCURACY).getAsFloat(), 
					workoutObject.get(WorkoutTick.KEY_SYSTEM_TIME).getAsLong(), 
					workoutObject.get(WorkoutTick.KEY_KCALS).getAsFloat(), 
					workoutObject.get(WorkoutTick.KEY_ACCUM_MINUTE_V).getAsDouble(), 
					workoutObject.get(WorkoutTick.KEY_ACCUM_MINUTE_H).getAsDouble(),
					workoutObject.get(WorkoutTick.KEY_PRIVACY_SETTING).getAsString());
			
			// Throw this particular workoutTick into the ArrayList<WorkoutTick>
			workoutTicks.add(workoutTick);
			
			// Save each workout to the datastore
			boolean newWorkoutWasSaved = saveIndividualTickToDatastore(imei, workoutTick);
			
			// Iterate the counter
			if (newWorkoutWasSaved) newWorkoutsSaved++;
		}
		
		// Tally up points in individual user scores
		calculateAndSavePoints(workoutTicks, imei);

		return "Saved " + newWorkoutsSaved + " new workout ticks";
	}

	/**
	 * Helper method to save the WorkoutTick to GAE datastore.
	 * 
	 * @param userimei
	 * @param newWorkoutTick
	 */
	private boolean saveIndividualTickToDatastore(String userimei,
			WorkoutTick newWorkoutTick) {

		// Create a new entity to save to the datastore
		Key userKey = KeyFactory.createKey("WorkoutTick User", userimei);
		Entity workoutTickEntity = new Entity("WorkoutTick", userKey);
		
		// Set all of the entity's properties
		workoutTickEntity.setProperty(WorkoutTick.KEY_GPS_TIME, newWorkoutTick.getTime()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_LATITUDE, newWorkoutTick.getLatitude()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_LONGITUDE, newWorkoutTick.getLongitude()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_ALTITUDE, newWorkoutTick.getAltitude()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_SPEED, newWorkoutTick.getSpeed()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_HAS_ACCURACY, newWorkoutTick.getHasAccuracy()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_ACCURACY, newWorkoutTick.getAccuracy()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_SYSTEM_TIME, newWorkoutTick.getSystemTime()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_KCALS, newWorkoutTick.getkCal()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_ACCUM_MINUTE_V, newWorkoutTick.getAccumMinuteV()); 
		workoutTickEntity.setProperty(WorkoutTick.KEY_ACCUM_MINUTE_H, newWorkoutTick.getAccumMinuteH());
		workoutTickEntity.setProperty(WorkoutTick.KEY_PRIVACY_SETTING, newWorkoutTick.getPrivacySetting().toString());
		
		// Now we actually save to the datastore
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		datastore.put(workoutTickEntity);
		
		return true;
	}
	
	/**
	 * Calculates the number of points to award to a user for the inputted
	 * workout ticks, then saves those points.
	 * 
	 * @param workoutTicks
	 */
	private void calculateAndSavePoints(List<WorkoutTick> workoutTicks, String imei) {
		
		float pointsEarned = 0;
		
		// Calculate the number of new points that the user earns from this batch
		for (WorkoutTick workoutTick : workoutTicks) {
			pointsEarned += workoutTick.getScore();
		}
		
		// Retrieve the user's current point total from Google Datastore (or 
		// set his current point total to 0 if it doesn't exist yet)
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key userKey = KeyFactory.createKey("WorkoutTick Ranking", imei);
		Query query = new Query("WorkoutRanking", userKey);
		
		// Execute the actual query for Ranking entity
		List<Entity> workoutRankingEntities = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());
		
		// The entity itself
		Entity workoutRankingEntity;

		// If there does not exist an entity for this user yet, make it
		if (workoutRankingEntities.size() == 0) {
			workoutRankingEntity = new Entity("WorkoutRanking", userKey);
			workoutRankingEntity.setProperty(UserRanking.USER_NAME, UserRanking.convertImeiToUsername(imei));
			
			// Initialize this user's point value to the points earned for this batch
			workoutRankingEntity.setProperty(UserRanking.POINT_TOTAL, pointsEarned);
		}
		// Sanity check: there can be no more than one "ranking" entity per user
		else if (workoutRankingEntities.size() == 1) {
			workoutRankingEntity = workoutRankingEntities.get(0);
			
			float currentPoints = ((Double)workoutRankingEntity.getProperty(UserRanking.POINT_TOTAL)).floatValue();
			// The user's new points = old points + this batch's points
			workoutRankingEntity.setProperty(UserRanking.POINT_TOTAL, currentPoints + pointsEarned);
		}
		else {
			// TODO: Error case -- too many "ranking" entities for this user
			return;
		}
		
		// Save the user's point total back into Google Datastore.
		datastore.put(workoutRankingEntity);
	}

	// ***************************************************************************
	// * Sample request
	// ***************************************************************************
	/**
	 * A test request, sent when you want to grab sample WorkoutTick data from
	 * GAE
	 * 
	 * @return
	 */
	@GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public WorkoutTick[] returnTestData() {

		WorkoutTick[] allWorkoutTicks = new WorkoutTick[3];

		WorkoutTick workoutTick1 = new WorkoutTick(
				-99, 
				-99,
				-99, 
				-99, 
				-99, 
				1, 
				0, 
				0,
				-99, 
				-99, 
				-99,
				PrivacyPreferenceEnum.highPrivacy.toString());
		WorkoutTick workoutTick2 = new WorkoutTick(
				-99, 
				-99,
				-99, 
				-99, 
				-99, 
				1, 
				0, 
				0,
				-99, 
				-99, 
				-99,
				PrivacyPreferenceEnum.mediumPrivacy.toString());
		WorkoutTick workoutTick3 = new WorkoutTick(
				-99, 
				-99,
				-99, 
				-99, 
				-99, 
				1, 
				0, 
				0,
				-99, 
				-99, 
				-99,
				PrivacyPreferenceEnum.lowPrivacy.toString());
		
		allWorkoutTicks[0] = workoutTick1;
		allWorkoutTicks[1] = workoutTick2;
		allWorkoutTicks[2] = workoutTick3;
		
		return allWorkoutTicks;
	}
	
	/**
	 * This is supposed to be a helper method to delete all entites from the datastore.
	 * 
	 * In reality, it can only delete ~1k entities from the datastore at a time (the request
	 * will time out before it has time to delete them all).  Also, this appears to be broken
	 * (as in it's currently written incorrectly).
	 * @param imei
	 * @return
	 */
	@GET
	@Path("/deleteEverything")
	@Produces(MediaType.TEXT_PLAIN)
	public String deleteEverything(@QueryParam("imei") String imei) {
		
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key key = KeyFactory.createKey("WorkoutTick User", imei);
		datastore.delete(key);
		
		return "Deleted some workout ticks";
	}

}
