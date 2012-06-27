/**
 * History.java
 * @version 0.2
 * 
 * Displays a list of recorded workouts and corresponding basic information.
 * 
 * @author Irving Lin, Curtis Wang, Edmund Seto
 */

package edu.berkeley.eecs.ruzenafit.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.access.CalFitDBAdapter;
import edu.berkeley.eecs.ruzenafit.access.ExternalDBHelper;
import edu.berkeley.eecs.ruzenafit.access.InternalDBHelper;
import edu.berkeley.eecs.ruzenafit.model.AnActualWorkoutModelX_X;
import edu.berkeley.eecs.ruzenafit.util.Constants;
import edu.berkeley.eecs.ruzenafit.util.Utils;
 
public class HistoryActivity extends ListActivity {
	private static CalFitDBAdapter mDbHelper;
	private final static String TAG = "History Activity";
	private static Context context;
	
	private Button sendData;
	private Button retrieveData;

	public static long rowId;
	
	/** The up-to-date workout history, once per this activity's resumption */
	private AnActualWorkoutModelX_X[] allWorkouts;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);		
		
		// TRUST-REU takeover.
		setupGAEConnectionButtons();
		
		mDbHelper = new CalFitDBAdapter(this);
		context = this;
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {			
				rowId = id;

				final CharSequence[] ITEMS = {"View", "SMS", "Post", "Delete", "Cancel"};
				final int VIEW = 0, SMS = 1, POST = 2, DELETE = 3;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Workout Options");
				builder.setItems(ITEMS, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == VIEW) {
							// TODO: make this better integrated into history
							// page. Shouldn't be loading a new activity. Need
							// to find way to reset activity of current tab to
							// this new one. If possible, will then also need to
							// set "back" button to return to original history
							// page view.
							startActivity(new Intent(context, ViewHistoryActivity.class)); // temporary solution.
						} else if (item == SMS) {
							try {
								mDbHelper.open();
								Cursor c = mDbHelper.getUserWorkout(1,rowId);
								String smsstring = "CalFit workout completed: "
									 + Utils.truncate(c.getFloat(c.getColumnIndex("total_calories")), "0.00", 4, false)
									 + " kCals "
									 + Utils.truncate(c.getFloat(c.getColumnIndex("total_distance")), "0.00", 4, false)
									 + " km";
								c.close();
								mDbHelper.close();
								Intent sendIntent = new Intent(Intent.ACTION_VIEW);
								sendIntent.putExtra("sms_body", smsstring);
								sendIntent.setType("vnd.android-dir/mms-sms");
								startActivity(sendIntent);
							} catch (Exception e) {
								// TODO: tell user save to database fail.
							}
						} else if (item == POST) {
							// post all the workouts from the "SD card/CalFitexperimentlog.txt" to server
			    			Toast.makeText(getApplicationContext(), "Sending workout data to server...", Toast.LENGTH_LONG).show();
							postFileToHTTP();
						} else if (item == DELETE) {
							try {
								mDbHelper.open();
								mDbHelper.deleteWorkout(rowId);
								fillData();
								mDbHelper.close();
							} catch (Exception e) {
								// TODO: tell user save to database fail.
							}
						} else {
							// "Cancel"... do nothing
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}

	
	/**
	 * Connects the GAE buttons to their respective methods in the Access layer.
	 */
	private void setupGAEConnectionButtons() {
		sendData = (Button) findViewById(R.id.buttonSubmit);
		retrieveData = (Button) findViewById(R.id.buttonRetrieveData);
		
		Log.d(TAG, "setupGAEConnectionButtons()");
		
		sendData.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				allWorkouts = InternalDBHelper.loadSQLiteWorkoutDataIntoMemory(mDbHelper.open());
				
				// Don't save workouts if there aren't any new ones.
				if (allWorkouts == null || allWorkouts.length == 0) {
					Toast.makeText(getApplicationContext(), "There are no workouts to send.", 3).show();
					return;
				}
				
				// Use app-wide SharedPreferences to get information needed by server.
				String facebookLoginEmail = getFacebookLoginEmail();
				String privacySetting = getPrivacySetting();
				
				// Execute code from the Access layer.
				if (facebookLoginEmail != null && privacySetting != null) {
					int successfulSubmissions = ExternalDBHelper.submitDataToGAE(allWorkouts, facebookLoginEmail, privacySetting);
					Toast.makeText(getApplicationContext(), "Submitted " + successfulSubmissions + 
							" new workouts to GAE", 5).show();
				}
				
			}
		});
		
		retrieveData.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				// Use SharedPreferences.  Abstracted to be DRY for method above.
				String facebookLoginEmail = getFacebookLoginEmail();
				
				// Execute code from the Access Layer.
				if (facebookLoginEmail != null) {
					allWorkouts = ExternalDBHelper.retrieveDataFromGAE(facebookLoginEmail);
					Toast.makeText(getApplicationContext(), "Got " + allWorkouts.length + " workouts from GAE", 5).show();
				}
			}
		});	
	}
	
	// TODO: Refactor this method and getPrivacySetting() to be DRY.
	private String getFacebookLoginEmail() {
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAMESPACE, 0);

		String facebookLoginName = sharedPreferences.getString("userEmail", Constants.UNDEFINED_USER_EMAIL);
		
		if (facebookLoginName.equals(Constants.UNDEFINED_USER_EMAIL)) {
			Toast.makeText(getApplicationContext(), "FB user email isn't set.", 3).show();
			return null;
		}
		
		return facebookLoginName;
	}
	
	// TODO: Refactor this method and getFacebookLoginEmail() to be DRY.
	private String getPrivacySetting() {
		SharedPreferences preferences = getSharedPreferences(Constants.SHARED_PREFS_NAMESPACE, 0);
		String privacySetting = preferences.getString("privacySetting", Constants.UNDEFINED_PRIVACY_SETTING);
		
		if (privacySetting.equals(Constants.UNDEFINED_PRIVACY_SETTING)) {
			Toast.makeText(getApplicationContext(), "Privacy settings aren't set.", 3).show();
			return null;
		}
		
		return privacySetting;
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			mDbHelper.open();
			fillData();
			
			// TRUST-REU takeover.
			this.allWorkouts = InternalDBHelper.loadSQLiteWorkoutDataIntoMemory(mDbHelper);
			
			mDbHelper.close();
		} catch (Exception e) {
			// TODO: tell user save to database fail.
			Log.e(TAG, "InternalDBHelper failed: " + e.getMessage());
		}
	}
	

	/**
	 * This method appears to use the DBHelper to fill up the ListView with the pertinent data.
	 */
	private void fillData() {
		// Get all of the workouts from the database 
		Cursor c = mDbHelper.getAllUserWorkouts(1);
		Log.d(TAG, "database entries (" + c.getCount() + "):");
		
		startManagingCursor(c);

		String[] from = new String[] { "date",
				"duration", "total_calories",
				"average_speed", "total_distance" };
		
		int[] to = new int[] { R.id.text1, R.id.text3, R.id.text5, R.id.text7,
				R.id.text9 };

		// Now create an array adapter and set it to display rows.
		PersonalizedCursorAdapter workouts = new PersonalizedCursorAdapter(this, R.layout.workout_row, c, from, to);
		setListAdapter(workouts);
	}
	
	public class PersonalizedCursorAdapter extends SimpleCursorAdapter { 
		PersonalizedCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) { 
            super(context, layout, cursor, from, to); 
            this.setViewBinder(new PersonalizedViewBinder()); 
        } 
	} 

	public class PersonalizedViewBinder implements SimpleCursorAdapter.ViewBinder { 
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        	if (view instanceof TextView) {
        		String tempValue = cursor.getString(columnIndex);
        		if (columnIndex == cursor.getColumnIndex("date")) {
        			((TextView) view).setText(tempValue.substring(5) + " ");
        			return true;
        		} else if (columnIndex == cursor.getColumnIndex("duration")) {
        			((TextView) view).setText(" " + Utils.convertMillisToTime(cursor.getInt(columnIndex)) + " ");
        			return true;
        		} else if (columnIndex == cursor.getColumnIndex("total_calories")) {
        			((TextView) view).setText(" " + Utils.setStringLength(Utils.removeBadAndSetDefault(tempValue), 4, " ") + " ");
        			return true;
        		} else if (columnIndex == cursor.getColumnIndex("average_speed")) {
        			((TextView) view).setText(" " + Utils.setStringLength(Utils.removeBadAndSetDefault(tempValue), 4, " ") + " ");
        			return true;
        		} else if (columnIndex == cursor.getColumnIndex("total_distance")) {
        			((TextView) view).setText(" " + Utils.setStringLength(Utils.removeBadAndSetDefault(tempValue), 4, " ") + " ");
        			return true;
        		}
        	}
//                if (view instanceof ImageView) { 
//                        String listType = cursor.getString(columnIndex); 
//                        if (listType.equals("wish_list")) { 
//                                ((ImageView) view).setImageResource(R.drawable.lander_plain); 
//                                return true; 
//                        } 
//                        if (listType.equals("todo_list")) { 
//                                ((ImageView) view).setImageResource(R.drawable.lander_firing); 
//                        } else { 
//                                ((ImageView) view).setImageResource(R.drawable.lander_crashed); 
//                        } 
//                        return true; 
//                } 
        	return false; 
        }
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			MainActivity.PersonalPage.tabHost.setCurrentTab(0);
			MainActivity.PersonalPage.tabHost.getCurrentView().setId(R.layout.workout);
		    Log.d(TAG, "current view id: " + MainActivity.PersonalPage.tabHost.getCurrentView().getId());
			return true;
		}
		return false;
	}
	
	
	private void postFileToHTTP() {
		new Thread() {
        	public void run() {
/*    			String str =  imei + "," +
    			((Long)(timestamp)).toString() + "," +
    			((Long)(totalWorkoutRunTime)).toString() + "," + 			
    			geofmt.format(geoPoint.getLatitudeE6()) + "," +
    			geofmt.format(geoPoint.getLongitudeE6()) + "," +
    			genfmt.format(averageSpeed) + "," +
    			genfmt.format(elevationChange) + "," +
    			genfmt.format(kCals) + "\n";
*/
				try {
				    File root = Environment.getExternalStorageDirectory();
			    	String authline = null;
				    if (root.canRead()){
				    	File authfile = new File(root, "CalFitauth.txt");
				    	BufferedReader bufRdr  = new BufferedReader(new FileReader(authfile));					
				    	 
				    	//read each line of text file
				    	authline = bufRdr.readLine();
				    	//close the file
				    	bufRdr.close();
				    }				    	
				    
				    if (root.canRead()){
				    	File file = new File(root, "CalFitexp.txt");
				    	BufferedReader bufRdr  = new BufferedReader(new FileReader(file));					
				    	String line = null;
				    	int col = 0;
				    	String [] values = new String [8];
				    	 
				    	//read each line of text file
				    	while((line = bufRdr.readLine()) != null)
				    	{
				    		StringTokenizer st = new StringTokenizer(line,",");
				    		while (st.hasMoreTokens())
				    		{
				    			//get next token and store it in the array
				    			values[col] = st.nextToken();
				    			col++;
				    		}
				    		if (col == 8) {
						    	// got good data, post to HTTP
				    			postToHTTP(authline, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7]);				    			
				    		}
				    		col = 0;
				    	}
				    	 
				    	//close the file
				    	bufRdr.close();

				    }
				} catch (IOException e) {
				    Log.e(getClass().getSimpleName(), "Could not read file " + e.getMessage());
				}
        	}
        }.start();
	}

	
	private void postToHTTP(final String auth, final String imei, final String timestamp, final String totalWorkoutRunTime, final String geoPointlat, final String geoPointlon, final String averageSpeed, final String elevationChange, final String kCals) {
		new Thread() {
        	public void run() {

        		// works OK with Phil's server...
				Log.d(TAG, "trying to post data.");
				HttpClient httpclient = new DefaultHttpClient();
			    HttpPost httppost = new HttpPost(auth);
//			    HttpPost httppost = new HttpPost("http://edmundseto.dyndns.org/PHAST/CalFit/index.php");
			  
			    try {
			        // Add your data
			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			        nameValuePairs.add(new BasicNameValuePair("cal_fit_workout[imei]", imei));
			        nameValuePairs.add(new BasicNameValuePair("cal_fit_workout[workout_end_timestamp]", timestamp));
			        nameValuePairs.add(new BasicNameValuePair("cal_fit_workout[duration]", totalWorkoutRunTime));
			        nameValuePairs.add(new BasicNameValuePair("cal_fit_workout[latitude]", geoPointlat));
			        nameValuePairs.add(new BasicNameValuePair("cal_fit_workout[longitude]", geoPointlon));
			        nameValuePairs.add(new BasicNameValuePair("cal_fit_workout[average_speed]", averageSpeed));
			        nameValuePairs.add(new BasicNameValuePair("cal_fit_workout[elevation_change]", elevationChange));
			        nameValuePairs.add(new BasicNameValuePair("cal_fit_workout[kcals]", kCals));
			        nameValuePairs.add(new BasicNameValuePair("commit", "Create"));
			        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			        
			        // Execute HTTP Post Request   
			        ResponseHandler<String> responseHandler = new BasicResponseHandler();
			        String responseBody = httpclient.execute(httppost, responseHandler);
					Log.d(TAG, "HTTP post success." + responseBody );
			    } catch (Exception e) {
					Log.d(TAG, "HTTP post error: " + e.getMessage());
			    }
        	}
        }.start();
	}	
}
