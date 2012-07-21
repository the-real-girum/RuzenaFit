package edu.berkeley.eecs.ruzenafit.activity;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import edu.berkeley.eecs.ruzenafit.R;
import edu.berkeley.eecs.ruzenafit.model.User;
import edu.berkeley.eecs.ruzenafit.util.AndroidUtils;
import edu.berkeley.eecs.ruzenafit.util.Constants;

// TODO: Move this out of the controller, and into a network or DB layer
// Change this one workout task to be used for all of the different network
// buttonss
/**
 * This activity displays the current rankings of the RuzenaFit game.
 * 
 * @author gibssa
 */
public class RankingActivity extends Activity {
	private static final String TAG = RankingActivity.class.getSimpleName();
	
	private static final String RETRIEVE_WORKOUTS_URL = "http://ruzenafit.appspot.com/rest/ranking/getRankings";

	// Initializing variables
	ListView listView;
	User[] rankings;
	Button refreshRankings;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ranking);
		Log.d(TAG, "Ranking Screen !!!!");

		listView = (ListView) findViewById(R.id.listviewRanking);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, new String[] {"Loading rankings..."}));
		
		refreshRankings = (Button) findViewById(R.id.buttonRefreshRankings);
		refreshRankings.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (AndroidUtils.isOnline(getApplicationContext())) {
					new FindRankingsAsyncTask().execute();
				}
			}
		});

		// If the phone is online, then get the rankings from the server
		if (AndroidUtils.isOnline(getApplicationContext())) {
			new FindRankingsAsyncTask().execute();
		}
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		
		if (!AndroidUtils.isOnline(getApplicationContext())) {
			Toast.makeText(
					getApplicationContext(),
					Constants.NO_INTERNET_CONNECTION_MESSAGE,
					Toast.LENGTH_LONG).show();
		}
	}



	private class FindRankingsAsyncTask extends AsyncTask<Void, Void, User[]> {

		@Override
		protected User[] doInBackground(Void... params) {

			// Setup the GET Request
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet request = new HttpGet(RETRIEVE_WORKOUTS_URL);

			String responseString = null;

			// Execute the GET request.
			try {
				HttpResponse response = httpClient.execute(request);
				responseString = EntityUtils.toString(response.getEntity());
				Log.d(TAG, "HttpResponse: " + responseString);
			} catch (Exception e) {
				Log.e(TAG, "HTTP ERROR: " + e.getMessage());
			}
			
			try {
				// Parse the resulting JSON into the correct rankings array
				JSONArray rankingsJSONArray = new JSONArray(responseString);
				rankings = new User[rankingsJSONArray.length()];

				for (int i = 0; i < rankingsJSONArray.length(); i++) {
					
					JSONObject rankingJSONObject = rankingsJSONArray.getJSONObject(i);
					
					String userName = rankingJSONObject.getString(User.KEY_USER);
					double userScore = rankingJSONObject.getDouble(User.KEY_SCORE);
					
					rankings[i] = new User(userName, userScore);
				}
			} catch (JSONException e) {
				Log.e(TAG, "JSON exception: " + e.getMessage());
			}

			return rankings;
		}

		@Override
		protected void onPostExecute(User[] result) {
			super.onPostExecute(result);
			
			// "result" could be null if the internet connection sanity check failed
			if (result == null) {
				return;
			}
			
			// Sort the list
			Arrays.sort(result, new Comparator<User>() {
				public int compare(User lhs, User rhs) {
					return ((Double)rhs.getScore()).compareTo((Double)lhs.getScore());
				}
			});

			// Serialize and format the results
			String[] formattedResults = new String[result.length];
			for (int i = 0; i < formattedResults.length; i++) {
				
				// Truncate the "user score" double value into an integer here.
				formattedResults[i] = result[i].getName() + ": " + ((Double)result[i].getScore()).intValue() + " points";
			}
			
			// Update the activity's list to show the scores
			listView.setAdapter(new ArrayAdapter<String>(
					getApplicationContext(),
					android.R.layout.simple_list_item_1, formattedResults));
			
			Toast.makeText(getApplicationContext(), "Loaded rankings.", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}

}
