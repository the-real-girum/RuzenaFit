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
import com.google.appengine.api.datastore.Query;

import edu.berkeley.eecs.ruzenafit.shared.model.UserRanking;

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
}
