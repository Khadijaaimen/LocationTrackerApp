package com.example.latlong.services;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.latlong.R;
import com.example.latlong.activities.MapsActivity;
import com.google.common.collect.Maps;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    DatabaseReference reference;
    String channelID = "notification_channel";
    String channelName = "location_tracker";

    @Override
    public void onNewToken(String token) {
        Log.d("TAG", "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(id).child("information").child("token").setValue(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    public void sendNotification(String title, String body) {

        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setSmallIcon(R.drawable.logo_image)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentTitle(title)
                .setContentText(body)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent);


        notificationManager.notify(0, builder.build());
    }
}
//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        if (remoteMessage.getData() != null) {
//            generateNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
//        }
//    }


//    void generateNotification(String title, String body){
//        Intent intent = new Intent(this, MapsActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID)
//                .setSmallIcon(R.drawable.logo_image)
//                .setAutoCancel(true)
//                .setOnlyAlertOnce(true)
//                .setContentIntent(pendingIntent);
//
//        builder = builder.setContent(getRemoveView(title, body));
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//
//        notificationManager.notify(0, builder.build());
//    }
//
//    private RemoteViews getRemoveView(String title, String body) {
//        RemoteViews remoteViews = new RemoteViews("com.example.latlong", R.layout.notification_layout);
//        remoteViews.setTextViewText(R.id.title, title);
//        remoteViews.setTextViewText(R.id.body, body);
//        remoteViews.setImageViewResource(R.id.app_logo, R.drawable.logo_image);
//
//        return remoteViews;
//    }
//}
