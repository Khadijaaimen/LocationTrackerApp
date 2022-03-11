package com.example.latlong.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.latlong.R;

public class MyGroups extends AppCompatActivity {

    Button groupNameBtn;
    ImageButton deleteGroupBtn;
    LinearLayout groupLayout, createdGroups, linearLayout;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups2);

        groupLayout = findViewById(R.id.groupNameButtonLayout);
        createdGroups = findViewById(R.id.createdGroupsLayout);

        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.group_name_button, groupLayout, false);

        groupNameBtn = view.findViewById(R.id.groupNameButton);
        deleteGroupBtn = view.findViewById(R.id.deleteGroup);

        linearLayout = new LinearLayout(getApplicationContext());
        linearLayout.addView(view);
        createdGroups.addView(linearLayout);

        String groupNameFromMakeGroup = getIntent().getStringExtra("groupName");
        Integer numberOfGroups= getIntent().getIntExtra("numberOfGroups", 1);
        groupNameBtn.setText(groupNameFromMakeGroup);
    }
}