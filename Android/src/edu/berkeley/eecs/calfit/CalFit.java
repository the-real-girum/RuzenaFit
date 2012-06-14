/**
 * CalFit.java
 * @version 1.0
 * 
 * The initial splash screen.
 * 
 * @author Irving Lin, Curtis Wang, Edmund Seto
 */

package edu.berkeley.eecs.calfit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings.System;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TabHost;

public class CalFit extends Activity {
	public static Context myContext;
	public static final String TAG = "CalFit Activity";
	private ProgressDialog progressDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		// if somehow a new CalFit application is opened before current one is
		// closed (shouldn't, but does happen), kill it and open old one.
        if (!WorkoutHelper.workoutStarted && !WorkoutHelper.workoutOpened) {
	        setContentView(R.layout.main);
	        myContext = this;
	        
	        // TODO: be able to login/change users
	        // temporarily fix: create a fake user (only the first time using this application)
//	         Log.d(TAG, "DEVICE ID: " + System.getString(this.getContentResolver(), System.ANDROID_ID));
	        DBAdapter dbHelper = new DBAdapter(this);
	        try {
		        dbHelper.open();
		        String user = System.getString(this.getContentResolver(), System.ANDROID_ID);
		        Cursor c = dbHelper.getUser(user);
		        if (c == null) {
		        	dbHelper.insertUser(user, (float) 0, (float) 0);
		        }
		        c.close();
		        dbHelper.close();
			} catch (Exception e) {
				// TODO: tell user save to database fail.
			}
	        
			// load tracking page
	        Button tracking = (Button) findViewById(R.id.button1);
	        tracking.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
//					Log.d(TAG, "Before startActivity()");
					progressDialog = ProgressDialog.show(CalFit.this, "", "Loading... please wait");
					startActivity(new Intent(myContext, PersonalPage.class));
//					Log.d(TAG, "After startActivity()");
				}
			});
	        
	        // load ranking page
	        Button ranking = (Button) findViewById(R.id.button5);
	        ranking.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
	    			startActivity(new Intent(myContext, XRanking.class));
				}
			});
	        
	        // load preferences page
	        Button preferences = (Button) findViewById(R.id.button2);
	        preferences.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
	           		// start the About view
	    			startActivity(new Intent(myContext, XPreferences.class));
				}
			});
        } else {
	        this.finish();
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (progressDialog != null) {
    		progressDialog.hide();
    	}
    	
    	if (WorkoutHelper.workoutStarted) {
    		WorkoutHelper.disableNotification();
    	}
    }
    
    public static class PersonalPage extends TabActivity {
    	private final String TAG = "PersonalPage TabActivity";
    	public static TabHost tabHost;
    	public static Context context;
    	public static TabHost.TabSpec tabSpecWorkout, tabSpecHistory;
    	
    	@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.personal);
			
			context = this;

	        tabHost = getTabHost();
	        tabSpecWorkout = tabHost.newTabSpec("tab_test1").setIndicator("Workout").setContent(new Intent(this, Workout.class));
	        tabHost.addTab(tabSpecWorkout);
	        tabSpecHistory = tabHost.newTabSpec("tab_test2").setIndicator("History").setContent(new Intent(this, History.class));
		    tabHost.addTab(tabSpecHistory);
		    tabHost.setCurrentTab(0);
		    tabHost.getTabWidget().getChildAt(0).getLayoutParams().height = 50;
		    tabHost.getTabWidget().getChildAt(1).getLayoutParams().height = 50;
//		    Log.d(TAG, "current view id: " + mTabHost.getCurrentView().getId());
		    
//		    mTabHost.getCurrentTabView().setOnClickListener(onClickListener);
		    tabHost.getCurrentView().setId(R.layout.personal);
		    Log.d(TAG, "current view id: " + tabHost.getCurrentView().getId());
    	}

    	private OnClickListener onClickListener = new OnClickListener() {
    		public void onClick(final View v) {
    			if (tabHost.getCurrentTab() == 0) {
    			    tabHost.getCurrentView().setId(R.layout.workout);
    			    Log.d(TAG, "current view id: " + tabHost.getCurrentView().getId());
    			} else {
    			    tabHost.getCurrentView().setId(R.layout.history);
    			    Log.d(TAG, "current view id: " + tabHost.getCurrentView().getId());
    			}
    		}
    	};
    }
}    