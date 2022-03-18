package com.example.latlong.modelClass;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

import java.net.URI;

public class UploadImage {
    String imageUrl;

    public UploadImage() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
