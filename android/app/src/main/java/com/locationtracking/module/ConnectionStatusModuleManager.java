package com.locationtracking.module;

import android.content.Intent;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.locationtracking.MainActivity;
import com.locationtracking.service.CurrentPositionService;



public class ConnectionStatusModuleManager extends ReactContextBaseJavaModule {
    public ConnectionStatusModuleManager (ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ConnectionStatusModule";
    }

    @ReactMethod
    public void ServiceBind()
    {
      MainActivity.getInstance().ServiceBind();
    }

    @ReactMethod
    public void ServiceUnbind()
    {
      MainActivity.getInstance().ServiceUnbind();
    }


    @ReactMethod
    public void checkStatusGps (Callback callback) {
        boolean checkgps = MainActivity.getInstance().checkGps();
        if (!checkgps && !MainActivity.getInstance().start) {
            MainActivity.getInstance().showDialog();
        }

        callback.invoke(MainActivity.getInstance().start);
    }


    @ReactMethod
    public void initGetCurrentPos (String data, Callback callback) {

        Intent intent = new Intent(MainActivity.getInstance(), CurrentPositionService.class);
        intent.setAction(CurrentPositionService.ACTION_START_CURRENT_POSITION_SERVICE);
        intent.putExtra("headlessToCall", data);
        intent.setAction(CurrentPositionService.ACTION_SET_DATA);
        MainActivity.getInstance().startService(intent);

        callback.invoke(true);
    }


    @ReactMethod
    public void destroyCurrentPosService( Callback callback) {

        Intent intent = new Intent(MainActivity.getInstance(), CurrentPositionService.class);
        intent.setAction(CurrentPositionService.ACTION_STOP_CURRENT_POSITION_SERVICE);
        MainActivity.getInstance().startService(intent);

        callback.invoke(true);
    }
    
}
