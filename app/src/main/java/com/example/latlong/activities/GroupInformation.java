package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.latlong.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupInformation extends AppCompatActivity {

    ImageView groupIcon;
    TextView groupName, groupMemberEmail, groupMemberStatus;
    LinearLayout membersLayout, newView;
    View view;
    Integer countMember= 0, countGroup =0;
    DatabaseReference reference;
    GoogleSignInAccount acct;
    com.example.latlong.modelClass.GroupInformation availableGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_information);

        groupIcon = findViewById(R.id.nav_header_view_profilePic);
        groupName = findViewById(R.id.groupNameTextView);

        membersLayout = findViewById(R.id.participantsLayout);
        reference = FirebaseDatabase.getInstance().getReference("groups");

        countMember = Integer.valueOf(getIntent().getStringExtra("memberCount"));
        countGroup = Integer.valueOf(getIntent().getStringExtra("groupNumber"));

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        if (getIntent().getBooleanExtra("isViewUpdate", false)) {
            availableGroup = (com.example.latlong.modelClass.GroupInformation) getIntent().getSerializableExtra("note");
            setViewOrUpdateGroup();
        }

        newView = new LinearLayout(this);
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.participants_info_layout, membersLayout, false);
        newView.addView(view);

        membersLayout.addView(newView);

        groupMemberEmail = view.findViewById(R.id.memberEmailTextView);
        groupMemberStatus = view.findViewById(R.id.memberStatus);

        groupMemberEmail.setText(acct.getEmail().toString());
        groupMemberStatus.setText("Admin");
    }

    private void setViewOrUpdateGroup() {

        for(int i=0; i<countMember; i++) {
            newView = new LinearLayout(this);
            view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.participants_info_layout, membersLayout, false);
            newView.addView(view);

            membersLayout.addView(newView);

            groupMemberEmail = view.findViewById(R.id.memberEmailTextView);
            groupMemberStatus = view.findViewById(R.id.memberStatus);

            groupMemberStatus.setText("Member");
            groupName.setText(availableGroup.getGroupName());

            int finalI = i;
            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups")
                    .child("Group "+countMember+1).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    groupMemberEmail.setText(snapshot.child("Member " + finalI).getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}