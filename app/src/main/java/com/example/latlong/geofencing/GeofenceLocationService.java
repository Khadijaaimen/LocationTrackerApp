package com.example.latlong.geofencing;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.latlong.modelClass.UpdatingLocations;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.collect.Iterables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GeofenceLocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = Iterables.getLast(locationList);
                Toast.makeText(GeofenceLocationService.this, "Latitude: " + location.getLatitude() + '\n' +
                        "Longitude: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                reference.child(id).child("information").child("updating_locations").child("latitude").setValue(location.getLatitude());
                reference.child(id).child("information").child("updating_locations").child("longitude").setValue(location.getLongitude());
            }
        }
    };

    GeofenceNotificationHelper notificationHelper;
    String name;
    List<Double> distanceList = new ArrayList<>();
    ArrayList<UpdatingLocations> geofence = new ArrayList<>();
    ArrayList<UpdatingLocations> data = new ArrayList<>();
    ArrayList<String> emails = new ArrayList<>();
    ArrayList<Integer> memberCounts = new ArrayList<>();
    Integer i, j, k, a, z;
    Integer noOfMembers;
    Boolean isChecked = false;

    @Override
    public void onCreate() {
        super.onCreate();

        LocationRequest locationRequest = new LocationRequest();

        locationRequest.setInterval(500000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel();
        else startForeground(1, new Notification());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "Permission required", Toast.LENGTH_LONG).show();
            return;
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChanel() {
        String notificationChannelId = "Location channel id";
        String channelName = "Background Service";
        NotificationChannel channel = new NotificationChannel(notificationChannelId, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(MODE_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, notificationChannelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Location updates:")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Bundle args = intent.getBundleExtra("geo");
        geofence = (ArrayList<UpdatingLocations>) args.getSerializable("geofenceData");

        Integer groupCount = intent.getIntExtra("groupCount", 0);

        Bundle args2 = intent.getBundleExtra("dataUser");
        data = (ArrayList<UpdatingLocations>) args2.getSerializable("userData");

        FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (i = 0; i < groupCount; i++) {
                                        if (geofence.get(i).getGroupNo().equals(i)) {
                                            noOfMembers = snapshot.child("Group " + i).child("no_of_members").getValue(Integer.class);
                                            memberCounts.add(0, noOfMembers);
                                        }
                                    }
                                    checkEmailExists();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return START_STICKY;
    }

    private void checkEmailExists() {
        isChecked = true;
        if (noOfMembers > 0)
            FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (z = 0; z < memberCounts.size(); z++) {
                                    for (a = 0; a < memberCounts.get(z); a++) {
                                        String memberEmail = snapshot.child("Group " + z).child("Member " + a).child("email").getValue(String.class);
                                        emails.add(memberEmail);
                                    }
                                }
                            }

                            for (j = 0; j < data.size(); j++) {
                                for (k = 0; k < emails.size(); k++) {
                                    if (distanceList.size() < emails.size()) {
                                        if (data.get(j).getUserEmail().equals(emails.get(k))) {
                                            Double latitude = data.get(j).getUserLat();
                                            Double longitude = data.get(j).getUserLng();
                                            name = data.get(j).getUserName();
                                            meterDistanceBetweenPoints(geofence.get(j).getUserLat(), geofence.get(j).getUserLng(), latitude, longitude);
                                            isChecked = false;
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
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

        notificationHelper = new GeofenceNotificationHelper(GeofenceLocationService.this);
        if (distance < 400) {
            notificationHelper.sendHighPriorityNotification("Tracking Location", data.get(j).getUserName() + " has entered geofence.", GeoFencingMap.class);
        } else {
            notificationHelper.sendHighPriorityNotification("Tracking Location", data.get(j).getUserName() + " has left geofence.", GeoFencingMap.class);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
