package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.latlong.R;
import com.example.latlong.adapter.GroupsAdapter;
import com.example.latlong.modelClass.GroupInformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyGroups extends AppCompatActivity {

    Button groupNameBtn;
    ImageButton deleteGroupBtn;
    LinearLayout groupLayout, createdGroups, linearLayout;
    View view;
    DatabaseReference reference;
    String groupNamesFromDb;
    Integer memberCountFromDb;
    ArrayList<String> groupNames;
    ArrayList<Integer> memberCount;
    Integer groupNumber;
    RecyclerView groupsRecyclerview;
    GroupsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups2);

//        groupLayout = findViewById(R.id.groupNameButtonLayout);
        createdGroups = findViewById(R.id.createdGroupsLayout);
        groupsRecyclerview = findViewById(R.id.myGroupsRecyclerView);

//        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.group_name_button, groupLayout, false);

//        deleteGroupBtn = view.findViewById(R.id.deleteGroup);

        int numberOfGroups = getIntent().getIntExtra("groupCount", 1);
        groupNumber = numberOfGroups;

        reference = FirebaseDatabase.getInstance().getReference("groups");
        groupNames = new ArrayList<>();
        memberCount = new ArrayList<>();

        for (int i = 1; i <= groupNumber; i++) {
            int finalI = i;
            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + finalI)
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        groupNamesFromDb = snapshot.child("group_name").getValue(String.class);
                        memberCountFromDb = snapshot.child("no_of_members").getValue(Integer.class);

                        groupNames.add(groupNamesFromDb);
                        memberCount.add(memberCountFromDb);

                        initRecyclerView();

//                        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.group_name_button, groupLayout, false);

//                        linearLayout = new LinearLayout(getApplicationContext());
//                        linearLayout.addView(view);
//                        createdGroups.addView(linearLayout);

//                        groupNameBtn = view.findViewById(R.id.groupNameButton);
//                        groupNameBtn.setText(groupNamesFromDb);
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

    public void initRecyclerView(){
        adapter = new GroupsAdapter(this, groupNames, memberCount);
        groupsRecyclerview.setAdapter(adapter);
    }
}