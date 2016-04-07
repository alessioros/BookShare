package it.polimi.dima.bookshare.activities;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ManageUser manageUser;
    private User user;
    private TextView askLocation;
    private LatLng user_city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manageUser = new ManageUser(MapsActivity.this);

        user = manageUser.getUser();

        askLocation = (TextView) findViewById(R.id.ask_location);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (user.getCity() != null && user.getCountry() != null) {

            askLocation.setText("Is " + user.getCity() + ", " + user.getCountry() + " your current location?");

            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.ITALY);

            try {
                List<Address> addresses = geocoder.getFromLocationName(user.getCity(), 1);

                user_city = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

                mMap.addMarker(new MarkerOptions().position(user_city).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(user_city));

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

            askLocation.setText("Search for a location");
        }

    }
}
