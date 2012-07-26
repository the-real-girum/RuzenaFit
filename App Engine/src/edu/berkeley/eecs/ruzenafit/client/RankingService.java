package edu.berkeley.eecs.ruzenafit.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.berkeley.eecs.ruzenafit.shared.model.UserData;
import edu.berkeley.eecs.ruzenafit.shared.model.UserRanking;

@RemoteServiceRelativePath("rankings")
public interface RankingService extends RemoteService {
	public UserRanking[] getRankings();
	public UserData[] getUserLocations();
}
