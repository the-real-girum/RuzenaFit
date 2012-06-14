/**
 * ViewHistory.java
 * @version 0.3
 * 
 * Shows a summary page of a particular workout selected from History.java
 * 
 * @author Irving Lin, Curtis Wang
 */

package edu.berkeley.eecs.ruzenafit;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class ViewHistory extends MapActivity{
	public static final String TAG = "ViewHistory Activity";
	public final short MAPVIEW = 0, SATVIEW = 1;

	public static Context context;
	
	private static DBAdapter dbHelper;
	private static long time_interval = 0;
	private int viewstate;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_details);
		
		init();
		
		dataSetup();
		
		mapSetup();
		
		chartSetup();
	}
	
	private void init() {
		context = this;
		dbHelper = new DBAdapter(this);
	}
	
	private void dataSetup() {
		dbHelper.open();
		Cursor c = dbHelper.getUserWorkout(1, History.rowId);

		TextView dateView = (TextView) findViewById(R.id.date);
		TextView durationView = (TextView) findViewById(R.id.duration);
		TextView totalCaloriesView = (TextView) findViewById(R.id.calories);
		TextView totalDistanceView = (TextView) findViewById(R.id.distance);
		TextView averageSpeedView = (TextView) findViewById(R.id.avg_speed);
		TextView averagePaceView = (TextView) findViewById(R.id.pace);
		TextView altitudeChangeView = (TextView) findViewById(R.id.altitude);
		
		String date = c.getString(c.getColumnIndex("date"));
		long millis = c.getInt(c.getColumnIndex("duration"));
		String duration = Utils.convertMillisToTime(millis);
		float totalCalories = c.getFloat(c.getColumnIndex("total_calories"));
		float averageSpeed = c.getFloat(c.getColumnIndex("average_speed"));
		float totalDistance = c.getFloat(c.getColumnIndex("total_distance"));
		float altitudeChange = c.getFloat(c.getColumnIndex("altitude_gain"));
		time_interval = c.getLong(c.getColumnIndex("time_interval"));

		dateView.setText(date.substring(5));
		durationView.setText(Utils.truncateAndFormatTime(duration, 2));
		totalCaloriesView.setText(Utils.truncate(totalCalories, "0.00", 4, false));
		totalDistanceView.setText(Utils.truncate(totalDistance, "0.00", 4, false));
		averageSpeedView.setText(Utils.truncate(averageSpeed, "0.00", 4, false));
		averagePaceView.setText(Utils.truncate(millis*1000*60/((float)totalDistance), "0.00", 4, false));
		altitudeChangeView.setText(Utils.truncate(altitudeChange, "0.00", 4, false));

		c.close();
		dbHelper.close();
	}
	
	private void mapSetup() {
		// for toggling between map/gps view
		final Button view = (Button) findViewById(R.id.view2);
		
		// TODO: eventually make this a user option to default to whatever she chooses.
		// initialize map to "map view"
		final MapView mapView = (MapView) findViewById(R.id.mapView2);
		mapView.setBuiltInZoomControls(true);
		viewstate = MAPVIEW;
		view.setText("View Sat");
		mapView.setSatellite(false);
		
		view.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (viewstate == MAPVIEW) { // go to satellite view
					viewstate = SATVIEW;
					view.setText("View Map");
					mapView.setSatellite(true);
				} else { // go to map view
					viewstate = MAPVIEW;
					view.setText("View Sat");
					mapView.setSatellite(false);
				}
				// there's also STREETVIEW, but it doesn't seem to allow access to viewing the street photos.
				mapView.invalidate();
			}
		});

	    // Setup and display route on map
		// TODO: this may take some time if large route. thus, should thread
		// this and show loading dialog while this is running instead of hanging
		// the activity.
		try {
			dbHelper.open();
			Cursor c = dbHelper.getWorkoutSampledata(History.rowId, new String[] {"geopoint_lat", "geopoint_long"});
			showRoute(removeBadAndRepeating(c));
			c.close();
			dbHelper.close();
		} catch (Exception e) {
			// TODO: tell user save to database fail.
		}
	}
	
	@SuppressWarnings("unchecked")
	private void chartSetup() {
		// TODO: need to make charts better integrated into CalFit summary. For
		// example: if the chart can be overlayed with the route, and show at
		// which points activity levels correspod to which parts of the route, etc.
		
		Spinner s = (Spinner) findViewById(R.id.chart_spinner);
	    ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.charts, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    s.setAdapter(adapter);
	    
		Button viewChart = (Button) findViewById(R.id.view_chart);
		viewChart.setOnClickListener(mChartListener);
	}
	
	private void showRoute(ArrayList<GeoPoint> gpList) {
		if (gpList == null) {
			return;
		}
		
		int gpListSize = gpList.size();
		
		MapView mapView = (MapView) findViewById(R.id.mapView2);
		List<Overlay> mapOverlays = mapView.getOverlays();
		MapController mapController = mapView.getController();
		Drawable drawable_green = this.getResources().getDrawable(R.drawable.map_pin_green_a2);
		Drawable drawable_red = this.getResources().getDrawable(R.drawable.map_pin_red_b2);
//		Drawable drawable_green = this.getResources().getDrawable(R.drawable.greenstartdot_a);
//		Drawable drawable_red = this.getResources().getDrawable(R.drawable.redenddot_b);
		Drawable drawable_blue = this.getResources().getDrawable(R.drawable.circledot2);
		RouteItemizedOverlay itemizedOverlay_green = new RouteItemizedOverlay(drawable_green);
		RouteItemizedOverlay itemizedOverlay_red = new RouteItemizedOverlay(drawable_red);
		RouteItemizedOverlay itemizedOverlay_blue = new RouteItemizedOverlay(drawable_blue);
		itemizedOverlay_blue.setGPList(gpList);
		mapOverlays = mapView.getOverlays();
		for (int i = 0; i < gpListSize; i++) {
			OverlayItem overlayItem = new OverlayItem(gpList.get(i), "", "");
			itemizedOverlay_blue.addOverlayItem(overlayItem);
		}
		if (gpListSize > 0) {
			// sets beginning position to a pin
			itemizedOverlay_green.addOverlayItem(new OverlayItem(gpList.get(0), "", ""));
		}
		if (gpListSize > 1) {
			// sets end position to a pin
			itemizedOverlay_red.addOverlayItem(new OverlayItem(gpList.get(gpList.size()-1), "", ""));
		}
		if (mapOverlays.size() > 0) {
			mapOverlays.clear();
		}
		mapOverlays.add(itemizedOverlay_blue);
		mapOverlays.add(itemizedOverlay_green);
		mapOverlays.add(itemizedOverlay_red);
		mapView.invalidate();
		RouteItemizedOverlay.animateAndZoom(mapController, gpList, false);			
	}
	
	//
	private OnClickListener mChartListener = new OnClickListener() {
	    public void onClick(View v) {
	    	Spinner spinner = (Spinner) findViewById(R.id.chart_spinner);
	    	Object currChart = spinner.getSelectedItem();
	    	String currString = currChart.toString();
	    	
	    	try {
		    	dbHelper.open();
				Cursor c = dbHelper.getWorkoutSampledata(History.rowId);
		    	
		    	showChart(currString, c);
		    	c.close();
		    	dbHelper.close();
			} catch (Exception e) {
				// TODO: tell user save to database fail.
			}
	    }
	};
	
	/**
	 * Based on spinner selection, show that chart with data from datasamples cursor.
	 * 
	 * @param currString
	 * @param c
	 */
	private void showChart(String currString, Cursor c) {
    	String title = "", yLabel = "", xLabel = "";
    	ArrayList<Float> xList = new ArrayList<Float>(), yList = new ArrayList<Float>();
		
		// since logging data is done in constant intervals, and we know the
		// update frequency by the time_interval column in the workouts database
		// table, we can extrapolate the time list in which everything occurs.
    	ArrayList<Float> timeList = new ArrayList<Float>();
    	for (int i = 0; i < c.getCount(); i++) {
    		timeList.add(i*(((float)time_interval)/1000));
    	}
    	
    	if (currString.equals("Speed vs. Time")) {
    		xList = timeList;
    		yList = cursorColToArrayList(c, "speed");
    		title = "Speed vs. Time";
    		xLabel = "Time (s)";
    		yLabel = "Speed (km/hr)";
    	} else if (currString.equals("Calories vs. Time")) {
    		xList = timeList;
    		yList = getDifference(cursorColToArrayList(c, "kcals"));
    		title = "Calories vs. Time";
    		xLabel = "Time (s)";
    		yLabel = "Calories (kCal)";
//    		createBarChart(xList, yList, title, xLabel, yLabel);
//    		return;
    	} else if (currString.equals("Cumul. Calories vs. Time")) {
    		xList = timeList;
    		yList = cursorColToArrayList(c, "kcals");
    		title = "Cumulative Calories vs. Time";
    		xLabel = "Time (s)";
    		yLabel = "Calories (kCal)";
    	} else if (currString.equals("Distance vs. Time")) {
    		xList = timeList;
    		yList = cursorColToArrayList(c, "distance");
    		title = "Distance vs. Time";
    		xLabel = "Time (s)";
    		yLabel = "Distance (km)";
    	} else if (currString.equals("Altitude vs. Time")) {
    		xList = timeList;
    		yList = cursorColToArrayList(c, "altitude");
    		title = "Altitude vs. Time";
    		xLabel = "Time (s)";
    		yLabel = "Altitude (m)";
    	} else if (currString.equals("Pace vs. Time")) {
    		xList = timeList;
    		yList = cursorColToArrayList(c, "pace");
    		title = "Pace vs. Time";
    		xLabel = "Time (s)";
    		yLabel = "Pace (min/km)";
    	} else if (currString.equals("Speed vs. Distance")) {
    		xList = cursorColToArrayList(c, "distance");
    		yList = cursorColToArrayList(c, "speed");
    		title = "Speed vs. Distance";
    		xLabel = "Distance (km)";
    		yLabel = "Speed (km/hr)";
    	} else if (currString.equals("Calories vs. Distance")) {
    		xList = cursorColToArrayList(c, "distance");
    		yList = cursorColToArrayList(c, "kcals");
    		title = "kCal vs. Distance";
    		xLabel = "Distance (km)";
    		yLabel = "Calories (kCal)";
    	} else if (currString.equals("Pace vs. Distance")) {
    		xList = cursorColToArrayList(c, "distance");
    		yList = cursorColToArrayList(c, "pace");
    		title = "Pace vs. Distance";
    		xLabel = "Distance (km)";
    		yLabel = "Pace (min/km)";
    	} else if (currString.equals("Altitude vs. Distance")) {
    		xList = cursorColToArrayList(c, "distance");
    		yList = cursorColToArrayList(c, "altitude");
    		title = "Altitude vs. Distance";
    		xLabel = "Distance (km)";
    		yLabel = "Altitude (m)";
    	}
    	createLineChart(xList, yList, title, xLabel, yLabel);
	}

	/**
	 * Takes a Cursor, finds the GeoPoint latitude and longitude columns per
	 * row, and returns an ArrayList of non-sequentially-repeating GeoPoints.
	 * For example: (111111,222222; 918332,179283; 103983,651239; 111111,222222;
	 * 111111,222222; 111111,222222; 320917,098731;) returns (111111,222222;
	 * 918332,179283; 103983,651239; 111111,222222; 320917,098731;)
	 * 
	 * @param c
	 * @return
	 */
	private ArrayList<GeoPoint> removeBadAndRepeating(Cursor c) {
		if (c.getCount() > 0) {
			c.moveToFirst();
			ArrayList<GeoPoint> newGPList = new ArrayList<GeoPoint>();
			int gpLatColIndex = c.getColumnIndex("geopoint_lat");
			int gpLongColIndex = c.getColumnIndex("geopoint_long");
			
			boolean first = true;
			GeoPoint tempGP1 = new GeoPoint(c.getInt(gpLatColIndex), c.getInt(gpLongColIndex));
			while (!c.isAfterLast()) {
				int GP2lat = c.getInt(gpLatColIndex);
				int GP2long = c.getInt(gpLongColIndex);
				GeoPoint tempGP2 = new GeoPoint(GP2lat, GP2long);
				if (first && !(GP2lat == -1 || GP2long == -1)) {
					newGPList.add(tempGP2);
					tempGP1 = tempGP2;
					first = false;
				} else if (!first && (tempGP1.getLatitudeE6() != tempGP2.getLatitudeE6() || tempGP1.getLongitudeE6() != tempGP2.getLongitudeE6())) {
					newGPList.add(tempGP2);
					tempGP1 = tempGP2;
				}
				c.moveToNext();
			}
			return newGPList;
		}
		return null;
	}

	/**
	 * Takes an ArrayList of GeoPoints and returns an ArrayList of
	 * non-sequentially-repeating GeoPoints. For example: (111111,222222;
	 * 918332,179283; 103983,651239; 111111,222222; 111111,222222;
	 * 111111,222222; 320917,098731;) returns (111111,222222; 918332,179283;
	 * 103983,651239; 111111,222222; 320917,098731;)
	 * 
	 * @param geopointsList
	 * @return
	 */
	@Deprecated
	private ArrayList<GeoPoint> removeBadAndRepeating(ArrayList<GeoPoint> geopointsList) {
		ArrayList<GeoPoint> newGPList = new ArrayList<GeoPoint>();
		GeoPoint tempGP1 = geopointsList.get(0);
		boolean first = true;
		for (int i = 0; i < geopointsList.size(); i++) {
			GeoPoint tempGP2 = geopointsList.get(i);
			long GP2lat = tempGP2.getLatitudeE6();
			long GP2long = tempGP2.getLongitudeE6();
			if (first && !(GP2lat == -1 || GP2long == -1)) {
				newGPList.add(tempGP2);
				tempGP1 = tempGP2;
				first = false;
			} else if (!first && (tempGP1.getLatitudeE6() != tempGP2.getLatitudeE6() || tempGP1.getLongitudeE6() != tempGP2.getLongitudeE6())) {
				newGPList.add(tempGP2);
				tempGP1 = tempGP2;
			}
		}
		return newGPList;
	}

	/**
	 * Takes in an arraylist of floats and returns a new arraylist of same
	 * length containing the magnitude difference between the change in each
	 * successive float. For example: {1, 4, 13, 2} returns {1, 3, 9, 11}.
	 * 
	 * @param alist
	 * @return
	 */
	private ArrayList<Float> getDifference(ArrayList<Float> alist) {
		if (alist.size() > 0) {
			ArrayList<Float> diffList = new ArrayList<Float>();
			diffList.add(alist.get(0));
			for (int i = 1; i < alist.size(); i++) {
				float item1 = alist.get(i-1);
				float item2 = alist.get(i);
				diffList.add(Math.abs(item2-item1));
			}
			return diffList;
		}
		return null;
	}

	/**
	 * Takes a cursor and a column name that contains float data and returns an
	 * ArrayList containing that float data.
	 * 
	 * @param c
	 * @param col
	 * @return
	 */
	private ArrayList<Float> cursorColToArrayList(Cursor c, String col) {
		ArrayList<Float> arraylist = new ArrayList<Float>();
		int colIndex = c.getColumnIndex(col);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			arraylist.add(c.getFloat(colIndex));
			if (!c.isNull(colIndex)) { // sanity check. probably don't need.
				c.moveToNext();
			} else { 
				break;
			}
		}
		return arraylist;
	}

	// The isRouteDisplayed() method is required
	protected boolean isRouteDisplayed() {
		return true;
	}
		
	/**
	 * Takes in two String objects from database (that encode an ArrayList<Float>) and starts a LineChart
	 * activity with that data.
	 * 
	 * @param xString
	 * @param yString
	 * @param title
	 * @param xAxis
	 * @param yAxis
	 */
	public void createChart(String xString, String yString, String title, String xAxis, String yAxis) {
		createLineChart(Utils.stringToFloatList(xString), Utils.stringToFloatList(yString), title, xAxis, yAxis);
	}
	
	/**
	 * Takes in two ArrayList<Float> objects and starts a LineChart activity.
	 * 
	 * TODO: Need to change this to just take in regular array to optimize this.
	 * 
	 * @param xArrayList
	 * @param yArrayList
	 * @param title
	 * @param xAxis
	 * @param yAxis
	 */
	public void createLineChart(ArrayList<Float> xArrayList, ArrayList<Float> yArrayList, String title, String xAxis, String yAxis) {
		//Change ArrayLists to suit the input to LineChart's execute method
		double[] xArray = Utils.arrayListToArray(xArrayList); 
		double[] yArray = Utils.arrayListToArray(yArrayList);
//		JSONArray yArray = new JSONArray();
//		for (int i = 0; i < yArrayList.size(); i++) {
//			yArray.put((Double)((double)yArrayList.get(i)));
//		}
		
		//Create the LineChart activity
		
		LineChart lc = new LineChart();
		try {
			startActivity(lc.execute(context, xArray, yArray, title, xAxis, yAxis));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Takes in two ArrayList<Float> objects and starts a BarChart activity.
	 * 
	 * TODO: Need to change this to just take in regular array to optimize this.
	 * 
	 * @param xArrayList
	 * @param yArrayList
	 * @param title
	 * @param xAxis
	 * @param yAxis
	 */
	public void createBarChart(ArrayList<Float> xArrayList, ArrayList<Float> yArrayList, String title, String xAxis, String yAxis) {
		//Change ArrayLists to suit the input to LineChart's execute method
		double[] xArray = Utils.arrayListToArray(xArrayList); 
		double[] yArray = Utils.arrayListToArray(yArrayList);
		
		//Create the BarChart activity
		MyBarChart mbc = new MyBarChart();
		try {
			startActivity(mbc.execute(context, xArray, yArray, title, xAxis, yAxis));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
