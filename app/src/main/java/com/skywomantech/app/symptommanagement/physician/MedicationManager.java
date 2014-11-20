package com.skywomantech.app.symptommanagement.physician;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Medication;

import java.util.Collection;
import java.util.concurrent.Callable;

public class MedicationManager {

    private static final String LOG_TAG = PhysicianManager.class.getSimpleName();

    public interface Callbacks {
        public void setMedication(Medication medication);
        public void getAllMedications(Collection<Medication> medications);
    }


    public static synchronized void saveMedication(final Context activity, final Medication medication){
        // no name to work with so we aren't gonna do anything here
        if (medication.getName() == null || medication.getName().isEmpty()) {
            Log.e(LOG_TAG, "No medication name was given. Unable to Save Medication.");
            return;
        }

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Medication>() {

                @Override
                public Medication call() throws Exception {
                    if (medication.getId() == null || medication.getId().isEmpty()) {
                        Log.d(LOG_TAG, "adding mMedication :" + medication.toDebugString());
                        return svc.addMedication(medication);
                    } else {
                        Log.d(LOG_TAG, "updating mMedication :" + medication.toDebugString());
                        return svc.updateMedication(medication.getId(), medication);
                    }
                }
            }, new TaskCallback<Medication>() {

                @Override
                public void success(Medication result) {
                    Log.d(LOG_TAG, "Medication change was successful." + medication);
                    ((Callbacks) activity).setMedication(result);
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(activity,
                            "Unable to SAVE Medication. " +
                                    "Please check Internet connection and try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public static synchronized void getAllMedications(final Context activity) {
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<Medication>>() {

                @Override
                public Collection<Medication> call() throws Exception {
                    Log.d(LOG_TAG,"getting all medications");
                    return svc.getMedicationList();
                }
            }, new TaskCallback<Collection<Medication>>() {

                @Override
                public void success(Collection<Medication> result) {
                    Log.d(LOG_TAG,"creating list of all medications");
                    if(result != null) {
                       // ((Callbacks) activity).setMedication(result);
                    }
                }
                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            activity,
                            "Unable to fetch the Medications please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
