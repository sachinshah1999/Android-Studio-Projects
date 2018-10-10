package com1032.cw2.ss01703.ss01703_assignment2;

/**
 * This class is used to periodically extract the user's location by using a service with an AsyncTasks.
 * A service has been defined, which starts the execution of an AsyncTask.
 * The AsyncTask implements location listener,
 * which is used for receiving notifications from the LocationManager when the location has changed.
 * A timer and a separate handler has been defined to repeatedly execute the async task, hence receiving constant location updates.
 *
 */

/**--All the imports--**/

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;

public class getLocationService extends Service {

    /**
     * This method return the communication channel to the service.
     * @param intent - The Intent that was used to bind to this service
     * @return - Since our location classes cannot bind to the service we are returning null.
     */

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*@Override
    public void onCreate() {
        super.onCreate();
        callAsynchronousTask();
    }*/

    /**
     * This method is called by the system every time the currentLocationActivity and PointsOfInterestActivity class
     * explicitly starts the service by calling startService() method.
     * @param intent - The intent supplied to startService(Intent)
     * @param flags - Additional data about this start request.
     * @param startId - A unique integer representing this specific request to start.
     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        callAsynchronousTask();
        /*getLocationTask performBackgroundTask = new getLocationTask();
        // PerformBackgroundTask this class is the class that extends AsynchTask
        performBackgroundTask.execute();*/
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * This is the method which repeatedly executes the AsyncTask, getLocationTask.
     *  We define a new handler because, while the async task does run on a separate thread,
     *  it cannot be started from other threads than the UI thread. So we define a new handler to allow this.
     *  We used a timer to post the execution of the AsyncTask as a runnable to our handler every 2 seconds.
     */

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
            Timer timer = new Timer();
            TimerTask doAsynchronousTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getLocationTask performBackgroundTask = new getLocationTask();
                                // PerformBackgroundTask this class is the class that extends AsynchTask
                                performBackgroundTask.execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };
            timer.schedule(doAsynchronousTask, 0, 2000); //execute in every 2 seconds
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }*/

    /**
     * This is the inner AsyncTask class.
     * The AsyncTask implements LocationListener.
     * This class is responsible for extracting the user's current location.
     */

    private class getLocationTask extends AsyncTask<Void, Void, Void> implements LocationListener {

        private LocationManager locationManager; //This class provides access to the system location ser

        private Location currentLocation; //Current location of the user.

        String provider; //The name of the provider with which to register for location updates

        /**
         * This method is invoked on the UI thread before the task is executed.
         * In this method we initialise the location manager.
         * We also use the location manager to get the last know location.
         * We do this to ensure that the location being sent, don't revert to their default intent values.
         * Instead if a new location is not found, the last know location will be sent instead.
         * Ultimately retaining location accuracy.
         */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Setup location manager
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            try
            {
                Criteria locCriteria = new Criteria();
                locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
                provider = locationManager.getBestProvider(locCriteria, false);
                //provider = locationManager.GPS_PROVIDER;

                if (ContextCompat.checkSelfPermission(getLocationService.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // No explanation needed, we can request the permission.

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                } else {
                    //Permission is granted
                }
                currentLocation = locationManager.getLastKnownLocation(provider);
            }
            catch ( SecurityException se )
            {
                se.printStackTrace();
            }
        }

        /**
         * This method is invoked in the background thread immediately after onPreExecute().
         * Here we are requesting location updates from the location manager. We are using a GPS provider,
         * requests are requested every 2 seconds or every 1 meter. The listener is the AsyncTask class.
         * @param voids
         * @return
         */

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... voids) {
            /**
             * If the calling thread is not associated with a Looper,
             * Initialise the current thread as a looper.
             */

            if (Looper.myLooper() == null)
            {
                Looper.myLooper().prepare();
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
            return null;
        }

        /**
         * This method is invoked on the UI thread after the doInBackground() method.
         * In this method we want to broadcast the intent containing the updated location coordinates.
         * We also need to stop requesting location updates, since the thread no longer exists, and to prevent memory leaks.
         * @param aVoid
         */

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent i = new Intent("location_update");
            if(currentLocation != null) {
                i.putExtra("latitude", currentLocation.getLatitude());
                i.putExtra("longitude", currentLocation.getLongitude());
            }
            sendBroadcast(i);

            /*if (locationManager != null) {
                locationManager.removeUpdates(this);
            }*/
        }

        /**
         * This method is called when the location has changed.
         * Location changes in the requestLocationUpdates method.
         * We also set our currentLocation to the new location object.
         * @param location - The new location, as a Location object.
         */

        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            Looper.myLooper().quit(); //Terminates the loop method without processing any more messages in the message queue.
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        /**
         * If the location services are disabled, we point user to the settings panel to enable them.
         * @param provider - the name of the location provider associated with this update.
         */

        @Override
        public void onProviderDisabled(String provider) {
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }
}
