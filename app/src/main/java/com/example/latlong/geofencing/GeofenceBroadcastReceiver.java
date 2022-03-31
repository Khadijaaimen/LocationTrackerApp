package com.example.latlong.geofencing;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiver";

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofenceNotificationHelper notificationHelper = new GeofenceNotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence: geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }
        int transitionType = geofencingEvent.getGeofenceTransition();

        String action = intent.getAction();

        if(action.equals("Sending")){
            double state = intent.getExtras().getDouble("extra", 0);
            String userName = intent.getExtras().getString("Name");
            if(state<400){
                notificationHelper.sendHighPriorityNotification("Tracking Location", userName + " has entered geofence.", GeoFencingMap.class);
            } else{
                notificationHelper.sendHighPriorityNotification("Tracking Location", userName + " has left geofence.", GeoFencingMap.class);
            }
        } else {
            switch (transitionType) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                    notificationHelper.sendHighPriorityNotification("", "Entered geofence", GeoFencingMap.class);
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                    notificationHelper.sendHighPriorityNotification("", "Left geofence", GeoFencingMap.class);
                    break;
            }
        }
    }
}