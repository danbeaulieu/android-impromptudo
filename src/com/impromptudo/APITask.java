package com.impromptudo;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

import com.impromptudo.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class APITask extends AsyncTask<Void, Void, JSONArray> {

    private static final String TAG = "APITask";

    MapView mapView;
    
    Context context;
    
    List<Overlay> mapOverlays;
    
    Drawable drawable;
    
    ActivitiesOverlay itemizedOverlay;
    
    LogoOverlay logo;
    
    public APITask(MapView mapView, Context context) {

        this.mapView = mapView;
        this.context = context;
        this.drawable = this.context.getResources().getDrawable(R.drawable.marker2);
        this.logo = new LogoOverlay(this.context, R.drawable.ido_md);
    }

    protected JSONArray doInBackground(Void... params) {
        Log.d(TAG, "Doing Background");
        JSONArray root = null;
        // URL=http://impromptudo.com/getEvents/?version=2&swLng=-77.507227&swLat=38.793011&neLng=-76.640680&neLat=39.104871&delta=1
        MapBounds bounds = calculateMapBounds(this.mapView);
        HttpClient hc = new DefaultHttpClient();
        //String apiUrl = 
        HttpGet get = new HttpGet("http://impromptudo.com/getEvents/?version=2"
            + "&swLng=" + bounds.getSWLng()
            + "&swLat=" + bounds.getSWLat()
            + "&neLng=" + bounds.getNELng()
            + "&neLat=" + bounds.getNELat()
            + "&delta=1");
        Log.d(TAG, "Requesting events with url=" + get.getURI().toString());
        
        try {
            HttpResponse rp = hc.execute(get);
            if(rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)  
            {  
                    String result = EntityUtils.toString(rp.getEntity());
                    root = new JSONArray(result);
            }
        } catch (Exception e) {  
            Log.e(TAG, "Error loading JSON", e);  
        } 
        return root;
    }

    protected void onPostExecute(JSONArray array) {
        Log.d(TAG, "onPostExecute");
        List<Overlay> mapOverlays = mapView.getOverlays();
        mapOverlays.clear();
        mapOverlays.add(this.logo);
        
        if (array != null) {
            for (int i=0; i < array.length(); i++) {
                try {
                    JSONObject jobj = array.getJSONObject(i);
                    GeoPoint point = geoPointFromJSON(jobj);
                    OverlayItem overlayitem = new OverlayItem(point, "", "" + jobj.getLong("pk"));
                    itemizedOverlay = new ActivitiesOverlay(drawable, this.context);
                    itemizedOverlay.addOverlay(overlayitem);
                    mapOverlays.add(itemizedOverlay);
                }
                catch (JSONException e) {
                    Log.d(TAG, "Exception handling JSON array");
                }
            }
            
            Log.d(TAG, "Total map overlays = " + mapOverlays.size());
        }
    }
    
    private GeoPoint geoPointFromJSON(JSONObject jobj) throws JSONException {
        // fields.location.fields.latitude/longitude as doubles
        double lat = jobj.getJSONObject("fields").getJSONObject("location").getJSONObject("fields").getDouble("latitude");
        double lon = jobj.getJSONObject("fields").getJSONObject("location").getJSONObject("fields").getDouble("longitude");

        int latE6 = (int)(lat * 1000000.0);
        int lonE6 = (int)(lon * 1000000.0);
        // need to multiply by 1,000,000
        GeoPoint p = new GeoPoint(latE6, lonE6);
        return p;
    }

    private MapBounds calculateMapBounds(MapView map) {

        GeoPoint center = map.getMapCenter();
        int latSpan = map.getLatitudeSpan();
        int longSpan = map.getLongitudeSpan();
        
        int neLat = center.getLatitudeE6() + (latSpan/2);
        int neLon = center.getLongitudeE6() + (longSpan/2);
        GeoPoint neCorner = new GeoPoint(neLat, neLon);
        
        int swLat = center.getLatitudeE6() - (latSpan/2);
        int swLon = center.getLongitudeE6() - (longSpan/2);
        GeoPoint swCorner = new GeoPoint(swLat, swLon);
        
        MapBounds bounds = new MapBounds(neCorner, swCorner);
        Log.d(TAG, bounds.toString());
        return bounds;
    }
    
    class ActivitiesOverlay extends ItemizedOverlay {

        private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
        
        Context mContext;
        
        public ActivitiesOverlay(Drawable defaultMarker, Context context) {

            super(boundCenterBottom(defaultMarker));
            mContext = context;
        }

        public void addOverlay(OverlayItem overlay) {
            mOverlays.add(overlay);
            populate();
        }
        
        @Override
        protected OverlayItem createItem(int i) {
          return mOverlays.get(i);
        }

        @Override
        public int size() {

            return mOverlays.size();
        }
        
        @Override
        protected boolean onTap(int index) {
          OverlayItem item = mOverlays.get(index);
          WebView webview = new WebView(context);
          String url = "http://impromptudo.com/getDetails/" + item.getSnippet() + "/";
          //webview.loadData(summary, "text/html", "utf-8");
          webview.loadUrl(url);
          AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
          
          builder.setView(webview);
          builder.setCancelable(true);
          AlertDialog dialog = builder.create();
          dialog.setCanceledOnTouchOutside(true);
          
          dialog.show();
          return true;
        }
    }
    
    

}
