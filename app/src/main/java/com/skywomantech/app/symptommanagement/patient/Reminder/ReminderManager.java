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
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

public class ReminderManager {

    private static final String LOG_TAG = ReminderManager.class.getSimpleName();

    static AlarmManager alarmManager = null;

    public static synchronized Collection<Reminder> sortRemindersByTime(Collection<Reminder> reminders) {
        Log.d(LOG_TAG, "Sorting Reminders by time");
        if (reminders == null || reminders.size() == 0)
            return null;
        ReminderSorter sorter = new ReminderSorter();
        TreeSet<Reminder> sorted = new TreeSet<Reminder>(sorter);
        for (Reminder r : reminders) {
            sorted.add(r);
        }
        return sorted;
    }

    public static long getHoursFromNow(int hours) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, hours); // negative hours are in the past
        return cal.getTimeInMillis();
    }

    public static long getStartOfToday() {
        Calendar cal = Calendar.getInstance();  // get current time
        cal.add(Calendar.HOUR, -1 * cal.get(Calendar.HOUR_OF_DAY)); // subtract hours
        cal.add(Calendar.MINUTE, -1 * cal.get(Calendar.MINUTE)); // subtract minutes
        return cal.getTimeInMillis();  // this should be the start of this day
    }

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
        Log.d(LOG_TAG, "Finally Creating a new Alarm with Interval DAY for " + r.getName());
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
            IntentSaver[] saverArray = getSavedIntents().toArray(new IntentSaver[getSavedIntents().size()]);
            for (IntentSaver s : saverArray) {
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
        IntentSaver[] saverArray = getSavedIntents().toArray(new IntentSaver[getSavedIntents().size()]);
        for (IntentSaver s : saverArray) {
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

    public static class ReminderSorter implements Comparator<Reminder> {
        public synchronized int compare(Reminder x, Reminder y) {
            return Long.compare(x.getHour() * 60 + x.getMinutes(),
                    y.getHour() * 60 + y.getMinutes());
        }
    }
}
