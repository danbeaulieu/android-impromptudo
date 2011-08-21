package com.dan.impromptu;

import com.google.android.maps.GeoPoint;


public class MapBounds {
    
    protected GeoPoint neCorner;
    
    protected GeoPoint swCorner;

    
    public MapBounds(GeoPoint neCorner, GeoPoint swCorner) {

        this.neCorner = neCorner;
        this.swCorner = swCorner;
    }


    public GeoPoint getNeCorner() {
    
        return neCorner;
    }

    
    public void setNeCorner(GeoPoint neCorner) {
    
        this.neCorner = neCorner;
    }

    
    public GeoPoint getSwCorner() {
    
        return swCorner;
    }

    
    public void setSwCorner(GeoPoint swCorner) {
    
        this.swCorner = swCorner;
    }


    public double getSWLng() {

        return this.swCorner.getLongitudeE6() / 1000000.0;
    }
    
    public double getSWLat() {

        return this.swCorner.getLatitudeE6() / 1000000.0;
    }
    
    public double getNELng() {

        return this.neCorner.getLongitudeE6() / 1000000.0;
    }
    
    public double getNELat() {

        return this.neCorner.getLatitudeE6() / 1000000.0;
    }
    
    public String toString() {
        
        return "Bounds NorthEast Corner=[" + this.neCorner + "] SouthWest Corner=[" + this.swCorner + "]";
    }

}
