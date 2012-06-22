/**
 * Workout.java
 * @version 0.3
 * 
 * The main activity screen displaying real-time information during a workout.
 * 
 * @author Irving Lin, Curtis Wang, Edmund Seto
 */

package edu.berkeley.eecs.ruzenafit.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.RouteItemizedOverlay;
import edu.berkeley.eecs.ruzenafit.Utils;
import edu.berkeley.eecs.ruzenafit.WAVE;
import edu.berkeley.eecs.ruzenafit.WorkoutHelper;
import edu.berkeley.eecs.ruzenafit.access.CalFitDBAdapter;
import edu.berkeley.eecs.ruzenafit.model.GeoPoint_Time;

/* To Input Mock Locations to Emulator 5554 for testing:
 *     telnet localhost 5554
 * 
 * Example Routes:
 * 
 * Route 1:
 *     geo fix -122.250795 37.873295
 *     geo fix -122.254975 37.863165
 *     geo fix -122.254658 37.86311
 *     geo fix -122.25449 37.862035
 * Route 2:
 *     geo fix -122 39
 *     geo fix -122 38
 *     geo fix -121 39
 *     geo fix -122 40
 *     geo fix -123 39
 */

public class WorkoutActivity extends MapActivity {
	public static final String TAG = "Workout Activity";

	// workout state
	public final short STARTED = 0, PAUSED = 1, ENDED = 2;
	public final short MAPVIEW = 0, SATVIEW = 1;

	private final String INFINITY_SYMBOL = new DecimalFormatSymbols()
			.getInfinity();

	public static Context workoutContext;
	private CalFitDBAdapter dbHelper;
	private GeoPoint prevGP, curGP;
	private MapView mapView;
	private MapController mapController;
	private LocationManager locationManager;
	private Drawable drawable;
	private static RouteItemizedOverlay itemizedOverlay;
	private MyLocationOverlay myLocationOverlay;

	// lists for saving all workout data -- why isn't this data encapsulated in one class?
	private List<Overlay> mapOverlays;
	private ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
	private ArrayList<GeoPoint_Time> gpList = new ArrayList<GeoPoint_Time>();
	private ArrayList<Float> caloriesList = new ArrayList<Float>();
	private ArrayList<Float> speedList = new ArrayList<Float>();
	private ArrayList<Float> distanceList = new ArrayList<Float>();
	private ArrayList<Float> paceList = new ArrayList<Float>();
	private ArrayList<Float> altitudeList = new ArrayList<Float>();
	private ArrayList<Long> timeList = new ArrayList<Long>();

	// for workout stats
	private Handler mHandler = new Handler();
	private Button startTimerButton, endButton;
	private TextView calories, distance, speed, pace, altitude, gpsStatus;
	private static short workoutState;
	private boolean tracking = false;
	private boolean trafficing = false;
	private boolean endDialog_on = false;
	private short viewstate;
	private float prevSpeed = 0, prevCal = 0, prevPace = 0, prevDistance = 0,
			averageSpeed = 0, mySpeed = 0, myPace = 0, kCals = 0,
			myDistance = 0, myAltitude = 0, prevAltitude = 0;

	// Timer Control
	public static long totalWorkoutRunTime = 0;
	public static long workoutRunStartTime = 0;
	public static long workoutPauseTime = 0;
	public static long workoutPauseStartTime = 0;
	public static long totalWorkoutPauseTime = 0;

	// Frequency between updates (for kcal, distance, pace, etc.) after starting
	// the workout. Default is 1 second
	private final int UPDATEFREQUENCY = 3000; // in milliseconds

	// For HTTP Post query
	private String genformat = "####0.00";
	private String geoformat = "####0.000000";
	private DecimalFormat genfmt = new DecimalFormat(genformat,
			new DecimalFormatSymbols(Locale.US));
	private DecimalFormat geofmt = new DecimalFormat(geoformat,
			new DecimalFormatSymbols(Locale.US));

	/* * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * *
	 * REQUIRED OR ACTIVITY SPECIFIC CLASS METHODS * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * *
	 */

