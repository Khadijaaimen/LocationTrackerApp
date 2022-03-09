package com.example.latlong.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.latlong.R;

public class GroupChoice extends AppCompatActivity {

    Button join, make;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_choice);

        join = findViewById(R.id.joinGroup);
        make = findViewById(R.id.makeGroup);

        make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChoice.this, MakeGroup.class);
                startActivity(intent);
            }
        });
    }
}