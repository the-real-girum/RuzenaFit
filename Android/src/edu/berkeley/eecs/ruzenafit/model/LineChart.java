package edu.berkeley.eecs.ruzenafit.model;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONException;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class LineChart extends AbstractChart {
	@SuppressWarnings("unused")
	private static final String TAG = "LineChart";
	
	/**
	 * Returns the maximum value in an array of doubles
	 * @param double[] arr
	 * @return double
	 */
	public double findMax(double[] arr) {
		double currMax = 0.0;
		if (arr.length > 0) {
			currMax = arr[0];
		}
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > currMax) { 
				currMax = arr[i];
			}
		}
		return currMax;
	}
	
	/**
	 * Returns the minimum value in an array of doubles
	 * @param double[] arr
	 * @return double
	 */
	public double findMin(double[] arr) {
		double currMin = 0.0;
		if (arr.length > 0) {
			currMin = arr[0];
		}
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] < currMin) { 
				currMin = arr[i];
			}
		}
		return currMin;
	} 

	public Intent execute(Context context, double[] xArray,
			double[] yArray, String title, String xAxis, String yAxis) throws JSONException {
		String[] titles = new String[] { yAxis };
		List<double[]> x = new ArrayList<double[]>();
		List<double[]> values = new ArrayList<double[]>();
//		double[] yArray;

		x.add(xArray);
//		int jsonLength = jsonArray.length();
//		yArray = new double[jsonLength];
//		for (int i = 0; i < jsonLength; i++) {
//			yArray[i] = jsonArray.getDouble(i);
//		}  

		values.add(yArray);
		
		int[] colors = new int[] { Color.GREEN};
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}

		// test if min max of yarray are negative
		double ymin = findMin(yArray);
		double ymax = findMax(yArray);
		double ymin_forchart, ymax_forchart;  
		if (ymin <=0) {
			ymin_forchart = ymin;
		} else { 
			ymin_forchart = 0;
		}
		if (ymax <=0) {
			ymax_forchart = ymax * 0.8;
		} else {
			ymax_forchart = ymax * 1.2;
		}
		
		setChartSettings(renderer, title, xAxis, yAxis,
				0, findMax(xArray)*1.2, 
				ymin_forchart, ymax_forchart, 
				Color.LTGRAY, Color.WHITE);
		renderer.setXLabels(12);
		renderer.setYLabels(10);

		
		Intent intent = ChartFactory.getLineChartIntent(context, buildDataset(titles, x, values), renderer, "CalFit");
		return intent;
	}

	public Intent execute(Context context) {
		return null;
	}

	public String getDesc() {
		return null;
	}

	public String getName() {
		return null;
	}

}
