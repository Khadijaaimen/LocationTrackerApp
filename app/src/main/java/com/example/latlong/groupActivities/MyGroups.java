package com.example.latlong.groupActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.latlong.R;
import com.example.latlong.modelClass.GroupInformation;
import com.example.latlong.modelClass.UpdatingLocations;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyGroups extends AppCompatActivity implements GroupListener {

    TextView pleaseWaitText;
    ImageView home;
    LinearLayout createdGroups;
    DatabaseReference reference;
    String groupNamesFromDb, groupIconUrlFromDb;
    Integer memberCountFromDb, groupNumberFromDb, groupNumber, groupClickedPosition = -1;
    ArrayList<Integer> memberCount, groupNo;
    ProgressBar progressBar;
    ArrayList<UpdatingLocations> updatingLocations = new ArrayList<>();
    Button makeGroup;
    RecyclerView groupsRecyclerview;
    GroupsAdapter adapter;
    GroupInformation groupInfo;
    ArrayList<GroupInformation> groupsList;

    boolean isAvailable = false;
    public static final int REQUEST_CODE_UPDATE_GROUP = 2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups2);

        createdGroups = findViewById(R.id.createdGroupsLayout);

        pleaseWaitText = findViewById(R.id.pleaseText);
        progressBar = findViewById(R.id.createdProgressBar);
        makeGroup = findViewById(R.id.makeGroupBtn);

        home = findViewById(R.id.homeBtn);

        progressBar.setVisibility(View.VISIBLE);
        pleaseWaitText.setVisibility(View.VISIBLE);

        int numberOfGroupsFromMake = getIntent().getIntExtra("groupCountFromMake", 0);
        int numberOfGroupsFromChoice = getIntent().getIntExtra("groupCountFromChoice", 0);
        if(numberOfGroupsFromMake != 0) {
            groupNumber = numberOfGroupsFromMake;
        } else{
            groupNumber = numberOfGroupsFromChoice;
        }

        reference = FirebaseDatabase.getInstance().getReference("groups");

        memberCount = new ArrayList<>();
        groupNo = new ArrayList<>();

        groupsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("groups");

        if (groupNumber > 0) {
            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups")
                    .addValueEventListener(new ValueEventListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    groupNamesFromDb = ds.child("group_name").getValue(String.class);
                                    memberCountFromDb = ds.child("no_of_members").getValue(Integer.class);
                                    groupIconUrlFromDb = ds.child("imageURL").getValue(String.class);
                                    groupNumberFromDb = ds.child("group_number").getValue(Integer.class);

                                    memberCount.add(memberCountFromDb);
                                    groupNo.add(groupNumberFromDb);

                                    groupInfo = new GroupInformation(groupNamesFromDb, groupIconUrlFromDb, memberCountFromDb, groupNumberFromDb);

                                    groupsList.add(groupsList.size(), groupInfo);

                                    isAvailable = true;
                                }

                                groupsRecyclerview = findViewById(R.id.myGroupsRecyclerView);
                                adapter = new GroupsAdapter(MyGroups.this, groupsList, MyGroups.this);
                                groupsRecyclerview.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                                pleaseWaitText.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            pleaseWaitText.setText("No Groups created. Please click the button below to make a new group.");
            makeGroup.setVisibility(View.VISIBLE);
        }

        makeGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyGroups.this, MakeGroup.class);
                startActivity(intent);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyGroups.this, GroupChoice.class);
                startActivity(intent);
                MyGroups.this.finish();
            }
        });
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MyGroups.this, GroupChoice.class);
        startActivity(intent);
        MyGroups.this.finish();
    }

    @Override
    public void onGroupClicked(GroupInformation note, int position) {
        groupClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), com.example.latlong.groupActivities.GroupInformation.class);
        intent.putExtra("isViewUpdate", true);
        intent.putExtra("note", note);
        intent.putExtra("memberCount", memberCount.get(groupClickedPosition));
        intent.putExtra("groupNumber", groupNo.get(groupClickedPosition));
        Bundle bundle = new Bundle();
        bundle.putSerializable("emails", updatingLocations);
        intent.putExtra("data", bundle);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_GROUP);
    }
}