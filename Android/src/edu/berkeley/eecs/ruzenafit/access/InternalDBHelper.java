package edu.berkeley.eecs.ruzenafit.access;


import edu.berkeley.eecs.ruzenafit.util.Constants;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class InternalDBHelper extends SQLiteOpenHelper {

	// SQL code needed to create the "workouts" table.
	private static final String WORKOUT_TABLE_CREATE = 
			"CREATE TABLE " + Constants.WORKOUT_TABLE + " (" +
					Constants.KEY_IMEI 				+ " TEXT, " +
					Constants.KEY_DELTA_KCALS 		+ " TEXT, " +
					Constants.KEY_SYSTEM_TIME 		+ " TEXT, " +
					Constants.KEY_LATITUDE 			+ " TEXT, " +
					Constants.KEY_LONGITUDE			+ " TEXT, " +
					Constants.KEY_SPEED 			+ " TEXT, " +
					Constants.KEY_ALTITUDE 			+ " TEXT, " +
					Constants.KEY_HAS_ACCURACY		+ " TEXT, " +
					Constants.KEY_ACCURACY 			+ " TEXT, " +
					Constants.KEY_ACCUM_MINUTE_V	+ " TEXT, " +
					Constants.KEY_ACCUM_MINUTE_H	+ " TEXT, " +
					Constants.KEY_GPS_TIME			+ " TEXT);";
	
	public InternalDBHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(WORKOUT_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// do nothing
	}
	
}










