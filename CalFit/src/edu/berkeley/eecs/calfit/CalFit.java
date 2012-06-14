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
	        
			// load personal page with workout and history tabs
	        Button personalWorkout = (Button) findViewById(R.id.personal_workout);
	        personalWorkout.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
					startActivity(new Intent(myContext, PersonalPage.class));
				}
			});
	        
	        Button competitiveWorkout = (Button) findViewById(R.id.competitive_workout);
	        competitiveWorkout.setVisibility(View.INVISIBLE);
/*	        competitiveWorkout.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
	    			Toast.makeText(getApplicationContext(), "Personal Workout allows you to SMS workout results to friends.  Additional competitive features of CalFit will be coming soon...", Toast.LENGTH_LONG).show();
				}
			});
			*/
	        
	        Button aboutBox = (Button) findViewById(R.id.about_box);
	        aboutBox.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
	           		// start the About view
	    			startActivity(new Intent(myContext, AboutActivity.class));
					
/*					// the intent will open our website.
					// TODO: need to change this to something specific to CalFit and this particular version.
					Intent i = new Intent();
					i.setAction(Intent.ACTION_VIEW);
					i.addCategory(Intent.CATEGORY_BROWSABLE);
					i.setData(Uri.parse("http://bsn.citris.berkeley.edu/"));
					startActivity(i);
					*/ 
				}
			});
        } else {
	        this.finish();
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
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