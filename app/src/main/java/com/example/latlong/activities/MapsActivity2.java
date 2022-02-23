package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.example.latlong.R;
import com.example.latlong.googleMaps.FetchURL;
import com.example.latlong.googleMaps.TaskLoadedCallback;
import com.example.latlong.modelClass.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity2 extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    double originLat, originLng, destLat, destLng, latDouble2, lngDouble2, latDouble3, lngDouble3, latDouble4, lngDouble4;
    LatLng origin, dest, location2, location3, location4;
    Polyline currentPolyline;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle b = getIntent().getExtras();
        originLat = b.getDouble("lat1");
        originLng = b.getDouble("long1");

        destLat = b.getDouble("lat2");
        destLng = b.getDouble("long2");

        latDouble2 = b.getDouble("loc2Lat");
        lngDouble2 = b.getDouble("loc2Lng");
        latDouble3 = b.getDouble("loc3Lat");
        lngDouble3 = b.getDouble("loc3Lng");
        latDouble4 = b.getDouble("loc4Lat");
        lngDouble4 = b.getDouble("loc4Lng");

//        origin = new LatLng(33.5719, 73.0833);
//        dest = new LatLng(destLat, destLng);
//        location2 = new LatLng(33.5969, 73.0528);
//        location3 = new LatLng(33.5842, 73.0724);
        origin = new LatLng(originLat, originLng);
        dest = new LatLng(destLat, destLng);
        location2 = new LatLng(latDouble2, lngDouble2);
        location3 = new LatLng(latDouble3, lngDouble3);
        location4 = new LatLng(latDouble4, lngDouble4);

        progressDialog = new ProgressDialog(MapsActivity2.this);
        progressDialog.setMessage("Please Wait, Polyline between two locations is building.");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = getUrl(origin, dest, "driving");

        new FetchURL(MapsActivity2.this).execute(url, "driving");
    }

    //
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.addMarker(new MarkerOptions()
                .position(origin)
                .title("Origin")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        googleMap.addMarker(new MarkerOptions()
                .position(dest)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        googleMap.addMarker(new MarkerOptions()
                .position(location2)
                .title("Location 2")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.addMarker(new MarkerOptions()
                .position(location3)
                .title("Location 3")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.addMarker(new MarkerOptions()
                .position(location4)
                .title("Location 4")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        progressDialog.dismiss();

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dest, 15));
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    //
    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
        }
    }
}