package com.skywomantech.app.symptommanagement.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SymptomManagementSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static SymptomManagementSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SymptomManagementSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
