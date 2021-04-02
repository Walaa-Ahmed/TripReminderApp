package com.elsoudany.said.tripreminderapp.reminderwork;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.elsoudany.said.tripreminderapp.FloatingWidget.FloatingViewService;
import com.elsoudany.said.tripreminderapp.R;
import com.elsoudany.said.tripreminderapp.room.AppDatabase;
import com.elsoudany.said.tripreminderapp.room.Trip;
import com.elsoudany.said.tripreminderapp.room.TripDAO;
import com.elsoudany.said.tripreminderapp.upcomingtrips.ProcessingTripsActivity;

public class ReminderService extends Service {
    private static final String TAG ="MYTAG" ;
    DialogHandler dialogHandler;
    TextView name;
    TextView startPoint;
    TextView endPoint;
    Trip trip;
    AppDatabase db;
    TripDAO tripDAO;
    Ringtone r;
    long uid;
    NotificationManager notificationManager;
    public ReminderService() {
        dialogHandler = new DialogHandler();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null && intent.hasExtra("tripUid"))
        {
            uid = intent.getLongExtra("tripUid",60);
            new Thread() {
                @Override
                public void run() {

                    db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "DataBase-name").build();
                    tripDAO = db.tripDAO();
                    trip = tripDAO.getTrip(uid);
                    dialogHandler.sendEmptyMessage(1);


                }
            }.start();
        }

        /*--------------------------start button in notification --------------------------*/
        if (intent != null && intent.hasExtra("startButton") ) {
            SharedPreferences sharedPreferences = getSharedPreferences("checkingComingFromService",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("comingFromService",true).commit();
            String endPoint=intent.getStringExtra("endPoint");
            Log.i(TAG, "point: "+trip.endPoint);
            Log.i(TAG, "onCreate: " + "notif");
            trip.status = "started";
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Intent googleIntent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + endPoint));

                googleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(googleIntent);
//            notificationManager.cancel(1);
                Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                this.sendBroadcast(closeIntent);
                displayBubble();
                // finish();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        tripDAO.insert(trip);
                        Log.i(TAG, "trip  from start: " + trip);
                        stopSelf();
                    }

                }.start();
            }
            else if (Settings.canDrawOverlays(ReminderService.this)) {
                Intent googleIntent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + endPoint));

                googleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(googleIntent);
//            notificationManager.cancel(1);
                Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                this.sendBroadcast(closeIntent);
                displayBubble();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        tripDAO.insert(trip);
                        Log.i(TAG, "trip  from start: " + trip);
                        stopSelf();
                    }

                }.start();
            }
        }
        /*--------------------------cancel button in notification --------------------------*/
        if(intent != null && intent.hasExtra("cancelButton"))
        {
            SharedPreferences sharedPreferences = getSharedPreferences("checkingComingFromService",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("comingFromService",true).commit();
            Log.i(TAG, "cancel button ");
            trip.status = "cancelled";
//            notificationManager.cancel(1);

            new Thread(){
                @Override
                public void run() {
                    super.run();
                    tripDAO.insert(trip);
                    stopSelf();
                }
            }.start();

        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void displayBubble()
    {
        Intent floatingService= new Intent(this,FloatingViewService.class);
        floatingService.putExtra("tripUid",uid);
        Log.i(TAG, "reminder: "+uid);
        startService(floatingService);
        stopSelf();
    }
    private void displayNotification(String tripName) {

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notification", "notificationChannel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        //when press notification open processing trip activity
//        Intent notificationTripsIntent=new Intent(getApplicationContext(), ProcessingTripsActivity.class);
//        notificationTripsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingStartTripsActivity = PendingIntent.getActivity(ReminderService.this, 0, notificationTripsIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
        //start button
        Intent notificationStartIntent = new Intent(getApplicationContext(), ReminderService.class);
        notificationStartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationStartIntent.putExtra("startButton", true);
        notificationStartIntent.putExtra("endPoint", trip.endPoint);
        PendingIntent pendingStartIntent = PendingIntent.getService(ReminderService.this, 1, notificationStartIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        //cancel button
        Intent notificationCancelIntent = new Intent(getApplicationContext(), ReminderService.class);
        notificationCancelIntent.putExtra("cancelButton",true);
        notificationCancelIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingCancelIntent = PendingIntent.getService(ReminderService.this, 2, notificationCancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Log.i(TAG, "displayNotification: "+ tripName);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "notification")
                .setContentTitle(tripName + "  "+"SNOOZED")
                .setContentText(tripName)
                .setSmallIcon(R.drawable.ic_notification)
                .addAction(R.drawable.ic_notification,"START",pendingStartIntent)
                .addAction(R.drawable.ic_notification,"CANCEL",pendingCancelIntent)
                .setColor(ContextCompat.getColor(ReminderService.this, R.color.black))
                .setSmallIcon(R.drawable.ic_notification)
//                .setContentIntent(pendingStartTripsActivity)
                .setStyle( new NotificationCompat.InboxStyle())
                .setAutoCancel(true);
        startForeground(1, notification.build());
    }

    private class DialogHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            final WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
                    500, 200, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE,
                    PixelFormat.TRANSPARENT);
            Uri ringTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            r = RingtoneManager.getRingtone(getApplicationContext(), ringTone);
            r.play();
            parameters.gravity = Gravity.CENTER | Gravity.CENTER;
            parameters.x = 0;
            parameters.y = 0;
            SharedPreferences sharedPreferences = getSharedPreferences("checkingComingFromService",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("comingFromService",true).commit();
            Dialog dialog = new Dialog(ReminderService.this);
            dialog.getWindow().setAttributes(parameters);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.trip_item_dialog);
            dialog.setTitle("Trip Reminder");
            name = dialog.findViewById(R.id.trip_name);
            startPoint = dialog.findViewById(R.id.startPointField);
            endPoint = dialog.findViewById(R.id.endPointField);
            name.setText(trip.tripName);
            startPoint.setText(trip.startPoint);
            endPoint.setText(trip.endPoint);
            dialog.show();
            Log.i(TAG, "handleMessage: "+trip.endPoint);
            Button startBtn = dialog.findViewById(R.id.startBtn);
            Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
            Button snoozeBtn = dialog.findViewById(R.id.snooze);
            snoozeBtn.setOnClickListener(view -> {
                r.stop();
                displayNotification( trip.tripName);
                dialog.dismiss();
            });
            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    r.stop();
                    trip.status = "started";
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr=" + trip.endPoint));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    displayBubble();
                    dialog.dismiss();
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            tripDAO.insert(trip);
                            Log.i(TAG, "trip  from start: "+trip);
                        }

                    }.start();


                }
            });
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    r.stop();
                    trip.status = "cancelled";
                    dialog.dismiss();
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            tripDAO.insert(trip);
                        }
                    }.start();

                }
            });

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}