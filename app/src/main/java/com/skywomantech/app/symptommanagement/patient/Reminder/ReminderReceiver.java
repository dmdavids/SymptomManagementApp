package com.skywomantech.app.symptommanagement.patient.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {
    private final static String LOG_TAG = ReminderReceiver.class.getSimpleName();
    public ReminderReceiver() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service1 = new Intent(context, ReminderService.class);
        Calendar cal = Calendar.getInstance();
        Log.d(LOG_TAG, "The REMINDER RECEIVER IS STARTING SERVICE AT " + cal.toString());
        context.startService(service1);
    }
}
