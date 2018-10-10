package com1032.cw2.ss01703.ss01703_assignment2;

/**
 * This class is primary used to store the information gotten from firebase,
 * in an marker object, and then access the information using getters.
 * Another benefit in storing the information as a marker object, is that it can be directly added to the map.
 */

public class myMarker {


    /**--All the fields--**/
    private String name; //Name of the marker; this will correspond to the name of the POI in firebase.
    private String snippet; //Snippet of the marker; this will correspond to the POI type (e.g. Gym or Hospital) in firebase.

    private double latitude ; //The latitude of the marker; this will correspond to the latitude of the POI in firebase.
    private double longitude; //The longitude of the marker; this will correspond to the longitude of the POI in firebase.

    /**--Default and parameterised constructor--**/

    public myMarker() {

    }

    public myMarker(String name, String snippet, double latitude, double longitude){
        super();
        this.name = name;
        this.snippet = snippet;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**--Getters--**/

    public String getName() {
        return name;
    }

    public String getSnippet() {
        return snippet;
    }

    public double getLatitude() {
        return latitude;
    }


    public double getLongitude() {
        return longitude;
    }
}
