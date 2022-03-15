package com.example.latlong.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.latlong.R;

public class GroupInformation extends AppCompatActivity {

    ImageView groupIcon;
    TextView groupName, groupMemberEmail, groupMemberStatus;
    LinearLayout membersLayout, newView;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_information);

        groupIcon = findViewById(R.id.nav_header_view_profilePic);
        groupName = findViewById(R.id.groupNameTextView);

        membersLayout = findViewById(R.id.participantsLayout);

        newView = new LinearLayout(this);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.participants_info_layout, membersLayout, false);

        groupMemberEmail = view.findViewById(R.id.memberEmailTextView);
        groupMemberStatus = view.findViewById(R.id.memberStatus);


    }

    private void saveGroup(){

    }
}