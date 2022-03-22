package com.example.latlong.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupInformation extends AppCompatActivity {

    ImageView groupIcon;
    TextView groupName, groupMemberEmail, groupMemberStatus;
    LinearLayout membersLayout, newView;
    View view;
    Integer countMember = 0, countGroup = 0;
    DatabaseReference reference;
    GoogleSignInAccount acct;
    com.example.latlong.modelClass.GroupInformation availableGroup;
    CardView cardView;
    ArrayList<String> emails = new ArrayList<>();
    String memberEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_information);

        groupIcon = findViewById(R.id.nav_header_view_profilePic);
        groupName = findViewById(R.id.groupNameTextView);
        cardView = findViewById(R.id.imageCardView);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        membersLayout = findViewById(R.id.participantsLayout);
        reference = FirebaseDatabase.getInstance().getReference("groups");

        countMember = getIntent().getIntExtra("memberCount", 0);
        countGroup = getIntent().getIntExtra("groupNumber", 0);

        acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        newView = new LinearLayout(this);
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.participants_info_layout, membersLayout, false);
        newView.addView(view);

        membersLayout.addView(newView);

        groupMemberEmail = view.findViewById(R.id.memberEmailTextView);
        groupMemberStatus = view.findViewById(R.id.memberStatus);

        groupMemberEmail.setText(acct.getEmail().toString());
        groupMemberStatus.setText("Admin");

        availableGroup = new com.example.latlong.modelClass.GroupInformation();

        if (getIntent().getBooleanExtra("isViewUpdate", false)) {
            availableGroup = (com.example.latlong.modelClass.GroupInformation) getIntent().getSerializableExtra("note");
            setViewOrUpdateGroup();
        }

        groupName.setText(availableGroup.getGroupName());

        if(availableGroup.getGroupIcon() == null){
            groupIcon.setPadding(30, 30, 30, 30);
            groupIcon.setImageResource(R.drawable.groups);
        } else {
            Picasso.get().load(availableGroup.getGroupIcon()).into(groupIcon);
        }
    }

    private void setViewOrUpdateGroup() {

        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Groups").child("Group " + countGroup)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (emails.size() < countMember) {
                                    memberEmail = ds.child("email").getValue().toString();
                                } else{
                                    return;
                                }
                                emails.add(memberEmail);
                                newView = new LinearLayout(GroupInformation.this);
                                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.participants_info_layout, membersLayout, false);
                                newView.addView(view);

                                membersLayout.addView(newView);

                                groupMemberEmail = view.findViewById(R.id.memberEmailTextView);
                                groupMemberStatus = view.findViewById(R.id.memberStatus);

                                groupMemberStatus.setText("Member");
                                groupMemberEmail.setText(memberEmail);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
//    }

}