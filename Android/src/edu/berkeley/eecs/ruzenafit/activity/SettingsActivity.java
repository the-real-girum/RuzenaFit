package edu.berkeley.eecs.ruzenafit.activity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import edu.berkeley.eecs.ruzenafit.R;

public class SettingsActivity extends Activity {

		// Your Facebook APP ID
		private static String APP_ID = "187275568066559"; // Replace with your App
															// ID

		// Instance of Facebook Class
		private Facebook facebook = new Facebook(APP_ID);
		private AsyncFacebookRunner mAsyncRunner;
		String FILENAME = "AndroidSSO_data";
		private SharedPreferences mPrefs;

		// Buttons
		Button btnFbLogin;
		Button btnFbGetProfile;
		Button btnPostToWall;
		Button btnShowAccessTokens;

		
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.settings);

			btnFbLogin = (Button) findViewById(R.id.btn_fblogin);
			btnFbGetProfile = (Button) findViewById(R.id.btn_get_profile);
			btnPostToWall = (Button) findViewById(R.id.btn_fb_post_to_wall);
			btnShowAccessTokens = (Button) findViewById(R.id.btn_show_access_tokens);
			mAsyncRunner = new AsyncFacebookRunner(facebook);

			/**
			 * Login button Click event
			 * */
			btnFbLogin.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					Log.d("Image Button", "button Clicked");
					loginToFacebook();
				}
			});

			/**
			 * Getting facebook Profile info
			 * */
			btnFbGetProfile.setOnClickListener(new View.OnClickListener() {

				
				public void onClick(View v) {
					getProfileInformation();
				}
			});

			/**
			 * Posting to Facebook Wall
			 * */
			btnPostToWall.setOnClickListener(new View.OnClickListener() {

				
				public void onClick(View v) {
					postToWall();
				}
			});

			/**
			 * Showing Access Tokens
			 * */
			btnShowAccessTokens.setOnClickListener(new View.OnClickListener() {

				
				public void onClick(View v) {
					showAccessTokens();
				}
			});

		}

		/**
		 * Function to login into facebook
		 * */
		public void loginToFacebook() {

			mPrefs = getPreferences(MODE_PRIVATE);
			String access_token = mPrefs.getString("access_token", null);
			long expires = mPrefs.getLong("access_expires", 0);

			if (access_token != null) {
				facebook.setAccessToken(access_token);
				
				// Saving facebook info onto phone's internal DB
				saveFacebookInfoInternally();

				btnFbLogin.setVisibility(View.INVISIBLE);

				// Making get profile button visible
				btnFbGetProfile.setVisibility(View.VISIBLE);

				// Making post to wall visible
				btnPostToWall.setVisibility(View.VISIBLE);

				// Making show access tokens button visible
				btnShowAccessTokens.setVisibility(View.VISIBLE);

				Log.d("FB Sessions", "" + facebook.isSessionValid());
			}

			if (expires != 0) {
				facebook.setAccessExpires(expires);
			}

			if (!facebook.isSessionValid()) {
				facebook.authorize(this,
						new String[] { "email", "publish_stream" },
						new DialogListener() {

							
							public void onCancel() {
								// Function to handle cancel event
							}

							
							public void onComplete(Bundle values) {
								// Function to handle complete event
								// Edit Preferences and update facebook acess_token
								SharedPreferences.Editor editor = mPrefs.edit();
								editor.putString("access_token",
										facebook.getAccessToken());
								editor.putLong("access_expires",
										facebook.getAccessExpires());
								editor.commit();

								// Making Login button invisible
								btnFbLogin.setVisibility(View.INVISIBLE);

								// Making logout Button visible
								btnFbGetProfile.setVisibility(View.VISIBLE);

								// Making post to wall visible
								btnPostToWall.setVisibility(View.VISIBLE);

								// Making show access tokens button visible
								btnShowAccessTokens.setVisibility(View.VISIBLE);
								
								// Saving facebook info onto phone's internal DB
								saveFacebookInfoInternally();
							}

							
							public void onError(DialogError error) {
								// Function to handle error

							}

							
							public void onFacebookError(FacebookError fberror) {
								// Function to handle Facebook errors

							}

						});
			}
		}
		
		private void saveFacebookInfoInternally() {
			mAsyncRunner.request("me", new RequestListener() {
				
				public void onComplete(String response, Object state) {
					Log.d("Profile", response);
					String json = response;
					try {
						// Facebook Profile JSON data
						JSONObject profile = new JSONObject(json);

						// getting name of the user
						final String name = profile.getString("name");

						// getting email of the user
						final String email = profile.getString("email");

						// Save this FB info internally.
						SharedPreferences sharedPreferences = getSharedPreferences("RuzenaFitPrefs", 0);
						SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
						sharedPreferencesEditor.putString("userName", name);
						sharedPreferencesEditor.putString("userEmail", email);
						sharedPreferencesEditor.commit();
						
						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(getApplicationContext(),
										"Saved Facebook data into phone:  " +
										"Name: " + name + "\nEmail: " + email,
										Toast.LENGTH_LONG).show();
							}
						});

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				public void onMalformedURLException(MalformedURLException e, Object state) {
				}
				
				public void onIOException(IOException e, Object state) {
				}
				
				public void onFileNotFoundException(FileNotFoundException e, Object state) {
				}
				
				public void onFacebookError(FacebookError e, Object state) {
				}
			});
		}

		
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			facebook.authorizeCallback(requestCode, resultCode, data);
		}

		/**
		 * Get Profile information by making request to Facebook Graph API
		 * */
		public void getProfileInformation() {
			mAsyncRunner.request("me", new RequestListener() {
				
				public void onComplete(String response, Object state) {
					Log.d("Profile", response);
					String json = response;
					try {
						// Facebook Profile JSON data
						JSONObject profile = new JSONObject(json);

						// getting name of the user
						final String name = profile.getString("name");

						// getting email of the user
						final String email = profile.getString("email");

						runOnUiThread(new Runnable() {

							
							public void run() {
								Toast.makeText(getApplicationContext(),
										"Name: " + name + "\nEmail: " + email,
										Toast.LENGTH_LONG).show();
							}

						});

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				
				public void onIOException(IOException e, Object state) {
				}

				
				public void onFileNotFoundException(FileNotFoundException e,
						Object state) {
				}

				
				public void onMalformedURLException(MalformedURLException e,
						Object state) {
				}

				
				public void onFacebookError(FacebookError e, Object state) {
				}
			});
		}

		/**
		 * Function to post to facebook wall
		 * */
		public void postToWall() {
			// post on user's wall.
			facebook.dialog(this, "feed", new DialogListener() {

				
				public void onFacebookError(FacebookError e) {
				}

				
				public void onError(DialogError e) {
				}

				
				public void onComplete(Bundle values) {
				}

				
				public void onCancel() {
				}
			});

		}

		/**
		 * Function to show Access Tokens
		 * */
		public void showAccessTokens() {
			String access_token = facebook.getAccessToken();

			Toast.makeText(getApplicationContext(),
					"Access Token: " + access_token, Toast.LENGTH_LONG).show();
		}

		/**
		 * Function to Logout user from Facebook
		 * */
		public void logoutFromFacebook() {
			mAsyncRunner.logout(this, new RequestListener() {
				
				public void onComplete(String response, Object state) {
					Log.d("Logout from Facebook", response);
					if (Boolean.parseBoolean(response) == true) {
						runOnUiThread(new Runnable() {

							
							public void run() {
								// make Login button visible
								btnFbLogin.setVisibility(View.VISIBLE);

								// making all remaining buttons invisible
								btnFbGetProfile.setVisibility(View.INVISIBLE);
								btnPostToWall.setVisibility(View.INVISIBLE);
								btnShowAccessTokens.setVisibility(View.INVISIBLE);
							}

						});

					}
				}

				
				public void onIOException(IOException e, Object state) {
				}

				
				public void onFileNotFoundException(FileNotFoundException e,
						Object state) {
				}

				
				public void onMalformedURLException(MalformedURLException e,
						Object state) {
				}

				
				public void onFacebookError(FacebookError e, Object state) {
				}
			});
		}

	}

