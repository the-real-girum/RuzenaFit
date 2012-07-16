package client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.berkeley.eecs.ruzenafit.client.GreetingService;
import edu.berkeley.eecs.ruzenafit.client.GreetingServiceAsync;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */

public class Table implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);

	private static final int REFRESH_INTERVAL = 5000; // ms
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable playerRankTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private ArrayList<String> points = new ArrayList<String>();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		// TODO Create table for player data.
		// TODO Assemble Main panel.
		// TODO Associate the Main panel with the HTML host page.

		    
		// Create table for stock data.
		playerRankTable.setText(0, 0, "Player ID");
		playerRankTable.setText(0, 1, "Rank");
		playerRankTable.setText(0, 2, "Calories");
		playerRankTable.setText(0, 3, "Points");

		// Add styles to elements in the stock list table.
		playerRankTable.getRowFormatter().addStyleName(0, "watchListHeader");
		playerRankTable.addStyleName("watchList");
		playerRankTable.getCellFormatter().addStyleName(0, 1,
				"watchListNumericColumn");
		playerRankTable.getCellFormatter().addStyleName(0, 2,
				"watchListNumericColumn");
		playerRankTable.getCellFormatter().addStyleName(0, 3,
				"watchListRemoveColumn");

		// Assemble Add Stock panel.
		addPanel.add(newSymbolTextBox);
		addPanel.add(addStockButton);
		addPanel.addStyleName("addPanel");

		// Assemble Main panel.
		mainPanel.add(playerRankTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);

		// Associate the Main panel with the HTML host page.
		RootPanel.get("RuzenaFitRanking").add(mainPanel);

		// Move cursor focus to the input box.
		newSymbolTextBox.setFocus(true);

		// Setup timer to refresh list automatically.
		Timer refreshTimer = new Timer() {
			@Override
			public void run() {
				refreshWatchList();
			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

		// Listen for mouse events on the Add button.
		addStockButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addStock();
			}
		});

		// Listen for keyboard events in the input box.
		newSymbolTextBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					addStock();
				}
			}
		});

	} // end of onModuleLoad

	/**
	 * Add stock to FlexTable. Executed when the user clicks the addStockButton
	 * or presses enter in the newSymbolTextBox.
	 */
	@SuppressWarnings("deprecation")
	private void addStock() {

		final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
		newSymbolTextBox.setFocus(true);

		// Stock code must be between 1 and 10 chars that are numbers, letters,
		// or dots.
		if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
			Window.alert("'" + symbol + "' is not a valid symbol.");
			newSymbolTextBox.selectAll();
			return;

		}
		// Sets Box back to blank
		newSymbolTextBox.setText("");

		// Don't add the stock if it's already in the table.
		if (points.contains(symbol))
			return;

		// Add the stock to the table.
		int row = playerRankTable.getRowCount();
		points.add(symbol);
		playerRankTable.setText(row, 0, symbol);
		playerRankTable.setWidget(row, 2, new Label());
		playerRankTable.getCellFormatter().addStyleName(row, 1,
				"watchListNumericColumn");
		playerRankTable.getCellFormatter().addStyleName(row, 2,
				"watchListNumericColumn");
		playerRankTable.getCellFormatter().addStyleName(row, 3,
				"watchListRemoveColumn");

		// Add a button to remove this stock from the table.
		Button removeStockButton = new Button("x");
		removeStockButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int removedIndex = points.indexOf(symbol);
				points.remove(removedIndex);
				playerRankTable.removeRow(removedIndex + 1);
			}
		});
		playerRankTable.setWidget(row, 3, removeStockButton);

		// Get the stock price.
		refreshWatchList();

		// Display timestamp showing last refresh.
		lastUpdatedLabel.setText("Last update : "
				+ DateTimeFormat.getMediumDateTimeFormat().format(new Date()));

	}

	private void refreshWatchList() {
		// TODO Auto-generated method stub

	}
}
