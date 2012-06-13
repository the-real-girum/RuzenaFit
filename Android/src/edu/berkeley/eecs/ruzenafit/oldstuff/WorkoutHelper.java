package edu.berkeley.eecs.ruzenafit.oldstuff;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import edu.berkeley.eecs.ruzenafit.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class WorkoutHelper extends Service {
	public static final String TAG = "WorkoutHelper";

	public static boolean workoutStarted = false;
	public static boolean first = true;
	public static short workoutState = 0;
	public static long startTime = 0, workoutTime = 0;
	public static boolean save = false;
	public static NotificationManager notificationManager;
	public static final int NOTIFICATION_ID = 1;
	public static Context context;
	public static Context wContext;
	public static boolean workoutOpened = false;
	private static double prevKCal = 0.0;
	private static Handler handler = new Handler();
	public static WorkoutHelper workoutHelper;
	public static Intent workoutIntent;

	private static String genformat = "####0.00";
	private static DecimalFormat genfmt = new DecimalFormat( genformat, new DecimalFormatSymbols(Locale.US));
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		workoutStarted = true;
		workoutOpened = true;
		workoutState = 0;
		startTime = 0;
		workoutTime = 0;
		save = false;
		first = true;
		context = this;
		prevKCal = 0.0;
		wContext = Workout.workoutContext;
		workoutHelper = new WorkoutHelper();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		workoutHelper = null;
		workoutStarted = false;
		workoutState = 0;
		startTime = 0;
		workoutTime = 0;
		prevKCal = 0.0;
		save = false;
		first = true;
	}

	public static void enableNotification() {
		first = true;
		handler.postDelayed(updateNotification, 1);
	}
	
	public static void disableNotification() {
		notificationManager.cancelAll();
		handler.removeCallbacks(updateNotification);
	}
	
	private static Runnable updateNotification = new Runnable() {
		public void run() {
			double curKCal = WAVE.getKCal();
			if (prevKCal + 10 < curKCal || first) {
				first = false;
				prevKCal = curKCal;
				int icon = R.drawable.star_on;
				CharSequence tickerText = "CalFit: " + genfmt.format(WAVE.getKCal());
				long when = System.currentTimeMillis();
				Notification notification = new Notification(icon, tickerText, when);
				
				CharSequence contentTitle = "CalFit";
				CharSequence contentText = genfmt.format(WAVE.getKCal()) + " total kCals";
				Intent notificationIntent = new Intent(context, CalFit.class); // must use CalFit.class because this notification loads a new activity onto the stack, and if you call CalFit.class, it is hacked to auto kill it and brings about the previous activity layer, which in the case is the tabbed view personal page. you can't load perosnal page directly because it'll reset many of the global/instance/static variables that need to remain untouched for the duration of the application.
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
				
				notificationManager.notify(NOTIFICATION_ID, notification);
			}
			handler.postDelayed(updateNotification, 10000);
		}
	};
	
	public IBinder onBind(Intent arg0) {
		return null;
	}
}