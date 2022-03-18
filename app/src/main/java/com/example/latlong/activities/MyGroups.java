package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class MyGroups extends AppCompatActivity implements GroupListener {

    TextView groupNameText, memberCountText, pleaseWaitText;
    ImageView deleteGroupBtn, groupImage, home;
    LinearLayout groupLayout, createdGroups, linearLayout;
    View view;
    DatabaseReference reference;
    String groupNamesFromDb, memberCountFromDb, groupNumberFromDb, name, count;
    ArrayList<Integer> memberCount, groupNo;
    Integer groupNumber;
    RelativeLayout relativeLayout;
    ProgressBar progressBar;
    RecyclerView groupsRecyclerview;
    GroupsAdapter adapter;
    GroupInformation groupInfo;
    ArrayList<GroupInformation> groupsList;
    int groupClickedPosition = -1;
    boolean isAvailable = false;

    public static final int REQUEST_CODE_ADD_GROUP = 1;
    public static final int REQUEST_CODE_UPDATE_GROUP = 2;
    public static final int REQUEST_CODE_SHOW_GROUP = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups2);

//        groupLayout = findViewById(R.id.groupNameButtonLayout);
        createdGroups = findViewById(R.id.createdGroupsLayout);
        groupsRecyclerview = findViewById(R.id.myGroupsRecyclerView);

        pleaseWaitText = findViewById(R.id.pleaseText);
        progressBar = findViewById(R.id.createdProgressBar);

        home = findViewById(R.id.homeBtn);

        progressBar.setVisibility(View.VISIBLE);
        pleaseWaitText.setVisibility(View.VISIBLE);

        groupInfo = new GroupInformation();

        int numberOfGroups = getIntent().getIntExtra("groupCount", 1);
        groupNumber = numberOfGroups;

        reference = FirebaseDatabase.getInstance().getReference("groups");

        memberCount = new ArrayList<>();
        groupNo = new ArrayList<>();

        groupsList = new ArrayList<>();
        adapter = new GroupsAdapter(this, groupsList);
        groupsRecyclerview.setAdapter(adapter);

        for (int i = 1; i <= groupNumber; i++) {
            int finalI = i;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("groups");
            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").
                    child("Group " + finalI).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        groupNamesFromDb = snapshot.child("group_name").getValue().toString();
                        memberCountFromDb = snapshot.child("no_of_members").getValue().toString();
                        groupNumberFromDb = snapshot.child("group_number").getValue().toString();

                        memberCount.add(Integer.valueOf(memberCountFromDb));
                        groupNo.add(Integer.valueOf(groupNumberFromDb));

                        groupInfo.setGroupName(groupNamesFromDb);
                        groupInfo.setMemberCount(memberCountFromDb);
                        groupInfo.setGroupNumber(groupNumberFromDb);

                        groupsList.add(groupInfo);

                        isAvailable = true;

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        getGroups(REQUEST_CODE_SHOW_GROUP, false);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyGroups.this, GroupChoice.class);
                startActivity(intent);
                MyGroups.this.finish();
            }
        });

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.groups_recyclerview, groupLayout, false);

        deleteGroupBtn = view.findViewById(R.id.deleteGroup);
//        relativeLayout = view.findViewById(R.id.relativeLayout);

//        relativeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                clickedMyGroup(v);
//            }
//        });

        deleteGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGroup(v);
            }
        });

    }

    private void deleteGroup(View v) {

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MyGroups.this, GroupChoice.class);
        startActivity(intent);
        MyGroups.this.finish();
    }

    private void getGroups(final int requestCode, final boolean isGroupDeleted) {
        class getGroupsTask extends AsyncTask<Void, Void, ArrayList<GroupInformation>> {

            @Override
            protected ArrayList<GroupInformation> doInBackground(Void... voids) {
                if(isAvailable)
                    return groupsList;
                else
                    return null;
            }

            @Override
            protected void onPostExecute(ArrayList<GroupInformation> groupInformation) {
                super.onPostExecute(groupInformation);
                if (requestCode == REQUEST_CODE_SHOW_GROUP) {
                    groupsList.addAll(groupInformation);
                    adapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_GROUP) {
                    groupsList.add(0, groupInformation.get(0));
                    adapter.notifyItemInserted(0);
                    groupsRecyclerview.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_GROUP) {
                    groupsList.remove(groupClickedPosition);
                    if (isGroupDeleted) {
                        adapter.notifyItemRemoved(groupClickedPosition);
                    } else {
                        groupsList.add(groupClickedPosition, groupInformation.get(groupClickedPosition));
                        adapter.notifyItemChanged(groupClickedPosition);
                    }
                }
            }
        }
        new getGroupsTask().execute();
    }

    @Override
    public void onGroupClicked(GroupInformation note, int position) {
        groupClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), com.example.latlong.activities.GroupInformation.class);
        intent.putExtra("isViewUpdate", true);
        intent.putExtra("note", String.valueOf(note));
        intent.putExtra("memberCount", memberCount.get(groupClickedPosition));
        intent.putExtra("groupNumber", groupNo.get(groupClickedPosition));
        startActivityForResult(intent, REQUEST_CODE_UPDATE_GROUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_GROUP && resultCode == RESULT_OK) {
            getGroups(REQUEST_CODE_ADD_GROUP, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_GROUP && resultCode == RESULT_OK) {
            if (data != null) {
                getGroups(REQUEST_CODE_UPDATE_GROUP, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }

}

//        for (int i = 1; i <= groupNumber; i++) {
//            int finalI = i;
//            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + finalI)
//                    .addValueEventListener(new ValueEventListener() {
//                @SuppressLint("SetTextI18n")
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//                        groupNamesFromDb = snapshot.child("group_name").getValue().toString();
//                        memberCountFromDb = snapshot.child("no_of_members").getValue().toString();
//
//                        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.groups_recyclerview, groupLayout, false);
//
//                        linearLayout = new LinearLayout(getApplicationContext());
//                        linearLayout.addView(view);
//                        createdGroups.addView(linearLayout);
//
//                        groupNameText = view.findViewById(R.id.groupNameTextView);
//                        memberCountText = view.findViewById(R.id.memberCountTextView);
//                        groupImage = view.findViewById(R.id.groupIcon);
//
//                        progressBar.setVisibility(View.GONE);
//                        pleaseWaitText.setVisibility(View.GONE);
//
//                        groupNameText.setText(groupNamesFromDb);
//                        memberCountText.setText(memberCountFromDb + " Member(s)");
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }

//    private void clickedMyGroup(View v) {
//        Intent intent = new Intent(MyGroups.this, GroupInformation.class);
//        for(int i = 0; i<((ViewGroup)v).getChildCount(); i++) {
//            View nextChild = ((ViewGroup)v).getChildAt(i);
//            if(nextChild.getId() == R.id.groupNameTextView){
//                name = ((TextView) nextChild).getText().toString();
//            }
//            if(nextChild.getId() == R.id.memberCountTextView){
//                count = ((TextView) nextChild).getText().toString();
//            }
//        }
//        intent.putExtra("name", name);
//        intent.putExtra("count", count);
//        startActivity(intent);
//    }
