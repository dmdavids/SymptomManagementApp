package com.skywomantech.app.symptommanagement.data;


import android.util.Log;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public class DataUtility {
    private final static String LOG_TAG = DataUtility.class.getSimpleName();

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

    public static class ReminderSorter implements Comparator<Reminder> {
        public synchronized int compare(Reminder x, Reminder y) {
            return Long.compare(x.getHour() * 60 + x.getMinutes(),
                    y.getHour() * 60 + y.getMinutes());
        }
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
}
