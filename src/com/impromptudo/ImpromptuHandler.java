package com.impromptudo;

import com.google.android.maps.MapView;

import android.content.Context;
import android.os.Handler;
import android.os.Message;


public class ImpromptuHandler extends Handler {

    private MapView mv;
    
    private Context context;
    
    ImpromptuHandler(MapView mv, Context context) {
        
        this.mv = mv;
        this.context = context;
    }
    
    @Override
    public void handleMessage(Message msg) {
        
        new APITask(mv, context).execute((Void[]) null);  
    }
    
    public void makeAPIRequest(long delayMillis) {
        
        removeMessages(0);
        sendMessageDelayed(obtainMessage(0), delayMillis);
    }
}
