package com.example.latlong.geofencing;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.googleMaps.GpsTracker;
import com.example.latlong.modelClass.UpdatingLocations;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GeoFencingMap extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    private Double latitudeRefresh;
    private Double longitudeRefresh;
    private Double latGeofence;
    private Double longGeofence;
    private DatabaseReference reference;
    private Integer groupNumber, memberNumber;
    private String name;
    private ArrayList<String> members = new ArrayList<>();
    private ArrayList<UpdatingLocations> data = new ArrayList<>();
    private List<Double> distanceList = new ArrayList<>();

    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        reference = FirebaseDatabase.getInstance().getReference("groups");

        groupNumber = getIntent().getIntExtra("groupNumber", 0);
        memberNumber = getIntent().getIntExtra("memberCount", 0);

        Intent i = getIntent();
        Bundle args = i.getBundleExtra("data");
        members = (ArrayList<String>) args.getSerializable("emails");

        Bundle args2 = i.getBundleExtra("dataUser");
        data = (ArrayList<UpdatingLocations>) args2.getSerializable("userData");

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
    }

    private void meterDistanceBetweenPoints(Double lat_a, Double lng_a, Double lat_b, Double lng_b) {
        double pk = (180.f / Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        double distance = 6366000 * tt;

        distanceList.add(distance);

        Intent intent = new Intent("Sending");
        intent.putExtra("Distance", distance);
        intent.putExtra("Name", name);
        sendBroadcast(intent);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        latGeofence = getIntent().getDoubleExtra("latGeofence", 0.0);
        longGeofence = getIntent().getDoubleExtra("longGeofence", 0.0);

        if (latGeofence != 0.0 && longGeofence != 0.0) {
            LatLng latLngGeofence = new LatLng(latGeofence, longGeofence);
            addMarker(latLngGeofence);
            float GEOFENCE_RADIUS = 400;
            addCircle(latLngGeofence, GEOFENCE_RADIUS);
            addGeofence(latLngGeofence, GEOFENCE_RADIUS);
        }

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(GeoFencingMap.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        GpsTracker gpsTracker = new GpsTracker(GeoFencingMap.this);
        if (gpsTracker.canGetLocation()) {
            latitudeRefresh = gpsTracker.getLatitudeFromNetwork();
            longitudeRefresh = gpsTracker.getLongitudeFromNetwork();
        } else {
            gpsTracker.showSettingsAlert();
        }

        LatLng currentLocation = new LatLng(latitudeRefresh, longitudeRefresh);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

        enableUserLocation();

        mMap.setOnMapLongClickListener(GeoFencingMap.this);
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                mMap.setMyLocationEnabled(true);
            } else {
                //We do not have the permission..

            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "You can add geofence.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handleMapLongClick(latLng);
                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + groupNumber).child("geofence").child("latitude").setValue(latLng.latitude);
                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + groupNumber).child("geofence").child("longitude").setValue(latLng.longitude);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }

        } else {
            handleMapLongClick(latLng);
            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + groupNumber).child("geofence").child("latitude").setValue(latLng.latitude);
            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + groupNumber).child("geofence").child("longitude").setValue(latLng.longitude);
        }

    }

    private void handleMapLongClick(LatLng latLng) {
        mMap.clear();
        addMarker(latLng);
        float GEOFENCE_RADIUS = 400;
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);

        for (int j = 0; j < data.size(); j++) {
            for (int i = 0; i < members.size(); i++) {
                if(distanceList.size()<memberNumber) {
                    if (data.get(j).getUserEmail().equals(members.get(i))) {
                        Double latitude = data.get(j).getUserLat();
                        Double longitude = data.get(j).getUserLng();
                        name = data.get(j).getUserName();
                        if (latGeofence != 0.0 && longGeofence != 0.0) {
                            meterDistanceBetweenPoints(latGeofence, longGeofence, latitude, longitude);
                        } else {
                            meterDistanceBetweenPoints(latLng.latitude, latLng.longitude, latitude, longitude);
                        }
                    }
                }
            }
        }
    }

    private void addGeofence(LatLng latLng, float radius) {

        String GEOFENCE_ID = "SOME_GEOFENCE_ID";
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + errorMessage);
                    }
                });
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
}