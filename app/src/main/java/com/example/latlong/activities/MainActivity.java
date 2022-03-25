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
import com.example.latlong.services.LocationService;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
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
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    LinearLayout buttonGoogle;
    private FirebaseAuth mAuth;
    double latitudeRefresh, longitudeRefresh;
    String newLongitude, newLatitude;
    GoogleApiClient mGoogleApiClient;
    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount acct;
    GpsTracker gpsTracker;
    DatabaseReference reference;
    String msg, token, id;

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;


    @Override
    public void onStart() {
        super.onStart();
        buttonGoogle.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (isNetwork(getApplicationContext())) {
            if (acct != null) {
//                reference = FirebaseDatabase.getInstance().getReference("users").child(id).child("information");
//                reference.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()) {
//                            String latCard = snapshot.child("latitude").getValue().toString();
//                            String longCard = snapshot.child("longitude").getValue().toString();
//                            String tokenMain = snapshot.child("token").getValue().toString();
//                            String intentFrom = "main";
//
//                            Intent intent = new Intent(MainActivity.this, GroupChoice.class);
//                            intent.putExtra("intented", intentFrom);
//                            intent.putExtra("latitudeFromMain", latCard);
//                            intent.putExtra("longitudeFromMain", longCard);
//                            intent.putExtra("tokenMain", tokenMain);
//
//                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                            startActivity(intent);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//            } else {
//                progressBar.setVisibility(View.GONE);
//                buttonGoogle.setEnabled(true);
//            }
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("27273984511-ljcd4cm9ccae3e758e9fl37d57sq5me3.apps.googleusercontent.com")
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                if (acct != null) {
                    mAuth.signOut();

                    mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Toast.makeText(getApplicationContext(), "Signed out from google", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Session not closed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
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

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mAuth = FirebaseAuth.getInstance();

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
                } else {
                    Toast.makeText(getApplicationContext(), "Please connect to your internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        FirebaseMessaging.getInstance().

                getToken().

                addOnCompleteListener(new OnCompleteListener<String>() {
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
                            Intent intent = new Intent(MainActivity.this, GroupChoice.class);
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
