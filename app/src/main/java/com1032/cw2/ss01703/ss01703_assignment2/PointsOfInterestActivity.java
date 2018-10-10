package com1032.cw2.ss01703.ss01703_assignment2;

/**
 * This activity/class has multiple functions.
 * 1. Obtain and display the user's current location, as a marker on the map and in text using TextViews.
 * 2. Read data from firebase, and populate the map with points of interest.
 * 3. Display a path on the map, when the marker info window is clicked, to allow users to navigate to the point of interest.
 * 4. Setup GeoFence around a point of interest, and give the user notifications and toast messages when they are close.
 * 5. Implement a search feature, where users can search for points of interest.
 */

/**--All the imports--**/

import android.Manifest;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.goncalves.pugnotification.notification.PugNotification;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;

public class PointsOfInterestActivity extends AppCompatActivity implements OnMapReadyCallback {

    /**--All the fields--**/
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1; //Fine location request code, used when requesting permissions

    private static final String TAG = PointsOfInterestActivity.class.getSimpleName(); //Used to show messages in logcat.

    private TextView latitudeField, longitudeField; //Used to display the current location coordinates in text for the user

    private LocationManager locationManager; //This class provides access to the system location services

    private String provider; //The name of the provider with which to register for location updates

    private Location location; //Initial location of the user.

    private LatLng currentLocation; //Current location of the user.

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
         * Essentially the parent activity for the pointsOfInterest activity is declared in the manifest file as the NavigationDrawer activity.
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

        mf.getMapAsync(PointsOfInterestActivity.this);   // calls onMapReady when loaded

       //setupGeoFence(51.264840, -0.591616, "GeoFence", "GeoFence Test Marker"); //Test Geo-fence marker

        /**--External methods defined here--**/

