package com.skywomantech.app.symptommanagement.patient.Reminder;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.data.Reminder;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

public class ReminderManager {

    private static final String LOG_TAG = ReminderManager.class.getSimpleName();

    static AlarmManager alarmManager = null;

    public static class IntentSaver {
        PendingIntent intent;
        long reminderCreated;  // this is how we id the reminders since name and times are changeable

        public IntentSaver() {
        }
        private IntentSaver(PendingIntent intent, long reminderCreated) {
            this.intent = intent;
            this.reminderCreated = reminderCreated;
        }
        public PendingIntent getIntent() {
            return intent;
        }
        public void setIntent(PendingIntent intent) {
            this.intent = intent;
        }
        public long getReminderCreated() {
            return reminderCreated;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IntentSaver that = (IntentSaver) o;

            if (reminderCreated != that.reminderCreated) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (reminderCreated ^ (reminderCreated >>> 32));
        }
    }
    static Collection<IntentSaver> savedIntents;


    private static AlarmManager getAlarmManager(Context context) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        return alarmManager;
    }

    public static void startPatientReminders(Context context, String id) {
        // currently the assumption is that we can never override
        // the local reminders with anything from the cloud
        // so that means we don't have to wait for cloud updates
        Collection<Reminder> reminders = PatientDataManager.loadReminderList(context, id);
        if (reminders != null) {
            Log.d(LOG_TAG, "There are " + reminders.size() + " reminders for id: " + id);
            for (Reminder r : reminders) {
                Log.d(LOG_TAG, "Checking reminder " + r.getName() +
                        "The reminder is " + (r.isOn() ? "ON" : "Not Activated."));
                if (r.isOn()) {
                    setSingleReminderAlarm(context, r);
                }
            }
        }
    }

    public static void setSingleReminderAlarm(Context context, Reminder r) {
        Log.d(LOG_TAG, "Attempting to set Alarm for " + r.getName() +
                "The reminder is " + (r.isOn() ? "ON" : "Not Activated."));
        if (!r.isOn()) return;  // alarm is not activated so do nothing
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        IntentSaver saver = new IntentSaver(alarmIntent, r.getCreated());
        if (getSavedIntents().contains(saver)) {
            Log.d(LOG_TAG, "Already have an alarm for this reminder. Not setting it.");
            return;
        }
        Log.d(LOG_TAG, "Finally Creating a new Alarm for " + r.getName());
        getAlarmManager(context)
                .setRepeating(AlarmManager.RTC_WAKEUP,
                              getAlarmTime(r.getHour(), r.getMinutes()),
                              AlarmManager.INTERVAL_DAY,
                              alarmIntent);
        getSavedIntents().add(saver);
    }

    private static long getAlarmTime(int hour, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        return calendar.getTimeInMillis();
    }

    private static Collection<IntentSaver> getSavedIntents() {
        if (savedIntents == null) {
            savedIntents = new HashSet<IntentSaver>();
        }
        return savedIntents;
    }

    public static void cancelPatientReminders(Context context, String id) {
        Collection<Reminder> reminders = PatientDataManager.loadReminderList(context, id);
        for (Reminder r : reminders) {
            for (IntentSaver s : getSavedIntents()) {
                if (s.getReminderCreated() == r.getCreated()) {
                    Log.d(LOG_TAG, "=> Found Intent for the Alarm " + r.getName() + " - Canceling");
                    cancelSingleReminderAlarm(s.getIntent());
                    getSavedIntents().remove(s);
                }
            }
        }
    }

    public static void cancelSingleReminderAlarm(PendingIntent alarmIntent) {
        // If the alarm has been set, cancel it.
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
        }
    }

    public static synchronized void cancelSingleReminderAlarm(Reminder reminder) {
        if (getSavedIntents() == null || getSavedIntents().size() <= 0) return;
        for (IntentSaver s : getSavedIntents()) {
            if (s.getReminderCreated() == reminder.getCreated()) {
                Log.d(LOG_TAG, "=> Found Intent for the Alarm " + reminder.getName() + " - Canceling");
                cancelSingleReminderAlarm(s.getIntent());
                getSavedIntents().remove(s);
            }
        }
    }

    public static String printAlarms(Context context, String id) {
        if (id == null || id.isEmpty()) return "invalid id unable to print alarms";
        Collection<Reminder> reminders = PatientDataManager.loadReminderList(context, id);
        if (getSavedIntents().size() <= 0) return "No Alarms Set ";
        int count = 0;
        String answer = "ALARMS SET ARE : ";
        Log.d(LOG_TAG, "There are " + getSavedIntents().size() + " alarms set at this time.");
        for (IntentSaver s: getSavedIntents()) {
            String info = "Saver " + count+1;
            for (Reminder r : reminders) {
                if (s.getReminderCreated() == r.getCreated()) {
                    info += " name = " + r.getName() + " \n";
                    break;
                }
            }
            answer += info;
        }
        Log.d(LOG_TAG, answer);
        return answer;
    }
}
