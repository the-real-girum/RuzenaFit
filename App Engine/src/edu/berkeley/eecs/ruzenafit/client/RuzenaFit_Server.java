package edu.berkeley.eecs.ruzenafit.client;

import java.util.ArrayList;
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
//	private HorizontalPanel addPanel = new HorizontalPanel();
//	private TextBox newSymbolTextBox = new TextBox();
//	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private List<UserRanking> rankings = new ArrayList<UserRanking>();
	private final RankingServiceAsync rankingService = GWT.create(RankingService.class);
	private final int REFRESH_INTERVAL = 5000;  // in milliseconds

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {

		// Create table for stock data.
		rankingsFlexTable.setText(0, 0, "Name");
		rankingsFlexTable.setText(0, 1, "Points");
//		rankingsFlexTable.setText(0, 2, "Hrs. Logged");
		
		// Add styles to elements in the stock list table.
	    rankingsFlexTable.getRowFormatter().addStyleName(0, "rankingsListHeader");
	    rankingsFlexTable.addStyleName("rankingsList");
	    rankingsFlexTable.getCellFormatter().addStyleName(0, 1, "rankingsListNumericColumn");
//	    rankingsFlexTable.getCellFormatter().addStyleName(0, 2, "rankingsListNumericColumn");

//		// Assemble Add Stock panel.
//		addPanel.add(newSymbolTextBox);
//		addPanel.add(addStockButton);

		// Assemble Main panel.
		mainPanel.add(rankingsFlexTable);
//		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);

		// Associate the Main panel with the HTML host page.
		RootPanel.get("ruzenaList").add(mainPanel);

//		// Move cursor focus to the input box.
//		newSymbolTextBox.setFocus(true);
//		addRanking(new UserRanking("Girum", (float) 12.30));
//		addRanking(new UserRanking("Maurice", (float) 23.30));
		
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
	
	private void addRanking(UserRanking newRanking) {
		
		for (UserRanking currentRanking : rankings) {
			if (newRanking.equals(currentRanking))
				return;
		}
		
		int row = rankingsFlexTable.getRowCount();
	    rankings.add(newRanking);
	    rankingsFlexTable.setText(row, 0, newRanking.getName());
	    rankingsFlexTable.setText(row, 1, ((Integer)(int)newRanking.getScore()).toString());
	    
//	    rankingsFlexTable.getCellFormatter().addStyleName(row, 0, "rankingsListNumericColumn");
	    rankingsFlexTable.getCellFormatter().addStyleName(row, 1, "rankingsListNumericColumn");	
//	    rankingsFlexTable.getCellFormatter().addStyleName(row, 2, "rankingsListNumericColumn");	
	    
	}
	
	private void loadRankings() {
		rankingService.getRankings(new AsyncCallback<UserRanking[]>() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onSuccess(UserRanking[] result) {
				for (UserRanking ranking : result) {
					addRanking(ranking);
				}
				lastUpdatedLabel.setText("Last update : "
						+ DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Failed to retrieve rankings: " + caught.getMessage());
			}
		});
	}
	

}