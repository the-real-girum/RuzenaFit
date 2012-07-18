package edu.berkeley.eecs.ruzenafit.servlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.users.User;

@Path("/ranking")
public class RankingServlet {
	
	/**
	 * Returns all of the current scores for all playing users.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getRankings")
	public User[] getRankings() {
		User[] rankings = null;
		
		// Retrieve all user rankings from Google Datastore
		
		// Parse the Google Entities into User[] values (to auto-marshall it to JSON through Jersey)

		
		return rankings;
	}
}
