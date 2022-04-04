package com.example.latlong.modelClass;

import java.io.Serializable;

public class UpdatingLocations implements Serializable {
    Double userLat;
    Double userLng;
    String userEmail, userName;

    public UpdatingLocations(Double userLat, Double userLng, String userEmail, String userName) {
        this.userLat = userLat;
        this.userLng = userLng;
        this.userEmail = userEmail;
        this.userName = userName;
    }

    public Double getUserLat() {
        return userLat;
    }

    public void setUserLat(Double userLat) {
        this.userLat = userLat;
    }

    public Double getUserLng() {
        return userLng;
    }

    public void setUserLng(Double userLng) {
        this.userLng = userLng;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
