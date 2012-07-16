package edu.berkeley.eecs.ruzenafit.servlet;

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
import edu.berkeley.eecs.ruzenafit.shared.model.WorkoutTick;

// TODO: Optimize the uniqueness sanity check to ensure that you only load in all
// of the WorkoutTicks for a user once.
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

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key key = KeyFactory.createKey("WorkoutTick User", imei);
		Query query = new Query("WorkoutTick", key);

		List<Entity> workoutEntities = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());
		
		WorkoutTick[] workoutTicks = new WorkoutTick[workoutEntities.size()];
		int i = 0;
		for (Entity workoutEntity : workoutEntities) {
			
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

		int newWorkoutsSaved = 0;
		
		// Parse the inputted JSON into an array of WorkoutTicks.
		JsonParser jsonParser = new JsonParser();
		JsonArray workoutTicksJSONArray = jsonParser.parse(workoutTicksJSONString).getAsJsonArray();
		
		for (JsonElement workoutElement : workoutTicksJSONArray) {
			JsonObject workoutObject = workoutElement.getAsJsonObject();
			
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

			// Save each workout to the datastore
			boolean newWorkoutWasSaved = saveWorkoutTickToDatastore(imei, workoutTick);
			
			// Iterate the counter
			if (newWorkoutWasSaved) newWorkoutsSaved++;
		}

		return "Saved " + newWorkoutsSaved + " new workout ticks";
	}

	/**
	 * Helper method to save the WorkoutTick to GAE datastore.
	 * 
	 * @param userimei
	 * @param workoutTick
	 */
	private boolean saveWorkoutTickToDatastore(String userimei,
			WorkoutTick workoutTick) {

		// TODO: I only need to query this Workout[] array once -- I can hold onto it for
		// each of the uniqueness checks that I have to do.
		WorkoutTick[] currentWorkoutTicks = getAllWorkoutTicks(userimei);
		boolean isUnique = true;

		for (WorkoutTick iteratedWorkoutTick : currentWorkoutTicks) {
			if (iteratedWorkoutTick.equals(workoutTick)) {
				return false;
			}
		}

		if (isUnique) {
			Key userKey = KeyFactory.createKey("WorkoutTick User", userimei);

			Entity workoutTickEntity = new Entity("WorkoutTick", userKey);
			
			workoutTickEntity.setProperty(WorkoutTick.KEY_GPS_TIME, workoutTick.getTime()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_LATITUDE, workoutTick.getLatitude()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_LONGITUDE, workoutTick.getLongitude()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_ALTITUDE, workoutTick.getAltitude()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_SPEED, workoutTick.getSpeed()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_HAS_ACCURACY, workoutTick.getHasAccuracy()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_ACCURACY, workoutTick.getAccuracy()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_SYSTEM_TIME, workoutTick.getSystemTime()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_KCALS, workoutTick.getkCal()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_ACCUM_MINUTE_V, workoutTick.getAccumMinuteV()); 
			workoutTickEntity.setProperty(WorkoutTick.KEY_ACCUM_MINUTE_H, workoutTick.getAccumMinuteH());
			workoutTickEntity.setProperty(WorkoutTick.KEY_PRIVACY_SETTING, workoutTick.getPrivacySetting().toString());
			
			DatastoreService datastore = DatastoreServiceFactory
					.getDatastoreService();
			datastore.put(workoutTickEntity);
			return true;
		} 
		else {
			return false;
		}
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
