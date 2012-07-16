package edu.berkeley.eecs.ruzenafit.servlet;

import java.util.List;

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

import edu.berkeley.eecs.ruzenafit.shared.model.WorkoutTick;

// TODO: Optimize the uniqueness sanity check to ensure that you only load in all
// of the WorkoutTicks for a user once.
@Path("/workout")
public class WorkoutServlet {

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
		Key key = KeyFactory.createKey("WorkoutTick IMEI", imei);
		Query query = new Query("WorkoutTick", key);

		List<Entity> workoutEntities = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());

		WorkoutTick[] workoutTicks = new WorkoutTick[workoutEntities.size()];
		int i = 0;
		for (Entity workoutEntity : workoutEntities) {
//			WorkoutTick workoutTick = new WorkoutTick();
//			workoutTick.setAverageSpeed((String) workoutEntity
//					.getProperty("averageSpeed"));
//			workoutTick.setDate((String) workoutEntity.getProperty("date"));
//			workoutTick.setDuration((String) workoutEntity
//					.getProperty("duration"));
//			workoutTick.setTotalCalories((String) workoutEntity
//					.getProperty("totalCalories"));
//			workoutTick.setTotalDistance((String) workoutEntity
//					.getProperty("totalDistance"));
//
//			workoutTicks[i++] = workoutTick;
		}

		return workoutTicks;

	}

	/**
	 * Saves one WorkoutTick for a particular user.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/saveWorkoutTicks")
	public String saveWorkoutTick(@FormParam("imei") String imei,
			@FormParam("workoutTicksJSONString") String workoutTicksJSONString) {

		String result = "";
		
		// Parse the inputted JSON into an array of WorkoutTicks.
		JsonParser jsonParser = new JsonParser();
		JsonArray workoutTicksArray = jsonParser.parse(workoutTicksJSONString).getAsJsonArray();
		
		for (JsonElement workoutElement : workoutTicksArray) {
			JsonObject workoutObject = workoutElement.getAsJsonObject();
			
			/** currently debugging my POST request setup */
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
					workoutObject.get(WorkoutTick.KEY_ACCUM_MINUTE_H).getAsDouble());
			
//			result += "Workout 1: " + workoutTick.toString() + ", ";
		}

		// TODO: Save each workoutTick into the datastore
		// boolean result = saveWorkoutTickToDatastore(user, WorkoutTick);

		// TODO: Return the number of new workouts that were saved into the datastore.
		// return (result ? "success" : "WorkoutTick already exists");
//		return "The IMEI " + imei + " send the following JSON String: " + workoutTicksJSONString;
		return "success with imei: " + imei;
	}

	/**
	 * Helper method to save the WorkoutTick to GAE datastore.
	 * 
	 * @param user
	 * @param WorkoutTick
	 */
	private boolean saveWorkoutTickToDatastore(String user,
			WorkoutTick WorkoutTick) {

		WorkoutTick[] currentWorkoutTicks = getAllWorkoutTicks(user);
		boolean isUnique = true;

		for (WorkoutTick iteratedWorkoutTick : currentWorkoutTicks) {
			if (iteratedWorkoutTick.equals(WorkoutTick)) {
				isUnique = false;
			}
		}

		if (isUnique) {
			Key userKey = KeyFactory.createKey("WorkoutTick User", user);

//			Entity WorkoutTickEntity = new Entity("WorkoutTick", userKey);
//			WorkoutTickEntity.setProperty("privacySetting",
//					WorkoutTick.getPrivacySetting());
//			WorkoutTickEntity.setProperty("date", WorkoutTick.getDate());
//			WorkoutTickEntity.setProperty("averageSpeed",
//					WorkoutTick.getAverageSpeed());
//			WorkoutTickEntity
//					.setProperty("duration", WorkoutTick.getDuration());
//			WorkoutTickEntity.setProperty("totalCalories",
//					WorkoutTick.getTotalCalories());
//			WorkoutTickEntity.setProperty("totalDistance",
//					WorkoutTick.getTotalDistance());
//			WorkoutTickEntity.setProperty("coordinatesXMLString",
//					WorkoutTick.getCoordinatesXMLString());
//
//			DatastoreService datastore = DatastoreServiceFactory
//					.getDatastoreService();
//			datastore.put(WorkoutTickEntity);
			return true;
		} else {
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
				-99);
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
				-99);
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
				-99);
		
		allWorkoutTicks[0] = workoutTick1;
		allWorkoutTicks[1] = workoutTick2;
		allWorkoutTicks[2] = workoutTick3;
		
		return allWorkoutTicks;
	}

}
