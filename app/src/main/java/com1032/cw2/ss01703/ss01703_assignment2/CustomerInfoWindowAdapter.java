package com1032.cw2.ss01703.ss01703_assignment2;

/**
 * This class is used to provides views for customized rendering of info windows.
 */

/**--All the imports--**/
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context; //The activity in which the custom info window is going to be set

    /**--Constructor--**/

    public CustomerInfoWindowAdapter(Activity context) {
        this.context = context;
    }

    /**
     * Provides a custom info window for a marker.
     * @param marker - The marker for which an info window is being populated.
     * @return - Return null, since we want to use the default info window frame
     */

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * Provides custom contents for the default info window frame of a marker.
     * This method is called since the getInfoWindow method returned null.
     * @param marker- The marker for which an info window is being populated.
     * @return - The custom view that will be displayed as contents in the info window for marker.
     */

    @Override
    public View getInfoContents(Marker marker) {

        //Get the layout from the resources/layouts folder and inflate it.
        View view = context.getLayoutInflater().inflate(R.layout.customer_info_window, null);

        //Inflate the TextViews from the layout
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        TextView tvSubTitle = (TextView) view.findViewById(R.id.tv_subtitle);

        //Set the TextViews to the marker title and snippet.
        tvTitle.setText(marker.getTitle());
        tvSubTitle.setText(marker.getSnippet());

        //Return the view
        return view;
    }
}
