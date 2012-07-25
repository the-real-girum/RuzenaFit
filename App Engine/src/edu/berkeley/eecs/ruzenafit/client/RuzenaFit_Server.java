package edu.berkeley.eecs.ruzenafit.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.berkeley.eecs.ruzenafit.shared.model.UserRanking;

public class RuzenaFit_Server implements EntryPoint {

	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable rankingsFlexTable = new FlexTable();
	private Label lastUpdatedLabel = new Label();
	private List<UserRanking> rankings = new ArrayList<UserRanking>();
	private final RankingServiceAsync rankingService = GWT.create(RankingService.class);
	private final int REFRESH_INTERVAL = 5000;  // in milliseconds

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {

		setupFlexTable();

		// Assemble Main panel.
		mainPanel.add(rankingsFlexTable);
		mainPanel.add(lastUpdatedLabel);

		// Associate the Main panel with the HTML host page.
		RootPanel.get("ruzenaList").add(mainPanel);

		// Load up the rankings initially (they'll be refreshed automatically after this)
		loadRankings();

		// Setup timer to refresh list automatically.
		Timer refreshTimer = new Timer() {
			@Override
			public void run() {
				loadRankings();
			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
		
	}
	
	/**
	 * Initial GUI setup for the flex table
	 */
	private void setupFlexTable() {
		// Create table for stock data.
		rankingsFlexTable.setText(0, 0, "Name");
		rankingsFlexTable.setText(0, 1, "Points");
		
		// Add styles to elements in the stock list table.
	    rankingsFlexTable.getRowFormatter().addStyleName(0, "rankingsListHeader");
	    rankingsFlexTable.addStyleName("rankingsList");
	    rankingsFlexTable.getCellFormatter().addStyleName(0, 1, "rankingsListNumericColumn");
	}
	
	/**
	 * Adds a {@link UserRanking} to the UI table
	 * @param newRanking
	 */
	private void addRankingToFlexTable(UserRanking newRanking) {
		
		int row = rankingsFlexTable.getRowCount();
	    rankings.add(newRanking);
	    rankingsFlexTable.setText(row, 0, newRanking.getName());
	    rankingsFlexTable.setText(row, 1, ((Integer)(int)newRanking.getScore()).toString());
	    
//	    rankingsFlexTable.getCellFormatter().addStyleName(row, 0, "rankingsListNumericColumn");
	    rankingsFlexTable.getCellFormatter().addStyleName(row, 1, "rankingsListNumericColumn");	
//	    rankingsFlexTable.getCellFormatter().addStyleName(row, 2, "rankingsListNumericColumn");	
	    
	}
	
	/**
	 * Helper method that simply runs the Async callback of the Rankings servlet.
	 */
	private void loadRankings() {
		rankingService.getRankings(new AsyncCallback<UserRanking[]>() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onSuccess(UserRanking[] result) {

				// Sort the array, based on the user's score
				Arrays.sort(result, new Comparator<UserRanking>() {
					@Override
					public int compare(UserRanking o1, UserRanking o2) {
						return ((Float)o2.getScore()).compareTo((Float)o1.getScore());
					}
				});
				
				// Clear out all of the rows, then re-setup the flex table
				rankingsFlexTable.removeAllRows();
				setupFlexTable();
				
				// Fill up the new table, and the internal array
				rankings = new ArrayList<UserRanking>(result.length);
				for (UserRanking ranking : result) {
					rankings.add(ranking);
					addRankingToFlexTable(ranking);
				}
				
				// Let the user know about the refresh
				lastUpdatedLabel.setText("Last updated at: "
						+ DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Failed to retrieve rankings: " + caught.getMessage());
			}
		});
	}
	

}