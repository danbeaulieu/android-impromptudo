package com.impromptudo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.impromptudo.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import java.io.IOException;
import java.util.List;

public class ImpromptuActivity extends MapActivity implements LocationListener {
    
    private static final String TAG = "ImpromptuActivity";
    
    private static final int DIALOG_MANUALLY_LOCATE = 1;
    
    private static final int DIALOG_LOCATE_WAIT = 2;
    
    public static final int DIALOG_EVENT_DETAIL = 3;
    
    private static final int LOCATION_SLEEP_TIME = 10000;
    
    public static final String BUNDLE_EVENT_ID_KEY = "id";

    LinearLayout linearLayout;
    
    MapView mapView;

    private LocationManager locationManager;
    
    private MapController mapController;
    
    String networkProvider = LocationManager.NETWORK_PROVIDER;
    
    String gpsProvider = LocationManager.GPS_PROVIDER;
    
    Location currentLocation = null;
    
    Geocoder coder;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Entered onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        coder = new Geocoder(this);
        mapView = (ImpromptuMapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        // so ugly, but need to get reference of activity to APITask...
        ((ImpromptuMapView) mapView).setActivity(this);
        mapController = mapView.getController();
        List<Overlay> overlays = mapView.getOverlays();
        
        overlays.add(new LogoOverlay(this, R.drawable.ido_md));
        
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        new GeoLocateWaitTask().execute((Void[]) null);
    }
    
    @Override
    protected Dialog onCreateDialog(int i) {
        return onCreateDialog(i, null);
    }
    
    @Override
    protected Dialog onCreateDialog(int i, Bundle b) {
        
        switch(i) {
            case DIALOG_MANUALLY_LOCATE:
                final EditText input = new EditText(this);
                return new AlertDialog.Builder(ImpromptuActivity.this)
                    .setMessage(getString(R.string.geoSearch))
                    .setView(input)
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                             Log.d(TAG, "Got value " + input.getText() );
                             try {
                                List<Address> addresses = coder.getFromLocationName(input.getText().toString(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    Location loc = new Location("geocoder");
                                    loc.setLatitude(address.getLatitude());
                                    loc.setLongitude(address.getLongitude());
                                    
                                    currentLocation = loc;
                                    
                                }
                                else {
                                    Log.e(TAG, "No valid addresses returned");
                                }
                            }
                            catch (IOException e) {
                                Log.e(TAG, "Error getting location", e);
                                // ugh http://code.google.com/p/android/issues/detail?id=8816
                                currentLocation = new Location("mockLocation");
                                currentLocation.setLatitude(38.895);
                                currentLocation.setLongitude(-77.0366667);
                            }
                            centerMap();
                        }
                    })
                    .create();
            case DIALOG_LOCATE_WAIT:
                ProgressDialog p = new ProgressDialog(this);
                p.setMessage("Waiting for location");
                p.setCancelable(true);
                p.setIndeterminate(true);
                p.setTitle("");
                return p;
            case DIALOG_EVENT_DETAIL:
                WebView webview = new WebView(this);
                String url = "http://impromptudo.com/getDetails/" + b.getString(BUNDLE_EVENT_ID_KEY) + "/";
                //webview.loadData(summary, "text/html", "utf-8");
                webview.loadUrl(url);
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setView(webview)
                    .setCancelable(true);
                
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                
                return dialog;
            default:
                return null;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ido_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.search:
            showDialog(DIALOG_MANUALLY_LOCATE);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    protected void centerMap() {
        mapController.setZoom(12);
        GeoPoint initGeoPoint = new GeoPoint((int)(currentLocation.getLatitude() * 1E6),
            (int)(currentLocation.getLongitude() * 1E6));
        mapController.animateTo(initGeoPoint);
        mapController.setCenter(initGeoPoint);
        ((ImpromptuMapView)mapView).setHasUserLocation(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationManager.requestLocationUpdates(gpsProvider, 20000, 1, this);
        locationManager.requestLocationUpdates(networkProvider, 20000, 1, this);
    }
    
    /** Register for the updates when Activity is in foreground */
    @Override
    protected void onResume() {
        super.onResume();
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
        dismissDialog(DIALOG_LOCATE_WAIT);
        printLocation(location);
        locationManager.removeUpdates(this);
        centerMap();
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
    
    protected void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }
    
    class GeoLocateWaitTask extends AsyncTask<Void, Void, Void> {
        
        protected void onPreExecute() {
            Log.d(TAG, "Showing dialog");
            showDialog(DIALOG_LOCATE_WAIT);
        }
        
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(LOCATION_SLEEP_TIME);
                Log.d(TAG, "done sleeping");
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleeping thread interrupted, should never happen", e);
            }
            return null;
        }       

        protected void onPostExecute(Void v) {
            if (currentLocation == null) {
                Log.d(TAG, "Never found location");
                stopLocationUpdates();
                dismissDialog(DIALOG_LOCATE_WAIT);
                // yikes this is ugly
                //manualLocate.setMessage(getString(R.string.geoError) + " " + getString(R.string.geoSearch));
                showDialog(DIALOG_MANUALLY_LOCATE);
            }
        }
    } 
}