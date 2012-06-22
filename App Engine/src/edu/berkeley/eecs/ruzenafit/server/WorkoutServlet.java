package edu.berkeley.eecs.ruzenafit.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import edu.berkeley.eecs.ruzenafit.shared.model.AnActualWorkoutModelX_X;
import edu.berkeley.eecs.ruzenafit.shared.model.CoordinateTime;

// TODO: Optimize the uniqueness sanity check to ensure that you only load in all
// of the workouts for a user once.
@Path("/workout")
public class WorkoutServlet {

	/**
	 * Returns all workout data for a particular user.
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/getAllWorkouts")
	public AnActualWorkoutModelX_X[] getAllWorkouts(
			@QueryParam("userName") String userName) {

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key key = KeyFactory.createKey("Workout User", userName);
		Query query = new Query("Workout", key);

		List<Entity> workoutEntities = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());

		AnActualWorkoutModelX_X[] workouts = new AnActualWorkoutModelX_X[workoutEntities
				.size()];
		int i = 0;
		for (Entity workoutEntity : workoutEntities) {
			AnActualWorkoutModelX_X workout = new AnActualWorkoutModelX_X();
			workout.setAverageSpeed((String) workoutEntity
					.getProperty("averageSpeed"));
			workout.setDate((String) workoutEntity.getProperty("date"));
			workout.setDuration((String) workoutEntity.getProperty("duration"));
			workout.setTotalCalories((String) workoutEntity
					.getProperty("totalCalories"));
			workout.setTotalDistance((String) workoutEntity
					.getProperty("totalDistance"));

			workouts[i++] = workout;
		}

		return workouts;

	}

	/**
	 * Saves one workout for a particular user.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/saveWorkout")
	public String saveWorkout(@FormParam("user") String user,
			@FormParam("privacySetting") String privacySetting,
			@FormParam("date") String date,
			@FormParam("averageSpeed") String averageSpeed,
			@FormParam("duration") String duration,
			@FormParam("totalCalories") String totalCalories,
			@FormParam("totalDistance") String totalDistance,
			@FormParam("coordinatesXMLString") String coordinatesXMLString) {

		AnActualWorkoutModelX_X workout = new AnActualWorkoutModelX_X();
		workout.setPrivacySetting(privacySetting);
		workout.setDate(date);
		workout.setAverageSpeed(averageSpeed);
		workout.setDuration(duration);
		workout.setTotalCalories(totalCalories);
		workout.setTotalDistance(totalDistance);

		try {
			Builder parser = new Builder();
			Document doc = parser.build(coordinatesXMLString, null);
			
			ArrayList<CoordinateTime> coordinates = new ArrayList<CoordinateTime>();
			
			for (int i = 0; i < doc.getChildCount(); i++) {
//				long latitude = doc.getChild(i).
			}
			
		} catch (ParsingException ex) {
			System.err
					.println("coordinatesXMLString is malformed.");
		} catch (IOException ex) {
			System.err
					.println("Could not open coordinatesXMLString");
		}
		

		// Save this particular workout to the GAE datastore.
		boolean result = saveWorkoutToDatastore(user, workout);

		return (result ? "success" : "failure");
	}

	/**
	 * Helper method to save the workout to GAE datastore.
	 * 
	 * @param user
	 * @param workout
	 */
	private boolean saveWorkoutToDatastore(String user,
			AnActualWorkoutModelX_X workout) {

		AnActualWorkoutModelX_X[] currentWorkouts = getAllWorkouts(user);
		boolean isUnique = true;

		for (AnActualWorkoutModelX_X iteratedWorkout : currentWorkouts) {
			if (iteratedWorkout.equals(workout)) {
				isUnique = false;
			}
		}

		if (isUnique) {
			Key userKey = KeyFactory.createKey("Workout User", user);

			Entity workoutEntity = new Entity("Workout", userKey);
			workoutEntity.setProperty("date", workout.getDate());
			workoutEntity
					.setProperty("averageSpeed", workout.getAverageSpeed());
			workoutEntity.setProperty("duration", workout.getDuration());
			workoutEntity.setProperty("totalCalories",
					workout.getTotalCalories());
			workoutEntity.setProperty("totalDistance",
					workout.getTotalDistance());

			DatastoreService datastore = DatastoreServiceFactory
					.getDatastoreService();
			datastore.put(workoutEntity);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Saves all workouts TODO: for a specific user.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/saveAllWorkouts")
	public String saveAllWorkouts(@FormParam("xmlInput") String xmlInput) {

		int newUniqueWorkouts = 0;

		return "" + newUniqueWorkouts;
	}

	// ***************************************************************************
	// * Sample request
	// ***************************************************************************
	/**
	 * A test request, sent when you want to grab sample workout data from GAE
	 * 
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/test")
	public AnActualWorkoutModelX_X[] returnTestData() {

		AnActualWorkoutModelX_X[] allWorkouts = new AnActualWorkoutModelX_X[3];

		AnActualWorkoutModelX_X workout1 = new AnActualWorkoutModelX_X();
		workout1.setDate("some date");
		workout1.setAverageSpeed("some average speed");
		workout1.setDuration("some average duration");
		workout1.setTotalCalories("some total number of calories");
		workout1.setTotalDistance("some total distance run");

		AnActualWorkoutModelX_X workout2 = new AnActualWorkoutModelX_X();
		workout2.setDate("some date 2");
		workout2.setAverageSpeed("some average speed 2");
		workout2.setDuration("some average duration 2");
		workout2.setTotalCalories("some total number of calories 2");
		workout2.setTotalDistance("some total distance run 2");

		AnActualWorkoutModelX_X workout3 = new AnActualWorkoutModelX_X();
		workout3.setDate("some date 3");
		workout3.setAverageSpeed("some average speed 3");
		workout3.setDuration("some average duration 3");
		workout3.setTotalCalories("some total number of calories 3");
		workout3.setTotalDistance("some total distance run 3");

		allWorkouts[0] = workout1;
		allWorkouts[1] = workout2;
		allWorkouts[2] = workout3;

		return allWorkouts;
	}

}
