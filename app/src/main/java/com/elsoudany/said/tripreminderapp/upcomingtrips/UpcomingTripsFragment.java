package com.elsoudany.said.tripreminderapp.upcomingtrips;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.reminderwork.ReminderWorker;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.Trip;
import com.elsoudany.said.tripreminderapp.room.TripDAO;
import com.elsoudany.said.tripreminderapp.room.User;
import com.elsoudany.said.tripreminderapp.room.UserDAO;
import com.elsoudany.said.tripreminderapp.room.UserTrip;
import com.elsoudany.said.tripreminderapp.room.UserTripDAO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class UpcomingTripsFragment extends Fragment {
    private static final int ADD_NEW_TRIP_CODE = 1005;
    private static final int EDIT_TRIP_CODE = 1234;
    private static final int BACK_PRESSED = 61;
    private static final String TAG = "MYTAG2";
    ArrayList<Trip> processingTripList = new ArrayList<>();
    RecyclerView processingTripListView;
    TripsAdapter tripsAdapter;
    TripDAO tripDAO;
    UserDAO userDAO;
    UserTripDAO userTripDAO;
    FloatingActionButton fab;
    String id;
    MyHandler handler;
    public UpcomingTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        handler = new MyHandler();
        View view = inflater.inflate(R.layout.fragment_upcoming_trips, container, false);
        processingTripListView = view.findViewById(R.id.processingTripList);
        view.getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
            @Override
            public void onWindowFocusChanged(final boolean hasFocus) {
                // do your stuff here
                Log.i(TAG, "onWindowFocusChanged: ");
                if (hasFocus == true && getActivity() != null) {
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("checkingComingFromService", getActivity().MODE_PRIVATE);
                    boolean comingFromService = sharedPreferences.getBoolean("comingFromService", false);
                    if (comingFromService) {
                        Log.i(TAG, "onWindowFocusChanged: ");
                        new Thread() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void run() {

                                List<UserTrip> tripList = userTripDAO.getAllTrips(id);
                                processingTripList.clear();
                                processingTripList.addAll((ArrayList<Trip>) tripList.get(0).tripList);
                                processingTripList.removeIf(new Predicate<Trip>() {
                                    @Override
                                    public boolean test(Trip trip) {
                                        return !trip.status.equals("processing");
                                    }
                                });
                                handler.sendEmptyMessage(1);
                            }
                        }.start();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("comingFromService", false).commit();
                    }
                }
            }

        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        processingTripListView.setLayoutManager(layoutManager);
        tripsAdapter = new TripsAdapter(getActivity(), processingTripList);
        processingTripListView.setAdapter(tripsAdapter);
        fab = view.findViewById(R.id.addBtn);

        fab.setOnClickListener(v -> {
            startActivityForResult(new Intent(getActivity(), AddTripActivity.class), ADD_NEW_TRIP_CODE);
        });
        AppDatabase db = Room.databaseBuilder(getActivity().getApplicationContext(), AppDatabase.class, "DataBase-name").build();
        tripDAO = db.tripDAO();
        userTripDAO = db.userTripDAO();
        userDAO = db.userDAO();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        id = firebaseAuth.getCurrentUser().getUid();
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {


                List<UserTrip> tripList = userTripDAO.getAllTrips(id);
                if(tripList != null) {
                    processingTripList.clear();

                    processingTripList.addAll((ArrayList<Trip>) tripList.get(0).tripList);
                    processingTripList.removeIf(new Predicate<Trip>() {
                        @Override
                        public boolean test(Trip trip) {
                            return !trip.status.equals("processing");
                        }
                    });
                    handler.sendEmptyMessage(1);
                }
            }

        }.start();
      return view;
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        SharedPreferences sharedPreferences = getSharedPreferences("checkingComingFromService",MODE_PRIVATE);
//        boolean comingFromService = sharedPreferences.getBoolean("comingFromService",false);
//        if(hasFocus == true && comingFromService)
//        {
//            Log.i(TAG, "onWindowFocusChanged: ");
//            new Thread()
//            {
//                @RequiresApi(api = Build.VERSION_CODES.N)
//                @Override
//                public void run() {
//
//                    List<UserTrip> tripList = userTripDAO.getAllTrips(id);
//                    processingTripList.clear();
//                    processingTripList.addAll((ArrayList<Trip>) tripList.get(0).tripList);
//                    processingTripList.removeIf(new Predicate<Trip>() {
//                        @Override
//                        public boolean test(Trip trip) {
//                            return !trip.status.equals("processing");
//                        }
//                    });
//                    tripsAdapter.notifyDataSetChanged();
//                }
//            }.start();
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("comingFromService",false).commit();
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle("Upcoming Trips");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        if (!Settings.canDrawOverlays(getActivity())) {
            int REQUEST_CODE = 101;
            Toast.makeText(getActivity(), "Allow OVERLAY_PERMISSION for floating Dialog", Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            myIntent.setData(Uri.parse("package:" + getActivity().getPackageName()));
            startActivityForResult(myIntent, REQUEST_CODE);
        }
//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("checkingComingFromService", getActivity().MODE_PRIVATE);
//        boolean comingFromService = sharedPreferences.getBoolean("comingFromService", false);
//        if (comingFromService) {
//            new Thread() {
//                @RequiresApi(api = Build.VERSION_CODES.N)
//                @Override
//                public void run() {
//
//                    List<UserTrip> tripList = userTripDAO.getAllTrips(id);
//                    processingTripList.clear();
//                    processingTripList.addAll((ArrayList<Trip>) tripList.get(0).tripList);
//                    processingTripList.removeIf(new Predicate<Trip>() {
//                        @Override
//                        public boolean test(Trip trip) {
//                            return !trip.status.equals("processing");
//                        }
//                    });
//                    handler.sendEmptyMessage(1);
//                }
//            }.start();
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("comingFromService", false).commit();
//        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_TRIP_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "onActivityResult: hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                String tripDirection = data.getStringExtra("radio");
                String start = data.getStringExtra("startPoint");
                String end = data.getStringExtra("endPoint");
                String date = data.getStringExtra("date");
                String time = data.getStringExtra("time");
                String tripName = data.getStringExtra("tripName");
                String userId = data.getStringExtra("userId");
                String status = data.getStringExtra("status");
                Trip addedTrip = new Trip(tripName,start,end,date,time,userId,status,tripDirection);

                processingTripList.add(addedTrip);
                tripsAdapter.notifyDataSetChanged();

                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        addedTrip.uid = tripDAO.insert(addedTrip);
                        Log.i(TAG, addedTrip.toString());
                        WorkManager mWorkManger = WorkManager.getInstance(getActivity().getApplicationContext());
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        LocalDateTime dateTime = LocalDateTime.parse(date + " " + time,formatter);
                        Duration duration = Duration.between(LocalDateTime.now(),dateTime);

                        Log.i(TAG, "onCreate: "+ duration);
                        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                                .setInputData(new Data.Builder().putLong("tripUid", addedTrip.uid).
                                        putString("tripName",addedTrip.tripName)
                                        .build())
                                .setInitialDelay(duration)
                                .build();

                        mWorkManger.enqueueUniqueWork(""+addedTrip.uid, ExistingWorkPolicy.REPLACE,oneTimeWorkRequest);


                    }
                }.start();
            }
        }
        else if(requestCode == EDIT_TRIP_CODE)
        {
            if (resultCode == Activity.RESULT_OK) {
                long tripUid = data.getLongExtra("tripUid", 0);
                String tripDirection = data.getStringExtra("radio");
                String start = data.getStringExtra("startPoint");
                String end = data.getStringExtra("endPoint");
                String date = data.getStringExtra("date");
                String time = data.getStringExtra("time");
                String tripName = data.getStringExtra("tripName");
                String userId = data.getStringExtra("userId");
                String status = data.getStringExtra("status");
                Trip addedTrip = new Trip(tripName, start, end, date, time, userId, status, tripDirection);
                int position = data.getIntExtra("position", 0);
                addedTrip.uid = tripUid;
                processingTripList.add(position, addedTrip);
                tripsAdapter.notifyDataSetChanged();
                Log.i(TAG, "onActivityResult: "+ position);
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        addedTrip.uid = tripDAO.insert(addedTrip);
                        Log.i(TAG, addedTrip.toString());
                        WorkManager mWorkManger = WorkManager.getInstance(getActivity().getApplicationContext());
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, formatter);
                        Duration duration = Duration.between(LocalDateTime.now(), dateTime);

                        Log.i(TAG, "onCreate: " + duration);
                        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                                .setInputData(new Data.Builder().putLong("tripUid", addedTrip.uid).
                                        putString("tripName", addedTrip.tripName)
                                        .build())
                                .setInitialDelay(duration)
                                .build();

                        mWorkManger.enqueueUniqueWork(""+addedTrip.uid, ExistingWorkPolicy.REPLACE,oneTimeWorkRequest);

                    }
                }.start();
            }
            else if(resultCode == BACK_PRESSED){
                Log.i(TAG, "onActivityResult: back Pressed");
                int position = data.getIntExtra("position", 0);
                Trip trip = (Trip) data.getSerializableExtra("tripData");
                processingTripList.add(position, trip);
                tripsAdapter.notifyDataSetChanged();
            }
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            tripsAdapter.notifyDataSetChanged();
        }
    }
}