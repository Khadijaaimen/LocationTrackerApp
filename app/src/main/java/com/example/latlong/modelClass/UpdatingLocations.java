package com.example.latlong.modelClass;

import java.io.Serializable;

public class UpdatingLocations implements Serializable {
    Double userLat;
    Double userLng;
    String userEmail, userName;
    Integer groupNo;

    public UpdatingLocations(Double userLat, Double userLng, String userEmail, String userName) {
        this.userLat = userLat;
        this.userLng = userLng;
        this.userEmail = userEmail;
        this.userName = userName;
    }

    public UpdatingLocations(Double userLat, Double userLng, Integer groupNo) {
        this.userLat = userLat;
        this.userLng = userLng;
        this.groupNo = groupNo;
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

    public Integer getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(Integer groupNo) {
        this.groupNo = groupNo;
    }
}
