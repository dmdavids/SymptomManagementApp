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

/**
 * When a Reminder Alarm is triggered this Service sets up the Patient app for
 * processing an official Check-In.  And it lets the user know that it is time for check-in.
 *
 * It sends a notification with a pending intent that will start the Login Activity
 * but first it sets the check-in flag to TRUE so the Login Activity will run Check-In
 * screen flow.
 *
 * So if the user is already in the app and tries to do anything it will be sent to the
 * check-in screen flow.  If the user clicks on the notification it will go to the app
 * and start checkin.  If the user does the check-in first and then clicks on the notification
 * it will NOT take them to checkin again.  It is smart enough to know that it was completed.
 *
 */
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

        // let the app know that it is time for a check-in so it will
        // go directly into the checkin flow for the patient
        // The check-in screen flow first shows the PainLog fragment and then the
        // Medication Log fragment.
        Log.d(LOG_TAG, "Creating Check-In Notification. Setting Check-In to true.");
        // setting check-in to true also creates a unique check-in that all logs
        // created during the check-in flow will use to associate their data with
        // this check-in
        LoginUtility.setCheckin(getApplicationContext(),true);

        // create the actual notification to start Symptom Management App
        // and tell the user that it is time to check-in
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("SymptomManagement")
                        .setContentText("Time to Check-In")
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true);

        // Open the app when the user clicks on the notification.
        // We have it go to the Login Activity because it is in charge of
        // redirecting users to the correct screen flow.
        Intent resultIntent = new Intent(getApplicationContext(), LoginActivity.class);

        // setting FLAG_ACTIVITY_CLEAR_TOP so if the Login Activity is already running
        // in the current task, then instead of launching a new instance of that activity,
        // all of the other activities on top of it will be closed and this Intent will be
        // delivered to the (now on top) old activity as a new Intent.
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder stackBuilder =
                TaskStackBuilder.create(getApplicationContext())
                                .addParentStack(LoginActivity.class)
                                .addNextIntent(resultIntent);


        // we are ok with reusing the intent again.
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );
        mBuilder.setContentIntent(resultPendingIntent);

        ((NotificationManager)getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(SYMPTOM_MANAGEMENT_NOTIFICATION_ID, mBuilder.build());

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
