package com.elsoudany.said.tripreminderapp.history;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.elsoudany.said.tripreminderapp.retrofit.MapsApi;
import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.retrofit.RetrofitInstance;
import com.elsoudany.said.tripreminderapp.models.MapResponse;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.NoteDao;
import com.elsoudany.said.tripreminderapp.room.Trip;
import com.elsoudany.said.tripreminderapp.room.TripDAO;
import com.elsoudany.said.tripreminderapp.room.TripNoteDao;
import com.elsoudany.said.tripreminderapp.upcomingtrips.AddTripActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{
    private static final int ADD_NEW_TRIP_CODE = 1005;
    Context context;
    List<Trip> list;
    RequestManager glide;

    public HistoryAdapter(Context context, List<Trip> list, RequestManager glide) {
        this.context = context;
        this.list = list;
        this.glide = glide;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recy_history_item,parent,false);
        HistoryAdapter.ViewHolder viewHolder = new HistoryAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {

        holder.tripName.setText(list.get(position).tripName);
        //  holder.startPoint.setText(list.get(position).startPoint);
        //  holder.endPoint.setText(list.get(position).endPoint);
        String status = list.get(position).status;
        if(status.equals("cancelled")) {
            holder.tripStatus.setTextColor(Color.RED);
        }
        else {
            holder.tripStatus.setTextColor(Color.GREEN);
        }
        holder.tripStatus.setText(status);

        //  holder.tripType.setText(list.get(position).tripType);
        if(list.get(position).tripType.equals("one")){
            holder.tripType.setText("One Direction");
        }
        if(list.get(position).tripType.equals("round")) {
            holder.tripType.setText("Round Trip");
        }
        MapsApi mapsApi = RetrofitInstance.getRetrofitInstance().create(MapsApi.class);
        Call<MapResponse> call = mapsApi.getDirections(list.get(position).startPoint,list.get(position).endPoint);
        call.enqueue(new Callback<MapResponse>() {
            @Override
            synchronized public void onResponse(Call<MapResponse> call, Response<MapResponse> response) {
                if(response.body().routes.size() != 0) {
                    String encodedPath = response.body().routes.get(0).overview_polyline.points;
                    glide.load("https://maps.googleapis.com/maps/api/staticmap?markers=size:mid%7Ccolor:red%7C\""
                            +list.get(position).startPoint
                            +"|"+list.get(position).endPoint
                            + "\"&size=800x400&path=color:0x212121|weight:5%7Cenc:"
                            + encodedPath
                            + "&key=AIzaSyCdXqSieoMfWeS3GunOh0FKQzKJnsCWIGM")
                            .placeholder(R.drawable.placeholder)
                            .into(holder.mapImageView);
                    holder.duration.setText(response.body().routes.get(0).legs.get(0).duration.text);
                    holder.distance.setText(response.body().routes.get(0).legs.get(0).distance.text);
                }
            }

            @Override
            public void onFailure(Call<MapResponse> call, Throwable t) {

            }
        });
        holder.delete.setOnClickListener(view -> {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.alertdialogsignoutuser);
            dialog.setCancelable(false);
            ((TextView) dialog.findViewById(R.id.alertViewMsg)).setText(R.string.want_delete);

            dialog.show();
            Button textViewYesLogout = dialog.findViewById(R.id.text_yes_logout);
            Button textViewNoLogout = dialog.findViewById(R.id.text_no_logout);
            textViewYesLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Trip trip = list.get(position);
                    list.remove(position);
                    notifyDataSetChanged();

                    new Thread(){
                        @Override
                        synchronized public void run() {
                            AppDatabase db = Room.databaseBuilder(context,AppDatabase.class,"DataBase-name").build();
                            TripDAO tripDAO = db.tripDAO();
                            TripNoteDao tripNoteDao = db.tripNoteDao();
                            NoteDao noteDao = db.noteDao();
                            if(tripNoteDao.getAllNotes(trip.uid).get(0) != null) {
                                noteDao.deleteAll(tripNoteDao.getAllNotes(trip.uid).get(0).noteList);
                            }
                            tripDAO.delete(trip);

                        }
                    }.start();
                }
            });
            textViewNoLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        });
        holder.itemLayout.setOnClickListener(view -> {
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                Intent intent = new Intent(context, TripDetailsActivity.class);
                intent.putExtra("tripDetails", list.get(position));
                context.startActivity(intent);
            }
        });
        holder.reTrip.setOnClickListener(view -> {
            Intent intent = new Intent(context, AddTripActivity.class);
            intent.putExtra("tripData",list.get(position));
            intent.putExtra("retrip",true);
            ((AppCompatActivity)context).startActivityForResult(intent,ADD_NEW_TRIP_CODE);
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tripName;
        //        TextView startPoint;
//        TextView endPoint;
        TextView tripStatus;
        TextView tripType;
        ImageView delete;
        ImageView mapImageView;
        TextView distance;
        TextView duration;
        ConstraintLayout itemLayout;
        ImageView reTrip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tripName = itemView.findViewById(R.id.tripName);
            //  startPoint = itemView.findViewById(R.id.startPoint);
            //   endPoint = itemView.findViewById(R.id.endPoint);
            distance = itemView.findViewById(R.id.distance);
            duration = itemView.findViewById(R.id.duration);
            delete = itemView.findViewById(R.id.delete);
            tripStatus=itemView.findViewById(R.id.tripStatus);
            tripType=itemView.findViewById(R.id.tripType);
            mapImageView = itemView.findViewById(R.id.mapImage);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            reTrip = itemView.findViewById(R.id.reTrip);

        }
    }

}

