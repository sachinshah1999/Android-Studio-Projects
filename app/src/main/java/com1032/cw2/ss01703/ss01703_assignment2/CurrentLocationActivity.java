package com1032.cw2.ss01703.ss01703_assignment2;

/**
 * This activity is used to obtain and display the user's current location
 */

/**--All the imports--**/

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CurrentLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    /**--All the fields--**/
    private static final String TAG = CurrentLocationActivity.class.getSimpleName(); //Used to show messages in logcat.
    private String provider; //The name of the provider with which to register for location updates

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1; //Fine location request code, used when requesting permissions

    private TextView latitudeField, longitudeField; //Used to display the current location coordinates in text for the user

    private LocationManager locationManager; //This class provides access to the system location services

    private LatLng currentLocation; //Current location of the user.

    private Location location; //Initial location of the user.

    private GoogleMap mMap; //Object reference to the google map displayed.

    private Marker myLocationMarker; //Current location marker to be displayed on the map.

    private BroadcastReceiver broadcastReceiver; //Receives and handles broadcast intents sent from getLocationService.class

    private PlaceAutocompleteFragment placeAutoComplete; //A fragment that provides auto-completion for places.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_handler);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * The method setDisplayHomeAsUpEnabled allows Up navigation with the app icon in the action bar.
         * Essentially the parent activity for the currentLocation activity is declared in the manifest file as the NavigationDrawer activity.
         * The parent activity has launch mode <singleTop>, meaning only one instance of that activity can be created.
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**--All fields/objects are inflated here--**/

        latitudeField = (TextView) findViewById(R.id.latitudeValue);
        longitudeField = (TextView) findViewById(R.id.longitudeValue);

        //Get a reference to the system's location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.the_map); //Inflate the map fragment

        Intent i = new Intent(getApplicationContext(), getLocationService.class); //Declare an intent to point to our service

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); //Inflate the floating button

        //Inflate the place auto complete fragment
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);

        /**--All fields/object details are set here--**/

        mf.getMapAsync(CurrentLocationActivity.this); //Calls onMapReady when loaded

        /**--External methods defined here--**/

        setLocationProvider(); //Method to set location provider for the location manager
        setInitialLocation(); //Method to obtain the initial location, by getting the last known location
        startService(i); //Start the service

        /**--All Listeners are defined here--**/

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 * When floating button is pressed, the map camera moves to the user's current location.
                 */

                if(currentLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                } else if (location != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                } else {
                    Toast.makeText(CurrentLocationActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                /**
                 * When a place is search for, a marker will be placed at that location.
                 * The camera will also be moved to the place's location.
                 */

                if(place != null) {
                    mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                            .title(place.getName().toString())
                            .snippet(place.getAddress().toString())
                    );
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                }
            }

            @Override
            public void onError(Status status) {

                /**
                 * Error checking.
                 */

                Log.e(TAG, status.toString());
                Toast.makeText(CurrentLocationActivity.this, status.getStatusMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    /**
                     * Get the updated latitude and longitude coordinated from the service.
                     * Provide default values if location is not obtained.
                     */

                    double lat = intent.getDoubleExtra("latitude", 51.507351);
                    double lng = intent.getDoubleExtra("longitude", -0.127758);

                    /**
                     * Set the latitude and longitude textViews to the updated location coordinates
                     */

                    latitudeField.setText(String.valueOf(lat));
                    longitudeField.setText(String.valueOf(lng));

                    /**
                     * Set the current location LatLang object to the updated location coordinates.
                     */

                    currentLocation = new LatLng(lat,lng);

                    /**
                     * Use a thread to update the current location marker's position on the map.
                     * Update the marker's snippet too.
                     */

                    new Thread(new Runnable() {
                        public void run(){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(currentLocation != null) {
                                        myLocationMarker.setPosition(currentLocation);
                                        myLocationMarker.setSnippet(currentLocation.latitude + ", " + currentLocation.longitude);
                                    }
                                }
                            });

                        }
                    }).start();

                    //Toast.makeText(CurrentLocationActivity.this, "Receiving location updates from service " + lat + " " + lng, Toast.LENGTH_SHORT).show();

                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update")); //Register the broadcastReceiver
    }

    /**
     * Unregister the broadcast receiver when the application is closed.
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    /**
     *  Method for obtaining the last know location, from the location manager.
     *  Once the last know location is obtained, the location object is set.
     *  The latitude and longitude text views are also set.
     */

    private void setInitialLocation(){
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            } else {

            }
            location = locationManager.getLastKnownLocation(provider);

            if (location == null) {
                latitudeField.setText("Location not available");
                longitudeField.setText("Location not available");
            } else {
                latitudeField.setText(location.getLatitude() + "");
                longitudeField.setText(location.getLongitude() + "");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method to set location provider for the location manager.
     * In our case we want fine location.
     */

    private void setLocationProvider(){
        try{
            Criteria locCriteria = new Criteria();
            locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
            provider = locationManager.getBestProvider(locCriteria, false);
        } catch (SecurityException se){
            se.printStackTrace();
        }
    }

    /**
     * Method is called when the map is ready to be used.
     * @param googleMap - Instance of a GoogleMap associated with the mapFragment(mf), that defines the callback.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; //Set the map object

        //Set the custom info window adapter
        mMap.setInfoWindowAdapter(new CustomerInfoWindowAdapter(CurrentLocationActivity.this));

        /**
         *  Get the initial location and set the current location marker to those coordinates.
         *  Move the camera to the initial location.
         */

        if(location != null) {
            LatLng initialLocation = new LatLng(location.getLatitude(), location.getLongitude());

            MarkerOptions a = new MarkerOptions().position(initialLocation)
                    .title("My current location!")
                    .snippet(initialLocation.latitude + ", " + initialLocation.longitude);

            myLocationMarker = googleMap.addMarker(a);
            myLocationMarker.showInfoWindow();

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 17));
        }
    }
}
