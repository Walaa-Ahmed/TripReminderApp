package com.elsoudany.said.tripreminderapp.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.mapactivity.MapsActivity;

public class SplachMapScreen extends AppCompatActivity {

    private static final int Splash_Screen = 2000;
    Animation topAnim, bottomAnim;
    ImageView imgLogo;
    TextView logoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splach_map_screen);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        imgLogo = findViewById(R.id.imgLogo);
        logoTitle = findViewById(R.id.logoTitle);

        imgLogo.setAnimation(topAnim);
        logoTitle.setAnimation(bottomAnim);

        if (savedInstanceState == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplachMapScreen.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, Splash_Screen);
        }


    }
}