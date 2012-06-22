/**
 * DBAdapter.java
 * @version 6.1
 * 
 * Manages the database and sets up a database helper.
 * 
 * @author Irving Lin, Curtis Wang
 */

package edu.berkeley.eecs.ruzenafit.access;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.maps.GeoPoint;

import edu.berkeley.eecs.ruzenafit.model.GeoPoint_Time;

public class CalFitDBAdapter {
	public static final String TAG = "DBAdapter";
    
    private static final String DATABASE_NAME = "history";
    private static final int DATABASE_VERSION = 6;
        
    private static final String USERS_TABLE = "users";
    private static final String CREATE_USERS_TABLE =
    	"CREATE TABLE " + USERS_TABLE + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, "
    		+ "weight REAL NOT NULL, height REAL NOT NULL );";
    
    private static final String WORKOUTS_TABLE = "workouts";
    private static final String CREATE_WORKOUTS_TABLE = 
    	"CREATE TABLE " + WORKOUTS_TABLE + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, "
    		+ "date TEXT NOT NULL, duration INTEGER NOT NULL, time_interval INTEGER NOT NULL, total_calories REAL NOT NULL, "
    		+ "average_speed REAL NOT NULL, total_distance REAL NOT NULL, altitude_gain REAL NOT NULL, FOREIGN KEY (user_id) REFERENCES users(_id) ON DELETE CASCADE );";
    
    private static final String DATASAMPLES_TABLE = "datasamples";
    private static final String CREATE_DATASAMPLES_TABLE =
    	"CREATE TABLE " + DATASAMPLES_TABLE + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, workout_id INTEGER, "
    		+ "kcals REAL NOT NULL, speed REAL NOT NULL, distance FLOAT NOT NULL, pace REAL NOT NULL, altitude REAL NOT NULL, "
    		+ "geopoint_lat INTEGER NOT NULL, geopoint_long INTEGER NOT NULL, geopoint_date TEXT NOT NULL, FOREIGN KEY (workout_id) REFERENCES workouts(_id) ON DELETE CASCADE );";
    
    private final Context context;
    private DatabaseHelper mDbHelper;
    private static SQLiteDatabase db;
    
	public CalFitDBAdapter(Context context) {
		this.context = context;
        mDbHelper = new DatabaseHelper(this.context);
	}
	
