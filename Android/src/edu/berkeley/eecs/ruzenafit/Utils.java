/**
 * Utils.java
 * @version 1.0
 * 
 * Some utility functions.
 * 
 * @author Irving Lin
 */


package edu.berkeley.eecs.ruzenafit;

import java.util.ArrayList;
import java.util.Calendar;

import com.google.android.maps.GeoPoint;

public class Utils {
	public final static String TAG = "Utilties Class";
	

	/* * * * * * * * * * * * *
	 * * * * * * * * * * * * *
	 * * UTILITY FUNCTIONS * *
	 * * * * * * * * * * * * *
	 * * * * * * * * * * * * */
	
	public static String convertMillisToTime(long timeMillis) {
		if (timeMillis == 0) {
			return "00:00:00.0";
		}
		
		long timeDecis = (timeMillis % 1000) / 100; 
		long timeSeconds = (timeMillis/1000) % 60;
		long timeMinutes = (timeMillis/(1000*60)) % 60;
		long timeHours = (timeMillis/(1000*60*60)) % 24;
		
		String time = "";
		if (timeHours > 0 && timeHours < 10) {
			time = "0" + timeHours + ":";
		} else if (timeHours >= 10) {
			time = timeHours + ":";
		} else {
			time = "00:";
		}
		
		if (timeMinutes < 10) {
			time = time + "0" + timeMinutes + ":";
		} else {
			time = time + timeMinutes + ":";
		}
		
		if (timeSeconds < 10) {
			time = time + "0" + timeSeconds;
		} else {
			time = time + timeSeconds;
		}
		
		time = time + "." + timeDecis;
		
		return time;
	}
	
	/**
	 * Converts a list of geopoints to a string
	 * @param geopoints
	 * @return
	 */
	public static String geoToString(ArrayList<GeoPoint> geopoints) {
		String gpString = "";
		for (int i = 0; i < geopoints.size(); i++) {
			GeoPoint tempGP = geopoints.get(i);
			gpString += tempGP.getLatitudeE6() + "," + tempGP.getLongitudeE6() + ";";
		}
		return gpString;
	}

	/**
	 * Returns the current date in a String object in the form "YYYY/MM/DD"
	 * 
	 * @return String in the form "YYYY/MM/DD"
	 */
	public static String getDate() {
		Calendar c = Calendar.getInstance();
		String date;
		int month = c.get(Calendar.MONTH) + 1; // Month+1 because January == 0
		int day = c.get(Calendar.DAY_OF_MONTH);
		// Make sure month and day are composed of two digits
		if (month < 10) {
			if (day < 10) {
				date = c.get(Calendar.YEAR) + "/" + "0" + month + "/" + "0" + day;
			} else {
				date = c.get(Calendar.YEAR) + "/" + "0" + month + "/" + day;
			}
		} else {
			if (day < 10) {
				date = c.get(Calendar.YEAR) + "/" + month + "/" + "0" + day;
			} else {
				date = c.get(Calendar.YEAR) + "/" + month + "/" + day;
			}
		}

		return date;
	}

	/**
	 * converts a general arraylist to string
	 * @param arr
	 * @return
	 */
	public static String arrayListToString(ArrayList arr) {
		// TODO: should not rely on toString() method and need to parametrize arraylist
		String result = "";
		for (int i = 0; i < arr.size()-1; i++) {
			result = result + arr.get(i).toString() + ",";
		}
		result += arr.get(arr.size()-1);
		return result;
	}
	
	/*
	public static static String convertSecToTime(long seconds) {
		String time = "";
		long minutes = (seconds / 60) % 60;
		long hours = minutes / 60;
		long remainingSeconds = seconds % 60;

		if (hours > 0 && hours < 10) {
			time = "0" + hours + ":";
		} else if (hours >= 10) {
			time = hours + ":";
		} else {
			time = "00:";
		}
		
		if (minutes < 10) {
			time = time + "0" + minutes + ":";
		} else {
			time = time + minutes + ":";
		}
		
		if (remainingSeconds < 10) {
			time = time + "0" + remainingSeconds;
		} else {
			time = time + remainingSeconds;
		}
		
		return time;
	}
	*/
	
