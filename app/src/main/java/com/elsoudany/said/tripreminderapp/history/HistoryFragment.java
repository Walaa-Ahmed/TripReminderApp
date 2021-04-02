package com.elsoudany.said.tripreminderapp.history;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.Trip;
import com.elsoudany.said.tripreminderapp.room.TripDAO;
import com.elsoudany.said.tripreminderapp.room.User;
import com.elsoudany.said.tripreminderapp.room.UserDAO;
import com.elsoudany.said.tripreminderapp.room.UserTrip;
import com.elsoudany.said.tripreminderapp.room.UserTripDAO;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class HistoryFragment extends Fragment {
    ArrayList<Trip> HistoryTripList = new ArrayList<>();
    RecyclerView HistoryTripListView;
    HistoryAdapter historyAdapter;
    TripDAO tripDAO;
    UserDAO userDAO;
    UserTripDAO userTripDAO;
    String id;
    HistoryFragment.MyHandler handler;
    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle("History");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        handler = new HistoryFragment.MyHandler();
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        HistoryTripListView = view.findViewById(R.id.HistoryTripList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        HistoryTripListView.setLayoutManager(layoutManager);
        historyAdapter = new HistoryAdapter(getActivity(), HistoryTripList, Glide.with(this));
        HistoryTripListView.setAdapter(historyAdapter);

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
                User user1 = new User(id);
                userDAO.insertAll(user1);
                List<UserTrip> tripList = userTripDAO.getAllTrips(id);
                HistoryTripList.clear();
                HistoryTripList.addAll((ArrayList<Trip>) tripList.get(0).tripList);
                HistoryTripList.removeIf(new Predicate<Trip>() {
                    // return started && cancelled trips....
                    @Override
                    public boolean test(Trip trip) {
                        return trip.status.equals("processing");
                    }
                });
                handler.sendEmptyMessage(1);
            }
        }.start();
        return view;
    }


    private class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            historyAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