    private class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_USERS_TABLE);
			db.execSQL(CREATE_WORKOUTS_TABLE);
			db.execSQL(CREATE_DATASAMPLES_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + WORKOUTS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DATASAMPLES_TABLE);
            onCreate(db);
		}
	}
    
    /**
     * Opens the database.
     * 
     * @return
     * @throws SQLException
     */
	public CalFitDBAdapter open() throws SQLException {
		db = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Closes the database.
	 */
	public void close() {
		mDbHelper.close();
	}
	
	/**
	 * Insert user into users table.
	 * 
	 * @param username
	 * @param weight
	 * @param height
	 * @return
	 */
	public long insertUser(String username, float weight, float height) {
		ContentValues userValues = new ContentValues();
		
		userValues.put("username", username);
		userValues.put("weight", weight);
		userValues.put("height", height);
		
		try {
			return db.insertOrThrow(USERS_TABLE, null, userValues);
		} catch (Exception e) {
			Log.e(TAG, "insert into " + USERS_TABLE + " table failed...");
			Log.e(TAG, e.getMessage());
			return -1;
		}
	}
	
	/**
	 * Insert workout data into workouts and datasamples databases.
	 * 
	 * @param user_id
	 * @param date
	 * @param duration
	 * @param time_interval
	 * @param total_calories
	 * @param average_speed
	 * @param total_distance
	 * @param altitude_gain
	 * @param calories
	 * @param speeds
	 * @param distances
	 * @param paces
	 * @param altitudes
	 * @param gpList
	 * 
	 * @return
	 */
	public long insertWorkout(long user_id, String date, long duration, long time_interval, 
			float total_calories, float average_speed, float total_distance, float altitude_gain,
			ArrayList<Float> calories, ArrayList<Float> speeds, ArrayList<Float> distances,
			ArrayList<Float> paces, ArrayList<Float> altitudes, ArrayList<GeoPoint_Time> gpList) {
		long row = 0;
		
		// insert basic workout data into workouts table
		ContentValues workoutValues = new ContentValues();
		workoutValues.put("user_id", user_id);
		workoutValues.put("date", date);
		workoutValues.put("duration", duration);
		workoutValues.put("time_interval", time_interval);
		workoutValues.put("total_calories", total_calories);
		workoutValues.put("average_speed", average_speed);
		workoutValues.put("total_distance", total_distance);
		workoutValues.put("altitude_gain", altitude_gain);
		try {
			row = db.insert(WORKOUTS_TABLE, null, workoutValues);
		} catch (Exception e) {
			Log.e(TAG, "insert into " + WORKOUTS_TABLE + " table failed...");
			Log.e(TAG, e.getMessage());
			return -1;
		}
		
		// insert workout comprehensive sample data into datasamples table
		int size = calories.size();
		if (speeds.size() == size && distances.size() == size && paces.size() == size && altitudes.size() == size && gpList.size() == size) {
			db.beginTransaction();
			try {
				long workoutID = row;
				for (int i = 0; i < size; i++) {
					row = insertDatasamples(workoutID, calories.get(i), speeds.get(i), distances.get(i), paces.get(i), altitudes.get(i), gpList.get(i));
				}
				db.setTransactionSuccessful();
				return workoutID;
			} catch (Exception e) {
				Log.e(TAG, "insert into " + DATASAMPLES_TABLE + " table failed...");
				Log.e(TAG, e.getMessage());
				return -1;
			} finally {
				db.endTransaction();
			}
		} else {
			Log.e(TAG, "insert into " + DATASAMPLES_TABLE + " table failed...");
			Log.e(TAG, "arraylist sizes don't match up.");
			return -1;
		}
    }
    
	public long insertDatasamples(long workout_id, float kcals, float speed, float distance, float pace, float altitude, GeoPoint_Time geoPoint_Time) throws Exception {
		ContentValues datasamplesValues = new ContentValues();

		datasamplesValues.put("workout_id", workout_id);
		datasamplesValues.put("kcals", kcals);
		datasamplesValues.put("speed", speed);
		datasamplesValues.put("distance", distance);
		datasamplesValues.put("pace", pace);
		datasamplesValues.put("altitude", altitude);
		datasamplesValues.put("geopoint_lat", geoPoint_Time.getGeopoint().getLatitudeE6());
		datasamplesValues.put("geopoint_long", geoPoint_Time.getGeopoint().getLongitudeE6());
		datasamplesValues.put("geopoint_date", geoPoint_Time.getDate());
		
		return db.insert(DATASAMPLES_TABLE, null, datasamplesValues);
	}
	
	/**
	 * Taking in a username String, find user.
	 * 
	 * @param username
	 * @return
	 */
	public Cursor getUser(String username) {
		Cursor tempCursor = db.rawQuery("SELECT * FROM users U WHERE U.username='" + username + "';", null);
        if (tempCursor != null) {
        	tempCursor.moveToFirst();
        }
        return tempCursor;
	}
	
	/**
	 * Taking in a user_id, find user.
	 * 
	 * @param username
	 * @return
	 */
	public Cursor getUser(long user_id) {
		Cursor tempCursor = db.rawQuery("SELECT * FROM users U WHERE U._id=" + user_id + ";", null);
        if (tempCursor != null) {
        	tempCursor.moveToFirst();
        }
        return tempCursor;
	}
	
	/**
	 * Taking in a particular username String, find all corresponding workouts.
	 * 
	 * @param username
	 * @return
	 */
    public Cursor getAllUserWorkouts(String username) {
        Cursor tempCursor = db.rawQuery("SELECT W._id, W.date, W.duration, W.time_interval, W.total_calories, W.average_speed, "
        		+ "W.total_distance, W.altitude_gain FROM users U, workouts W WHERE U._id=W.user_id AND U.username='" + username + "' ORDER BY W._id DESC;",
        		null);
        if (tempCursor != null) {
        	tempCursor.moveToFirst();
        }
        return tempCursor;
    }
    
    /**
     * Taking in a particular user_id, find all corresponding workouts.
     * 
     * @param user_id
     * @return
     */
    public Cursor getAllUserWorkouts(long user_id) {
        Cursor tempCursor = db.rawQuery("SELECT W._id, W.date, W.duration, W.time_interval, W.total_calories, W.average_speed, "
        		+ "W.total_distance, W.altitude_gain FROM workouts W WHERE W.user_id=" + user_id + " ORDER BY W._id DESC;",
        		null);
        if (tempCursor != null) {
        	tempCursor.moveToFirst();
        }
        return tempCursor;
    }
    
    /**
     * Taking in a particular username String and workout_id, find specific corresponding workout.
     * 
     * @param username
     * @param workout_id
     * @return
     */
    public Cursor getUserWorkout(String username, long workout_id) {
        Cursor tempCursor = db.rawQuery("SELECT W._id, W.date, W.duration, W.time_interval, W.total_calories, W.average_speed, "
        		+ "W.total_distance, W.altitude_gain FROM workouts W WHERE W.username='" + username + "' AND W._id=" + workout_id + ";",
        		null);
        if (tempCursor != null) {
        	tempCursor.moveToFirst();
        }
        return tempCursor;
    }
    
    /**
     * Taking in a particular user_id and workout_id, find specific corresponding workout.
     * 
     * @param user_id
     * @param workout_id
     * @return
     */
    public Cursor getUserWorkout(long user_id, long workout_id) {
        Cursor tempCursor = db.rawQuery("SELECT W._id, W.date, W.duration, W.time_interval, W.total_calories, W.average_speed, "
        		+ "W.total_distance, W.altitude_gain FROM workouts W WHERE W.user_id=" + user_id + " AND W._id=" + workout_id + ";",
        		null);
        if (tempCursor != null) {
        	tempCursor.moveToFirst();
        }
        return tempCursor;
    }
    
    /**
     * Return a Cursor positioned at the workout that matches the given rowId
     * 
     * @param rowId id of workout to retrieve
     * @return Cursor positioned to matching workout, if found
     * @throws SQLException if workout could not be found/retrieved
     */
    public Cursor getWorkoutSampledata(long workout_id) throws SQLException {
    	Cursor tempCursor = db.rawQuery("SELECT * FROM datasamples D WHERE D.workout_id=" + workout_id +";", 
    			null);
        if (tempCursor != null) {
        	tempCursor.moveToFirst();
        }
        return tempCursor;
    }

	/**
	 * Return a Cursor with set cols positioned at the workout that matches the
	 * given rowId and desired cols.
	 * 
	 * @param workout_id
	 * @param cols
	 * @return
	 * @throws SQLException
	 */
    public Cursor getWorkoutSampledata(long workout_id, String[] cols) throws SQLException {
    	// format the cols into sql readable string
    	String selectedCols = "";
    	for (int i = 0; i < cols.length; i++) {
    		selectedCols += "D." + cols[i];
    		if (i < cols.length-1) {
    			selectedCols += ", ";
    		}
    	}

    	Cursor tempCursor = db.rawQuery("SELECT " + selectedCols + " FROM datasamples D WHERE D.workout_id=" + workout_id +";", null);
        if (tempCursor != null) {
        	tempCursor.moveToFirst();
        }
        return tempCursor;
    }
        
    /**
     * Delete the workout and corresponding datasamples with the given rowId
     * 
     * @param rowId id of workout to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteWorkout(long workout_id) {
    	return db.delete(WORKOUTS_TABLE, "workouts._id = " + workout_id, null) > 0;
    }
    
    /**
     * Delete all workouts and corresponding datasamples
     * 
     * @param rowId id of workout to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteAllWorkouts(long workout_id) {
    	return db.delete(WORKOUTS_TABLE, null, null) > 0;
    }
}
