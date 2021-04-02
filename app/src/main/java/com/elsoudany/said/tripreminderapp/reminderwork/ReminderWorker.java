package com.elsoudany.said.tripreminderapp.reminderwork;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.elsoudany.said.tripreminderapp.R;

public class ReminderWorker extends Worker {
    private static final String TAG = "MYTAG";
    long uid;
    String tripName;
    Context context;
    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Data data =  workerParams.getInputData();
        uid = data.getLong("tripUid",0);
        tripName=data.getString("tripName");
        this.context = getApplicationContext();
        Log.i(TAG, "MyWorker: ");
    }

    @NonNull
    @Override
    public Result doWork() {
        Intent intent = new Intent(context,ReminderService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("tripUid",uid);
        context.startService(intent);
//        context.sendBroadcast(intent);
        return Result.success(); //true - success / false - failure
    }

}

