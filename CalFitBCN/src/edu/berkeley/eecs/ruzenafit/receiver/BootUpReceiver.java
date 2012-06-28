package edu.berkeley.eecs.ruzenafit.receiver;

import edu.berkeley.eecs.ruzenafit.activity.WorkoutTrackerActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	            Intent i = new Intent(context, WorkoutTrackerActivity.class);  
	            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            context.startActivity(i);
	    }
}
