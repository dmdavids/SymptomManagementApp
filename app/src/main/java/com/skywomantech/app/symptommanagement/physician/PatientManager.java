package com.skywomantech.app.symptommanagement.physician;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;

import java.util.concurrent.Callable;

public class PatientManager {

    public interface Callbacks {
        public void setPatient(Patient patient);
    }

    private static final String LOG_TAG = PatientManager.class.getSimpleName();

    /**
     * Go to the server and get the patient record with this id then
     * use the callback to send the information back to the caller
     *
     * @param activity
     * @param patientId
     */
    public static synchronized void getPatient(final Activity activity, final String patientId) {
        if (patientId == null) {
            Log.e(LOG_TAG, "NO PATIENT identified.. unable to get data from cloud.");
            return;
        }
        Log.d(LOG_TAG, "getting Patient ID : " + patientId);
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (SymptomManagementService.getService() != null) {
            CallableTask.invoke(new Callable<Patient>() {
                @Override
                public Patient call() throws Exception {
                    return svc.getPatient(patientId);
                }
            }, new TaskCallback<Patient>() {
                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "Found Patient :" + result.toString());
                    ((Callbacks) activity).setPatient(result);
                }
                @Override
                public void error(Exception e) {
                    Toast.makeText(activity,
                            "Unable to fetch the Patient data. " +
                                    "Please check Internet connection and try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