    /**
     * Takes in a String value, and makes sure it is a number value. If it is 0 or is not a number, then return 0.
     * @param tempValue
     * @return
     */
	public static String removeBadAndSetDefault(String tempValue) {
		try {
			float val = Float.valueOf(tempValue);
			if (val == 0) {
				tempValue = "0.00";
			}
		} catch (Exception e) {
			tempValue = "0.00";
		}
		return tempValue;
	}
	
	/**
	 * Creates a String of length() length.
	 * @param value
	 * @param length
	 * @return
	 */
	public static String setStringLength(String value, int length, String filler) {
		if (value == null) {
			String temp = "";
			for (int i = 0; i < length; i++) {
				temp += filler;
			}
			return temp;
		} else if (value.length() > length) {
			return value.substring(0, length);
		} else {
			String temp = "";
			for (int i = value.length(); i < length; i++) {
				temp += filler;
			}
			return temp + value;
		}
	}
	
	/**
	 * converts an arraylits to an array
	 * @param arrayList
	 * @return
	 */
	public static double[] arrayListToArray(ArrayList<Float> arrayList) {
		double[] result = new double[arrayList.size()];
		for (int i = 0; i < arrayList.size(); i++) {
			result[i] = (double)arrayList.get(i);
		}
		return result;
	}
	
	/**
	 * returns a list of geopoints from a given string.
	 * precondition: string is a list of semicolon separated geopoints (that are comma separated)
	 * @param gpString
	 * @return gpList
	 */
	public static ArrayList<GeoPoint> stringToGeoList(String gpString) {
		ArrayList<GeoPoint> gpList = new ArrayList<GeoPoint>();
		// sanity check in case we somehow used the original Object toString() method instead of our own.
		// checks for the bracket at the beginning, and if there is one, remove both it and the end bracket.
		if (!gpString.equals("")) {
//			if (gpString.charAt(0) == '[') {
//				gpString = gpString.substring(1, gpString.length()-1);
//			}
		
			for (String gpTemp : gpString.split("[;]+")) {
				String[] gp = gpTemp.split("[,]+");
				gpList.add(new GeoPoint(Integer.parseInt(gp[0]), Integer.parseInt(gp[1])));
			}
		}
		return gpList;
	}
	
	/**
	 * returns a list of floats from a given string.
	 * precondition: string is a list of comma separated floats
	 * @param str
	 * @return floatList
	 */
	public static ArrayList<Float> stringToFloatList(String str) {
		// sanity check in case we somehow used the original Object toString() method instead of our own.
		// checks for the bracket at the beginning, and if there is one, remove both it and the end bracket.
		ArrayList<Float> floatList = new ArrayList<Float>();
		if (!str.equals("")) {
			for (String floatTemp : str.split("[,]+")) {
				floatList.add(Float.parseFloat(floatTemp));
			}
		}
		return floatList;
	}
	
	/**
	 * returns a list of strings from a given string.
	 * precondition: string is a list of comma separated strings
	 * @param str
	 * @return stringList
	 */
	public static ArrayList<String> stringToStringList(String str) {
		// sanity check in case we somehow used the original Object toString() method instead of our own.
		// checks for the bracket at the beginning, and if there is one, remove both it and the end bracket.
		ArrayList<String> stringList = new ArrayList<String>();
		if (!str.equals("")) {
			str = str.substring(1, str.length() - 1);
			for (String stringTemp : str.split("[,]+")) {
				stringList.add(stringTemp);
			}
		}
		return stringList;
	}

