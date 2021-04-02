package com.elsoudany.said.tripreminderapp.retrofit;

import com.elsoudany.said.tripreminderapp.models.MapResponse;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapsApi {
    @GET("json?key=AIzaSyCdXqSieoMfWeS3GunOh0FKQzKJnsCWIGM")
    Call<MapResponse> getDirections(@Query("origin") String origin ,@Query("destination") String destination);
}