        setLocationProvider();
        setInitialLocation();
        startService(i);

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
                    Toast.makeText(PointsOfInterestActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
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

            /**
             * Error checking.
             */

            @Override
            public void onError(Status status) {
                Log.e(TAG, status.toString());
                Toast.makeText(PointsOfInterestActivity.this, status.getStatusMessage().toString(), Toast.LENGTH_SHORT).show();
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

                    double lat = intent.getDoubleExtra("latitude", 23);
                    double lng = intent.getDoubleExtra("longitude", 69);

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

                    //Toast.makeText(PointsOfInterestActivity.this, "Receiving location updates from service", Toast.LENGTH_SHORT).show();

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
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {

            location = locationManager.getLastKnownLocation(provider);

            if(location == null){
                latitudeField.setText("Location not available");
                longitudeField.setText("Location not available");
            } else {
                latitudeField.setText(location.getLatitude() + "");
                longitudeField.setText(location.getLongitude() + "");
            }
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
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap; //Set the map object

        //Set the custom info window adapter
       mMap.setInfoWindowAdapter(new CustomerInfoWindowAdapter(PointsOfInterestActivity.this));


        /**
         * When the info window is clicked, a path is displayed from the user's current location marker to the clicked marker.
         */

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                //TODO Remove polyline's for a new path

                //Create the URL to get the request from user's current location marker to clicked marker
                String url = getRequestUrl(myLocationMarker.getPosition(), marker.getPosition());
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url); //Execute AsyncTask TaskRequestDirections.

                Toast.makeText(PointsOfInterestActivity.this, "Path calculated. \nDistance to " + marker.getTitle() + " " +
                        SphericalUtil.computeDistanceBetween(myLocationMarker.getPosition(), marker.getPosition()) + " meters" , Toast.LENGTH_SHORT).show();
            }
        });

        /**
         *  Get the initial location and set the current location marker to those coordinates.
         *  Move the camera to the initial location.
         */

        if(location != null){
            LatLng initialLocation = new LatLng(location.getLatitude(),location.getLongitude());

            MarkerOptions a = new MarkerOptions().position(initialLocation)
                    .title("My current location!")
                    .snippet(initialLocation.latitude + ", " + initialLocation.longitude);

            myLocationMarker = googleMap.addMarker(a);
            myLocationMarker.showInfoWindow();

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 13));
        }

        /**
         * GeoFence test marker setup
         */

        /*MarkerOptions b = new MarkerOptions().position(new LatLng(51.264714, -0.589915))
                .title("GeoFence").snippet("GeoFence Test Marker");
        googleMap.addMarker(b);*/

        /**
         * Call the setMarkers() method when the map is loaded.
         */

        setMarkers();
    }

    /**
     * This method is used to get the point of interest data from firebase.
     * Namely, the name of the POI, the type of POI (Gym or Hospital etc.), and the LatLang coordinates of the POI.
     * This information will be used to construct a marker.
     * The marker will be added to the map, and the a geoFence will also be setup around the marker.
     */

    public void setMarkers(){

        /**
         * Get the database reference of the fitness markers. e.g. Gym POI.
         * call the addListenerForSingleValueEvent method on the database reference.
         * This method executes onDataChange method  immediately and after executing that method once, it stops listening to the reference location it is attached to.
         * Since the details of POI only need to be loaded once, and isn't expected to change frequently or require active listening.
         */

        DatabaseReference fitnessMarkers = FirebaseDatabase.getInstance().getReference("marker/fitness"); //Database reference obtains fitness markers

        fitnessMarkers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /**
                 * The DataSnapshot instance contains data from the database reference location.
                 * Use a for loop, since we want the data of all the children associated to the reference .i.e. all the fitness markers.
                 * Store the values obtained from the DataSnapShot instance as a marker object. This allows getters to be used for obtaining values.
                 * Declare a LatLang object to store the marker's latitude and longitude coordinates.
                 * Use this to add the appropriate marker to the map.
                 * Call the setupGeoFence method to setup the appropriate GeoFence for the marker location.
                 */

                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    myMarker marker = ds.getValue(myMarker.class);
                    LatLng location = new LatLng(marker.getLatitude(), marker.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(location).title(marker.getName()).snippet(marker.getSnippet()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    setupGeoFence(marker.getLatitude(), marker.getLongitude(), marker.getName(), marker.getSnippet());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference supermarketMarkers = FirebaseDatabase.getInstance().getReference("marker/supermarket"); //Database reference obtains supermarket markers

        supermarketMarkers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /**
                 * The DataSnapshot instance contains data from the database reference location.
                 * Use a for loop, since we want the data of all the children associated to the reference .i.e. all the supermarket markers.
                 * Store the values obtained from the DataSnapShot instance as a marker object. This allows getters to be used for obtaining values.
                 * Declare a LatLang object to store the marker's latitude and longitude coordinates.
                 * Use this to add the appropriate marker to the map.
                 * Call the setupGeoFence method to setup the appropriate GeoFence for the marker location.
                 */

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    myMarker marker = ds.getValue(myMarker.class);
                    LatLng location = new LatLng(marker.getLatitude(), marker.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(location).title(marker.getName()).snippet(marker.getSnippet()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    setupGeoFence(marker.getLatitude(), marker.getLongitude(), marker.getName(), marker.getSnippet());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference pharmacyMarkers = FirebaseDatabase.getInstance().getReference("marker/pharmacy"); //Database reference obtains pharmacy markers

        pharmacyMarkers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /**
                 * The DataSnapshot instance contains data from the database reference location.
                 * Use a for loop, since we want the data of all the children associated to the reference .i.e. all the pharmacy markers.
                 * Store the values obtained from the DataSnapShot instance as a marker object. This allows getters to be used for obtaining values.
                 * Declare a LatLang object to store the marker's latitude and longitude coordinates.
                 * Use this to add the appropriate marker to the map.
                 * Call the setupGeoFence method to setup the appropriate GeoFence for the marker location.
                 */

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    myMarker marker = ds.getValue(myMarker.class);
                    LatLng location = new LatLng(marker.getLatitude(), marker.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(location).title(marker.getName()).snippet(marker.getSnippet()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                    setupGeoFence(marker.getLatitude(), marker.getLongitude(), marker.getName(), marker.getSnippet());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference restaurantMarkers = FirebaseDatabase.getInstance().getReference("marker/restaurant"); //Database reference obtains restaurant markers

        restaurantMarkers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /**
                 * The DataSnapshot instance contains data from the database reference location.
                 * Use a for loop, since we want the data of all the children associated to the reference .i.e. all the restaurant markers.
                 * Store the values obtained from the DataSnapShot instance as a marker object. This allows getters to be used for obtaining values.
                 * Declare a LatLang object to store the marker's latitude and longitude coordinates.
                 * Use this to add the appropriate marker to the map.
                 * Call the setupGeoFence method to setup the appropriate GeoFence for the marker location.
                 */

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    myMarker marker = ds.getValue(myMarker.class);
                    LatLng location = new LatLng(marker.getLatitude(), marker.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(location).title(marker.getName()).snippet(marker.getSnippet()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    setupGeoFence(marker.getLatitude(), marker.getLongitude(), marker.getName(), marker.getSnippet());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });
    }

    /**
     * This method is used setup a geoFence at a specific location.
     * A library has been used to implement the geoFence. See gradle file for more details.
     * @param lat - The latitude of the location
     * @param lng - The longitude of the location
     * @param markerName - The name of the marker, for which the GeoFence is being setup
     * @param markerSnippet - The snippet of the marker, for which the GeoFence is being setup.
     */

    private void setupGeoFence(final double lat, final double lng, final String markerName, final String markerSnippet){

        /**
         * Create a geoFenceModel and give it id 'id_enter'.
         * This will be responsible for monitoring when the user enter's the geoFence.
         * Set the transition to 'GEOFENCE_TRANSITION_ENTER' to indicate we are tracking when the user enter's the geoFence.
         * Set the latitude and longitude of the location, where the geoFence is to be set.
         * Set the radius to adjust the proximity for the location.
         * The latitude, longitude, and radius define a geoFence, creating a circular area, or fence, around the location of interest.
         * finally call the build method to create the geoFenceModel.
         */

        GeofenceModel enterGeo = new GeofenceModel.Builder("id_enter")
                .setTransition(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setLatitude(lat)
                .setLongitude(lng)
                .setRadius(100)
                .build();

        /**
         * Create a geoFenceModel and give it id 'id_dwell'.
         * This will be responsible for monitoring when the user is in the geoFence.
         * Set the transition to 'GEOFENCE_TRANSITION_DWELL' to indicate we are tracking when the user is in the geoFence.
         * Set the latitude and longitude of the location, where the geoFence is to be set.
         * Set the radius to adjust the proximity for the location.
         * The latitude, longitude, and radius define a geoFence, creating a circular area, or fence, around the location of interest.
         * finally call the build method to create the geoFenceModel.
         */

        GeofenceModel inGeo = new GeofenceModel.Builder("id_dwell")
                .setTransition(Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLatitude(lat)
                .setLongitude(lng)
                .setRadius(150)
                .build();

        /**
         * Create a geoFenceModel and give it id 'id_exit'.
         * This will be responsible for monitoring when the user exits the geoFence.
         * Set the transition to 'GEOFENCE_TRANSITION_EXIT' to indicate we are tracking when the user exits the geoFence.
         * Set the latitude and longitude of the location, where the geoFence is to be set.
         * Set the radius to adjust the proximity for the location.
         * The latitude, longitude, and radius define a geoFence, creating a circular area, or fence, around the location of interest.
         * finally call the build method to create the geoFenceModel.
         */

        GeofenceModel exitGeo = new GeofenceModel.Builder("id_exit")
                .setTransition(Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLatitude(lat)
                .setLongitude(lng)
                .setRadius(200)
                .build();

        /**
         * Call the SmartLocation object with this activity as the context.
         * Add all three GeofenceModels, and start the OnGeofencingTransitionListener for each one.
         */

        SmartLocation.with(PointsOfInterestActivity.this).geofencing()
                .add(enterGeo)
                .add(inGeo)
                .add(exitGeo)
                .start(new OnGeofencingTransitionListener() {
                    @Override
                    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {

                        /**
                         * PugNotification is a library used to send notifications to the user. See gradle for more details.
                         * Construct the notification with this activity as the context.
                         * The title informs the user of that they are near a POI.
                         * The message contains the type of POI. i.e. Gym or Hospital etc.
                         * The smallIcon and largeIcon's are used to add an image to the notification.
                         * The flag means to use all default values (where applicable).
                         * AutoCancel is set to true, meaning the notification is automatically cancelled after the user has seen it.
                         * Declare the notification type as simple as build the notification
                         */

                        PugNotification.with(PointsOfInterestActivity.this)
                                .load()
                                .title("You are near: " + markerName)
                                .message(markerSnippet)
                                .smallIcon(R.drawable.poi)
                                .largeIcon(R.drawable.poi)
                                .flags(Notification.DEFAULT_ALL)
                                .autoCancel(true)
                                .simple()
                                .build();

                        //Calculate the distance between the user's and POI marker's location.
                       double distanceBetween =  SphericalUtil.computeDistanceBetween(myLocationMarker.getPosition(), new LatLng(lat, lng));

                       //Inform user how far away they are from the marker.
                        Toast.makeText(PointsOfInterestActivity.this, "You are near: " + markerName + "  -  " + distanceBetween + "  meters away" , Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * This method is used to create a Directions API request
     * @param origin - Starting location
     * @param dest - Ending location
     * @return - Return the string request, which is a URL.
     */

    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude +","+origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_org +"&" + str_dest + "&" +sensor+"&" +mode;
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }

    /**
     * This method is used to get the direction using HttpURLConnection.
     * @param reqUrl - Directions API request URL
     * @return - The responseString
     * @throws IOException
     */

    public String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    /**
     * This AsyncTask is used to call the requestDirection method.
     */

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return  responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    /**
     * This AsyncTask will be used to parse the json response result.
     */

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList<LatLng> points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList<LatLng>();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                //polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions!=null) {
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    }