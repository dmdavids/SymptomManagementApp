package com.skywomantech.app.symptommanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.data.UserCredential;

public class LoginUtility {

    private static final String LOG_TAG = LoginUtility.class.getSimpleName();

    static String mUserName;
    static String mLoginId;
    static UserCredential.UserRole mRole;
    static UserCredential mCredential = null;

    // everything EXCEPT for the service is checked here
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
        return true;
    }

    // Everything except for the service is checked here
    public static boolean isLoggedIn(Context context) {
        mUserName   = getUsername(context);
        mLoginId    = getLoginId(context);
        mRole       = UserCredential.UserRole.findByValue(LoginUtility.getUserRoleValue(context));
        if (mCredential == null  || mUserName.isEmpty()
           || mLoginId.isEmpty() || mRole == UserCredential.UserRole.NOT_ASSIGNED) {
            return false;
        }
        return true;
    }

    public static synchronized void logout(Context context ) {
        // reset all the values that make the app think we are logged in
        mUserName = "";
        mLoginId = "";
        mRole =  UserCredential.UserRole.NOT_ASSIGNED;
        mCredential = null;
        setUsername(context, mUserName);
        setLoginId(context, mLoginId);
        setUserRoleValue(context, mRole.getValue());
        // Go ahead and reset the service so that it has to reconnect next time its called
        SymptomManagementService.reset();
    }

    private static synchronized boolean setCheckin(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isCheckin", value);
        editor.apply();
        return isCheckin(context);
    }

    public static boolean isCheckin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("isCheckin", false);
    }

    public static synchronized String setLoginId(Context context, String value) {
        if (value == null) value = "";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("login_id", value);
        editor.apply();
        return getLoginId(context);
    }

    public static String getLoginId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("login_id", "");
    }

    public static synchronized String setUsername(Context context, String value) {
        if (value == null) value = "";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", value.toLowerCase());
        editor.apply();
        return getUsername(context);
    }

    public static String getUsername(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("username", "").toLowerCase();
    }

    public static synchronized int setUserRoleValue(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("role_value", value);
        editor.apply();
        return getUserRoleValue(context);
    }

    public static int getUserRoleValue(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("role_value", UserCredential.UserRole.NOT_ASSIGNED.getValue());
    }

    public static UserCredential.UserRole getUserRole(Context context) {
        return UserCredential.UserRole.findByValue(getUserRoleValue(context));
    }

    public static synchronized boolean setRememberMe(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("remember_me", value);
        editor.apply();
        return getRememberMe(context);
    }

    public static boolean getRememberMe(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("remember_me", false);
    }

    public static synchronized long setLastDeviceLogin(Context context, long value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_device_login", value);
        editor.apply();
        return getLastDeviceLogin(context);
    }

    public static long getLastDeviceLogin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong("last_device_login", 0L);
    }

}
