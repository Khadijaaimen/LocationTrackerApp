package com.example.latlong.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

public class Util {
    public static Boolean isMyServiceRunning(Class<?> serviceClass, Activity mActivity) {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName() == service.service.getClassName()) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

}
