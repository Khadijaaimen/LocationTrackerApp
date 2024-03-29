package com.example.latlong.groupActivities;

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
import com.example.latlong.activities.MainActivity;
import com.example.latlong.activities.ProfileActivity;
import com.example.latlong.geofencing.GeofenceLocationService;
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

    Button join, make, myGroups, myProfile, logout;
    String intentFrom, intentTo, id;
    String oldLatitude, oldLongitude, tokenFromGoogle, latCard, longCard, tokenFromMain;
    DatabaseReference reference, reference3;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount acct;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    Integer number = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_choice);

        join = findViewById(R.id.joinGroup);
        make = findViewById(R.id.makeGroup);
        myGroups = findViewById(R.id.myGroups);
        myProfile = findViewById(R.id.myProfile);
        logout = findViewById(R.id.logout);

        progressBar = findViewById(R.id.progressMakeGroupBtn);

        reference = FirebaseDatabase.getInstance().getReference("groups");
        reference3 = FirebaseDatabase.getInstance().getReference("token");

        firebaseAuth = FirebaseAuth.getInstance();
        //from main google
        Intent intent = getIntent();

        intentTo = intent.getStringExtra("intented");
        oldLatitude = intent.getStringExtra("latitudeFromGoogle");
        oldLongitude = intent.getStringExtra("longitudeFromGoogle");
        tokenFromGoogle = intent.getStringExtra("token");

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

        progressBar.setVisibility(View.VISIBLE);
        if(tokenFromGoogle !=null) {
            FirebaseMessaging.getInstance().subscribeToTopic(tokenFromGoogle);
        } else {
            reference = FirebaseDatabase.getInstance().getReference("users").child(id).child("information");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        latCard = snapshot.child("latitude").getValue().toString();
                        longCard = snapshot.child("longitude").getValue().toString();
                        tokenFromMain = snapshot.child("token").getValue().toString();

                        FirebaseMessaging.getInstance().subscribeToTopic(tokenFromMain);
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

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
                progressBar.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference("groups").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Admin_Information").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        number = snapshot.child("no_of_groups").getValue(Integer.class);
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(GroupChoice.this, MyGroups.class);
                        intent.putExtra("groupCountFromChoice", number);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                FirebaseDatabase.getInstance().getReference("token").child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Intent intent = new Intent(GroupChoice.this, ProfileActivity.class);

                            if (intentTo != null) {
                                intentFrom = "google";
                                intent.putExtra("latitudeFromGoogle", oldLatitude);
                                intent.putExtra("longitudeFromGoogle", oldLongitude);
                                intent.putExtra("token", tokenFromGoogle);
                                intent.putExtra("intented", intentFrom);
                            } else {
                                intentFrom = "main";
                                intent.putExtra("intented", intentFrom);
                                intent.putExtra("latitudeFromMain", latCard);
                                intent.putExtra("longitudeFromMain", longCard);
                                intent.putExtra("tokenMain", tokenFromMain);
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(GroupChoice.this);
        builder1.setMessage("Are you sure you want to exit?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                        finish();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}