package com.example.latlong.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.latlong.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    Animation topAnim, bottomAnim;
    ImageView logoImage;
    TextView textView1, textView2;
    FirebaseAuth fAuth;
    private static final int SPLASH_SCREEN = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        fAuth = FirebaseAuth.getInstance();

//        if(fAuth.getCurrentUser() != null){
//            startActivity(new Intent(getApplicationContext(), LatLongActivity.class));
//            finish();
//        }

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        logoImage = findViewById(R.id.logo);
        textView1 = findViewById(R.id.logoText);
        textView2 = findViewById(R.id.text);

        logoImage.setAnimation(topAnim);
        textView1.setAnimation(bottomAnim);
        textView2.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(logoImage, "logo_image");
                pairs[1] = new Pair<View, String>(textView1, "logo_text");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation( SplashActivity.this, pairs);

                startActivity(intent, options.toBundle());
                SplashActivity.this.finish();
            }
        }, SPLASH_SCREEN);
    }
    @Override
    public void onBackPressed() {
        SplashActivity.this.finish(); // Remove this
        super.onBackPressed();
    }
}