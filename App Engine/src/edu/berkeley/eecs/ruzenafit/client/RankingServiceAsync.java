package edu.berkeley.eecs.ruzenafit.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.berkeley.eecs.ruzenafit.shared.model.UserRanking;

public interface RankingServiceAsync {

	void getRankings(AsyncCallback<UserRanking[]> callback);
	
}
