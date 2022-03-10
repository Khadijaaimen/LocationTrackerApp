package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.latlong.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class GroupChoice extends AppCompatActivity {

    Button join, make, myGroups, myProfile;
    String latCard, longCard, tokenFromMain, intentFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_choice);

        join = findViewById(R.id.joinGroup);
        make = findViewById(R.id.makeGroup);
        myGroups = findViewById(R.id.myGroups);
        myProfile = findViewById(R.id.myProfile);

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