package edu.berkeley.eecs.ruzenafit.deprecated;
//package edu.berkeley.eecs.ruzenafit.legacy;
//
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteDatabase.CursorFactory;
//import android.database.sqlite.SQLiteOpenHelper;
//import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;
//import edu.berkeley.eecs.ruzenafit.util.Constants;
//
///**
// * Our custom extension of {@link SQLiteOpenHelper} to read/write to our
// * SQLite database internally on the phone.
// * 
// * Don't use this class outside of the /access package.
// * 
// * @author gibssa
// */
//public class DBConn extends SQLiteOpenHelper {
//
//	// SQL code needed to create the "workouts" table.
//	private static final String WORKOUT_TABLE_CREATE = null;
////			"CREATE TABLE " + Constants.WORKOUT_TABLE + " (" +
////					WorkoutTick.KEY_IMEI 				+ " TEXT, " +
////					WorkoutTick.KEY_KCALS 			+ " TEXT, " +
////					WorkoutTick.KEY_SYSTEM_TIME 		+ " TEXT, " +
////					WorkoutTick.KEY_LATITUDE 			+ " TEXT, " +
////					WorkoutTick.KEY_LONGITUDE			+ " TEXT, " +
////					WorkoutTick.KEY_SPEED 			+ " TEXT, " +
////					WorkoutTick.KEY_ALTITUDE 			+ " TEXT, " +
////					WorkoutTick.KEY_HAS_ACCURACY		+ " TEXT, " +
////					WorkoutTick.KEY_ACCURACY 			+ " TEXT, " +
////					WorkoutTick.KEY_ACCUM_MINUTE_V	+ " TEXT, " +
////					WorkoutTick.KEY_ACCUM_MINUTE_H	+ " TEXT, " +
////					WorkoutTick.KEY_GPS_TIME			+ " TEXT);";
//	
//	/** Required constructor */
//	public DBConn(Context context, String name,
//			CursorFactory factory, int version) {
//		super(context, name, factory, version);
//	}
//
//	@Override
//	public void onCreate(SQLiteDatabase db) {
//		// Simply execute the SQL code used to create the table
//		db.execSQL(WORKOUT_TABLE_CREATE);
//	}
//
//	@Override
//	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		// do nothing -- onUpgrade is a SQL concept involving updates of the DB schema
//	}
//	
//}
//
