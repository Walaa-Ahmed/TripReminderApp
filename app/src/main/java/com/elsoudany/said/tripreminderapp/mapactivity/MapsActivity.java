package com.elsoudany.said.tripreminderapp.mapactivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.models.MapResponse;
import com.elsoudany.said.tripreminderapp.retrofit.MapsApi;
import com.elsoudany.said.tripreminderapp.retrofit.RetrofitInstance;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.Trip;
import com.elsoudany.said.tripreminderapp.room.TripDAO;
import com.elsoudany.said.tripreminderapp.room.User;
import com.elsoudany.said.tripreminderapp.room.UserDAO;
import com.elsoudany.said.tripreminderapp.room.UserTrip;
import com.elsoudany.said.tripreminderapp.room.UserTripDAO;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final Dot DOT = new Dot();
    private static final Gap GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private static final String TAG = "MapsActivity";
    Polyline prevPolyline;
    Marker prevStartMarker;
    Marker prevEndMarker;
    Button nextPressed;
    Button prevPressed;
    ImageView backBtnPressed;
    GoogleMap mGoogleMap;
    int tripTurn;
    ArrayList<Trip> tripsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getResources().getConfiguration().setLocale(Locale.US);
        tripsList = new ArrayList<>();
        if(savedInstanceState == null) {
            tripTurn = 0;
        }
        else{
            tripTurn = savedInstanceState.getInt("turn");
        }
        nextPressed = findViewById(R.id.nextPressed);
        prevPressed = findViewById(R.id.prevPressed);
        backBtnPressed = findViewById(R.id.mapBackPressed);
        backBtnPressed.setVisibility(View.VISIBLE);
        backBtnPressed.setOnClickListener(view -> {
            finish();
        });
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "DataBase-name").build();
        UserTripDAO userTripDAO = db.userTripDAO();
        UserDAO userDAO = db.userDAO();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String id = firebaseAuth.getCurrentUser().getUid();
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                User user1 = new User(id);
                userDAO.insertAll(user1);
                List<UserTrip> tripList = userTripDAO.getAllTrips(id);
                tripsList.clear();
                tripsList.addAll((ArrayList<Trip>) tripList.get(0).tripList);
                tripsList.removeIf(new Predicate<Trip>() {
                    // return started && cancelled trips....
                    @Override
                    public boolean test(Trip trip) {
                        return trip.status.equals("processing");
                    }
                });
            }
        }.start();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        nextPressed.setClickable(false);
        prevPressed.setClickable(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap = googleMap;
        if(!tripsList.isEmpty()) {
            getTripDirections(tripsList.get(tripTurn));
        }
        nextPressed.setClickable(true);
        prevPressed.setClickable(true);
        nextPressed.setOnClickListener(view -> {
            if (tripTurn >= tripsList.size() - 1) {
                tripTurn = 0;
            } else {
                tripTurn ++;
            }
            getTripDirections(tripsList.get(tripTurn));

        });
        prevPressed.setOnClickListener(view -> {
            if (tripTurn <= 0) {
                tripTurn = tripsList.size() - 1;
            } else {
                tripTurn --;
            }
            getTripDirections(tripsList.get(tripTurn));

        });
        mGoogleMap.setOnPolylineClickListener(new  GoogleMap.OnPolylineClickListener(){
            @Override
            public void onPolylineClick(Polyline polyline) {
                Trip trip = (Trip) polyline.getTag();
                Log.i(TAG, "onPolylineClick: ");
                Toast.makeText(MapsActivity.this,"Trip Name : " + trip.tripName, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("turn",tripTurn);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    void getTripDirections(Trip trip) {
        MapsApi mapsApi = RetrofitInstance.getRetrofitInstance().create(MapsApi.class);
        Call<MapResponse> call = mapsApi.getDirections(trip.startPoint,trip.endPoint);
        call.enqueue(new Callback<MapResponse>() {
            @Override
            synchronized public void onResponse(Call<MapResponse> call, Response<MapResponse> response) {
                if(response.body().routes.size() != 0) {

                    String encodedPath = response.body().routes.get(0).overview_polyline.points;
                    List<LatLng> decodedPath = PolyUtil.decode(encodedPath);
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(decodedPath.get((decodedPath.size() - 1)/2), decodedPath.size()/25));
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.addAll(decodedPath);
                    polylineOptions.color(Color.BLACK);
                    if(prevPolyline != null){
                        prevPolyline.remove();
                        prevStartMarker.remove();
                        prevEndMarker.remove();
                    }
                    prevPolyline = mGoogleMap.addPolyline(polylineOptions);
                    prevPolyline.setTag(trip);
                    prevPolyline.setClickable(true);
                    MarkerOptions startMark = new MarkerOptions();
                    startMark.position(decodedPath.get(0));
                    startMark.title(trip.startPoint);
                    MarkerOptions endMark = new MarkerOptions();
                    endMark.position(decodedPath.get(decodedPath.size() - 1));
                    endMark.title(trip.endPoint);
                    prevStartMarker = mGoogleMap.addMarker(startMark);
                    prevEndMarker = mGoogleMap.addMarker(endMark);
                }
            }

            @Override
            public void onFailure(Call<MapResponse> call, Throwable t) {

            }

        });
    }
}