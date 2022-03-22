package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.latlong.R;
import com.example.latlong.adapter.GroupsAdapter;
import com.example.latlong.listener.GroupListener;
import com.example.latlong.modelClass.GroupInformation;
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
    String groupNamesFromDb;
    Integer memberCountFromDb;
    Integer groupNumberFromDb;
    String groupIconUrlFromDb;
    ArrayList<Integer> memberCount, groupNo;
    Integer groupNumber;
    ProgressBar progressBar;
    Button makeGroup;
    RecyclerView groupsRecyclerview;
    GroupsAdapter adapter;
    GroupInformation groupInfo;
    ArrayList<GroupInformation> groupsList;
    boolean isAvailable = false;
    Integer groupClickedPosition = -1;
    public static final int REQUEST_CODE_UPDATE_GROUP = 2;

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
        int  numberOfGroupsFromChoice = getIntent().getIntExtra("groupCountFromChoice", 0);
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
        Intent intent = new Intent(getApplicationContext(), com.example.latlong.activities.GroupInformation.class);
        intent.putExtra("isViewUpdate", true);
        intent.putExtra("note", note);
        intent.putExtra("memberCount", memberCount.get(groupClickedPosition));
        intent.putExtra("groupNumber", groupNo.get(groupClickedPosition));
        startActivityForResult(intent, REQUEST_CODE_UPDATE_GROUP);
    }
}

//
//    private void getGroups(final int requestCode, final boolean isGroupDeleted) {
//        class getGroupsTask extends AsyncTask<Void, Void, ArrayList<GroupInformation>> {
//
//            @Override
//            protected ArrayList<GroupInformation> doInBackground(Void... voids) {
//                if (isAvailable)
//                    return groupsList;
//                else
//                    return null;
//            }
//
//            @Override
//            protected void onPostExecute(ArrayList<GroupInformation> groupInformation) {
//                super.onPostExecute(groupInformation);
//                if (requestCode == REQUEST_CODE_SHOW_GROUP) {
//                    groupsList.addAll(groupInformation);
//                    adapter.notifyDataSetChanged();
//                } else if (requestCode == REQUEST_CODE_ADD_GROUP) {
//                    groupsList.add(0, groupInformation.get(0));
//                    adapter.notifyItemInserted(0);
//                    groupsRecyclerview.smoothScrollToPosition(0);
//                } else if (requestCode == REQUEST_CODE_UPDATE_GROUP) {
//                    groupsList.remove(groupClickedPosition);
//                    if (isGroupDeleted) {
//                        adapter.notifyItemRemoved(groupClickedPosition);
//                    } else {
//                        groupsList.add(groupClickedPosition, groupInformation.get(groupClickedPosition));
//                        adapter.notifyItemChanged(groupClickedPosition);
//                    }
//                }
//            }
//        }
//        new getGroupsTask().execute();
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_ADD_GROUP && resultCode == RESULT_OK) {
//            getGroups(REQUEST_CODE_ADD_GROUP, false);
//        } else if (requestCode == REQUEST_CODE_UPDATE_GROUP && resultCode == RESULT_OK) {
//            if (data != null) {
//                getGroups(REQUEST_CODE_UPDATE_GROUP, data.getBooleanExtra("isNoteDeleted", false));
//            }
//        }
//    }
