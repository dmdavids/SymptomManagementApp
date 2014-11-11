package com.skywomantech.app.symptommanagement.patient.Reminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;

public class ReminderService extends Service {

    private static final String LOG_TAG = ReminderService.class.getSimpleName();
    private static final int SYMPTOM_MANAGEMENT_NOTIFICATION_ID = 1111;
    private NotificationManager mManager;

    public ReminderService() {
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // let the app know that it is time for a checkin so it will go directly into the checkin
        // flow for the patient
        Log.d(LOG_TAG, "NOTIFICATION IS BEING SET ...CHECKIN IS TRUE!!!");
        LoginUtility.setCheckin(getApplicationContext(),true);

        // set the notification to clear after a click
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("SymptomManagementApp")
                        .setContentText("Time to CheckIn")
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true);

        // Open the app when the user clicks on the notification.
        Intent resultIntent = new Intent(getApplicationContext(), LoginActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext())
                .addParentStack(LoginActivity.class)
                .addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT );

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(SYMPTOM_MANAGEMENT_NOTIFICATION_ID, mBuilder.build());

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
