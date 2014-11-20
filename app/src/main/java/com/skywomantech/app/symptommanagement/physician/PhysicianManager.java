package com.skywomantech.app.symptommanagement.physician;


import android.content.Context;
import android.util.Log;

import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.data.StatusLog;

import java.util.HashSet;
import java.util.concurrent.Callable;

public class PhysicianManager {

    public interface Callbacks {
        public void setPhysician(Physician physician);
    }

    private static final String LOG_TAG = PhysicianManager.class.getSimpleName();

    /**
     * Attach a status log to the physician's patient list.  the physician's name will
     * be added to the status text.
     *
     * @param physician the physician for the patient list
     * @param patientId the patient to attach the status
     * @param statusLog the status to attach to the patient
     * @return
     */
    public static synchronized boolean
                attachPhysicianStatusLog (Physician physician, String patientId,
                                          StatusLog statusLog) {
        String s = statusLog.getNote() + " [" + physician.getName() + "] ";
        statusLog.setNote(s);
        boolean added = false;
        for (Patient p : physician.getPatients()) {
            if (p.getId().contentEquals(patientId)) {
                if (p.getStatusLog() == null) {
                    p.setStatusLog(new HashSet<StatusLog>());
                }
                p.getStatusLog().add(statusLog);
                added = true;
                break;
            }
        }
        return added;
    }

    /**
     * asks the server for the physician information and sends the result to the activity
     *
     * @param activity
     * @param id
     */
    public static synchronized void getPhysician(final Context activity, final String id) {
        if (id == null) {
            Log.e(LOG_TAG, "Tried to get physician without a valid ID.");
            return;
        }
        Log.d(LOG_TAG, "Getting Physician ID Key is : " + id);
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Physician>() {

                @Override
                public Physician call() throws Exception {
                    Log.d(LOG_TAG, "getting single physician with id : " + id);
                    return svc.getPhysician(id);
                }
            }, new TaskCallback<Physician>() {
                @Override
                public void success(Physician result) {
                    Log.d(LOG_TAG, "Found Physician :" + result.toString());
                    ((Callbacks) activity).setPhysician(result);
                }
                @Override
                public void error(Exception e) {
                    Log.d(LOG_TAG,
                            "Unable to fetch Physician to update the status logs. " +
                                    "Please check Internet connection.");
                }
            });
        }
    }

    /**
     * Updates the physician record on the server and sends the result to the activity
     *
     * @param physician object to update
     */
    public static synchronized void savePhysician(final Context activity, final Physician physician) {
        if (physician == null) {
            Log.e(LOG_TAG, "Tried to update physician with null object.");
            return;
        }
        Log.d(LOG_TAG, "Updating Physician to Cloud. ID Key is : " + physician.getId());
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Physician>() {
                @Override
                public Physician call() throws Exception {
                    Log.d(LOG_TAG, "Saving physician with status notes : " + physician.getId());
                    return svc.updatePhysician(physician.getId(), physician);
                }
            }, new TaskCallback<Physician>() {
                @Override
                public void success(Physician result) {
                    Log.d(LOG_TAG, "Updated Physician :" + result.toString());
                    ((Callbacks) activity).setPhysician(result);
                }
                @Override
                public void error(Exception e) {
                    Log.d(LOG_TAG,
                            "Unable to update status logs for the Physician. " +
                                    "Please check Internet connection.");
                }
            });
        }
    }
}
