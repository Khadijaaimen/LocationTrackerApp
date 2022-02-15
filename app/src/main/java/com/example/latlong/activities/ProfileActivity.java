package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.modelClass.UserModelClass;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity  {

    TextView mName, mEmail, mPhone, lati, longii;
    String latitudes, longitudes;
    ImageView addImage;
    FirebaseAuth firebaseAuth;
    Button logoutBtn;
    GpsTracker gpsTracker;

    FirebaseFirestore firebaseFirestore;
    String userID, personName, personEmail;
    UserModelClass userModelClass;
    GoogleApiClient googleApiClient;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mName = findViewById(R.id.nameProfile);
        mEmail = findViewById(R.id.emailProfile);
        mPhone = findViewById(R.id.phoneProfile);
        addImage = findViewById(R.id.imageAddImage);
        logoutBtn = findViewById(R.id.logoutButton);
        lati = findViewById(R.id.latProfile);
        longii = findViewById(R.id.longProfile);
        firebaseAuth = FirebaseAuth.getInstance();

        userModelClass = new UserModelClass(latitudes, longitudes);

        firebaseFirestore = FirebaseFirestore.getInstance();

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gpsTracker = new GpsTracker(ProfileActivity.this);
        if (gpsTracker.canGetLocation()) {
            double latitude = gpsTracker.getLatitudeFromNetwork();
            double longitude = gpsTracker.getLongitudeFromNetwork();
            lati.setText(String.valueOf(latitude));
            userModelClass.setLatitude(String.valueOf(latitude));
            longii.setText(String.valueOf(longitude));
            userModelClass.setLongitude(String.valueOf(longitude));

            database = FirebaseDatabase.getInstance("https://location-tracker-2be22-default-rtdb.firebaseio.com/");
            reference = database.getReference("users");
            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userModelClass);
        } else {
            gpsTracker.showSettingsAlert();
        }

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()){
                                    Intent intent =new Intent(ProfileActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Session not close",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();
        }


        userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

//        DocumentReference documentReference = firebaseFirestore.collection("users").document(userID);
//
//        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (value != null) {
//                    if(acct != null){
//                        mName.setText(value.getString("name"));
//                        mEmail.setText(value.getString("email"));
//                    } else {
//                        mName.setText(acct.getDisplayName());
//                        mEmail.setText(acct.getEmail());
//                    }
//                } else {
//                    Log.d("tag", "onEvent: Document do not exists");
//                }
//            }
//        });
    }


}