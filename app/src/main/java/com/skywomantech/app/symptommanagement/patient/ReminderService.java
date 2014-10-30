package com.skywomantech.app.symptommanagement.patient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.skywomantech.app.symptommanagement.Login;
import com.skywomantech.app.symptommanagement.R;

public class ReminderService extends Service {
    public ReminderService() {
    }

    private NotificationManager mManager;

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

        // build the information to put into the notification
        int iconId = R.drawable.ic_launcher;
        String title = "SymptomManagementApp";

        String news = "This is a test of the Reminder Service";
        String contentText = news;

        // set the notification to clear after a click
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setContentText(contentText)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true);

        // Open the app when the user clicks on the notification.
        Intent resultIntent = new Intent(getApplicationContext(), Login.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext())
                .addParentStack(Login.class)
                .addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT );

        mBuilder.setContentIntent(resultPendingIntent);

        // we can notify for two reasons but lets just always have one notification at a time
        NotificationManager mNotificationManager =
                (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1111, mBuilder.build());

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
