package edu.berkeley.eecs.ruzenafit.util;

import android.content.Context;
import android.net.ConnectivityManager;

public class AndroidUtils {
	
	/**
	 * Quick little helper method to check if the app is connected to the internet or not.
	 * @return
	 */
	public static boolean isOnline(Context context) {
	    ConnectivityManager connectivityManager =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	    return connectivityManager.getActiveNetworkInfo() != null && 
	       connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
}
