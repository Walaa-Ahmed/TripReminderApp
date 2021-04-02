package com.elsoudany.said.tripreminderapp.history;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.models.MapResponse;
import com.elsoudany.said.tripreminderapp.retrofit.MapsApi;
import com.elsoudany.said.tripreminderapp.retrofit.RetrofitInstance;
import com.elsoudany.said.tripreminderapp.room.Trip;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripDetailsActivity extends AppCompatActivity {
    ImageView mapImageView;
    TextView tripName;
    TextView startPoint;
    TextView endPoint;
    TextView tripStatus;
    TextView tripType;
    TextView date;
    TextView time;
    ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        mapImageView = findViewById(R.id.detailsMapImage);
        tripName = findViewById(R.id.detailTripName);
        startPoint = findViewById(R.id.detailsStartPoint);
        endPoint = findViewById(R.id.detailsEndPoint);
        tripStatus = findViewById(R.id.detailsStatus);
        tripType = findViewById(R.id.detailsType);
        date = findViewById(R.id.detailsDate);
        backBtn = findViewById(R.id.detailsBackPressed);
        time = findViewById(R.id.detailsTime);
        Trip trip = (Trip) getIntent().getSerializableExtra("tripDetails");
        tripName.setText(trip.tripName);
        startPoint.setText(trip.startPoint);
        endPoint.setText(trip.endPoint);
        tripStatus.setText(trip.status);
        tripType.setText(trip.tripType);
        date.setText(trip.date);
        time.setText(trip.time);
        backBtn.setOnClickListener(view -> {
            finish();
        });
        MapsApi mapsApi = RetrofitInstance.getRetrofitInstance().create(MapsApi.class);
        Call<MapResponse> call = mapsApi.getDirections(trip.startPoint,trip.endPoint);
        call.enqueue(new Callback<MapResponse>() {
            @Override
            public void onResponse(Call<MapResponse> call, Response<MapResponse> response) {
                if(response.body().routes.size() != 0) {
                    String encodedPath = response.body().routes.get(0).overview_polyline.points;
                    Glide.with(getApplicationContext()).load("https://maps.googleapis.com/maps/api/staticmap?markers=size:mid%7Ccolor:red%7C\""
                            +trip.startPoint
                            +"|"+trip.endPoint
                            + "\"&size=800x400&path=color:0x212121|weight:5%7Cenc:"
                            + encodedPath
                            + "&key=AIzaSyCdXqSieoMfWeS3GunOh0FKQzKJnsCWIGM")
                            .placeholder(R.drawable.placeholder)
                            .into(mapImageView);

                }
            }

            @Override
            public void onFailure(Call<MapResponse> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}