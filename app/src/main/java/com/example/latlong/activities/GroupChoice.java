package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.modelClass.AdminInformation;
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
    String tokenFromMain, intentFrom, intentTo, adminName, adminEmail, token, adminToken, id;
    String oldLatitude, oldLongitude, oldLatitudeMain, oldLongitudeMain, tokenFromGoogle, get;
    Uri image;
    DatabaseReference reference, reference2, reference3;
    GoogleSignInAccount acct;
    GoogleSignInClient mGoogleSignInClient;
    AdminInformation adminInformation;
    Integer noOfGroups=0, groupNoFromDb;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

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
        reference2 = FirebaseDatabase.getInstance().getReference("users");
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

        assert acct != null;
        adminName = acct.getDisplayName();
        adminEmail = acct.getEmail();

        adminInformation = new AdminInformation();

        adminInformation.setAdminName(adminName);
        adminInformation.setAdminEmail(adminEmail);

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getApplicationContext(), "Please wait while data is being loaded", Toast.LENGTH_LONG).show();
        join.setEnabled(false);
        make.setEnabled(false);
        myGroups.setEnabled(false);
        myProfile.setEnabled(false);

        reference.child(id).child("Admin_Information").child("no_of_groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    progressBar.setVisibility(View.GONE);
                    int number = snapshot.getValue(Integer.class);
                    join.setEnabled(true);
                    make.setEnabled(true);
//                    myGroups.setEnabled(true);
                    myProfile.setEnabled(true);
                    groupNoFromDb = number;
                } else{
                    progressBar.setVisibility(View.GONE);
                    join.setEnabled(true);
                    make.setEnabled(true);
//                    myGroups.setEnabled(true);
                    myProfile.setEnabled(true);
                    groupNoFromDb = noOfGroups;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                groupNoFromDb = noOfGroups;
            }
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    token = task.getResult();
                    adminToken = token;
                    adminInformation.setToken(adminToken);
                    reference3.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("user_token").setValue(token);
                }
            }
        });

        make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChoice.this, MakeGroup.class);
                groupNoFromDb++;

                adminInformation.setNumberOfGroups(groupNoFromDb);

                reference.child(id).child("Admin_Information").child("name").setValue(adminInformation.getAdminName());
                reference.child(id).child("Admin_Information").child("email").setValue(adminInformation.getAdminEmail());
                reference.child(id).child("Admin_Information").child("token").setValue(adminInformation.getToken());
                reference.child(id).child("Admin_Information").child("no_of_groups").setValue(adminInformation.getNumberOfGroups());

                intent.putExtra("noOfGroups", groupNoFromDb);
                startActivity(intent);
            }
        });


        myGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChoice.this, MyGroups.class);
                startActivity(intent);
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
}