package edu.berkeley.eecs.ruzenafit;

import winterwell.jtwitter.Twitter;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class Twit extends Activity {
	static final String TAG = "StatusActivity";
	EditText editStatus;

	/** Called when the activity is first created. **/
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Log.d(TAG, "onCreated with Bundle" + bundle);

		setContentView(R.layout.twitter);

		//editStatus = (EditText) findViewById(R.id.edit_status);

	}

	public void onClick(View v) {
		String statusText = editStatus.getText().toString();

		Twitter twitter = new Twitter("student", "password");
		
		//twitter.setAPIRootUrl("http://yamba.markana.com/api");
		twitter.setStatus(statusText);
		
		Log.d(TAG, "onClicked with text: " + statusText);
	}
}