	/*
	 * Refer to
	 * http://developer.android.com/guide/topics/fundamentals.html#actlife for
	 * information on an Activity's lifecycle.
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// display activity view
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Bar at the top of
															// screen will not
															// display
		setContentView(R.layout.workout);

		// initialize activity
		workoutInit();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (workoutState != ENDED && !endDialog_on) {
			enableNotification();
			WorkoutHelper.save = true;
			WorkoutHelper.workoutState = workoutState;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// reload state
		if (WorkoutHelper.workoutStarted) { // this if condition checks if
											// workout helper has been created
											// (and prevents this from being
											// called the first time before a
											// workout has been started)
			// disable title bar notification
			disableNotification();

			// start workout again with updated data
			updateInfo();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// set this workoutOpened boolean to false to ensure that CalFit
		// launches
		WorkoutHelper.workoutOpened = false;

		// probably unnecessary, but reset all data
		endWorkout(false, false);

		// kill WorkoutHelper service
		stopService(new Intent(workoutContext, WorkoutHelper.class));
	}

	protected boolean isRouteDisplayed() {
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// when the generic Android "back" button is pressed and if workout is
		// running. This is a fix to prevent the "back" button from killing the
		// workout without saving.
		if (keyCode == KeyEvent.KEYCODE_BACK && workoutState != ENDED) {
			pauseWorkout();

			endDialog(true);

			return true;
		}
		return false;
	}

	/* * * * * * * * * * * * * * * * * * * * * * *
	 * *
	 * NON-REQUIRED ACTIVITY CLASS METHODS * * * * * * * * * * * * * * * * * * *
	 * * * * *
	 */
	/**
	 * Sets up the workout.
	 */
	private void workoutInit() {
		workoutContext = this; // set context to a global variable to be used in
								// other scopes
		WorkoutHelper.workoutOpened = true; // must set this to be true so that
											// if somehow, another CalFit
											// activity is launched, it's killed
											// instead of overriding this
											// current workout
		workoutState = ENDED; // initialize workout to start in the "ended"
								// state

		initTextViews();

		// hack for android 1.6.. shouldn't be a problem for 2.0+?
		// starting it early (as application starts) to circumvent kcal
		// service starting before service has started (not supposed to
		// happen) and (not hack) to locate gps position as soon as app
		// is loaded
		startService(new Intent(workoutContext, WAVE.class));

		activateStartButton();

		activateEndButton();

		gpsSetup();

		mapSetup();
	}

	/**
	 * Initializes sidebar textviews variables to be set later with updated
	 * data.
	 */
	private void initTextViews() {
		calories = (TextView) findViewById(R.id.calories);
		distance = (TextView) findViewById(R.id.distance);
		speed = (TextView) findViewById(R.id.speed);
		pace = (TextView) findViewById(R.id.pace);
		altitude = (TextView) findViewById(R.id.altitude);
	}

