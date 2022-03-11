package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.latlong.R;
import com.example.latlong.modelClass.AdminInformation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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

    Button join, make, myGroups, myProfile;
    String latCard, longCard, tokenFromMain, intentFrom, adminName, adminEmail, token, adminToken, id;
    DatabaseReference reference;
    GoogleSignInAccount acct;
    AdminInformation adminInformation;
    Integer noOfGroups=0, groupNoFromDb;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_choice);

        join = findViewById(R.id.joinGroup);
        make = findViewById(R.id.makeGroup);
        myGroups = findViewById(R.id.myGroups);
        myProfile = findViewById(R.id.myProfile);
        progressBar = findViewById(R.id.progressMakeGroupBtn);

        reference = FirebaseDatabase.getInstance().getReference("groups");

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

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
                    myGroups.setEnabled(true);
                    myProfile.setEnabled(true);
                    groupNoFromDb = number;
                } else{
                    progressBar.setVisibility(View.GONE);
                    join.setEnabled(true);
                    make.setEnabled(true);
                    myGroups.setEnabled(true);
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

                FirebaseDatabase.getInstance().getReference("users").child(id).child("information").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            latCard = snapshot.child("latitude").getValue().toString();
                            longCard = snapshot.child("longitude").getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                FirebaseDatabase.getInstance().getReference("token").child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            tokenFromMain = snapshot.child("user_token").getValue().toString();

                            intentFrom = "main";
                            Intent intent = new Intent(GroupChoice.this, ProfileActivity.class);
                            intent.putExtra("intented", intentFrom);
                            intent.putExtra("latitudeFromMain", latCard);
                            intent.putExtra("longitudeFromMain", longCard);
                            intent.putExtra("tokenMain", tokenFromMain);
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
}