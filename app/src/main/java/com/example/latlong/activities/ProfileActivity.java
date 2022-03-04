package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.latlong.R;
import com.example.latlong.modelClass.Location;
import com.example.latlong.modelClass.UserModelClass;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    TextView latitudes, longitudes, boldName;
    TextInputLayout mName, mEmail, lastLat, lastLong, shareLocation;
    String lat, lng, oldLatitudeMain, oldLongitudeMain, oldLatitude, oldLongitude, latRefresh, longRefresh;
    ImageView addImage;
    FirebaseAuth firebaseAuth;
    Button logoutBtn, refreshButton, locBtn, updateLocButton, navigateBtn, shareLocationBtn, sendBtn;
    ;
    GpsTracker gpsTracker;
    Double latitude, longitude, latitudeRefresh, longitudeRefresh;
    FirebaseUser currentUser;
    String time;
    String intentFrom, newLatitude, newLongitude;
    Boolean isButtonClicked = false;
    FrameLayout frameLayout;
    EditText editText;
    Boolean visibility_Flag;
    LinearLayout linearLayout;
    String FCM_API = "https://fcm.googleapis.com/fcm/send";
    String serverKey =
            "key=" + "AAAABlmn-f8:APA91bFdofVriN4LhIoa_yHSFlu6OtgTZvm1HV1oUCF5gDRccmCuEJJ0vsZMgVFUpJcJYBqbUIV8lQdeEVtewMLgNbVGRoWdPiO_tgnsWQ-SYgXojXKv0qxalCkAGrGNWk2_eDvnc4F0";
    String contentType = "application/json";
    RequestQueue mRequestQueue;

    String personName, personEmail;
    UserModelClass userModelClass;
    GoogleSignInAccount acct;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;
    DatabaseReference reference;
    List<Location> userLocations;
    Location locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        boldName = findViewById(R.id.nameBoldProfile);
        logoutBtn = findViewById(R.id.logoutButton);
        latitudes = findViewById(R.id.latProfile);
        longitudes = findViewById(R.id.longProfile);
        addImage = findViewById(R.id.imageAddImage);
        refreshButton = findViewById(R.id.refreshLocation);
        locBtn = findViewById(R.id.lastLocation);
        updateLocButton = findViewById(R.id.updateLocation);
        navigateBtn = findViewById(R.id.navigateLocation);
        shareLocationBtn = findViewById(R.id.shareButton);
        sendBtn = findViewById(R.id.send);
        frameLayout = findViewById(R.id.layout);
        mName = findViewById(R.id.layout1);
        editText = findViewById(R.id.title);
        mEmail = findViewById(R.id.layout2);
        linearLayout = findViewById(R.id.linearLayout);

        lastLong = findViewById(R.id.layout8);
        lastLat = findViewById(R.id.layout9);
        shareLocation = findViewById(R.id.layout11);

        firebaseAuth = FirebaseAuth.getInstance();
        userModelClass = new UserModelClass();

        Intent intent = getIntent();

        // from main
        oldLatitudeMain = intent.getStringExtra("latitudeFromMain");
        oldLongitudeMain = intent.getStringExtra("longitudeFromMain");

        //from main google
        oldLatitude = intent.getStringExtra("latitudeFromGoogle");
        oldLongitude = intent.getStringExtra("longitudeFromGoogle");

        intentFrom = intent.getStringExtra("intented");

        Toast.makeText(ProfileActivity.this, "Please press refresh button to get the current location", Toast.LENGTH_LONG).show();

        currentUser = firebaseAuth.getCurrentUser();
        locations = new Location();

        visibility_Flag = false;
        mRequestQueue = Volley.newRequestQueue(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("27273984511-ljcd4cm9ccae3e758e9fl37d57sq5me3.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        database = FirebaseDatabase.getInstance("https://location-tracker-2be22-default-rtdb.firebaseio.com/");
        reference = database.getReference("users");


        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gpsTracker = new GpsTracker(ProfileActivity.this);
        if (gpsTracker.canGetLocation()) {
            latitudeRefresh = gpsTracker.getLatitudeFromNetwork();
            longitudeRefresh = gpsTracker.getLongitudeFromNetwork();
            newLatitude = String.valueOf(latitudeRefresh);
            newLongitude = String.valueOf(longitudeRefresh);
        } else {
            gpsTracker.showSettingsAlert();
        }

        showAllUserData();

        locBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (visibility_Flag) {
                    linearLayout.setVisibility(View.GONE);
                    visibility_Flag = false;
                } else {
                    linearLayout.setVisibility(View.VISIBLE);
                    visibility_Flag = true;
                }
                if (intentFrom.equals("main")) {
                    lastLong.getEditText().setText(oldLongitudeMain);
                    lastLat.getEditText().setText(oldLatitudeMain);
                } else {
                    lastLong.getEditText().setText(oldLongitude);
                    lastLat.getEditText().setText(oldLatitude);
                }

            }
        });

        userLocations = new ArrayList<Location>();

        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("locations").child("0").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (userLocations.size() < 4) {
                        for (DataSnapshot dss : snapshot.getChildren()) {
                            String latitude = dss.child("latitude").getValue().toString();
                            String longitude = dss.child("longitude").getValue().toString();
                            String time = dss.child("time").getValue().toString();
                            userLocations.add(userLocations.size(), new Location(latitude, longitude, time));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(ProfileActivity.this);
                if (gpsTracker.canGetLocation()) {
                    latitude = gpsTracker.getLatitudeFromNetwork();
                    longitude = gpsTracker.getLongitudeFromNetwork();
                    lat = String.valueOf(latitude);
                    lng = String.valueOf(longitude);
                    lastLong.getEditText().setText(lng);
                    lastLat.getEditText().setText(lat);
                    locations.setLatitude(lat);
                    locations.setLongitude(lng);
                    oldLongitude = lng;
                    oldLatitude = lat;
                    oldLongitudeMain = lng;
                    oldLatitudeMain = lat;

                    Date date = new Date();
                    time = DateFormat.getDateTimeInstance().format(date);

                    if (userLocations.size() < 4) {
                        userLocations.add(userLocations.size(), new Location(lat, lng, time));
                        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("locations").setValue(Arrays.asList(userLocations));
                    } else {
                        userLocations.add(userLocations.size(), new Location(lat, lng, time));
                        userLocations.remove(0);
                        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("locations").setValue(Arrays.asList(userLocations));
                    }
                } else {
                    gpsTracker.showSettingsAlert();
                }

                showAllUserData();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isButtonClicked = true;
                gpsTracker = new GpsTracker(ProfileActivity.this);
                if (gpsTracker.canGetLocation()) {
                    latitudeRefresh = gpsTracker.getLatitudeFromNetwork();
                    longitudeRefresh = gpsTracker.getLongitudeFromNetwork();
                    latRefresh = String.valueOf(latitudeRefresh);
                    longRefresh = String.valueOf(longitudeRefresh);
                } else {
                    gpsTracker.showSettingsAlert();
                }
                showAllUserData();
            }
        });

        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(ProfileActivity.this, MapsActivity2.class);
                Bundle b = new Bundle();
                if (intentFrom.equals("google")) {
                    b.putDouble("lat1", Double.parseDouble(oldLatitude));
                    b.putDouble("long1", Double.parseDouble(oldLongitude));
                } else {
                    b.putDouble("lat1", Double.parseDouble(oldLatitudeMain));
                    b.putDouble("long1", Double.parseDouble(oldLongitudeMain));
                }
                b.putDouble("lat2", latitudeRefresh);
                b.putDouble("long2", longitudeRefresh);

                if (userLocations.get(0) != null) {
                    b.putDouble("loc1Lat", Double.parseDouble(userLocations.get(0).getLatitude()));
                    b.putDouble("loc1Lng", Double.parseDouble(userLocations.get(0).getLongitude()));
                }
                if (userLocations.size() == 2) {
                    b.putDouble("loc2Lat", Double.parseDouble(userLocations.get(1).getLatitude()));
                    b.putDouble("loc2Lng", Double.parseDouble(userLocations.get(1).getLongitude()));
                }
                if (userLocations.size() == 3) {
                    b.putDouble("loc3Lat", Double.parseDouble(userLocations.get(2).getLatitude()));
                    b.putDouble("loc3Lng", Double.parseDouble(userLocations.get(2).getLongitude()));
                }

                intent2.putExtras(b);
                startActivity(intent2);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        shareLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibility_Flag) {
                    frameLayout.setVisibility(View.GONE);
                    editText.setVisibility(View.GONE);
                    visibility_Flag = false;
                } else {
                    frameLayout.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.VISIBLE);
                    visibility_Flag = true;
                }
            }
        });
    }

    private void signOut() {
        if (acct != null) {
            firebaseAuth.signOut();

            mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Toast.makeText(getApplicationContext(), "Signed out from google", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Session not close", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showAllUserData() {

        if (acct != null) {
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();

            userModelClass.setEmail(personEmail);
            userModelClass.setName(personName);
            if (intentFrom.equals("google")) {
                userModelClass.setLatitude(oldLatitude);
                userModelClass.setLongitude(oldLongitude);
            } else {
                userModelClass.setLatitude(oldLatitudeMain);
                userModelClass.setLongitude(oldLongitudeMain);
            }


            mName.getEditText().setText(personName);
            mEmail.getEditText().setText(personEmail);
            boldName.setText(personName);
            if (isButtonClicked) {
                latitudes.setText(latRefresh);
                longitudes.setText(longRefresh);
            } else {
                latitudes.setText(newLatitude);
                longitudes.setText(newLongitude);
            }
        }
        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("information").setValue(userModelClass);
    }
}

