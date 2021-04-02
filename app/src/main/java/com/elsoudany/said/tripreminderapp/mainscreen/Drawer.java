package com.elsoudany.said.tripreminderapp.mainscreen;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.splashscreen.SplachMapScreen;
import com.elsoudany.said.tripreminderapp.auth.Login;
import com.elsoudany.said.tripreminderapp.history.HistoryFragment;
import com.elsoudany.said.tripreminderapp.reminderwork.ReminderWorker;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.Note;
import com.elsoudany.said.tripreminderapp.room.Trip;
import com.elsoudany.said.tripreminderapp.room.TripDAO;
import com.elsoudany.said.tripreminderapp.room.TripNote;
import com.elsoudany.said.tripreminderapp.room.TripNoteDao;
import com.elsoudany.said.tripreminderapp.room.UserTrip;
import com.elsoudany.said.tripreminderapp.room.UserTripDAO;
import com.elsoudany.said.tripreminderapp.upcomingtrips.UpcomingTripsFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Drawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "MYTAG2";
    private static final int RESULT_RETRIP = 3698;
    private DrawerLayout drawer;
    Toolbar toolbar;
    UpcomingTripsFragment upcomingTripsFragment;
    TextView userEmail;
    View headerView;
    String email;
    Snackbar bar;
    SyncHandler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        getResources().getConfiguration().setLocale(Locale.US);
        NavigationView navigationView = findViewById(R.id.nav_view);
        headerView=navigationView.getHeaderView(0);
        setTitle("Upcoming Trips");
        handler = new SyncHandler();
        // shared........27/3
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences preferencesConfig = getSharedPreferences("status", MODE_PRIVATE);
            preferencesConfig.edit().clear().commit();
            Intent intent = new Intent(Drawer.this, Login.class);
            startActivity(intent);
            finish();
        }
        else {
            email = firebaseAuth.getCurrentUser().getEmail();
            ImageView userImage = headerView.findViewById(R.id.userImage);
            Glide.with(this).load(firebaseAuth.getCurrentUser().getPhotoUrl()).circleCrop().placeholder(R.drawable.user).into(userImage);
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_id);

        //access header view in navigation

        userEmail = headerView.findViewById(R.id.txt_userEmail);
        userEmail.setText(email);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //selected item
        if (savedInstanceState == null) {
            upcomingTripsFragment = new UpcomingTripsFragment();
            getSupportFragmentManager().beginTransaction().add(upcomingTripsFragment,"upComingTrip").commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    upcomingTripsFragment).commit();
            navigationView.setCheckedItem(R.id.nav_Upcoming);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_Upcoming:
                // Show Upcoming Trips Fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new UpcomingTripsFragment(),"upComingTrip").commit();
                break;
            case R.id.nav_history:
                // Show History Trips Fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HistoryFragment()).commit();
                break;

            case R.id.nav_map:
                // Open Map
                Intent map=new Intent(Drawer.this, SplachMapScreen.class);
                startActivity(map);
               // Intent map=new Intent(Drawer.this, MapsActivity.class);
               // startActivity(map);
                break;
            case R.id.nav_sync:
                //show Snackbar
                LinearLayout linearLayout = findViewById(R.id.linearLayout);
                bar = Snackbar.make(linearLayout,"Syncing...", Snackbar.LENGTH_INDEFINITE);
                ViewGroup contentLay = (ViewGroup) bar.getView();
                ProgressBar progressBar = new ProgressBar(getApplicationContext());
                progressBar.setPadding(800,0,0,0);
                contentLay.addView(progressBar);
                bar.show();
                // Sync to firebase
                new Thread () {
                    @Override
                    synchronized public void  run(){
                        super.run();
                        AppDatabase db = Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"DataBase-name").build();
                        DatabaseReference mDatabase;
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        UserTripDAO userTripDAO = db.userTripDAO();
                        TripNoteDao tripNoteDao = db.tripNoteDao();
                        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                        List<UserTrip> userTripList = userTripDAO.getAllTrips(uid);
                        ArrayList<Trip> tripList = (ArrayList<Trip>) userTripList.get(0).tripList;
                        for(Trip trip : tripList) {
                            List<TripNote> tripNotesList = tripNoteDao.getAllNotes(trip.uid);
                            List<Note> noteList = tripNotesList.get(0).noteList;
                            mDatabase.child("users").child(uid).child("trips").child(""+trip.uid).setValue(trip);
                            for(Note note : noteList) {
                                mDatabase.child("users").child(uid).child("notes").child(""+ note.uid).setValue(note);
                            }
                        }
                        handler.sendEmptyMessage(1);
                    }
                }.start();
                Toast.makeText(this, "nav_sync", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                Toast.makeText(this, "nav_logout", Toast.LENGTH_SHORT).show();
                //Alert Dialog for logout
                final Dialog dialog = new Dialog(Drawer.this);
                dialog.setContentView(R.layout.alertdialogsignoutuser);
                dialog.setCancelable(false);
                dialog.show();
                Button textViewYesLogout = dialog.findViewById(R.id.text_yes_logout);
                Button textViewNoLogout = dialog.findViewById(R.id.text_no_logout);
                textViewYesLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences preferencesConfig = getSharedPreferences("status", MODE_PRIVATE);
                        preferencesConfig.edit().clear().commit();
                        Intent intent = new Intent(Drawer.this, Login.class);
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
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    class SyncHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            bar.dismiss();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("upComingTrip");
        if(fragment == null) {
            if (resultCode == RESULT_RETRIP) {
                String tripDirection = data.getStringExtra("radio");
                String start = data.getStringExtra("startPoint");
                String end = data.getStringExtra("endPoint");
                String date = data.getStringExtra("date");
                String time = data.getStringExtra("time");
                String tripName = data.getStringExtra("tripName");
                String userId = data.getStringExtra("userId");
                String status = data.getStringExtra("status");
                Trip addedTrip = new Trip(tripName,start,end,date,time,userId,status,tripDirection);
                AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "DataBase-name").build();
                TripDAO tripDAO = db.tripDAO();
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        addedTrip.uid = tripDAO.insert(addedTrip);
                        WorkManager mWorkManger = WorkManager.getInstance(getApplicationContext());
                        DateTimeFormatter formatter = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, formatter);
                            Duration duration = Duration.between(LocalDateTime.now(), dateTime);

                            Log.i(TAG, "onCreate: " + duration);
                            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                                    .setInputData(new Data.Builder().putLong("tripUid", addedTrip.uid).
                                            putString("tripName", addedTrip.tripName)
                                            .build())
                                    .setInitialDelay(duration)
                                    .build();

                            mWorkManger.enqueueUniqueWork("" + addedTrip.uid, ExistingWorkPolicy.REPLACE, oneTimeWorkRequest);
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UpcomingTripsFragment(),"upComingTrip").commit();

                    }
                }.start();
            }
        }
        else {
            fragment.onActivityResult(requestCode, resultCode, data);
        }




    }
}