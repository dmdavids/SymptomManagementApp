package com.skywomantech.app.symptommanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.data.UserCredential;
import com.skywomantech.app.symptommanagement.patient.Reminder.ReminderManager;

/**
 * These are a group of utility and helper methods for the Login and Check In processing
 *
 */
public class LoginUtility {
    private static final String LOG_TAG = LoginUtility.class.getSimpleName();

    static String mUserName;
    static String mLoginId;
    static UserCredential.UserRole mRole;
    static UserCredential mCredential = null;

    /**
     * Everything except for the service is checked here
     *
     * @param context
     * @param credential
     * @return
     */
    public static synchronized boolean
    setLoggedIn(Context context, UserCredential credential) {
        Log.d(LOG_TAG, "Setting Logged In Values for credential:" + credential.toString());
        if (credential == null
                || credential.getUserName() == null || credential.getUserName().isEmpty()
                || credential.getUserType() == null
                || credential.getUserType() == UserCredential.UserRole.NOT_ASSIGNED
                || (credential.getUserType() != UserCredential.UserRole.ADMIN &&
                (credential.getUserId() == null || credential.getUserId().isEmpty()))) {
            Log.e(LOG_TAG, "Invalid Credentials so they are not getting set!!!");
            return false;
        }
        mCredential = credential;
        mUserName = setUsername(context, credential.getUserName());
        mLoginId = setLoginId(context, credential.getUserId());
        int roleValue = setUserRoleValue(context, credential.getUserRoleValue());
        Log.d(LOG_TAG, "User Role Value saved is " + Integer.toString(roleValue));
        mRole = getUserRole(context);
        Log.d(LOG_TAG, "User Role is: " + mRole);
        if (mRole == UserCredential.UserRole.PATIENT) {
            Log.d(LOG_TAG, "Starting Patient Reminders.");
            ReminderManager.startPatientReminders(context, mLoginId);
        }
        return true;
    }

    /**
     * Everything except for the service is checked here
     *
     * @param context
     * @return
     */
    public static boolean isLoggedIn(Context context) {
        mUserName = getUsername(context);
        mLoginId = getLoginId(context);
        mRole = UserCredential.UserRole.findByValue(LoginUtility.getUserRoleValue(context));
        if (mCredential == null || mUserName.isEmpty()
                || mLoginId.isEmpty() || mRole == UserCredential.UserRole.NOT_ASSIGNED) {
            return false;
        }
        return true;
    }

    /**
     * set everything that is needed to reset the app and logout
     *
     * @param context
     */
    public static synchronized void logout(Context context) {
        // reset all the values that make the app think we are logged in
        if (mRole == UserCredential.UserRole.PATIENT) {
            Log.d(LOG_TAG, "Cancelling Patient Reminders.");
            ReminderManager.cancelPatientReminders(context);
        }
        mUserName = "";
        mLoginId = "";
        mRole = UserCredential.UserRole.NOT_ASSIGNED;
        mCredential = null;
        setUsername(context, mUserName);
        setLoginId(context, mLoginId);
        setUserRoleValue(context, mRole.getValue());
        // Go ahead and reset the service so that it has to reconnect next time its called
        SymptomManagementService.reset();
    }

    /**
     * Store the patient's credentials so they can log in without internet
     *
     * @param context
     * @param credential
     */
    public static synchronized void savePatientCredential(Context context, UserCredential credential) {
        if (mRole == UserCredential.UserRole.PATIENT) {
            Log.d(LOG_TAG, "Saving the PATIENT credentials to CP.");
            PatientDataManager.addCredentialToCP(context, credential);
        } else {
            Log.d(LOG_TAG, "Not saving these credentials because its not a PATIENT.");
        }
    }

    /**
     * This is where the Check-in event processing begins and ends
     *
     * @param context
     * @param value
     * @return
     */
    public static synchronized boolean setCheckin(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isCheckin", value);
        editor.apply();
        // time for a reminder-prompted checkin so set the checkin id ..
        // pain and med log use this id
        // if it is not a checkin then reset the checkin id to
        // show that pain or med log is
        // not associated with a checkin process
        setCheckInLogId(context, (value) ? System.currentTimeMillis() : 0L);
        return isCheckin(context);
    }

    /**
     * c
     * @param context
     * @return
     */
    public static boolean isCheckin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("isCheckin", false);
    }

    /**
     *
     * @param context
     * @param value
     * @return
     */
    public static synchronized boolean setCheckInLogId(Context context, long value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("checkin_log_id", value);
        editor.apply();
        return isCheckin(context);
    }

    /**
     * called by status and med logs to see if they need to associate with a checkin log
     * @param context
     * @return
     */
    public static long getCheckInLogId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong("checkin_log_id", 0L);
    }

    /**
     *
     * @param context
     * @param value
     * @return
     */
    public static synchronized String setLoginId(Context context, String value) {
        if (value == null) value = "";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("login_id", value);
        editor.apply();
        return getLoginId(context);
    }

    /**
     *
     * @param context
     * @return
     */
    public static String getLoginId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("login_id", "");
    }

    /**
     *
     * @param context
     * @param value
     * @return
     */
    public static synchronized String setUsername(Context context, String value) {
        if (value == null) value = "";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", value.toLowerCase());
        editor.apply();
        return getUsername(context);
    }

    /**
     *
     * @param context
     * @return
     */
    public static String getUsername(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("username", "").toLowerCase();
    }

    /**
     *
     * @param context
     * @param value
     * @return
     */
    public static synchronized int setUserRoleValue(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("role_value", value);
        editor.apply();
        return getUserRoleValue(context);
    }

    /**
     *
     * @param context
     * @return
     */
    public static int getUserRoleValue(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("role_value", UserCredential.UserRole.NOT_ASSIGNED.getValue());
    }

    /**
     * helper method for converting the type to a value.. stuff need because
     * json wasn't liking the enums
     *
     * @param context
     * @return
     */
    public static UserCredential.UserRole getUserRole(Context context) {
        return UserCredential.UserRole.findByValue(getUserRoleValue(context));
    }

    /**
     *
     * @param context
     * @param value
     * @return
     */
    public static synchronized boolean setRememberMe(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("remember_me", value);
        editor.apply();
        return getRememberMe(context);
    }

    /**
     *  for future enhancements
     * @param context
     * @return
     */
    public static boolean getRememberMe(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("remember_me", false);
    }

    /**
     *  for future enhancements
     *
     * @param context
     * @param value
     * @return
     */
    public static synchronized long setLastDeviceLogin(Context context, long value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_device_login", value);
        editor.apply();
        return getLastDeviceLogin(context);
    }

    /**
     * for future enhancements
     *
     * @param context
     * @return
     */
    public static long getLastDeviceLogin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong("last_device_login", 0L);
    }

}
