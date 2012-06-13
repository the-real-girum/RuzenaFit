package edu.berkeley.eecs.ruzenafit;

import edu.berkeley.eecs.ruzenafit.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class Preferences extends Activity {
	// Initializing variables
	RadioButton rbLow, rbMedium, rbHigh;
	TextView textOut;
	EditText getInput;

	private static String t = "Message";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);

		Log.d(t, "Preferences Screen !!!!");

		rbLow = (RadioButton) findViewById(R.id.radioButton1);
		rbMedium = (RadioButton) findViewById(R.id.radioButton2);
		rbHigh = (RadioButton) findViewById(R.id.radioButton3);

		textOut = (TextView) findViewById(R.id.textView2);
		// getInput = (EditText) findViewById(R.id.editText1);

		// Listening to button event
		rbLow.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
 
				textOut.setText("Low Preferences");

			}
		});

		// Listening to button event
		rbMedium.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				textOut.setText("Medium Preferences");

			}
		});

		// Listening to button event
		rbHigh.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				textOut.setText("High Preferences");
			}
		});

	}
}
