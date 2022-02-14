package com.example.latlong.modelClass;

public class UserModelClass {

    String phoneNo, name, email, password, latitude, longitude, re_password;

    public UserModelClass(String phoneNo, String name, String email, String password, String latitude, String longitude, String re_password) {
        this.phoneNo = phoneNo;
        this.name = name;
        this.email = email;
        this.password = password;
        this.latitude = latitude;
        this.longitude = longitude;
        this.re_password = re_password;
    }

    public UserModelClass() {
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getRe_password() {
        return re_password;
    }

    public void setRe_password(String re_password) {
        this.re_password = re_password;
    }
}