	/**
	 * Initialize and set listener for start workout button.
	 */
	private void activateStartButton() {
		startTimerButton = (Button) findViewById(R.id.time_button);
		startTimerButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (workoutState == ENDED) { // if workout not started
					// set workout state
					workoutState = STARTED;

					// TODO: should add a "lock screen" button as screen cannot
					// be turned off if we still want accelerometer data.

					// enable location updates for MyLocationOverlay class for
					// updating blue dot location on map.
					myLocationOverlay.enableMyLocation();

					// display hitherto hidden end button
					endButton.setVisibility(View.VISIBLE);

					// start helper services
					startService(new Intent(workoutContext, WAVE.class));
					startService(new Intent(workoutContext, WorkoutHelper.class));

					// start counting kcals
					WAVE.startKCal(workoutContext);

					// Timer Control
					mHandler.postDelayed(runTimer, 1);
					workoutRunStartTime = SystemClock.uptimeMillis();

					updateInfo();
				} else if (workoutState == STARTED) { // if workout running
					pauseWorkout();
				} else if (workoutState == PAUSED) { // if workout paused
					resumeWorkout();
				}
			}
		});
	}

	/**
	 * Initialize and set listener for end workout button.
	 */
	private void activateEndButton() {
		endButton = (Button) findViewById(R.id.end_button);
		endButton.setVisibility(View.INVISIBLE);
		endButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				pauseWorkout();

				// show popup window with options on finishing workout
				endDialog(false);
			}
		});
	}

	/**
	 * Helps a user enable GPS if it's not on.
	 */
	private void gpsSetup() {
		gpsStatus = (TextView) findViewById(R.id.GPS_status);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// if GPS is not enabled, enable it.
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			gpsStatus.setText("GPS is disabled");
			gpsStatus.setTextColor(-65536);
			Toast.makeText(
					getApplicationContext(),
					"GPS is disabled. To enable all functions of the application, please toggle the GPS.",
					Toast.LENGTH_LONG).show();
		} else {
			gpsStatus.setText("GPS is enabled");
			gpsStatus.setTextColor(-8983040);
		}

		Button gpsSettings = (Button) findViewById(R.id.settings_button);
		gpsSettings.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// opens the GPS settings activity window
				Intent openGPS = new Intent(Settings.ACTION_SECURITY_SETTINGS);
				startActivity(openGPS);
			}
		});
	}

	/**
	 * Initializes and sets up the map view. Does some pre-processing to try to
	 * center map about last known geo location.
	 */
	@SuppressWarnings("unused")
	private void mapSetup() {
		mapView = (MapView) findViewById(R.id.mapView1);
		mapView.setBuiltInZoomControls(true);
		mapOverlays = mapView.getOverlays();
		mapController = mapView.getController();
		drawable = this.getResources().getDrawable(R.drawable.circledot2);
		itemizedOverlay = new RouteItemizedOverlay(drawable);
		myLocationOverlay = new MyLocationOverlay(workoutContext, mapView);

		viewstate = MAPVIEW;

		// set default map
		ArrayList<GeoPoint> tempGPList = new ArrayList<GeoPoint>();
		Location pastLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (pastLocation != null) {
			tempGPList.add(new GeoPoint(
					(int) (pastLocation.getLatitude() * 1E6),
					(int) (pastLocation.getLongitude() * 1E6)));
		}
		RouteItemizedOverlay.animateAndZoom(mapController, tempGPList, false);
		// mapView.invalidate();
		
		final Button track = (Button) findViewById(R.id.centermap_button);
		track.setVisibility(View.GONE);

		// button for live traffic
		final Button traffic = (Button) findViewById(R.id.traffic);
		traffic.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (trafficing) { // disable traffic feature on map
					trafficing = false;
					traffic.setText("Enable Traffic");
					mapView.setTraffic(false);
				} else { // enable traffic feature on map
					trafficing = true;
					traffic.setText("Disable Traffic");
					mapView.setTraffic(true);
				}
				mapView.invalidate();
			}
		});

		// button for toggling between map/gps view.
		// initialize map to default to "map view".
		// TODO: eventually make this a user option to default to whatever she
		// chooses.
		viewstate = MAPVIEW;
		mapView.setSatellite(false);
		final Button view = (Button) findViewById(R.id.view1);
		view.setText("View Sat");
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
				// there's also STREETVIEW, but it doesn't seem to allow access
				// to viewing the street photos.
				mapView.invalidate();
			}
		});
	}

	/**
	 * pause workout and stop kcal and initiate new gps route
	 */
	private void pauseWorkout() {
		workoutState = PAUSED;

		// Timer Control
		workoutPauseStartTime = SystemClock.uptimeMillis();

		// pause the accumulation of kcals
		WAVE.pauseKCal();

		// stop updating route and data
		mHandler.removeCallbacks(updateMapInfo);
		mHandler.removeCallbacks(updateDataInfoAndLists);

		// TODO: Need to somehow determine a pause in gps mapping of data.
		// For instance, if pause, a new gps route should be created, instead
		// of continuing from previous point. All routes should still be
		// plotted.
	}

	/**
	 * resume workout and stop kcal and initiate new gps route
	 */
	private void resumeWorkout() {
		workoutState = STARTED;

		// Timer Control
		totalWorkoutPauseTime += workoutPauseTime;

		// resume counting kcals
		WAVE.resumeKCal();

		// TODO: Need to somehow determine a resume in gps mapping of data.
		// For instance, if resume, a new gps route should be created, instead
		// of continuing from previous point. All routes should still be
		// plotted.

		// need to resume updating info sidebar
		updateInfo();
	}

	// Timer Control
	// Please refer to state space diagram (timer_diagram.png) in outer folder.
	private Runnable runTimer = new Runnable() {
		public void run() {
			long curTime = SystemClock.uptimeMillis();
			String startButtonText = "";
			if (workoutState == STARTED) {
				totalWorkoutRunTime = curTime - workoutRunStartTime
						- totalWorkoutPauseTime;
				startButtonText = "Pause";
			} else if (workoutState == PAUSED) {
				workoutPauseTime = curTime - workoutPauseStartTime;
				startButtonText = "Resume";
			} else {
				return; // do nothing and exit
			}

			startTimerButton.setText(startButtonText + ": "
					+ Utils.convertMillisToTime(totalWorkoutRunTime));
			mHandler.postDelayed(runTimer, 100);
		}
	};

	/**
	 * Call this method to update all on-screen map routes and data and saved
	 * data lists.
	 */
	private void updateInfo() {
		// Data Control
		mHandler.postDelayed(updateDataInfoAndLists, 1);

		// Map Control
		mHandler.postDelayed(updateMapInfo, 1);
	}

	// Called by updateInfo().
	// Update the data lists with periodic information at UPDATEFREQUENCY.
	// This allows for data analysis comparisons of any attribute over any other
	// attribute.
	private Runnable updateDataInfoAndLists = new Runnable() {
		public void run() {
			if (workoutState == STARTED) { // this will only post if workout
											// running
				// append data to lists
				if (curGP != null) {
					gpList.add(new GeoPoint_Time(curGP));
				} else {
					gpList.add(new GeoPoint_Time(new GeoPoint(-1, -1)));
				}
				caloriesList.add((Float) Utils.validateFloat(kCals));
				speedList.add((Float) Utils.validateFloat(mySpeed));
				distanceList.add((Float) Utils.validateFloat(myDistance));
				paceList.add((Float) Utils.validateFloat(myPace));
				altitudeList.add((Float) Utils.validateFloat(myAltitude));
				timeList.add((Long) totalWorkoutRunTime / 1000);

				updateWorkoutInfo();

				// TODO: eventually need to give user ability to choose between
				// update frequencies.
				// run this again in UPDATEFREQUENCY milliseconds.
				mHandler.postDelayed(updateDataInfoAndLists, UPDATEFREQUENCY);
			}
		}
	};

	// Called by the runnable updateDataInfoAndLists. No need to directly call.
	// Update the workout sidebar information with latest data we just saved.
	public void updateWorkoutInfo() {
		new Thread(new Runnable() {
			public void run() {
				updateWorkout.sendEmptyMessage(0);
			}
		}).start();
	}

	// Called by updateWorkoutInfo().
	// Updates the side-bar data TextViews.
	private Handler updateWorkout = new Handler() {
		public void handleMessage(Message msg) {
			// TODO: eventually need to allow user to choose what metric format
			// to use. For example, American or SI units. Currently defaults to
			// SI units.

			int length = 4;

			mySpeed = WAVE.curSpeed * (float) 3.6; // conversion from m/s to
													// km/hr
			if (mySpeed != prevSpeed) {
				prevSpeed = mySpeed;
				speed.setText(Utils.truncate(mySpeed, "0.00", length, false));
			}

			myPace = (float) 60.0 / mySpeed;
			if (myPace != prevPace) {
				prevPace = myPace;
				pace.setText(Utils.truncate(myPace, "0.00", length, false));
				if (mySpeed < 0.5) { // need this for pace since setting pace to
										// infinity uniquely requires mySpeed to
										// be 0, which different from other
										// values.
					pace.setText(INFINITY_SYMBOL);
				}
			}

			kCals = (float) WAVE.getKCal();
			if (kCals != prevCal) {
				prevCal = kCals;
				calories.setText(Utils.truncate(kCals, "0.00", length, false));
			}

			myDistance = WAVE.distance;
			if (myDistance != prevDistance) {
				prevDistance = myDistance;
				distance.setText(Utils.truncate(myDistance, "0.00", length,
						false));
			}

			myAltitude = (float) WAVE.curAltitude;
			if (myAltitude != prevAltitude) {
				prevAltitude = myAltitude;
				altitude.setText(Utils.truncate(myAltitude, "0.00", length,
						false));
			}
		}
	};

	// Called by updateInfo().
	// Checks for a new GeoPoint. If so, calls UpdateMapInfo() to update map.
	private Runnable updateMapInfo = new Runnable() {
		public void run() {
			GeoPoint tempGP = WAVE.curGeoPoint;
			if (tempGP != null && tempGP != prevGP) { // ensure geopoint isn't
				curGP = tempGP;
				Log.d(TAG, "GeoPoint has been updated. Current GeoPoint: "
						+ curGP);
				updateMapInfo(curGP);
			}

			mHandler.postDelayed(updateMapInfo, 100);
		}
	};

	public static Lock lock = new ReentrantLock();

	/**
	 * Takes in a GeoPoint and adds it to the route, and then draws it.
	 * 
	 * @param currentGP
	 */
	public void updateMapInfo(final GeoPoint currentGP) {
		// TODO: update map with locations other than current route points based
		// on activity classification. For example: if running, show nearby
		// refreshment/food locations, which could potentially be from Yelp's
		// API.

		// TODO: optimize map route drawing here or in RouteItemizedOverlay.
		// Currently starts lagging when route becomes relatively long (think in
		// the tens of minutes of GeoPoint locations).

		prevGP = currentGP;

		geoPoints.add(currentGP);
		itemizedOverlay.setGPList(geoPoints);

		// add GeoPoint to overlay
		lock.lock();
		mapOverlays = mapView.getOverlays();
		OverlayItem overlayItem = new OverlayItem(currentGP, "", "");
		itemizedOverlay.addOverlayItem(overlayItem);
		lock.unlock();

		// since RouteItemizedOverlay draws every map point every time anyway,
		// remove all old map overlays to minimize lag, and then add the new
		// overlay.
		lock.lock();
		if (mapOverlays.size() > 0) {
			mapOverlays.clear();
		}
		mapOverlays.add(itemizedOverlay);
		mapOverlays.add(myLocationOverlay);
		lock.unlock();

		// invalidate to call RouteItemizedOverlay onDraw()
		mapView.invalidate();

		if (tracking) {
			RouteItemizedOverlay.animateAndZoom(mapController, geoPoints, true);
		}
	}

	/**
	 * Brings up a popup asking whether to save/don't save/cancel and
	 * corresponding instructions.
	 * 
	 * @param finishActivity
	 *            Whether this activity should exit to original CalFit splash
	 *            page if "save" or "don't save" is selected. Should be TRUE
	 *            only for the "onKeyDown()" back button feature
	 */
	private void endDialog(final boolean finishActivity) {
		// boolean for preventing updateNotification from showing in taskbar
		// while dialog is shown.
		endDialog_on = true;

		AlertDialog alertDialog = new AlertDialog.Builder(workoutContext)
				.create();
		alertDialog.setTitle("End Workout Confirmation");
		alertDialog
				.setMessage("Would you like to save this workout to history?\n\n(Note: Saving longer workouts may take some time)");
		alertDialog.setButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// call endworkout() with save to database
				endWorkout(true, finishActivity);

				endDialog_on = false;
			}
		});
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				resumeWorkout();

				endDialog_on = false;
			}
		});
		alertDialog.setButton3("Don't Save",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// call endworkout() without save to database
						endWorkout(false, finishActivity);

						endDialog_on = false;
					}
				});
		alertDialog.show();
	}

	/**
	 * Ends the current workout, saves data to database if requested, reset all
	 * variables, and finishes activity if necessary.
	 * 
	 * @param saveWorkout
	 *            Whether or not to save workout to internal database
	 * @param finishActivity
	 *            Whether or not to kill this Workout activity
	 */
	private void endWorkout(boolean saveWorkout, boolean finishActivity) {
		workoutState = ENDED;

		// stop helper services
		WAVE.stopKCal();
		stopService(new Intent(workoutContext, WAVE.class));
		stopService(new Intent(workoutContext, WorkoutHelper.class));

		if (saveWorkout) {
			saveData();
		}

		// reset all global variables for next workout.
		resetData();

		// kill this workout activity
		if (finishActivity) {
			this.finish();
		}
	}

	/**
	 * Finalize data and saves data to internal sqlite database and to file on
	 * SD card.
	 */
	private void saveData() {
		// initialize new dbadatper
		dbHelper = new CalFitDBAdapter(this);

		// calcuate average speed for database
		averageSpeed = myDistance
				/ (totalWorkoutRunTime / ((float) 1000 * 3600)); // convert
																	// timeInMillis
																	// to hours
		if (Float.isInfinite(averageSpeed) || Float.isNaN(averageSpeed)) { // sanity
																			// check
			averageSpeed = 0;
		}

		// get elevation change for database
		float altitude_gain = elevationChange(altitudeList, gpList);

		// get last geopoint
		GeoPoint lastGeoPoint;
		if (geoPoints.size() > 0) {
			lastGeoPoint = geoPoints.get(geoPoints.size() - 1);
		} else {
			lastGeoPoint = null;
		}

		// finalize all data so that we can run database and http post
		// operations in other threads
		// final long finalRunTime = totalWorkoutRunTime, finalUpdateFreq =
		// UPDATEFREQUENCY;
		// final float finalAltitudeGain = altitude_gain, finalKcals = kCals,
		// finalAvgSpeed = averageSpeed,
		// finalDist = myDistance;
		// final ArrayList<Float> finalCaloriesList = (ArrayList<Float>)
		// caloriesList.clone(),
		// finalSpeedList = (ArrayList<Float>) speedList.clone(),
		// finalDistanceList = (ArrayList<Float>) distanceList.clone(),
		// finalPaceList = (ArrayList<Float>) paceList.clone(),
		// finalAltitudeList = (ArrayList<Float>) altitudeList.clone();
		// final ArrayList<GeoPoint> finalGpList = (ArrayList<GeoPoint>)
		// gpList.clone(),
		// finalGeoPoints = (ArrayList<GeoPoint>) geoPoints.clone();

		// get IMEI to uniquely identify users' posts to the server
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
		if (TextUtils.isEmpty(imei)) {
			imei = "0";
		}
		// Returns the current system time in milliseconds since January 1, 1970
		// 00:00:00 UTC.
		// this is the clock on the device as set by user/network.
		long timestamp = System.currentTimeMillis();

		// save to file
		// write the data to file on the device's SD card
		Log.i(getClass().getSimpleName(), "calling write ");
		writeToFile(imei, timestamp, totalWorkoutRunTime, lastGeoPoint,
				averageSpeed, altitude_gain, kCals);

		// postToHTTP(imei, timestamp, totalWorkoutRunTime, lastGeoPoint,
		// averageSpeed, lastAltitude, kCals);

		// save to database
		// TODO: thread this? only problem that might be encountered is that if
		// a person starts another workout and tries to save a new workout
		// before the current one is completely saved. Thus, would need to
		// handle that case as well.
		// Toast.makeText(getApplicationContext(),
		// "Saving data to database... Long workouts may take a few moments to appear in history.",
		// Toast.LENGTH_LONG).show();
		// new Thread() {
		// public void run() {
		// save to sqlite database on phone
		try {
			dbHelper.open();
			dbHelper.insertWorkout(1, Utils.getDate(), totalWorkoutRunTime,
					UPDATEFREQUENCY, kCals, averageSpeed, myDistance,
					altitude_gain, caloriesList, speedList, distanceList,
					paceList, altitudeList, gpList);
			// mDbHelper.insertWorkout(1, Utils.getDate(), finalRunTime,
			// finalUpdateFreq, finalKcals, finalAvgSpeed, finalDist,
			// finalAltitudeGain,
			// finalCaloriesList, finalSpeedList, finalDistanceList,
			// finalPaceList, finalAltitudeList, finalGpList, finalGeoPoints);
			dbHelper.close();
		} catch (Exception e) {
			// TODO: tell user save to database fail.
		}
		// }
		// }.start();
	}

	/**
	 * Write data to File on SD card.
	 * 
	 * @param imei
	 * @param timestamp
	 * @param totalWorkoutRunTime
	 * @param geoPoint
	 * @param averageSpeed
	 * @param elevationChange
	 * @param kCals
	 */
	private void writeToFile(final String imei, final long timestamp,
			final long totalWorkoutRunTime, final GeoPoint geoPoint,
			final float averageSpeed, final float elevationChange,
			final float kCals) {
		new Thread() {
			public void run() {

				/*
				 * // works OK with Phil's server... Log.d(TAG,
				 * "trying to post data."); HttpClient httpclient = new
				 * DefaultHttpClient(); HttpPost httppost = new HttpPost(
				 * "http://warm-wind-50.heroku.com/cal_fit_workouts?user_credentials=s2Z35hA1cnj0Neac3Tnb"
				 * );
				 * 
				 * try { // Add your data List<NameValuePair> nameValuePairs =
				 * new ArrayList<NameValuePair>(2); nameValuePairs.add(new
				 * BasicNameValuePair("cal_fit_workout[imei]", imei));
				 * nameValuePairs.add(new
				 * BasicNameValuePair("cal_fit_workout[workout_end_timestamp]",
				 * ((Long)(timestamp)).toString())); nameValuePairs.add(new
				 * BasicNameValuePair("cal_fit_workout[duration]",
				 * ((Long)(totalWorkoutRunTime)).toString()));
				 * nameValuePairs.add(new
				 * BasicNameValuePair("cal_fit_workout[latitude]",
				 * geofmt.format(geoPoint.getLatitudeE6())));
				 * nameValuePairs.add(new
				 * BasicNameValuePair("cal_fit_workout[longitude]",
				 * geofmt.format(geoPoint.getLongitudeE6())));
				 * nameValuePairs.add(new
				 * BasicNameValuePair("cal_fit_workout[average_speed]",
				 * genfmt.format(averageSpeed))); nameValuePairs.add(new
				 * BasicNameValuePair("cal_fit_workout[elevation_change]",
				 * genfmt.format(elevationChange))); nameValuePairs.add(new
				 * BasicNameValuePair("cal_fit_workout[kcals]",
				 * genfmt.format(kCals))); nameValuePairs.add(new
				 * BasicNameValuePair("commit", "Create"));
				 * httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				 * 
				 * 
				 * // Execute HTTP Post Request ResponseHandler<String>
				 * responseHandler = new BasicResponseHandler(); String
				 * responseBody = httpclient.execute(httppost, responseHandler);
				 * Log.d(TAG, "HTTP post success." + responseBody ); } catch
				 * (Exception e) { Log.d(TAG, "HTTP post error: " +
				 * e.getMessage()); }
				 */

				Log.i(getClass().getSimpleName(), "starting write ");

				GeoPoint tempgeopt = new GeoPoint(0, 0);

				if (geoPoint != null) {
					tempgeopt = geoPoint;
				}

				String str = imei + "," + ((Long) (timestamp)).toString() + ","
						+ ((Long) (totalWorkoutRunTime)).toString() + ","
						+ geofmt.format(tempgeopt.getLatitudeE6()) + ","
						+ geofmt.format(tempgeopt.getLongitudeE6()) + ","
						+ genfmt.format(averageSpeed) + ","
						+ genfmt.format(elevationChange) + ","
						+ genfmt.format(kCals) + "\n";

				try {
					File root = Environment.getExternalStorageDirectory();
					if (root.canWrite()) {
						File myfile = new File(root, "CalFitexp.txt");
						myfile.createNewFile();
						FileWriter mywriter = new FileWriter(myfile, true);
						BufferedWriter out = new BufferedWriter(mywriter);
						// use out.write statements to write data...
						out.write(str);
						out.close();
					} else {
						Log.i(getClass().getSimpleName(),
								"Could not write to root dir ");
					}

				} catch (IOException e) {
					Log.e(getClass().getSimpleName(), "Could not write file "
							+ e.getMessage());
				}
			}
		}.start();
	}

	/**
	 * Post to HTTP
	 * 
	 * @param imei
	 * @param timestamp
	 * @param totalWorkoutRunTime
	 * @param geoPoint
	 * @param averageSpeed
	 * @param elevationChange
	 * @param kCals
	 */
	@SuppressWarnings("unused")
	private void postToHTTP(final String imei, final long timestamp,
			final long totalWorkoutRunTime, final GeoPoint geoPoint,
			final float averageSpeed, final float elevationChange,
			final float kCals) {
		new Thread() {
			public void run() {

				// works OK with Phil's server...
				Log.d(TAG, "trying to post data.");
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						"http://warm-wind-50.heroku.com/cal_fit_workouts?user_credentials=s2Z35hA1cnj0Neac3Tnb");
				// HttpPost httppost = new
				// HttpPost("http://calfitd.dyndns.org/PHAST/CalFitd/index.php");

				try {
					// Add your data
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair(
							"cal_fit_workout[imei]", imei));
					nameValuePairs.add(new BasicNameValuePair(
							"cal_fit_workout[workout_end_timestamp]",
							((Long) (timestamp)).toString()));
					nameValuePairs.add(new BasicNameValuePair(
							"cal_fit_workout[duration]",
							((Long) (totalWorkoutRunTime)).toString()));
					nameValuePairs.add(new BasicNameValuePair(
							"cal_fit_workout[latitude]", geofmt.format(geoPoint
									.getLatitudeE6())));
					nameValuePairs.add(new BasicNameValuePair(
							"cal_fit_workout[longitude]", geofmt
									.format(geoPoint.getLongitudeE6())));
					nameValuePairs.add(new BasicNameValuePair(
							"cal_fit_workout[average_speed]", genfmt
									.format(averageSpeed)));
					nameValuePairs.add(new BasicNameValuePair(
							"cal_fit_workout[elevation_change]", genfmt
									.format(elevationChange)));
					nameValuePairs.add(new BasicNameValuePair(
							"cal_fit_workout[kcals]", genfmt.format(kCals)));
					nameValuePairs.add(new BasicNameValuePair("commit",
							"Create"));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					// Execute HTTP Post Request
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					String responseBody = httpclient.execute(httppost,
							responseHandler);
					Log.d(TAG, "HTTP post success." + responseBody);
				} catch (Exception e) {
					Log.d(TAG, "HTTP post error: " + e.getMessage());
				}
			}
		}.start();

		/*
		 * try {
		 * 
		 * // Reading from a URLConnection // URL is read through php on the
		 * server side; the values from // the URL are stored onto the server
		 * database. Only URL's with // a User-agent name of "calfit" are
		 * accepted. All other URL's // are ignored.
		 * 
		 * URL calfit = new URL("moteserver1.eecs.berkeley.edu/"); URLConnection
		 * cf = calfit.openConnection(); cf.setDoOutput(true);
		 * cf.setRequestProperty("User-agent", "calfit");
		 * 
		 * OutputStreamWriter out = new OutputStreamWriter(cf
		 * .getOutputStream()); out.write("getDate=" + getDate() +
		 * "totalTimeStr=" + totalWorkoutRunTime + "kCals=" + kCals +
		 * "averageSpeed=" + averageSpeed + "myDistance=" + myDistance +
		 * "caloriesList=" + arrayListToString(caloriesList) + "speedList=" +
		 * arrayListToString(speedList) + " distanceList=" +
		 * arrayListToString(distanceList) + "paceList=" +
		 * arrayListToString(paceList) + " altitudeList=" +
		 * arrayListToString(altitudeList) + "geoToString=" +
		 * geoToString(geoPoints) + "timeList=" + arrayListToString(timeList));
		 * out.close();
		 * 
		 * } catch (Exception e) {
		 * 
		 * }
		 */
	}

	/**
	 * Reset all data for next workout.
	 */
	private void resetData() {
		// reset workout buttons
		startTimerButton.setText("Start Workout");
		endButton.setVisibility(View.INVISIBLE);

		// stop timer, map updates, and data updates
		mHandler.removeCallbacks(runTimer);
		mHandler.removeCallbacks(updateMapInfo);
		mHandler.removeCallbacks(updateDataInfoAndLists);

		// reset timer vars
		totalWorkoutRunTime = 0;
		workoutRunStartTime = 0;
		workoutPauseTime = 0;
		workoutPauseStartTime = 0;
		totalWorkoutPauseTime = 0;

		// reset data TextViews
		calories.setText("0.00");
		distance.setText("0.00");
		speed.setText("0.00");
		pace.setText("0.00");
		altitude.setText("0.00");

		// reset data lists
		caloriesList.clear();
		speedList.clear();
		distanceList.clear();
		paceList.clear();
		altitudeList.clear();
		timeList.clear();

		// reset data vars
		prevAltitude = 0;
		prevDistance = 0;
		prevSpeed = 0;
		prevCal = 0;
		prevPace = 0;
		myDistance = 0;
		myAltitude = 0;
		mySpeed = 0;
		myPace = 0;
		kCals = 0;

		// reset map
		myLocationOverlay.disableMyLocation();
		mapOverlays.clear();
		geoPoints.clear();
		gpList.clear();
		itemizedOverlay.clearOverlayItems();
		itemizedOverlay.clearGPListItems();
	}

	/**
	 * Takes in an ArrayList of altitudes and GeoPoints to calculate and return
	 * the total elevation change (positive and negative) in a workout. The
	 * GeoPoint ArrayList is used to scratch out bad altitudes as any bad
	 * altitudes is stored as "0" in the altitude ArrayList.
	 * 
	 * @param altitudes
	 * @param gpList2
	 * @return
	 */
	public static float elevationChange(ArrayList<Float> altitudes,
			ArrayList<GeoPoint_Time> gpList2) {
		if (altitudes.size() == gpList2.size()) {
			float elevationChange = 0;
			boolean first = true;
			float startAlt = altitudes.get(0);
			for (int i = 0; i < altitudes.size(); i++) {
				GeoPoint tempGP = gpList2.get(i).getGeopoint();
				long GPlat = tempGP.getLatitudeE6();
				long GPlong = tempGP.getLongitudeE6();
				float curAlt = altitudes.get(i);
				if (first && !(GPlat == -1 || GPlong == -1) && curAlt != 0) {
					startAlt = curAlt;
					first = false;
				} else if (!first) {
					elevationChange += Math.abs((startAlt - curAlt));
					startAlt = curAlt;
				}
			}
			return (float) elevationChange;
		} else {
			if (altitudes.size() > 0) {
				return (float) altitudes.get(altitudes.size() - 1);
			}
			return (float) 0;
		}
	}

	/**
	 * Enables taskbar kcal count notification.
	 */
	private void enableNotification() {
		WorkoutHelper.enableNotification();
	}

	/**
	 * Disables taskbar kcal count notification.
	 */
	private void disableNotification() {
		WorkoutHelper.disableNotification();
	}
}
