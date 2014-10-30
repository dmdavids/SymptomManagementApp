package com.skywomantech.app.symptommanagement.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.skywomantech.app.symptommanagement.client.SymptomManagementService;

/**
 * The service which allows the sync adapter framework to access the authenticator.
 */
public class SymptomManagementAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private SymptomManagementAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new SymptomManagementAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
