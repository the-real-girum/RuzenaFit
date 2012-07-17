package edu.berkeley.eecs.ruzenafit.util;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import edu.berkeley.eecs.ruzenafit.access.SharedPreferencesHelper;
import edu.berkeley.eecs.ruzenafit.model.PrivacyPreferenceEnum;
import edu.berkeley.eecs.ruzenafit.model.WorkoutTick;

public class KCalUtils {

	// This is called on every sample window
	public static double[] calculateKcal(double[][] data, int samplesPerWindow) {

		double res[] = new double[2]; // holds V and H result
		double history[] = new double[3];
		double d[] = new double[3];
		double p[] = new double[3];
		// double sdata[][] = new double[3][manysamples];

		// smooth the data first
		// 3-sample moving average
		/*
		 * for (int i=0; i<3; i++) { for (int j=0; j<samplesperwindow; j++) { if
		 * (j==0) { sdata[i][j] = twoprevious[i] + oneprevious[i] + data[i][j];
		 * } else if (j==1) { sdata[i][j] = oneprevious[i] + data[i][j-1] +
		 * data[i][j]; } else { sdata[i][j] = data[i][j-2] + data[i][j-1] +
		 * data[i][j]; if (j==(samplesperwindow-2)) twoprevious[i]=data[i][j];
		 * else if (j==(samplesperwindow-1)) oneprevious[i]=data[i][j]; } } }
		 */

		// this is historical average of the past samples
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < samplesPerWindow; j++) {
				history[i] += data[i][j];
			}
			history[i] /= samplesPerWindow;
		}

		for (int j = 0; j < samplesPerWindow; j++) {

			for (int i = 0; i < 3; i++) {
				d[i] = history[i] - data[i][j];
			}

			double num = 0;
			double den = 0;
			double value = 0;
			for (int i = 0; i < 3; i++) {
				num = (d[0] * history[0] + d[1] * history[1] + d[2]
						* history[2]);
				den = (history[0] * history[0] + history[1] * history[1] + history[2]
						* history[2]);

				if (den == 0)
					den = 0.01;
				value = (num / den) * history[i];
				p[i] = value;
			}

			double pMagn = p[0] * p[0] + p[1] * p[1] + p[2] * p[2];

			res[0] += Math.sqrt(pMagn);

			res[1] += Math.sqrt((d[0] - p[0]) * (d[0] - p[0]) + (d[1] - p[1])
					* (d[1] - p[1]) + (d[2] - p[2]) * (d[2] - p[2]));
		}
		return res;
	}
	
	/**
	 * Utility function to round floats (used for binning).
	 * 
	 * @param input
	 * @return
	 */
	public static int roundDownToNearestMultipleOfThree(float input) {
		// Truncate and round the float
		return roundIntegerToNearestMultiple((int) input, 3);
	}

	/**
	 * Utility function to round floats (used for binning).
	 * 
	 * @param input
	 * @return
	 */
	public static int roundDownToNearestMultipleOfFive(float input) {
		// Truncate and round the float
		return roundIntegerToNearestMultiple((int) input, 5);
	}
	
	/**
	 * Private
	 * @param numberToRound
	 * @param multipleToRoundTo
	 * @return
	 */
	private static int roundIntegerToNearestMultiple(int numberToRound, int multipleToRoundTo) {
		return numberToRound / multipleToRoundTo * multipleToRoundTo;
	}
	
}
