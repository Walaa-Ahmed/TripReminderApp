package com.elsoudany.said.tripreminderapp.FloatingWidget;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.notes.NoteAdapter;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.Note;
import com.elsoudany.said.tripreminderapp.room.TripNoteDao;

import java.util.ArrayList;
import java.util.List;

public class FloatingViewService extends Service implements View.OnClickListener {


    private static final String TAG = "MYTAG";
    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
    private View expandedView;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    List<Note> notes;
    AppDatabase db;
    Handler noteHandler;
    long tripUid;
    public FloatingViewService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        if (intent != null && intent.hasExtra("tripUid"))
        {
            Log.i(TAG, "uid from start: "+tripUid);
            //get uid from reminder service
            tripUid=intent.getLongExtra("tripUid",60);

            //get notes from room
            new Thread() {
                @Override
                public void run() {
                    db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "DataBase-name").build();

                    TripNoteDao tripNoteDao = db.tripNoteDao();
                    notes.addAll(tripNoteDao.getAllNotes(tripUid).get(0).noteList);
                    noteHandler.sendEmptyMessage(1);
                }
            }.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize notes list
        notes=new ArrayList<>();
        Log.i(TAG, "notes from: "+notes);

        //getting the widget layout from xml using layout inflater

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);
        Log.i(TAG, "uidddd: "+tripUid);
        Log.i(TAG, "notes: "+notes);

        //getting the collapsed and expanded view from the floating view
        collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);
        expandedView = mFloatingView.findViewById(R.id.layoutExpanded);
        recyclerView=  expandedView.findViewById(R.id.floatingWidgetRecyclerView);
        adapter=new NoteAdapter(FloatingViewService.this,notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(FloatingViewService.this));
        recyclerView.setAdapter(adapter);
        //adding click listener to close button and expanded view
        mFloatingView.findViewById(R.id.buttonClose).setOnClickListener(this);
        expandedView.setOnClickListener(this);
        //check when switching between view s
        collapsedView.setOnClickListener(v -> {
              if(collapsedView.getVisibility()== View.VISIBLE && expandedView.getVisibility()== View.VISIBLE)
              {
                  collapsedView.setVisibility(View.VISIBLE);
                  expandedView.setVisibility(View.GONE);
              }
              else {
                  switch (v.getId()) {
                      case R.id.layoutCollapsed:
                          Log.i(TAG, "collapsed ");
                          //switching views
                          collapsedView.setVisibility(View.VISIBLE);
                          expandedView.setVisibility(View.VISIBLE);
                          break;
                  }
              }
        });


        //adding an touchlistener to make drag movement of the floating widget
        mFloatingView.findViewById(R.id.layoutCollapsed).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        return false;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);

                        return false;
                }
                return false;
            }
        });


        //to update recyclerview with notes
        noteHandler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                adapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.layoutExpanded:
                //switching views
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                break;


            case R.id.buttonClose:
                //closing the widget
                stopSelf();
                break;
        }
    }
}
