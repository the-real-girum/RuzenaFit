package edu.berkeley.eecs.ruzenafit.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.berkeley.eecs.ruzenafit.shared.model.UserData;
import edu.berkeley.eecs.ruzenafit.shared.model.UserRanking;
import edu.berkeley.eecs.ruzenafit.shared.model.WorkoutTick;

// TODO: This service isn't DRY against the servlets that we already have.  This class
// and the servlets need to reference some common methods.
public class RankingServiceImpl extends RemoteServiceServlet implements RankingService {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Returns all of the current scores for all playing users.
	 */
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
	
	// FIXME: Make this actually retrieve user location data from datastore.
	/**
	 * Returns an array of arrays of LatLngs, one for each user.
	 */
	public UserData[] getUserLocations() {
		// Retrieve all user rankings from Google Datastore
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("WorkoutTick");
		
		// Execute the actual query for WorkoutTick entities
		List<Entity> workoutRankingEntities = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());
		
		Set<String> imeis = new HashSet<String>(workoutRankingEntities.size());
		for (Entity workoutEntity : workoutRankingEntities) {
			imeis.add(workoutEntity.getKey().getParent().getName());
			List<WorkoutTick> workoutTicks = new ArrayList<WorkoutTick>();
			return new UserData[] { new UserData(workoutEntity.getKey().getParent().getName(), (ArrayList<WorkoutTick>) workoutTicks) };
		}
		
		List<UserData> userdataList = new ArrayList<UserData>();
		for (String imei : imeis) {
			UserData userData = new UserData();
			userData.setUsername(imei);
			userdataList.add(userData);
		}
		
		return userdataList.toArray(new UserData[userdataList.size()]);
	}
	
}
