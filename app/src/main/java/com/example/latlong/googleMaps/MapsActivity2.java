package com.example.latlong.googleMaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.latlong.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MapsActivity2 extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double originLat, originLng, destLat, destLng, latDouble2, lngDouble2, latDouble3, lngDouble3, latDouble1, lngDouble1;
    LatLng dest, location1, location2, location3, location4;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle b = getIntent().getExtras();
        originLat = b.getDouble("lat1");
        originLng = b.getDouble("long1");

        destLat = b.getDouble("lat2");
        destLng = b.getDouble("long2");

        latDouble1 = b.getDouble("loc1Lat");
        lngDouble1 = b.getDouble("loc1Lng");
        latDouble2 = b.getDouble("loc2Lat");
        lngDouble2 = b.getDouble("loc2Lng");
        latDouble3 = b.getDouble("loc3Lat");
        lngDouble3 = b.getDouble("loc3Lng");

        dest = new LatLng(destLat, destLng);

        location1 = new LatLng(latDouble1, lngDouble1);
        location2 = new LatLng(latDouble2, lngDouble2);
        location3 = new LatLng(latDouble3, lngDouble3);
        location4 = new LatLng(originLat, originLng);

        drawPolylines();
    }


    private void drawPolylines() {
        progressDialog = new ProgressDialog(MapsActivity2.this);
        progressDialog.setMessage("Please Wait, Polyline between two locations is building.");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (location3.equals("0.0") && location2 != null && location1 != null && location4 != null && dest != null) {
            String url = getDirectionsUrl(dest, location4);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);

            String url4 = getDirectionsUrl(location4, location3);
            DownloadTask downloadTask4 = new DownloadTask();
            downloadTask4.execute(url4);

            String url3 = getDirectionsUrl(location3, location2);
            DownloadTask downloadTask3 = new DownloadTask();
            downloadTask3.execute(url3);

            String url2 = getDirectionsUrl(location2, location1);
            DownloadTask downloadTask2 = new DownloadTask();
            downloadTask2.execute(url2);
        } else if (location2 != null && location1 != null && location4 != null && dest != null) {
            String url = getDirectionsUrl(dest, location4);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);

            String url4 = getDirectionsUrl(location4, location2);
            DownloadTask downloadTask4 = new DownloadTask();
            downloadTask4.execute(url4);

            String url2 = getDirectionsUrl(location2, location1);
            DownloadTask downloadTask2 = new DownloadTask();
            downloadTask2.execute(url2);
        } else if(location1 != null && location4 != null && dest != null ) {
            String url = getDirectionsUrl(dest, location4);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);

            String url4 = getDirectionsUrl(location4, location1);
            DownloadTask downloadTask4 = new DownloadTask();
            downloadTask4.execute(url4);
        } else if (location4 != null && dest != null) {
                String url = getDirectionsUrl(location4, dest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.addMarker(new MarkerOptions()
                .position(location4)
                .title("Last Updated Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        googleMap.addMarker(new MarkerOptions()
                .position(dest)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        googleMap.addMarker(new MarkerOptions()
                .position(location1)
                .title("Location 1")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.addMarker(new MarkerOptions()
                .position(location2)
                .title("Location 2")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.addMarker(new MarkerOptions()
                .position(location3)
                .title("Location 3")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        progressDialog.dismiss();

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dest, 15));
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {


            progressDialog.dismiss();
            Log.d("result", result.toString());
            ArrayList points = null;
            PolylineOptions lineOptions = null;

            int z = 0;
            while (z < result.size()) {

                points = new ArrayList();
                List<List<HashMap<String, String>>> path1 = Collections.singletonList(result.get(z));

                for (int i = 0; i < path1.size(); i++) {

                    List<HashMap<String, String>> path = path1.get(i);

                    for (int j = 0; j < path.size(); j++) {

                        lineOptions = new PolylineOptions();
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }
                }

                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(getApplicationContext().getResources().getColor(R.color.blue));
                lineOptions.geodesic(true);
                z++;

            }

// Drawing polyline in the Google Map for the i-th
            if (points != null) {
                mMap.addPolyline(lineOptions);
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();
            Log.d("data", data);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}

// 33.5420228 73.0964454
// 33.5283394 73.1057193
// 33.5212751 73.104945