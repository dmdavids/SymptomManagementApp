package com.skywomantech.app.symptommanagement.patient.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    public ReminderReceiver() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service1 = new Intent(context, ReminderService.class);
        context.startService(service1);
    }
}
