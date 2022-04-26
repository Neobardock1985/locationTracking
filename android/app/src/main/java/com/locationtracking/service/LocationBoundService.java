package com.locationtracking.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;

public class LocationBoundService extends Service {
    public Context context = this;

    //Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private LocationManager locationManager;

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
            Log.d("BoundService ", "onLocationChanged");
            //1.39 ms equivale a 5 k/h velocidad caminata promedio
            if( location.getAccuracy() <= 23)
            {
                Intent myIntent = new Intent(getApplicationContext(), LocationService.class);
                myIntent.putExtra("lat", location.getLatitude());
                myIntent.putExtra("lng", location.getLongitude());
                myIntent.putExtra("speed",location.getSpeed());
                myIntent.putExtra("acc", location.getAccuracy());
                myIntent.putExtra("appState", "BoundService onLocationChanged");
                myIntent.putExtra("headlessTask", "LogLocation");
                getApplicationContext().startService(myIntent);
                HeadlessJsTaskService.acquireWakeLockNow(getApplicationContext());
            }

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("A service is running in the background")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }


    public void beginTrackLocation()
    {
        // Start requesting for location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager = (LocationManager) getSystemService(context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("BoundService", "beginTrackLocation");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2, listener);
        } else {
            Log.d("BoundService", "Location not found!");
        }
    }


    public void stopTrackLocation() {
        Log.d("BoundService", "stopTrackLocation");
        locationManager = (LocationManager) getSystemService(context.LOCATION_SERVICE);
        locationManager.removeUpdates(listener);
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationBoundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationBoundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }




}

