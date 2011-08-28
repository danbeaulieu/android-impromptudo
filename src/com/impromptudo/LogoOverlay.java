package com.impromptudo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


public class LogoOverlay extends Overlay {

    private final Context context;
    private final int drawable;

    /**
     * @param context the context in which to display the overlay
     * @param drawable the ID of the desired drawable
     */
    public LogoOverlay(Context context, int drawable) {
      this.context = context;
      this.drawable = drawable;
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
      super.draw(canvas, mapView, shadow);

      // Read the image
      Bitmap markerImage = BitmapFactory.decodeResource(context.getResources(), drawable);
      // Draw it, centered around the given coordinates
      canvas.drawBitmap(markerImage,
          4,
          4, null);
      return true;
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
      // Handle tapping on the overlay here
      return true;
    }
  }

