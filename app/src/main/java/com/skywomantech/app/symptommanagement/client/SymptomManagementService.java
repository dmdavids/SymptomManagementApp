package com.skywomantech.app.symptommanagement.client;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.client.oauth.SecuredRestBuilder;
import com.skywomantech.app.symptommanagement.client.oauth.unsafe.EasyHttpClient;


import retrofit.RestAdapter;
import retrofit.client.ApacheClient;

public class SymptomManagementService {
    private static final String LOG_TAG = SymptomManagementService.class.getSimpleName();

    private static SymptomManagementApi symptomManagementSvc;

    public static final String CLIENT_ID = "mobile";
    public final static String SERVER_ADDRESS = "https://192.168.0.34:8443";

    private static String mUser = "";
    private static String mPassword = "";

    // Use this one when you are already logged in and you just need your server connection
    // and if it isn't available you want to go back to login activity
    public static synchronized SymptomManagementApi getServiceOrShowLogin(Context context) {
        if (symptomManagementSvc != null) {
            Log.d(LOG_TAG, "We do have a service... no need to Login. Yeah!");
            return symptomManagementSvc;
        } else {
            Log.d(LOG_TAG, "We do not have a service so we need to LOGIN!");
            LoginActivity.restartLoginActivity(context);
            return null;
        }
    }

    // use this one when you are in LoginActivity class and you want a NEW server connection
    public static synchronized SymptomManagementApi getService(String username, String password) {
        if (username == null || username.isEmpty() || password == null ) {
            Log.e(LOG_TAG, "INVALID username or password. Unable to login.");
            symptomManagementSvc = null;
            return null;
        }
        mUser = username;
        mPassword = password;
        Log.d(LOG_TAG, "Attempting to INIT the service with a new username and password.");
        return init(SERVER_ADDRESS, username, password);
    }

    public static synchronized SymptomManagementApi getService(String server) {
        if (symptomManagementSvc != null) {
            Log.d(LOG_TAG, "GETTING the service.");
            return symptomManagementSvc;
        }
        else {
            Log.d(LOG_TAG, "Attempting to INIT the service.");
            return init(server, mUser, mPassword);
        }
    }

    public static synchronized SymptomManagementApi getService() {

        if (symptomManagementSvc != null) {
            Log.d(LOG_TAG, "GETTING the service.");
            return symptomManagementSvc;
        }
        else {
            Log.d(LOG_TAG, "Attempting to INIT the service with previously set username and password.");
            return init(SERVER_ADDRESS, mUser, mPassword);
        }
    }

    // This is the one that does the actual login at the server
    public static synchronized  SymptomManagementApi init(String server, String user, String pass) {
    Log.d(LOG_TAG, "Getting service Server : " + server + " mUser : " + user + " mPassword : " + pass);
        symptomManagementSvc = new SecuredRestBuilder()
                .setLoginEndpoint(server + SymptomManagementApi.TOKEN_PATH)
                .setUsername(user)
                .setPassword(pass)
                .setClientId(CLIENT_ID)
                .setClient(
                        new ApacheClient(new EasyHttpClient()))
                .setEndpoint(server).setLogLevel(RestAdapter.LogLevel.FULL).build()
                .create(SymptomManagementApi.class);

        if (symptomManagementSvc == null) {
            Log.d(LOG_TAG, "Server login FAILED!!!");
        }
        return symptomManagementSvc;
    }

    public static synchronized void reset() {
        Log.d(LOG_TAG, "RESETTING the service and username and password.");
        mPassword = "";
        mUser = "";
        symptomManagementSvc = null;
    }

}
