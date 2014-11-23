package com.skywomantech.app.symptommanagement.patient.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Simple Receiver to accept the Alarm Trigger and start up the Reminder Service
 * The Reminder Service will start the check-in process for the app and notify the patient that
 * its time to check-in.
 */
public class ReminderReceiver extends BroadcastReceiver {
    public ReminderReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentReminderService = new Intent(context, ReminderService.class);
        context.startService(intentReminderService);
    }
}
