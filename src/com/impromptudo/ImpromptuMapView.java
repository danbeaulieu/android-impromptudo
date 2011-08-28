package com.impromptudo;

import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import org.json.JSONArray;


public class ImpromptuMapView extends MapView {

    private final String TAG = "ImpromptuMapView";
    
    private int previousZoomLevel = -1;

    private GeoPoint previousCenter = null;
    
    private Handler mHandler = new Handler();

    private DelayedMapUpdate delayedMapUpdate;

    private boolean hasUserLocation = false;

    public ImpromptuMapView(Context context, AttributeSet attrs) {

        super(context, attrs);
        delayedMapUpdate = new DelayedMapUpdate(context, this);
    }
    
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // only fetch if zoomlevel or center has changed.
        if (hasUserLocation && hasChanged()) {
            this.previousZoomLevel = this.getZoomLevel();
            this.previousCenter = this.getMapCenter();
             // this method gets called a bunch if the map is moved around.
             // We only want to do anything once the movement has stopped,
             // so we clear the queue before queueing anything up
             mHandler.removeCallbacks(delayedMapUpdate);
             Log.d(TAG, "Fetching events after redraw"); 
             mHandler.postDelayed(delayedMapUpdate, 500);

            
        }
    }
    
    private class DelayedMapUpdate implements Runnable {

        Context context;
        
        MapView mapView;
        
        public DelayedMapUpdate(Context ctx, MapView mv) {
            this.context = ctx;
            this.mapView = mv;
        }
        
        public void run() {
            AsyncTask<Void, Void, JSONArray> findAndDisplayMarkers = new APITask(this.mapView, this.context);
            findAndDisplayMarkers.execute((Void[]) null);
            
        }
        
    }

    private boolean hasChanged() {

        if (previousZoomLevel != this.getZoomLevel()) return true;
        if (!this.getMapCenter().equals(previousCenter)) return true;
        return false;
    }

    public void setHasUserLocation(boolean b) {

        hasUserLocation  = b;
        
    }

}
