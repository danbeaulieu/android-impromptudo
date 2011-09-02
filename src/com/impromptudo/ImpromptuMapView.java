package com.impromptudo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;


public class ImpromptuMapView extends MapView {

    private final String TAG = "ImpromptuMapView";
    
    private int previousZoomLevel = -1;

    private GeoPoint previousCenter = null;
    
    private ImpromptuHandler iHandler;

    private boolean hasUserLocation = false;

    public ImpromptuMapView(Context context, AttributeSet attrs) {

        super(context, attrs);
        iHandler = new ImpromptuHandler(this, context);
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
             Log.d(TAG, "Fetching events after redraw");
             iHandler.makeAPIRequest(500);
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
