package edu.berkeley.eecs.ruzenafit.controller;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import edu.berkeley.eecs.ruzenafit.R;

public class RankingActivity extends Activity {
	// Initializing variables
	Button buttonRanking, buttonTracking, buttonPreferences;

	
	private static String t = "Message";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ranking);

                
				Log.d(t, "Ranking Screen !!!!");
			};
}
