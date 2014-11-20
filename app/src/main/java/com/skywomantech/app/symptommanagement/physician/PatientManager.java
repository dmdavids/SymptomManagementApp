package com.skywomantech.app.symptommanagement.physician;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.MedicationLog;
import com.skywomantech.app.symptommanagement.data.PainLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.StatusLog;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * Manages data related to the Patient
 *
 * Retrieves patients from the server
 * Stores patients to the server
 * Searches patients on the server
 * Generates a list of HistoryLogs from the patient data
 *
 */
public class PatientManager {

    private static final String LOG_TAG = PatientManager.class.getSimpleName();

    // Notifies the activity about the following events
    // setPatient - if the patient result was returned
    // failedSearch - if the patient name search failed
    // successfulSearch - if the patient name search was successful
    public interface Callbacks {
        public void setPatient(Patient patient);

        public void failedSearch(String message);

        public void successfulSearch(Patient patient);
    }

    /**
     * Go to the server and get the patient record with this id then
     * on success uses the callback to send the information back to the activity
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

    /**
     * updates the patient on the server
     * on successful results sends the new patient back to the activity
     *
     * @param activity
     * @param patientRecord
     */
    public static synchronized void updatePatient(final Activity activity, final Patient patientRecord) {

        if (patientRecord == null || patientRecord.getId() == null ||
                patientRecord.getId().isEmpty()) {
            Log.e(LOG_TAG, "Trying to update a patient that is null or has no server id.");
            return;
        }
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {
                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "Updating single Patient id : " + patientRecord.getId());
                    return svc.updatePatient(patientRecord.getId(), patientRecord);
                }
            }, new TaskCallback<Patient>() {
                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "Returned Updated Patient from Server:" + result.toDebugString());
                    ((Callbacks) activity).setPatient(result);
                }
                @Override
                public void error(Exception e) {
                    Log.e(LOG_TAG, "Unable to UPDATE Patient record to Internet." +
                            "Patient changes did not save. Try again later");
                }
            });
        }
    }


    /**
     * Does a patient name search for the last name on the server
     * and then checks for a first name match.. returns the first match found
     * Uses activity callbacks to let the calling activity deal with processing of the
     * the searches.
     * If the search fails it gives a detailed message
     *
     * @param activity
     * @param last
     * @param first
     */
    public static synchronized void findPatientByName(final Activity activity,
                                                      final String last, final String first) {
        if (activity == null || last == null) {
            Log.e(LOG_TAG, "Invalid parameters for the findByPatientName search.");
            return;
        }
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<Patient>>() {

                @Override
                public Collection<Patient> call() throws Exception {
                    Log.d(LOG_TAG, "Searching for last name on the server : " + last);
                    return svc.findByPatientLastName(last);
                }
            }, new TaskCallback<Collection<Patient>>() {
                @Override
                public void success(Collection<Patient> result) {
                    // check for first name match
                    Patient patient = null;
                    for (Patient p : result) {
                        Log.d(LOG_TAG, "Checking patient first name for match : " + p.getFirstName());
                        if (p.getFirstName().toLowerCase().contentEquals(first.toLowerCase())) {
                            // found a match on both first and last
                            patient = p;
                        }
                    }
                    if (patient == null) {
                        ((Callbacks) activity).failedSearch("No patients match that name.");
                    } else {
                        ((Callbacks) activity).successfulSearch(patient);
                    }
                }
                @Override
                public void error(Exception e) {
                    ((Callbacks) activity).failedSearch("No patients with that last name.");
                }
            });
        }
    }

    /**
     * Generates a sorted list of the pain, medication and status logs to display
     * Puts them into a generalized format for display
     *
     * @param mPatient Patient with logs
     * @return array of HistoryLog message
     */
    public static synchronized HistoryLog[] createLogList(Patient mPatient) {
        HistoryLogSorter sorter = new HistoryLogSorter();
        TreeSet<HistoryLog> sortedLogs = new TreeSet<HistoryLog>(
                Collections.reverseOrder(sorter));
        Collection<PainLog> painLogs = mPatient.getPainLog();
        if (painLogs != null) {
            for (PainLog p : painLogs) {
                HistoryLog h = new HistoryLog();
                h.setCreated(p.getCreated());
                h.setType(HistoryLog.LogType.PAIN_LOG);
                String severity = (p.getSeverity() == PainLog.Severity.SEVERE) ? "SEVERE"
                        : (p.getSeverity() == PainLog.Severity.MODERATE) ? "Moderate"
                        : "Well-Defined";
                String eating = (p.getEating() == PainLog.Eating.NOT_EATING) ? "NOT EATING"
                        : (p.getEating() == PainLog.Eating.SOME_EATING) ? "Some Eating" : "Eating";
                String info = "Pain : " + severity + " -- " + eating;
                h.setInfo(info);
                sortedLogs.add(h);
            }
        }
        Collection<MedicationLog> medLogs = mPatient.getMedLog();
        if (medLogs != null) {
            for (MedicationLog m : medLogs) {
                HistoryLog h = new HistoryLog();
                h.setCreated(m.getCreated());
                h.setType(HistoryLog.LogType.MED_LOG);
                String name = m.getMed().getName();
                String taken = m.getTakenDateFormattedString(" hh:mm a 'on' E, MMM d yyyy");
                String info = name + " taken " + taken;
                h.setInfo(info);
                sortedLogs.add(h);
            }
        }
        Collection<StatusLog> statusLogs = mPatient.getStatusLog();
        if (statusLogs != null) {
            for (StatusLog s : statusLogs) {
                HistoryLog h = new HistoryLog();
                h.setCreated(s.getCreated());
                h.setType(HistoryLog.LogType.STATUS_LOG);
                String image = (s.getImage_location() != null && !s.getImage_location().isEmpty())
                        ? "Image Taken" : "";
                String info = "Note: " + s.getNote() + " " + image;
                h.setInfo(info);
                sortedLogs.add(h);
            }
        }

        if (sortedLogs.size() <= 0) return new HistoryLog[0];
        return sortedLogs.toArray(new HistoryLog[sortedLogs.size()]);
    }

    public static class HistoryLogSorter implements Comparator<HistoryLog> {
        public int compare(HistoryLog x, HistoryLog y) {
            return Long.compare(x.getCreated(), y.getCreated());
        }
    }
}
