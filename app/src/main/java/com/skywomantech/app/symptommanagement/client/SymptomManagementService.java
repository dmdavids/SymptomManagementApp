package com.skywomantech.app.symptommanagement.client;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.oauth.SecuredRestBuilder;
import com.skywomantech.app.symptommanagement.oauth.unsafe.EasyHttpClient;


import retrofit.RestAdapter;
import retrofit.client.ApacheClient;

public class SymptomManagementService {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private static SymptomManagementApi symptomManagementSvc;

    public static final String CLIENT_ID = "mobile";
    public final static String SERVER_ADDRESS = "https://192.168.0.34:8443";

    // make them go to login screen if these values are empty!
    private static String user = "admin";
    private static String pass = "pass";

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
            return init(server, user, pass);
        }
    }

    public static synchronized SymptomManagementApi getService() {
        if (symptomManagementSvc != null) {
            return symptomManagementSvc;
        }
        else {
            return init(SERVER_ADDRESS, user, pass);
        }
    }

    // This is the one that does the actual login at the server
    public static synchronized  SymptomManagementApi init(String server, String user, String pass) {

/*        symptomManagementSvc = new RestAdapter.Builder()
                .setEndpoint(server)
                .build()
                .create(SymptomManagementApi.class);*/
    Log.d(LOG_TAG, "Getting service Server : " + server + " user : " + user + " pass : " + pass);
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
