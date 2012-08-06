package edu.berkeley.eecs.ruzenafit.objectify;

import java.util.List;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;

import edu.berkeley.eecs.ruzenafit.shared.model.PrivacyPreferenceEnum;
import edu.berkeley.eecs.ruzenafit.shared.model.UserData;
import edu.berkeley.eecs.ruzenafit.shared.model.UserRanking;
import edu.berkeley.eecs.ruzenafit.shared.model.WorkoutTick;

/**
 * A proprietary Data Access Object, specific to our model objects.
 * Servlets should access the Google Datastore through this class, to
 * abstract away usage of the Objectify library. 
 * 
 * @author Girum Ibssa
 */
public final class DAO extends DAOBase {
	
	/**
	 * Plumbing for Objectify.  You must register any POJOs that you
	 * want to track in the datastore here.
	 */
	static {
		ObjectifyService.register(PrivacyPreferenceEnum.class);
		ObjectifyService.register(UserData.class);
		ObjectifyService.register(UserRanking.class);
		ObjectifyService.register(WorkoutTick.class);
	}
	
	public List<WorkoutTick> getAllWorkoutTicks(String imei) {
		return null;
	}
	
	public String saveWorkoutTicks(List<WorkoutTick> workoutTicks) {
		return null;
	}
	
	public List<UserRanking> getRankings() {
		return null;
	}
	
}
