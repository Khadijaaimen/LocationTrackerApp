package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.latlong.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyGroups extends AppCompatActivity {

    TextView groupNameText, memberCountText, pleaseWaitText;
    ImageView deleteGroupBtn, groupImage, home;
    LinearLayout groupLayout, createdGroups, linearLayout;
    View view;
    DatabaseReference reference;
    String groupNamesFromDb;
    String memberCountFromDb;
    ArrayList<String> groupNames;
    ArrayList<Integer> memberCount;
    Integer groupNumber;
    RelativeLayout relativeLayout;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups2);

        groupLayout = findViewById(R.id.groupNameButtonLayout);
        createdGroups = findViewById(R.id.createdGroupsLayout);

        pleaseWaitText = findViewById(R.id.pleaseText);
        progressBar = findViewById(R.id.createdProgressBar);

        home = findViewById(R.id.homeBtn);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.groups_recyclerview, groupLayout, false);

        deleteGroupBtn = view.findViewById(R.id.deleteGroup);
        relativeLayout = view.findViewById(R.id.relativeLayout);

        progressBar.setVisibility(View.VISIBLE);
        pleaseWaitText.setVisibility(View.VISIBLE);

        int numberOfGroups = getIntent().getIntExtra("groupCount", 1);
        groupNumber = numberOfGroups;

        reference = FirebaseDatabase.getInstance().getReference("groups");
        groupNames = new ArrayList<>();
        memberCount = new ArrayList<>();

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyGroups.this, GroupChoice.class);
                startActivity(intent);
                MyGroups.this.finish();
            }
        });


        for (int i = 1; i <= groupNumber; i++) {
            int finalI = i;
            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + finalI)
                    .addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        groupNamesFromDb = snapshot.child("group_name").getValue().toString();
                        memberCountFromDb = snapshot.child("no_of_members").getValue().toString();

                        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.groups_recyclerview, groupLayout, false);

                        linearLayout = new LinearLayout(getApplicationContext());
                        linearLayout.addView(view);
                        createdGroups.addView(linearLayout);

                        groupNameText = view.findViewById(R.id.groupNameTextView);
                        memberCountText = view.findViewById(R.id.memberCountTextView);
                        groupImage = view.findViewById(R.id.groupIcon);

                        progressBar.setVisibility(View.GONE);
                        pleaseWaitText.setVisibility(View.GONE);

                        groupNameText.setText(groupNamesFromDb);
                        memberCountText.setText(memberCountFromDb + " Member(s)");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MyGroups.this, GroupChoice.class);
        startActivity(intent);
        MyGroups.this.finish();
    }
}