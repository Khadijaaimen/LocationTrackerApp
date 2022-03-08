package com.example.latlong.activities;

import androidx.appcompat.app.AppCompatActivity;

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

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}