	/**
	 * formats time based on number of sigfigs (1, 2, 3, or 4, where the number denotes number of returned sigfigs)
	 * precondition: requires duration to be formatted 00:00:00.0
	 * @param duration
	 * @param sigfigs
	 * @return
	 */
	public static String truncateAndFormatTime(String duration, int sigfigs) {
		try {
			int hours = Integer.parseInt(duration.substring(0, 2));
			int minutes = Integer.parseInt(duration.substring(3, 5));
			int seconds = Integer.parseInt(duration.substring(6, 8));
			int millis = Integer.parseInt(duration.substring(9, 10));
			
			if (sigfigs == 1) {
				if (hours != 0) {
					return Utils.setStringLength(Integer.toString(hours), 2, "0") + "h";
				} else if (minutes != 0) {
					return Utils.setStringLength(Integer.toString(minutes), 2, "0") + "m";
				} else if (seconds != 0) {
					return Utils.setStringLength(Integer.toString(seconds), 2, "0") + "s";
				} else if (millis != 0) {
					return millis + "ms";
				} else {
					return "n/a";
				}
			} else if (sigfigs == 2) {
				if (hours != 0) {
					return Utils.setStringLength(Integer.toString(hours), 2, "0") + "h" + Utils.setStringLength(Integer.toString(minutes), 2, "0") + "m";
				} else if (minutes != 0) {
					return Utils.setStringLength(Integer.toString(minutes), 2, "0") + "m" + Utils.setStringLength(Integer.toString(seconds), 2, "0") + "s";
				} else if (seconds != 0) {
					return Utils.setStringLength(Integer.toString(seconds), 2, "0") + "." + millis + "s";
				} else if (millis != 0) {
					return "00." + millis + "s";
				} else {
					return "n/a";
				}
			} else if (sigfigs == 3) {
				if (hours != 0) {
					return Utils.setStringLength(Integer.toString(hours), 2, "0") + "h" + Utils.setStringLength(Integer.toString(minutes), 2, "0") + "m" + Utils.setStringLength(Integer.toString(seconds), 2, "0") + "s";
				} else if (minutes != 0) {
					return Utils.setStringLength(Integer.toString(minutes), 2, "0") + "m" + Utils.setStringLength(Integer.toString(seconds), 2, "0") + "." + millis + "s";
				} else if (seconds != 0) {
					return "00m" + Utils.setStringLength(Integer.toString(seconds), 2, "0") + "." + millis + "s";
				} else if (millis != 0) {
					return "00m00." + millis + "s";
				} else {
					return "n/a";
				}
			} else {
				if (hours != 0) {
					return Utils.setStringLength(Integer.toString(hours), 2, "0") + "h" + Utils.setStringLength(Integer.toString(minutes), 2, "0") + "m" + Utils.setStringLength(Integer.toString(seconds), 2, "0") + "." + millis + "s";
				} else if (minutes != 0) {
					return "00h" + Utils.setStringLength(Integer.toString(minutes), 2, "0") + "m" + Utils.setStringLength(Integer.toString(seconds), 2, "0") + "." + millis + "s";
				} else if (seconds != 0) {
					return "00h00m" + Utils.setStringLength(Integer.toString(seconds), 2, "0") + "." + millis + "s";
				} else if (millis != 0) {
					return "00h00m00." + millis + "s";
				} else {
					return "n/a";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return duration;
	}
	
	public static String truncate(float value, String initValue, int length, boolean afterDecimal) {
		if (!afterDecimal) {
			if (value == 0.0) {
				return initValue;
			} else {
				String valueString = Float.toString(value);
				if (valueString.length() < length) {
					return valueString;
				} else {
					return valueString.substring(0, length);
				}
			}
		} else {
			// TODO: return the String value with <length> values after the decimal point.
			return "";
		}
	}

	/**
	 * Takes in a float and returns either the float if not infinite or NaN, or 0.
	 * @param num
	 * @return
	 */
	public static float validateFloat(float num) {
		if (!Float.isInfinite(num) && !Float.isNaN(num)) {
			return num;
		}
		return (float) 0;
	}
}