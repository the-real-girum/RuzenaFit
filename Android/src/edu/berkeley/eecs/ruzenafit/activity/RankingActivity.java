package edu.berkeley.eecs.ruzenafit.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.access.SharedPreferencesHelper;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
import edu.berkeley.eecs.ruzenafit.util.Constants;

// FIXME: Change this whole activity to be a programmatic display of current rankings.
public class RankingActivity extends Activity {
	private static final String TAG = RankingActivity.class.getSimpleName();

	// Initializing variables
	ListView listView;
	String[] rankings;

	// Move this out of the controller, and into a network or DB layer
	// Change this one workout task to be used for all of the different network
	// buttonss

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ranking);
		Log.d(TAG, "Ranking Screen !!!!");

		listView = (ListView) findViewById(R.id.listviewRanking);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new String[] {"Loading rankings..."}));

		new FindRankingsAsyncTask().execute();
	}

	private class FindRankingsAsyncTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {

			rankings = new String[] { "test1", "test2", "test3" };

			listView.setAdapter(new ArrayAdapter<String>(
					getApplicationContext(),
					android.R.layout.simple_list_item_1, rankings));

			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}

}
