package edu.berkeley.eecs.ruzenafit.deprecated;

public class LegacyWriteToFileCode {
	
	/** File-related instance variables */
//	private static int fileNum = 0;
//	private static int numTimesWritten = 0;
//	private static int MAX_WRITTEN = 10000; // start a new file for every 10,000
//											// entries writen to file
//	private static int MAX_WRITTEN2 = 1000; // start a new file for every 1,000
//											// entries writen to file (for the
//											// new accel detail writer that only
//											// writes chunks depending on
//											// windowtimemillisec)
//	private static int fileNumGPS = 0;
//	private static int writtenGPS = 0;
	
	
	/** File-related methods */
//	/**
//	 * Writes new workout data to SD card under the "/CalFitD" folder.
//	 */
//	private static void writeToFile() {
//
//		String state = Environment.getExternalStorageState();
//
//		String str = imei + "," + mMostrecent_GPS_Time + ","
//				+ mMostrecent_System_Time + ","
//				+ geofmt.format(mMostrecent_GPS_Latitude) + ","
//				+ geofmt.format(mMostrecent_GPS_Longitude) + ","
//				+ genfmt.format(mMostrecent_GPS_Speed) + ","
//				+ genfmt.format(mMostrecent_GPS_Altitude) + ","
//				+ genfmt.format(mMostrecent_GPS_HasAccuracy) + ","
//				+ genfmt.format(mMostrecent_GPS_Accuracy) + ","
//				+ genfmt.format(accum_minute_V) + ","
//				+ genfmt.format(accum_minute_H) + ","
//				+ genfmt.format(mMostrecent_kCal);
//
//		/*
//		 * // encrypt line String encstr = null; try { encstr =
//		 * SimpleCrypto.toHex( SimpleCrypto.encrypt(SimpleCrypto.toByte(ENCKEY),
//		 * str.getBytes())); } catch (Exception e) { encstr = ""; }
//		 */
//
//		if (Environment.MEDIA_MOUNTED.equals(state)) {
//			// We can read and write the media
//
//			// write the data to file on the device's SD card
//			try {
//				File root = new File(Environment.getExternalStorageDirectory()
//						+ "/CalFitD");
//				if (!root.exists()) {
//					root.mkdir();
//				}
//				if (root.canWrite()) {
//					File myfile = new File(root, "CalFitEE.txt");
//					myfile.createNewFile();
//					FileWriter mywriter = new FileWriter(myfile, true);
//					BufferedWriter out = new BufferedWriter(mywriter);
//
//					// use out.write statements to write data...
//
//					out.write(str + "\n");
//					out.close();
//					out = null;
//					mywriter = null;
//					myfile = null;
//				}
//			} catch (IOException e) {
//				Log.e("CalFitService", "Could not write file " + e.getMessage());
//			}
//		}
//	}

//	public static void FindNewFiles() {
//		File myfile, mygpsfile;
//
//		// figure out which file we're up to on the SD card
//		try {
//			File root = new File(Environment.getExternalStorageDirectory()
//					+ "/CalFitD");
//			if (!root.exists()) {
//				root.mkdir();
//			}
//			if (root.canWrite()) {
//				myfile = new File(root, "CFdet" + fileNum + ".txt");
//				while (myfile.exists()) {
//					fileNum++;
//					myfile = new File(root, "CFdet" + fileNum + ".txt");
//				}
//				mygpsfile = new File(root, "CFgps" + fileNumGPS + ".txt");
//				while (mygpsfile.exists()) {
//					fileNumGPS++;
//					mygpsfile = new File(root, "CFgps" + fileNumGPS + ".txt");
//				}
//
//			}
//		} catch (Exception e) {
//			Log.e("CalFitService", "Could not write file " + e.getMessage());
//		}
//	}

//	public static void writeFileDetailNew2() {
//		File myfile;
//
//		String state = Environment.getExternalStorageState();
//
//		if (Environment.MEDIA_MOUNTED.equals(state)) {
//			// We can read and write the media
//
//			// figure out which file we're up to on the SD card
//			try {
//				File root = new File(Environment.getExternalStorageDirectory()
//						+ "/CalFitD");
//				if (!root.exists()) {
//					root.mkdir();
//				}
//				if (root.canWrite()) {
//					numTimesWritten++;
//					if (numTimesWritten >= MAX_WRITTEN2) { // start a new file
//						numTimesWritten = 0;
//						fileNum++;
//					}
//					// write the data to file on the device's SD card
//					myfile = new File(root, "CFdet" + fileNum + ".txt");
//					myfile.createNewFile();
//					FileWriter mywriter = new FileWriter(myfile, true);
//					BufferedWriter out = new BufferedWriter(mywriter);
//
//					// use out.write statements to write data...
//					out.write(accumAccelDetail);
//					out.close();
//					out = null;
//					mywriter = null;
//					myfile = null;
//					accumAccelDetail = "";
//				}
//			} catch (IOException e) {
//				Log.e("CalFitService", "Could not write file " + e.getMessage());
//			}
//		}
//	}

//	public static void writeFileGPS() {
//		File myfile;
//
//		String state = Environment.getExternalStorageState();
//
//		if (Environment.MEDIA_MOUNTED.equals(state)) {
//			// We can read and write the media
//			// figure out which file we're up to on the SD card
//			try {
//				File root = new File(Environment.getExternalStorageDirectory()
//						+ "/CalFitD");
//				if (!root.exists()) {
//					root.mkdir();
//				}
//				if (root.canWrite()) {
//					writtenGPS++;
//					if (writtenGPS >= MAX_WRITTEN) { // start a new file
//						writtenGPS = 0;
//						fileNumGPS++;
//					}
//					// write the data to file on the device's SD card
//					myfile = new File(root, "CFgps" + fileNumGPS + ".txt");
//					myfile.createNewFile();
//					FileWriter mywriter = new FileWriter(myfile, true);
//					BufferedWriter out = new BufferedWriter(mywriter);
//
//					String str = mMostrecent_GPS_Time + ","
//							+ mMostrecent_System_Time + ","
//							+ geofmt.format(mMostrecent_GPS_Latitude) + ","
//							+ geofmt.format(mMostrecent_GPS_Longitude) + ","
//							+ genfmt.format(mMostrecent_GPS_Speed) + ","
//							+ genfmt.format(mMostrecent_GPS_Altitude) + ","
//							+ genfmt.format(mMostrecent_GPS_HasAccuracy) + ","
//							+ genfmt.format(mMostrecent_GPS_Accuracy) + ","
//							+ mMostrecent_Provider + ","
//							+ genfmt.format(accum_minute_V) + ","
//							+ genfmt.format(accum_minute_H);
//
//					/*
//					 * // encrypt line String encstr = null; try { encstr =
//					 * SimpleCrypto.toHex(
//					 * SimpleCrypto.encrypt(SimpleCrypto.toByte(ENCKEY),
//					 * str.getBytes())); } catch (Exception e) { encstr = ""; }
//					 */
//					// use out.write statements to write data...
//					out.write(str + "\n");
//					out.close();
//					out = null;
//					mywriter = null;
//					myfile = null;
//				}
//			} catch (IOException e) {
//				Log.e("CalFitService", "Could not write file " + e.getMessage());
//			}
//
//		}
//	}
}
