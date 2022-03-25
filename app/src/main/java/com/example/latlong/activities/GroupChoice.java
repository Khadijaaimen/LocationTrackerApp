package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.modelClass.AdminInformation;
import com.example.latlong.services.LocationService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class GroupChoice extends AppCompatActivity {

    Button join, make, myGroups, myProfile, logout, location;
    String tokenFromMain, intentFrom, intentTo, id;
    String oldLatitude, oldLongitude, oldLatitudeMain, oldLongitudeMain, tokenFromGoogle;
    DatabaseReference reference, reference3;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount acct;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    int number;

    LocationService mLocationService;
    Intent mServiceIntent;
    private static final int MY_FINE_LOCATION_REQUEST = 99;
    private static final int MY_BACKGROUND_LOCATION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_choice);

        join = findViewById(R.id.joinGroup);
        make = findViewById(R.id.makeGroup);
        myGroups = findViewById(R.id.myGroups);
        myProfile = findViewById(R.id.myProfile);
        logout = findViewById(R.id.logout);
        location = findViewById(R.id.location);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("Background permission");
                    alertDialog.setMessage(R.string.background_location_permission_message);
                    alertDialog.setPositiveButton("Start service anyway", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            starServiceFunc();
                        }
                    });
                    alertDialog.setNegativeButton("Grant background Permission", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestBackgroundLocationPermission();
                        }
                    });
                    alertDialog.create().show();

                } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    starServiceFunc();
                }
            } else {
                starServiceFunc();
            }

        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("ACCESS_FINE_LOCATION")
                        .setMessage("Location permission required")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestFineLocationPermission();
                            }
                        }).create().show();
            } else {
                requestFineLocationPermission();
            }

        }

        progressBar = findViewById(R.id.progressMakeGroupBtn);

        reference = FirebaseDatabase.getInstance().getReference("groups");
        reference3 = FirebaseDatabase.getInstance().getReference("token");

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();

        // from main
        oldLatitudeMain = intent.getStringExtra("latitudeFromMain");
        oldLongitudeMain = intent.getStringExtra("longitudeFromMain");
        tokenFromMain = intent.getStringExtra("tokenMain");

        //from main google
        oldLatitude = intent.getStringExtra("latitudeFromGoogle");
        oldLongitude = intent.getStringExtra("longitudeFromGoogle");
        tokenFromGoogle = intent.getStringExtra("token");

        intentTo = intent.getStringExtra("intented");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("27273984511-ljcd4cm9ccae3e758e9fl37d57sq5me3.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChoice.this, MakeGroup.class);
                startActivity(intent);
            }
        });


        myGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child(id).child("Admin_Information").child("no_of_groups").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Intent intent = new Intent(GroupChoice.this, MyGroups.class);
                            number = snapshot.getValue(Integer.class);
                            intent.putExtra("groupCountFromChoice", number);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                FirebaseDatabase.getInstance().getReference("token").child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Intent intent = new Intent(GroupChoice.this, ProfileActivity.class);

                            if(intentTo.equals("main")){
                                intentFrom = "main";
                                intent.putExtra("intented", intentFrom);
                                intent.putExtra("latitudeFromMain", oldLatitudeMain);
                                intent.putExtra("longitudeFromMain", oldLongitudeMain);
                                intent.putExtra("tokenMain", tokenFromMain);
                            } else{
                                intentFrom = "google";
                                intent.putExtra("latitudeFromGoogle", oldLatitude);
                                intent.putExtra("longitudeFromGoogle", oldLongitude);
                                intent.putExtra("token", tokenFromGoogle);
                                intent.putExtra("intented", intentFrom);
                            }

                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(GroupChoice.this, GeoFencingMap.class);
                startActivity(intent1);
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
                        Intent intent = new Intent(GroupChoice.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Session not close", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void starServiceFunc() {
        mLocationService = new LocationService();
        mServiceIntent = new Intent(this, LocationService.class);
        if (!Util.isMyServiceRunning(LocationService.class, this)) {
            startService(mServiceIntent);
        } else {
            Toast.makeText(this, "Service already running", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_BACKGROUND_LOCATION_REQUEST);
    }

    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, MY_FINE_LOCATION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(this, requestCode, Toast.LENGTH_LONG).show();
        if (requestCode == MY_FINE_LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestBackgroundLocationPermission();
                    }
                }

            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
            return;
        }

        if (requestCode == MY_BACKGROUND_LOCATION_REQUEST) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Background location Permission Granted", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Background location permission denied", Toast.LENGTH_LONG).show();
            }
            return;
        }
    }
}