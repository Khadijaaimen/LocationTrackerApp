package com.example.latlong.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.latlong.R;
import com.example.latlong.geofencing.GeofenceLocationService;
import com.example.latlong.googleMaps.GpsTracker;
import com.example.latlong.groupActivities.GroupChoice;
import com.example.latlong.groupActivities.GroupInformation;
import com.example.latlong.groupActivities.Util;
import com.example.latlong.modelClass.UpdatingLocations;
import com.example.latlong.modelClass.UserModelClass;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    LinearLayout buttonGoogle;
    FirebaseAuth mAuth;
    Double latitudeRefresh, longitudeRefresh, latGeofence, longGeofence;
    String newLongitude, newLatitude;
    GoogleApiClient mGoogleApiClient;
    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseUser acct;
    GpsTracker gpsTracker;
    DatabaseReference databaseReference, reference2;
    String msg, token;
    UserModelClass userModelClass = new UserModelClass();
    Integer userCount = 0, groupCount = 0;
    ArrayList<UpdatingLocations> locationsArrayList = new ArrayList<>();
    ArrayList<UpdatingLocations> geofenceArrayList = new ArrayList<>();
    UpdatingLocations locations;

    GeofenceLocationService mLocationService;
    Intent mServiceIntent;
    private static final int MY_FINE_LOCATION_REQUEST = 99;
    private static final int MY_BACKGROUND_LOCATION_REQUEST = 100;

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;


    @Override
    public void onStart() {
        super.onStart();
        buttonGoogle.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        acct = FirebaseAuth.getInstance().getCurrentUser();
        if (isNetwork(getApplicationContext())) {
            if (acct != null) {
                if (geofenceArrayList.size() > 0 && locationsArrayList.size() > 0) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {

                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
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

                            } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
                                starServiceFunc();
                            }
                        } else {
                            starServiceFunc();
                        }

                    } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            new AlertDialog.Builder(MainActivity.this)
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
                    Intent intent = new Intent(MainActivity.this, GroupChoice.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                buttonGoogle.setEnabled(true);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to your internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBarSignBtn);
        mAuth = FirebaseAuth.getInstance();

        progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                            groupCount = snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Admin_Information").child("no_of_groups").getValue(Integer.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseDatabase.getInstance().getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String uEmail = ds.child("information").child("email").getValue(String.class);
                        String uName = ds.child("information").child("name").getValue(String.class);
                        Double lat = ds.child("information").child("updating_locations").child("latitude").getValue(Double.class);
                        Double lng = ds.child("information").child("updating_locations").child("longitude").getValue(Double.class);
                        locations = new UpdatingLocations(lat, lng, uEmail, uName);
                        locationsArrayList.add(locations);
                    }
                    if (geofenceArrayList.size() > 0 && locationsArrayList.size() > 0) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {

                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
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

                                } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                        == PackageManager.PERMISSION_GRANTED) {
                                    starServiceFunc();
                                }
                            } else {
                                starServiceFunc();
                            }

                        } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                new AlertDialog.Builder(MainActivity.this)
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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                latGeofence = ds.child("geofence").child("latitude").getValue(Double.class);
                                longGeofence = ds.child("geofence").child("longitude").getValue(Double.class);
                                Integer number = ds.child("group_number").getValue(Integer.class);
                                locations = new UpdatingLocations(latGeofence, longGeofence, number);
                                geofenceArrayList.add(locations);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("27273984511-ljcd4cm9ccae3e758e9fl37d57sq5me3.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        buttonGoogle = findViewById(R.id.googleSignin);
        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetwork(getApplicationContext())) {
                    signIn();
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            } else {
                                token = task.getResult();
                                msg = token;
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect to your internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reference2 = FirebaseDatabase.getInstance().getReference("token");
    }


    private void starServiceFunc() {
        mLocationService = new GeofenceLocationService();
        mServiceIntent = new Intent(this, GeofenceLocationService.class);
        if (!Util.isMyServiceRunning(GeofenceLocationService.class, this)) {
            mServiceIntent.putExtra("groupCount", groupCount);
            Bundle bundle = new Bundle();
            bundle.putSerializable("geofenceData", geofenceArrayList);
            mServiceIntent.putExtra("geo", bundle);
            Bundle b2 = new Bundle();
            b2.putSerializable("userData", locationsArrayList);
            mServiceIntent.putExtra("dataUser", b2);
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

    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.clearDefaultAccountAndReconnect();
        }
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public boolean isNetwork(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();

                firebaseAuthWithGoogle(account);
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            Intent intent = new Intent(MainActivity.this, GroupChoice.class);

                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://location-tracker-2be22-default-rtdb.firebaseio.com/");

                            databaseReference = database.getReference("users");
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        userCount = (int) snapshot.getChildrenCount();
                                        FirebaseDatabase.getInstance().getReference("users").child("no_of_users").setValue(userCount - 1);
                                        userModelClass.setNoOfUsers(userCount - 1);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                    userCount = 0;
                                }
                            });

                            gpsTracker = new GpsTracker(MainActivity.this);
                            if (gpsTracker.canGetLocation()) {
                                latitudeRefresh = gpsTracker.getLatitudeFromNetwork();
                                longitudeRefresh = gpsTracker.getLongitudeFromNetwork();
                                newLatitude = String.valueOf(latitudeRefresh);
                                newLongitude = String.valueOf(longitudeRefresh);
                            } else {
                                gpsTracker.showSettingsAlert();
                            }
                            String intentFrom = "google";

                            intent.putExtra("latitudeFromGoogle", newLatitude);
                            intent.putExtra("longitudeFromGoogle", newLongitude);
                            intent.putExtra("token", msg);
                            intent.putExtra("intented", intentFrom);

                            reference2.child(uid).child("user_token").setValue(msg);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

                            reference.child(uid).child("information").child("latitude").setValue(newLatitude);
                            reference.child(uid).child("information").child("longitude").setValue(newLongitude);
                            reference.child(uid).child("information").child("name").setValue(user.getDisplayName());
                            reference.child(uid).child("information").child("email").setValue(user.getEmail());
                            reference.child(uid).child("information").child("token").setValue(msg);


                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }
}
