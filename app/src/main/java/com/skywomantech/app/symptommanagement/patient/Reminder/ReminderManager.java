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

/**
 * This class manages the Check-In reminders for the patient version of the app
 */
public class ReminderManager {

    private static final String LOG_TAG = ReminderManager.class.getSimpleName();

    // Need the alarm manager to set up the system reminders to trigger at a specific
    // time every day
    static AlarmManager alarmManager = null;
    static Collection<ActivatedAlarm> activatedAlarms = null;
    static int nextAvailableResponseCode = 1;  // keep a unique response code available for new alarms

    /**
     * Get the alarm manager to use
     *
     * @param context
     * @return AlarmManager
     */
    private static AlarmManager getAlarmManager(Context context) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        return alarmManager;
    }

    /**
     * Get the Activated Alarms List
     *
     * @return set of activated alarms for this patient
     */
    private static Collection<ActivatedAlarm> getActivatedAlarms() {
        if (activatedAlarms == null)
            activatedAlarms = new HashSet<ActivatedAlarm>();
        return activatedAlarms;
    }

    /**
     * When displaying the reminders its nicer to have them sorted by time of day
     *
     * @param reminders
     * @return Set of Reminders that are sorted by time of day
     */
    public static synchronized Collection<Reminder> sortRemindersByTime(Collection<Reminder> reminders) {
        if (reminders == null || reminders.size() == 0) return null;
        TreeSet<Reminder> sorted = new TreeSet<Reminder>(new ReminderSorter());
        for (Reminder r : reminders) sorted.add(r);
        return sorted;
    }

    /**
     * Sorts the reminders by hour and minutes
     */
    public static class ReminderSorter implements Comparator<Reminder> {
        public synchronized int compare(Reminder x, Reminder y) {
            return Long.compare(x.getHour() * 60 + x.getMinutes(), y.getHour() * 60 + y.getMinutes());
        }
    }

    /**
     * Utility method to get the time X hours from the current time
     * Use negative values for past time
     *
     * @param hours
     * @return
     */
    public static long getHoursFromNow(int hours) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, hours); // negative hours are in the past
        return cal.getTimeInMillis();
    }

    /**
     * Utility method to find the millisecond value for midnight of the current day
     *
     * @return Milliseconds for the midnight today
     */
    public static long getStartOfToday() {
        Calendar cal = Calendar.getInstance();  // get current time
        cal.add(Calendar.HOUR, -1 * cal.get(Calendar.HOUR_OF_DAY)); // subtract hours
        cal.add(Calendar.MINUTE, -1 * cal.get(Calendar.MINUTE)); // subtract minutes
        return cal.getTimeInMillis();  // this should be the start of this day
    }


    /**
     * This method gets a list of Reminders for this patient and
     * if they are activated then starts them
     * Used by Login to start alarms for a new logged in patient
     * <p/>
     * The design is set up so that if there are any reminders on this device (stored
     * by the ContentProvider) then we default to them.  But if there are no reminders
     * on this device AND there are reminders in the patient object that came from the cloud then
     * we load them but we don't activate them.
     *
     * @param context
     * @param id
     */
    public static void startPatientReminders(Context context, String id) {
        // get reminders from persisted storage
        Collection<Reminder> reminders = PatientDataManager.loadReminderList(context, id);

        if (reminders == null || reminders.size() <= 0) return;  // no reminders found
        Log.d(LOG_TAG, "There are " + reminders.size() + " reminders for id: " + id);
        Reminder[] reminderArray = reminders.toArray(new Reminder[reminders.size()]);
        for (Reminder r : reminderArray) {
            Log.d(LOG_TAG, "Checking reminder " + r.getName()
                    + " it is " + (r.isOn() ? "ON" : "OFF"));
            if (r.isOn()) setSingleReminderAlarm(context, r);
        }
    }

    /**
     * This method greats an activated alarm object for the reminder and then creates the
     * actual alarm with the System
     * <p/>
     * This method assumes that all verification has already occurred to make sure this is
     * not a duplicate alarm.
     *
     * @param context
     * @param r       Reminder to use to cancel or activate an alarm
     */
    public static void setSingleReminderAlarm(Context context, Reminder r) {
        Log.d(LOG_TAG, "Attempting to set Alarm for " + r.getName()
                + " is " + (r.isOn() ? "ON" : "OFF."));

        // if the reminder is turned off then cancel its alarm from the system
        if (!r.isOn()) {
            cancelSingleReminderAlarm(context, r);
            return;
        }

        // otherwise the reminder is turned on so create a new Activate alarm to
        // store the alarm information and match it to the reminder
        // keeps a static counter to use to make a unique response code
        ActivatedAlarm activatedAlarm = new ActivatedAlarm(r.getCreated(), nextAvailableResponseCode++);
        getActivatedAlarms().add(activatedAlarm);

        // Create the Pending Intent that the Alarm Manager needs and use the response code
        // that was generated by the Activated Alarm
        PendingIntent alarmIntent =
                createReminderPendingIntent(context, activatedAlarm.getResponseCode());

        Log.d(LOG_TAG, "Creating a new Alarm with Interval DAY for reminder " + r.getName());
        getAlarmManager(context)
                .setRepeating(AlarmManager.RTC_WAKEUP, // use real time clock and wake up
                        getAlarmTime(r.getHour(), r.getMinutes()),
                        AlarmManager.INTERVAL_DAY,
                        alarmIntent);
    }

    /**
     * Utility method to get the next time to throw this alarm
     * If the hour and minute is in the past then this alarm will adjust
     * it for the next day or the alarm triggers immediately and
     * that is not a desired option.
     *
     * @param hour time of day in hours to trigger alarm
     * @param minutes time of day in minutes to trigger alarm
     * @return next alarm time in milliseconds
     */
    public static final long MILLISECONDS_IN_A_DAY = 86400000;  // pre-calculated

    private static long getAlarmTime(int hour, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.setTimeInMillis(System.currentTimeMillis());
        // the alarm time was in the past so set it to the next day
        if (calendar.getTimeInMillis() <= testCalendar.getTimeInMillis()) {
            long nextDay = calendar.getTimeInMillis() + MILLISECONDS_IN_A_DAY;
            calendar.setTimeInMillis(nextDay);
        }
        return calendar.getTimeInMillis();
    }

    /**
     * This cancels all the active alarms .. use for logging out
     * or for resetting the alarms, this just uses the list we keep
     * so we don't need to reload the reminders.
     *
     * @param context
     */
    public static void cancelPatientReminders(Context context) {
        ActivatedAlarm[] alarmArray = getActivatedAlarms().toArray(new ActivatedAlarm[getActivatedAlarms().size()]);
        for (ActivatedAlarm a : alarmArray) {
            // create a pending intent with the same response code
            if (isAlarmActivated(context, a)) {
                PendingIntent alarmIntent = createReminderPendingIntent(context, a.getResponseCode());
                cancelSingleReminderAlarm(alarmIntent);
                if (!isAlarmActivated(context, a)) {
                    Log.d(LOG_TAG, "The alarm was successfully deactivated.");
                    getActivatedAlarms().remove(a);
                    return;
                }
            }
        }
        Log.d(LOG_TAG, "The alarm was not deactivated. It may have not have been activated or existed.");
    }

    /**
     * This method cancels an alarm with the Alarm manager
     *
     * @param alarmIntent
     */
    public static void cancelSingleReminderAlarm(PendingIntent alarmIntent) {
        if (alarmManager != null) {
            Log.d(LOG_TAG, "Actually Canceling the alarm!");
            alarmManager.cancel(alarmIntent);
        }
    }

    /**
     * Check list of activated alarms for one that matches the Reminder
     * if found then cancel that single alarm and verify that it was cancelled
     *
     * @param context
     * @param reminder Reminder for the alarm to be cancelled
     */
    public static synchronized void cancelSingleReminderAlarm(Context context, Reminder reminder) {
        if (getActivatedAlarms() == null || getActivatedAlarms().size() <= 0) return;

        ActivatedAlarm[] alarmArray =
                getActivatedAlarms().toArray(new ActivatedAlarm[getActivatedAlarms().size()]);

        for (ActivatedAlarm s : alarmArray) {
            if (s.matches(reminder)) {
                Log.d(LOG_TAG, "Found Activated Alarm for the Reminder " + reminder.getName() + " - Canceling");
                cancelSingleReminderAlarm(createReminderPendingIntent(context, s.getResponseCode()));
                getActivatedAlarms().remove(s);
            }
            Log.d(LOG_TAG, "Tried to cancel this reminder but did not find it. " +
                    "Not necessarily an error ... may just be canceling before restarting on edit."
                    + reminder.toString());
        }
    }

    /**
     * Creates a pending intent for the reminders with the given response code
     * The response code is what keeps the alarms unique since they are all starting the
     * same Activity.
     *
     * @param context
     * @param responseCode unique identifier for the pending intent
     * @return Pending intent created with the given arguments
     */
    public static synchronized PendingIntent createReminderPendingIntent(Context context, int responseCode) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        return PendingIntent.getBroadcast(context, responseCode, intent, 0);
    }

    /**
     * Checks to see if an alarm is already in the list that matches the reminder information
     * the unique id for the reminder is the created timestamp
     *
     * @param context
     * @param reminder to check
     * @return true if already activated or false if not activated
     */
    public static boolean isAlarmActivated(Context context, Reminder reminder) {
        if (getActivatedAlarms() != null && getActivatedAlarms().size() > 0) {
            for (ActivatedAlarm a : getActivatedAlarms()) {
                if (a.matches(reminder)) { // compares created timestamps
                    return isAlarmActivated(context, a.getResponseCode());
                }
            }
        }
        return false;
    }

    /**
     * Checks to see if an alarm is already in the list that matches the response code.
     * The response code is a unique identifier for the pending intent that was created.
     *
     * @param context
     * @param activatedAlarm
     * @return true if activated false if not activated
     */
    public static boolean isAlarmActivated(Context context, ActivatedAlarm activatedAlarm) {
        return isAlarmActivated(context, activatedAlarm.getResponseCode());
    }

    /**
     * Tries to get an existing Pending Intent (does not create if it does not already exist)
     * If the Pending Intent was created then it means that it exists
     *
     * @param context
     * @param requestCode unique id for the pending intent
     * @return true if pending intent was found meaning that the alarm is already in the system
     */
    public static boolean isAlarmActivated(Context context, int requestCode) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent alarmIntent =
                PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE);
        return alarmIntent != null;
    }

    /**
     * Class created to hold information on alarms that have been created in the system
     * to help track them.  Especially it keeps track of the response code
     * connected with the reminder.
     */
    public static class ActivatedAlarm {
        private long reminderCreated;
        private int responseCode;

        public ActivatedAlarm(long reminderCreated, int responseCode) {
            this.reminderCreated = reminderCreated;
            this.responseCode = responseCode;
        }

        public long getReminderCreated() {
            return reminderCreated;
        }

        public void setReminderCreated(long reminderCreated) {
            this.reminderCreated = reminderCreated;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ActivatedAlarm)) return false;

            ActivatedAlarm that = (ActivatedAlarm) o;

            if (reminderCreated != that.reminderCreated) return false;
            if (responseCode != that.responseCode) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (reminderCreated ^ (reminderCreated >>> 32));
            result = 31 * result + responseCode;
            return result;
        }

        /**
         * Compares the reminders created timestamp to its reminderCreated timestamp
         *
         * @param reminder
         * @return true if the timestamps are the same false if they are not
         */
        public boolean matches(Reminder reminder) {
            Log.d(LOG_TAG, "Does " + Long.toString(reminderCreated) + " = "
                    + Long.toString(reminder.getCreated()) + " "
                    + Boolean.toString(reminderCreated == reminder.getCreated()));
            return reminderCreated == reminder.getCreated();
        }
    }

    /**
     * Utility to print out the list of reminders in the database and the list of activated
     * alarms that are currently being tracked.
     *
     * @param context
     * @param id
     * @return String to use for debug printing with alarm information
     */
    public static String printAlarms(Context context, String id) {
        if (id == null || id.isEmpty()) return "Invalid id unable to print alarms";
        if (getActivatedAlarms().size() <= 0) return "No Alarms Set ";

        Collection<Reminder> reminders = PatientDataManager.loadReminderList(context, id);
        if (reminders == null || reminders.size() <= 0) return "No reminders to print.";

        Reminder[] reminderArray = reminders.toArray(new Reminder[reminders.size()]);
        Log.d(LOG_TAG, "There are " + reminders.size() + " reminders in the database." +
                " There are " + getActivatedAlarms().size() + " alarms activated.");
        int count = 0;
        String answer = "The ALARMS Activated are : ";
        Log.d(LOG_TAG, "There are " + getActivatedAlarms().size() + " alarms set at this time.");
        for (ActivatedAlarm s : getActivatedAlarms()) {
            String info = "Activated Alarm " + count++;
            for (Reminder r : reminderArray) {
                if (s.matches(r)) {
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
