package com.example.latlong.modelClass;

public class UserLocation {
    private String locationName;
    private double latitude;
    private double longitude;
    private String address;

    public UserLocation(String locationName, double latitude, double longitude, String address) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public UserLocation() {
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
