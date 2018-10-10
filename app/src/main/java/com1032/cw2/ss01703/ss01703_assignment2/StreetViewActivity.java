package com1032.cw2.ss01703.ss01703_assignment2;

/**
 * This activity is used to display a street view location
 */

/**--All the imports--**/
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

public class StreetViewActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

    /**--All the fields--**/
    private static final String TAG = StreetViewActivity.class.getSimpleName(); //Used to show messages in logcat.

    private PlaceAutocompleteFragment placeAutoComplete; //A fragment that provides auto-completion for places.

    private StreetViewPanoramaFragment streetViewPanoramaFragment; //Street view fragment

    private StreetViewPanorama search; //StreetViewPanorama object declared as a variable, so we can set the street view location in the PlaceAutocompleteFragment.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        /**--All fields/objects are inflated here--**/

        //Inflate the place auto complete fragment
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);

        //Inflate the street view fragment
        streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.street_view_panorama);

        /**--All fields/object details are set here--**/

        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        /**--All Listeners are defined here--**/

        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                /**
                 * When a place is searched for, the street view will change to the corresponding place.
                 */

                if(place != null)
                search.setPosition(place.getLatLng());
            }

            @Override
            public void onError(Status status) {

                /**
                 * Error checking.
                 */

                Log.e(TAG, status.toString());
                Toast.makeText(StreetViewActivity.this, status.getStatusMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Called when the Street View panorama is ready to be used.
     * @param streetViewPanorama- A non-null instance of a StreetViewPanorama associated with the StreetViewPanoramaFragment
     */

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {

        this.search = streetViewPanorama; //Initialise the StreetViewPanorama instance, search.

        streetViewPanorama.setPosition(new LatLng(51.242572, -0.587946)); //Set street view to 'University of Surrey'
    }
}
