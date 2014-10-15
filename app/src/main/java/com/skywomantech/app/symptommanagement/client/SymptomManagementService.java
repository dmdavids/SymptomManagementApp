package com.skywomantech.app.symptommanagement.client;


import android.content.Context;

import retrofit.RestAdapter;

public class SymptomManagementService {

    private static SymptomManagementApi symptomManagementSvc;

    public static synchronized SymptomManagementApi getService(String server) {
        if (symptomManagementSvc != null) {
            return symptomManagementSvc;
        }
        else {
            return init(server);
        }
    }

    public static synchronized  SymptomManagementApi init(String server) {

        symptomManagementSvc = new RestAdapter.Builder()
                .setEndpoint(server)
                .build()
                .create(SymptomManagementApi.class);

        return symptomManagementSvc;
    }

}
