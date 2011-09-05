package com.impromptudo;

import com.google.android.maps.MapView;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;


public class ImpromptuHandler extends Handler {

    private MapView mv;
    
    private Activity mActivity;
    
    ImpromptuHandler(MapView mv, Activity activity) {
        
        this.mv = mv;
        this.mActivity = activity;
    }
    
    @Override
    public void handleMessage(Message msg) {
        
        new APITask(mv, mActivity).execute((Void[]) null);  
    }
    
    public void makeAPIRequest(long delayMillis) {
        // this method gets called a bunch if the map is moved around.
        // We only want to do anything once the movement has stopped,
        // so we clear the queue before queueing anything up
        removeMessages(0);
        sendMessageDelayed(obtainMessage(0), delayMillis);
    }
}
