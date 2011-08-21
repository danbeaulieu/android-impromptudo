package com.dan.impromptu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import org.json.JSONArray;

import java.util.Timer;

public class ImpromptuActivity extends MapActivity implements LocationListener {
    
    private static final String TAG = "ImpromptuActivity";
    
    LinearLayout linearLayout;
    
    MapView mapView;

    private LocationManager locationManager;
    
    private MapController mapController;
    
    String networkProvider = LocationManager.NETWORK_PROVIDER;
    
    String gpsProvider = LocationManager.GPS_PROVIDER;
    
    Location currentLocation = null;
    
    ProgressDialog dialog;
    
    AlertDialog unableToLocate;
    
    Timer timeout;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Entered onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        Log.d(TAG, "Showing dialog");
        dialog = ProgressDialog.show(ImpromptuActivity.this, "", 
            "Loading. Please wait...", true);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(ImpromptuActivity.this);
        
        builder.setMessage("Could not geolocate you.")
        .setCancelable(false)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 // TODO
            }
        });
        unableToLocate = builder.create();
        //String provider = locationManager.getBestProvider(Criteria.ACCURACY_FINE, true);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        AsyncTask<Void, Void, Void> task = new GeoLocateWaitTask();
        task.execute((Void[])null);
        
    }
    
    protected void centerMap(Location location) {
        mapController.setZoom(12);
        currentLocation.getLatitude();
        GeoPoint initGeoPoint = new GeoPoint((int)(currentLocation.getLatitude() * 1000000.0),
            (int)(currentLocation.getLongitude() * 1000000.0));
        Log.d(TAG, "Centering map at " + initGeoPoint.toString());
        mapController.animateTo(initGeoPoint);
        AsyncTask<Void, Void, JSONArray> findAndDisplayMarkers = new APITask(mapView, this); 
        findAndDisplayMarkers.execute((Void[]) null);
    }
    
    protected void displayActivities() {
        // get 
    }

    /** Register for the updates when Activity is in foreground */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(gpsProvider, 20000, 1, this);
        locationManager.requestLocationUpdates(networkProvider, 20000, 1, this);
    }
    
    /** Stop the updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }
    
    @Override
    protected boolean isRouteDisplayed() {

        // TODO Auto-generated method stub
        return false;
    }

    public void onLocationChanged(Location location) {
        currentLocation = location;
        dialog.dismiss();
        printLocation(location);
        locationManager.removeUpdates(this);
        centerMap(location);
    }

    public void onProviderDisabled(String provider) {
        // let okProvider be bestProvider
        // re-register for updates
        Log.d(TAG, "\n\nProvider Disabled: " + provider);
    }

    public void onProviderEnabled(String provider) {
        // is provider better than bestProvider?
        // is yes, bestProvider = provider
        Log.d(TAG, "\n\nProvider Enabled: " + provider);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "\n\nProvider Status Changed: " + provider + ", Status="
                + status + ", Extras=" + extras);
    }

    private void printLocation(Location location) {
        if (location == null)
            Log.d(TAG, "\nLocation[unknown]\n\n");
        else
            Log.d(TAG, "\n\n Found Location" + location.toString());
    }
    
    class GeoLocateWaitTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            try {
                Log.d(TAG, "sleeping");
                Thread.sleep(5000);
                Log.d(TAG, "done sleeping");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }       

        protected void onPostExecute(Void v) {
            Log.d(TAG, "onPostExecute");
            if (currentLocation == null) {
                dialog.dismiss();
                Log.d(TAG, "Never found location");
                unableToLocate.show();
            }
        }
    } 
}