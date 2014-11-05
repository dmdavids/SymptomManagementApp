package com.skywomantech.app.symptommanagement.client;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.client.oauth.SecuredRestBuilder;
import com.skywomantech.app.symptommanagement.client.oauth.unsafe.EasyHttpClient;


import retrofit.RestAdapter;
import retrofit.client.ApacheClient;

public class SymptomManagementService {
    private static final String LOG_TAG = SymptomManagementService.class.getSimpleName();

    private static SymptomManagementApi symptomManagementSvc;

    public static final String CLIENT_ID = "mobile";
    public final static String SERVER_ADDRESS = "https://192.168.0.34:8443";

    // make them go to login screen if these values are empty!
    private static String mUser = "admin";
    private static String mPassword = "pass";

    // Use this one when you aren't already in the LoginActivity class
    public static synchronized SymptomManagementApi getServiceOrShowLogin(Context ctx) {
        if (symptomManagementSvc != null) {
            return symptomManagementSvc;
        } else {
            ctx.startActivity(new Intent(ctx, LoginActivity.class));
            return null;
        }
    }


    // use this one when you are already in the LoginActivity class
    public static synchronized SymptomManagementApi getService(String username, String password) {
        if (symptomManagementSvc != null) {
            return symptomManagementSvc;
        }
        else {
            return init(SERVER_ADDRESS, username, password);
        }
    }

    //these are testing versions TODO: remove this later

    public static synchronized SymptomManagementApi getService(String server) {
        if (symptomManagementSvc != null) {
            return symptomManagementSvc;
        }
        else {
            return init(server, mUser, mPassword);
        }
    }

    public static synchronized SymptomManagementApi getService() {
        if (symptomManagementSvc != null) {
            return symptomManagementSvc;
        }
        else {
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

}
