package com.chenp_racheliv.finalProject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    private ImageView icon;
    private TextView logo;
    private TextView names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the Activity Action Bar
        getSupportActionBar().hide();

        // get all refrences from XML
        icon = findViewById(R.id.iconID);
        logo = findViewById(R.id.logoID);
        names = findViewById(R.id.namesID);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.animation);
        icon.startAnimation(anim);
        logo.startAnimation(anim);
        names.startAnimation(anim);

        // delay of 3 seconds before moving to next screen
        Thread timer = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(3000);
                    Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.start();
    }
}
