package com.impromptudo;

import android.app.Activity;
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
    
    private Activity activity;

    public ImpromptuMapView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }
    
    public void dispatchDraw(Canvas canvas) {
        
        super.dispatchDraw(canvas);
        // only fetch if zoomlevel or center has changed.
        if (hasUserLocation && hasChanged()) {
            this.previousZoomLevel = this.getZoomLevel();
            this.previousCenter = this.getMapCenter();
             
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

    public void setActivity(Activity activity) {

        this.activity = activity;
        iHandler = new ImpromptuHandler(this, this.activity);
    }

    public Activity getActivity() {

        return activity;
    }

}
