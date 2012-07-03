package edu.berkeley.eecs.ruzenafit.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.access.InternalDBHelper;

public class RankingActivity extends Activity {
	private static final String TAG = RankingActivity.class.getSimpleName();

	// Initializing variables
	Button displayWorkouts, explicitSend;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ranking);
		Log.d(TAG, "Ranking Screen !!!!");

		displayWorkouts = (Button) findViewById(R.id.buttonSQLiteWorkouts);
		displayWorkouts.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Access layer.
				InternalDBHelper.retrieveWorkouts(getApplicationContext());
			}
		});

		explicitSend = (Button) findViewById(R.id.buttonExplcitSend);
		explicitSend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Network layer.
				sendDataToGAE();
			}
		});
	};

	// TODO: Move to access layer.
	private static void sendDataToGAE() {
//		ExternalDBHelper.submitDataToGAE(allWorkouts, userEmail, privacySetting);
	}

}
