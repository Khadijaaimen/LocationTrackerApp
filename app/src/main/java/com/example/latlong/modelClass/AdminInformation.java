package com.example.latlong.modelClass;

public class AdminInformation {
    String adminName, adminEmail, token;
    Integer numberOfGroups;

    public AdminInformation() {

    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String name) {
        this.adminName = name;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getNumberOfGroups() {
        return numberOfGroups;
    }

    public void setNumberOfGroups(Integer numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }
}
