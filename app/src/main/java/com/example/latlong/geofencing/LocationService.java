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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.collect.Iterables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size()>0) {
                Location location = Iterables.getLast(locationList);
//                Toast.makeText(LocationService.this, "Latitude: " + location.getLatitude() + '\n' +
//                        "Longitude: "+ location.getLongitude(), Toast.LENGTH_LONG).show();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                reference.child(id).child("information").child("updating_locations").child("latitude").setValue(location.getLatitude());
                reference.child(id).child("information").child("updating_locations").child("longitude").setValue(location.getLongitude());

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        LocationRequest locationRequest = new LocationRequest();

        locationRequest.setInterval(300000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel(); else startForeground(1, new Notification());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "Permission required", Toast.LENGTH_LONG).show();
            return;
        }else{
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
