package edu.berkeley.eecs.ruzenafit.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import edu.berkeley.eecs.ruzenafit.shared.model.UserRanking;
import edu.berkeley.eecs.ruzenafit.shared.model.WorkoutTick;

@Path("/ranking")
public class RankingServlet {
	
	/**
	 * Returns all of the current scores for all playing users.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getRankings")
	public UserRanking[] getRankings() {

		// Retrieve all user rankings from Google Datastore
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("WorkoutRanking");
		
		// Execute the actual query for Ranking entity
		List<Entity> workoutRankingEntities = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());
		
		List<UserRanking> rankings = new ArrayList<UserRanking>(workoutRankingEntities.size());
		
		// Parse the Google Entities into User[] values (to auto-marshall it to JSON through Jersey)
		for (Entity workoutRankingEntity : workoutRankingEntities) {
			UserRanking userRanking = new UserRanking();
			userRanking.setName((String) workoutRankingEntity.getProperty(UserRanking.USER_NAME));
			userRanking.setScore(((Double)workoutRankingEntity.getProperty(UserRanking.POINT_TOTAL)).floatValue());
			
			rankings.add(userRanking);
		}
		
		return rankings.toArray(new UserRanking[rankings.size()]);
	}
	
	/**
	 * Calculates the number of points to award to a user for the inputted
	 * workout ticks, then saves those points.
	 * 
	 * @param workoutTicks
	 */
	protected static void calculateAndSavePoints(List<WorkoutTick> workoutTicks, String imei) {
		
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
}
