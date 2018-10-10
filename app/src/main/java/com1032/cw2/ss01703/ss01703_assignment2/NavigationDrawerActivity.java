package com1032.cw2.ss01703.ss01703_assignment2;

/**
 * This class is used to setup the navigation drawer.
 */

/**--All the imports--**/
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * CardView is a widget used to display data in similarly styled containers.
     * I obtained CardView by importing an android v7 support library in my gradle.
     * There is a cardView for the currentLocation activity, PointsOfInterest activity and StreetView activity.
     */

    private CardView currentLocationCard, poiCard, streetViewCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**--All fields/objects are inflated here--**/

        //Inflate the card-views
        currentLocationCard = (CardView) findViewById(R.id.currentLocationId);
        poiCard = (CardView) findViewById(R.id.poiId);
        streetViewCard = (CardView) findViewById(R.id.streetViewId);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        /**--All fields/object details are set here--**/

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        /**--All Listeners are defined here--**/

        /**
         * Add the onClick listeners for each of the cards.
         * Each onClick will issue an intent to the start the intended activity.
         * The flag ensures if an instance of this activity already exists, then it will be moved to the front.
         * If an instance does NOT exist, a new instance will be created.
         */

        currentLocationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationDrawerActivity.this, CurrentLocationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        poiCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationDrawerActivity.this, PointsOfInterestActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        streetViewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NavigationDrawerActivity.this, StreetViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    /**
     * This method is called when the activity has detected the user's press of the back key.
     * If the drawer is open, close it.
     * else just go back anyway.
     */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    /**
     * This method is called when an item in the navigation menu is selected.
     * @param item - The selected item
     * @return -  true to display the item as the selected item
     */

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /**
         * The intent flag ensures,
         * If an instance of an Activity already exists, then it will be moved to the front.
         * If an instance does NOT exist, a new instance will be created.
         */

        if (id == R.id.current_location) {
            Intent intent = new Intent(this, CurrentLocationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else if (id == R.id.points_of_interest) {

            Intent intent = new Intent(this, PointsOfInterestActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

        } else if (id == R.id.street_view) {

            Intent intent = new Intent(this, StreetViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

        }

        //Close the drawer after an item is selected

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;

        //displaySelectedScreen(item.getItemId());
    }

    /**
     * Method for fragments
     */

    /*private void displaySelectedScreen(int id){
        android.support.v4.app.Fragment fragment = null;

        switch(id){
            case R.id.street_view:
                fragment = new Login();
                break;
        }

        if(fragment != null){
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_layout, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }*/
}
