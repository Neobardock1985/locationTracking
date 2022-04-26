package com.locationtracking;

import com.facebook.react.ReactActivity;

import com.locationtracking.service.LocationBoundService;
import com.locationtracking.service.LocationBoundService.LocalBinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;



public class MainActivity extends ReactActivity {

    boolean isBound = false;

    LocationBoundService myService;

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "locationtracking";
    }
    public  Context context = this;
    public Boolean start = false;
    private LocationManager locationManager;
    private static MainActivity sInstance = null;

    public static MainActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;

    }


    private ServiceConnection myConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            myService = binder.getService();
            myService.beginTrackLocation();
            isBound = true;
            Log.d("BoundService", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Log.d("BoundService", "onServiceDisconnected");
        }
    };

    @Override
    public  void onStart() {
        super.onStart();

        if (!checkGps()){
            showDialog();
        } else {
            start = false;
        }

    }


    @Override
    public void onResume() {
       super.onResume();
       if (!checkGps() && !start){ showDialog(); }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!checkGps() && !start){ showDialog(); }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if (!checkGps() && !start){ showDialog(); }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (!checkGps() && !start){ showDialog(); }

    }


    public void ServiceBind()
    {
        Intent intent = new Intent(this, LocationBoundService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }


    public void ServiceUnbind()
    {
        // Unbind from the service
        if (isBound) {
            myService.stopTrackLocation();
            unbindService(myConnection);
            isBound = false;
        }
    }

    public boolean checkGps () {
        locationManager = (LocationManager) getSystemService(context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }


    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Sentimos las molestias!")
               .setMessage("Esta app requiere que la ubicación se encuentre encendida")
               .setCancelable(false)
               .setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       dialogInterface.dismiss();
                       Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                       context.startActivity(myIntent);
                   }
               })
               .setNegativeButton("No activar", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       finish();
                       dialogInterface.dismiss();
                       moveTaskToBack(true);
                   }
               })
               .setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            finish();
                            arg0.dismiss();
                        }
                        return true;
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        start = true;
    }

}
