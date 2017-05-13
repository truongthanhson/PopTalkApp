package com.poptech.poptalk.maps;

/**
 * Created by sontt on 13/05/2017.
 */

import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.poptech.poptalk.R;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private double mLatitude;
    private double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(getIntent() != null){
            mLatitude = getIntent().getDoubleExtra("lat", 0);
            mLongitude = getIntent().getDoubleExtra("long", 0);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng currentPos = new LatLng(mLatitude,mLongitude);
        googleMap.addMarker(new MarkerOptions().position(currentPos)
                .title("Image Location"));
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentPos)
                .zoom(17).build();
        //Zoom in and animate the camera.
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
