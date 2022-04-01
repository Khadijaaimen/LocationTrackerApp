package com.example.latlong.modelClass;

import java.io.Serializable;

public class UpdatingLocations implements Serializable {
    String userLat, userLng, userEmail;

    public UpdatingLocations(String userLat, String userLng, String userEmail) {
        this.userLat = userLat;
        this.userLng = userLng;
        this.userEmail = userEmail;
    }

    public String getUserLat() {
        return userLat;
    }

    public void setUserLat(String userLat) {
        this.userLat = userLat;
    }

    public String getUserLng() {
        return userLng;
    }

    public void setUserLng(String userLng) {
        this.userLng = userLng;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
