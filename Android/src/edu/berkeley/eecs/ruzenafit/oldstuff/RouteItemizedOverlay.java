package edu.berkeley.eecs.ruzenafit.oldstuff;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;


public class RouteItemizedOverlay extends ItemizedOverlay {
	public static final String TAG = "RouteItemizedOverlay Class";
	
	private final static double INFINITY = Double.POSITIVE_INFINITY;
	
	private ArrayList<OverlayItem> overlayItems;
	Context mContext;
	private ArrayList<GeoPoint> gpList;
	int itemsSize;

	public RouteItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		gpList = new ArrayList<GeoPoint>();
		overlayItems = new ArrayList<OverlayItem>();
		itemsSize = 0;
		populate();
	}

	public RouteItemizedOverlay(Drawable defaultMarker, Context context) {
		super(defaultMarker);
		mContext = context;
		gpList = new ArrayList<GeoPoint>();
		overlayItems = new ArrayList<OverlayItem>();
		itemsSize = 0;
		populate();
	}

	public void setGPList(ArrayList<GeoPoint> gpList) {
		this.gpList = gpList;
	}
	
	public void addGPtoList(GeoPoint gp) {
		gpList.add(gp);
	}
	
	public void clearGPListItems() {
		gpList = new ArrayList<GeoPoint>();
		itemsSize = 0;
	}
	
	protected OverlayItem createItem(int i) {
		OverlayItem tempItem = overlayItems.get(i);
		return tempItem;
	}

	public int size() {
		int overlaySize = overlayItems.size();
		return overlaySize;
	}

	public void addOverlayItem(OverlayItem overlayItem) {
		overlayItems.add(overlayItem);
		itemsSize++;
		setLastFocusedIndex(-1);
		populate();
	}

	public void clearOverlayItems() {
		overlayItems.clear();
		itemsSize = 0;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
		
		Workout.lock.lock();
		ArrayList<GeoPoint> gpListTemp = gpList;
		Workout.lock.unlock();
		Projection projection = mapView.getProjection();
		Point curPoint = new Point();
		Point prevPoint = new Point();
		
		// set path style
		Path thePath = new Path();
		Paint pathPaint = new Paint();
		pathPaint.setAntiAlias(true);
		pathPaint.setStrokeWidth(4);
		pathPaint.setARGB(100, 113, 105, 252);
		pathPaint.setStyle(Paint.Style.STROKE);
		
		// set circle style
		int radius = 3;
		Paint circlePaint = new Paint();
		circlePaint.setStrokeWidth(3);
		circlePaint.setColor(0xFF097286);

		// drawing initialize point
		if (gpListTemp.size() > 0) {
			projection.toPixels(gpListTemp.get(0), prevPoint);
			canvas.drawCircle(prevPoint.x, prevPoint.y, radius, circlePaint);
		}
		
		// drawing lines
		if (gpListTemp.size() > 1) {
			for (int i = 0; i < gpListTemp.size()-1; i++) {
				projection.toPixels(gpListTemp.get(i), prevPoint);
				thePath.moveTo(prevPoint.x, prevPoint.y);

				projection.toPixels(gpListTemp.get(i+1), curPoint);
				thePath.lineTo(curPoint.x, curPoint.y);

//				canvas.drawCircle(curPoint.x, curPoint.y, radius, circlePaint);
			}
			canvas.drawPath(thePath, pathPaint);
//		} else if (gpListTemp.size() > 0) {
//			projection.toPixels(gpListTemp.get(0), curPoint);
//			canvas.drawCircle(curPoint.x, curPoint.y, radius, circlePaint);
		}
	}

	@Override
	protected boolean onTap(int index) {
		// OverlayItem item = mOverlays.get(index);
		// AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		// dialog.setTitle(item.getTitle());
		// dialog.setMessage(item.getSnippet());
		// dialog.show();
		return true;
	}

	/**
	 * Determines the appropriate center and zoom level, and sets it.
	 * 
	 * @param gpList
	 */
	public static void animateAndZoom(MapController mc, ArrayList<GeoPoint> gpList, boolean trackMotion) {
		int gpLatSpan = 0, gpLongSpan = 0, gpNum = gpList.size(), gpLatAvg = 0, gpLongAvg = 0;
		double gpLatMax = -INFINITY, gpLatMin = INFINITY, gpLongMax = -INFINITY, gpLongMin = INFINITY;
		
		int gpListSize = gpList.size();
		
		if (gpNum == 0) {
			Log.d(TAG, "animating to location 36,-97 with zoom 3");
			mc.animateTo(new GeoPoint((int) (36 * 1E6), (int) (-97 * 1E6)));
			mc.setZoom(3);
		} else if (gpNum == 1 || trackMotion) {
			Log.d(TAG, "animating to location " + gpList.get(gpListSize-1).getLatitudeE6() + "," + gpList.get(gpListSize-1).getLongitudeE6() + " with zoom 20");
//			mc.animateTo(new GeoPoint((int) (gpList.get(gpListSize-1).getLatitudeE6()), (int) (gpList.get(gpListSize-1).getLongitudeE6())));
			mc.animateTo(gpList.get(gpListSize-1));
			mc.setZoom(20);
		} else {
			for (GeoPoint gp : gpList) {
				int gpLat = gp.getLatitudeE6(), gpLong = gp.getLongitudeE6();
				
				// for latitude and longitude average
				gpLatAvg += gpLat;
				gpLongAvg += gpLong;
				
				// for latitude and longitude span
				if (gpLatMax < gpLat) gpLatMax = gpLat;
				if (gpLatMin > gpLat) gpLatMin = gpLat;
				if (gpLongMax < gpLong) gpLongMax = gpLong;
				if (gpLongMin > gpLong) gpLongMin = gpLong;
			}
			
			// find average latitude and longitude
//			gpLatAvg /= gpNum;
//			gpLongAvg /= gpNum;
			gpLatAvg = (int) ((gpLatMax + gpLatMin)/2);
			gpLongAvg = (int) ((gpLongMax + gpLongMin)/2);
			
			// find span of latitude and longitude
			gpLatSpan = (int) (gpLatMax - gpLatMin);
			gpLongSpan = (int) (gpLongMax - gpLongMin);

			Log.d(TAG, "animating to location " + gpLatAvg + "," + gpLongAvg + " with zoom on latitude +" + gpLatSpan/10 + " and longitude +" + gpLongSpan/10);
			GeoPoint tempGP = new GeoPoint(gpLatAvg, gpLongAvg);
//			mc.animateTo(gpList.get(gpList.size()-1));
//			mc.zoomToSpan(gpLatSpan/2 + gpLatSpan/10, gpLongSpan/2 + gpLongSpan/10);
			mc.animateTo(tempGP);
			mc.zoomToSpan(gpLatSpan + gpLatSpan/10, gpLongSpan + gpLongSpan/10);
		}
	}
}