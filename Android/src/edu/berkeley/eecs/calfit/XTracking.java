package edu.berkeley.eecs.calfit;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class XTracking extends Activity {
	// Initializing variables
	Button buttonRanking, buttonTracking, buttonPreferences;

	private static String t = "Message";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tracking);

		Log.d(t, "Tracking Screen !!!!");
	}
}
