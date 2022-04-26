package com.locationtracking.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;

import com.facebook.react.HeadlessJsTaskService;


public class CurrentPositionService extends Service {

    public Context context = this;


    public static final String ACTION_START_CURRENT_POSITION_SERVICE = "ACTION_START_CURRENT_POSITION_SERVICE";
    public static final String ACTION_STOP_CURRENT_POSITION_SERVICE = "ACTION_STOP_CURRENT_POSITION_SERVICE";
    public static final String ACTION_SET_DATA = "ACTION_SET_DATA";


    private LocationManager locationManager;
    private String headlessTask = "";


    //When a location change update comes from system, this listener will handle it
    private final LocationListener listener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            
            Toast.makeText(context, "Ubicación detectada " + location.getAccuracy(), Toast.LENGTH_SHORT).show();

            if (location.getAccuracy() <= 7) {
                Intent myIntent = new Intent(getApplicationContext(), LocationService.class);
                myIntent.putExtra("lat", location.getLatitude());
                myIntent.putExtra("lng", location.getLongitude());
                myIntent.putExtra("headlessTask", headlessTask);
                getApplicationContext().startService(myIntent);
                HeadlessJsTaskService.acquireWakeLockNow(getApplicationContext());
            }

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        // Start requesting for location

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permisos denegados", Toast.LENGTH_LONG).show();
            return;
        }

        locationManager = (LocationManager) getSystemService(context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);
    }

    @Override
    public void onDestroy() {
        locationManager = (LocationManager) getSystemService(context.LOCATION_SERVICE);
        locationManager.removeUpdates(listener);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();

        switch (action)
        {
            case ACTION_START_CURRENT_POSITION_SERVICE:
                //Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
                break;
            case ACTION_STOP_CURRENT_POSITION_SERVICE:
                stopCurrentPositionService();
                //Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                break;
            case ACTION_SET_DATA:
                Bundle extras = intent.getExtras();
                headlessTask = extras.getString("headlessToCall");
                break;
        }


        return super.onStartCommand(intent, flags, startId);
    }



    private void stopCurrentPositionService()
    {
        stopSelf();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent){
        stopCurrentPositionService();
        super.onTaskRemoved(rootIntent);
    }

}
