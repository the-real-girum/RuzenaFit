package edu.berkeley.eecs.ruzenafit;

import edu.berkeley.eecs.ruzenafit.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity {

	// Initializing variables
	Button buttonRanking, buttonTracking, buttonPreferences;

	private static String t = "Something";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		buttonTracking = (Button) findViewById(R.id.button1);
		buttonRanking = (Button) findViewById(R.id.button5);
		buttonPreferences = (Button) findViewById(R.id.button2);

		// Listening to button event
		buttonTracking.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// Starting a new Intent
				Intent nextScreen = new Intent(getApplicationContext(),
						Tracking.class);

				// Sending data to another Activity

				startActivity(nextScreen);

				Log.d(t, "Blah  !!!!");
			}
		});

		// Listening to button event
		buttonRanking.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// Starting a new Intent
				Intent nextScreen = new Intent(getApplicationContext(),
						Ranking.class);

				// Sending data to another Activity

				startActivity(nextScreen);

				Log.d(t, "Blah Blah  !!!!");
			}
		});

		// Listening to button event
		buttonPreferences.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// Starting a new Intent
				Intent nextScreen = new Intent(getApplicationContext(),
						Preferences.class);

				// Sending data to another Activity

				startActivity(nextScreen);

				Log.d(t, "Blah Blah Blah !!!!");
			}
		});

	}

}
