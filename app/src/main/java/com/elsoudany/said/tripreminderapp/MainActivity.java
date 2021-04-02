package com.elsoudany.said.tripreminderapp;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.elsoudany.said.tripreminderapp.auth.Login;
import com.elsoudany.said.tripreminderapp.mainscreen.Drawer;
import com.elsoudany.said.tripreminderapp.upcomingtrips.AddTripActivity;
import com.elsoudany.said.tripreminderapp.upcomingtrips.ProcessingTripsActivity;
import com.google.firebase.auth.FirebaseAuth;




public class MainActivity extends AppCompatActivity {

    private static final String TAG = "act";
    Button buttonLogout,buttonDrawer;
    Button btn;
    Button btnMayar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null)
        {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences preferencesConfig = getSharedPreferences("status", MODE_PRIVATE);
            preferencesConfig.edit().clear().commit();
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
        btnMayar = findViewById(R.id.BtnMayar);
        buttonLogout=findViewById(R.id.Logout);
        buttonDrawer=findViewById(R.id.drawer);
        btn = findViewById(R.id.RecyclerViewBtn);
        btn.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProcessingTripsActivity.class);
            startActivity(intent);
        });
        btnMayar.setOnClickListener(view ->{
            Intent intent = new Intent(this, AddTripActivity.class);
//            startActivity(intent);
            startActivityForResult(intent,100);

        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.alertdialogsignoutuser);
                dialog.setCancelable(false);
                dialog.show();
                TextView textViewYesLogout = dialog.findViewById(R.id.text_yes_logout);
                TextView textViewNoLogout = dialog.findViewById(R.id.text_no_logout);
                textViewYesLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences preferencesConfig = getSharedPreferences("status", MODE_PRIVATE);
                        preferencesConfig.edit().clear().commit();
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                        finish();

                    }
                });
                textViewNoLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });


            }
        });

        buttonDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Click To Drawer", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Drawer.class);
                startActivity(intent);
            }});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("radio");
                String start =data.getStringExtra("startPoint");
                String end=data.getStringExtra("endPoint");
                String date =data.getStringExtra("date");
                String time=data.getStringExtra("time");
                String name =data.getStringExtra("tripName");
                String userid=data.getStringExtra("userId");
               String dic=

                             "name" +name +"\n"+
                             "start"+start+"\n"+
                            "end" +end+"\n"+
                            "date"+ date+"\n"+
                            "time" +time+"\n"+
                            "user"+userid

                        ;

                Toast.makeText(this, ""+dic, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onActivityResult: "+dic);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }
}