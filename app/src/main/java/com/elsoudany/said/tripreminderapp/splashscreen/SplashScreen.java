package com.elsoudany.said.tripreminderapp.splashscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.mainscreen.Drawer;

import java.util.Locale;

public class SplashScreen extends AppCompatActivity {

    private static int Splash_Screen = 3000;

    Animation topAnim,bottomAnim;
    ImageView imgLogo;
    TextView logoTitle,logoDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        getResources().getConfiguration().setLocale(Locale.US);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        imgLogo = findViewById(R.id.imgLogo);
        logoTitle = findViewById(R.id.logoTitle);
        logoDesc = findViewById(R.id.logoDesc);

        imgLogo.setAnimation(topAnim);
        logoTitle.setAnimation(bottomAnim);
        logoDesc.setAnimation(bottomAnim);
        if (savedInstanceState == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreen.this, Drawer.class);
                    startActivity(intent);
                    finish();
                }
            }, Splash_Screen);
        }
    }
}