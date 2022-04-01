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
    ArrayList<String> emails = new ArrayList<>();
    ArrayList<String> listEmail = new ArrayList<>();
    ArrayList<Integer> memberCounts = new ArrayList<>();
    DatabaseReference reference;
    Location location;
    Integer i, a, z, memberNumber, no;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = Iterables.getLast(locationList);
                Toast.makeText(GeofenceLocationService.this, "Latitude: " + location.getLatitude() + '\n' +
                        "Longitude: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                reference = FirebaseDatabase.getInstance().getReference("users");
                reference.child(id).child("information").child("updating_locations").child("latitude").setValue(location.getLatitude());
                reference.child(id).child("information").child("updating_locations").child("longitude").setValue(location.getLongitude());

//                reference.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()) {
//                            for (DataSnapshot ds : snapshot.getChildren()) {
//                                String email = ds.child("information").child("email").getValue(String.class);
//                                listEmail.add(email);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        return;
//                    }
//                });
//
//                FirebaseDatabase.getInstance().getReference("groups").child(id).
//                        addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if (snapshot.exists()) {
//                                    no = snapshot.child("Admin_Information").child("no_of_groups").getValue(Integer.class);
//                                    if (no > 0) {
//                                        FirebaseDatabase.getInstance().getReference("groups").child(id).child("Groups")
//                                                .addValueEventListener(new ValueEventListener() {
//                                                    @Override
//                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                                        if (snapshot.exists()) {
//                                                            for (i = 0; i < no; i++) {
//                                                                memberNumber = snapshot.child("Group " + i).child("no_of_members").getValue(Integer.class);
//                                                                memberCounts.add(memberNumber);
//                                                            }
//                                                        }
//                                                        gettingInformation();
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                                    }
//                                                });
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });

            }
        }
    };

    private void gettingInformation() {

        if (memberNumber > 0) {
            FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Groups").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (a = 0; a < memberCounts.size(); a++) {
                            for (z = 0; z < memberNumber; z++) {
                                String email = snapshot.child("Group " + a).child("Member " + z).child("email").getValue(String.class);
                                emails.add(email);
                            }

                                for (int k = 0; k < listEmail.size(); k++) {
                                    for (int j = 0; j < emails.size(); j++) {
                                        if (emails.get(j).equals(listEmail.get(k))) {
                                        FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + a)
                                                .child("Member " + j).child("updating_locations").child("latitude").setValue(location.getLatitude());
                                        FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + a)
                                                .child("Member " + j).child("updating_locations").child("longitude").setValue(location.getLongitude());
                                    }
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
    }

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
        return START_STICKY;
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
