package edu.berkeley.eecs.ruzenafit;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class HomeActivity extends Activity {
	
	Button buttonRanking, buttonTracking, buttonPreferences;
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}

