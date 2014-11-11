package com.skywomantech.app.symptommanagement.data;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.patient.Reminder.ReminderManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class PatientDataManager {

    private final static String LOG_TAG = PatientDataManager.class.getSimpleName();

    /**
     *  This method is important to saving patient data from the cloud to CP
     *  Not currently saving or updating all the information ...
     *
     *  TODO:  but we could even if we aren't using it
     *
     * @param context
     * @param patient
     */
    public static synchronized void processPatientToCP(Context context, Patient patient) {
        storePatient(context, patient);

        // put the prescriptions in the CP (could have been changed by Physician)
        if (patient.getPrescriptions() != null) {
            updatePrescriptionsToCP(context, patient.getPrescriptions());
        }
        // NOTE:  Making the assumption that a patient mostly uses one device and so this
        // most of these issues are trivial but still try to handle them when possible

        // insert any new logs into the local database
        // ... this could have been changed on another device
        // This works OK because logs are insert only no editing/updating
        // just combine the device and the cloud logs together.
        updateLogsToCP(context, patient);

        // what about reminders... this is a catch 22 since they can be edited on the device
        // we can't just insert / update cloud data because it can lose changes
        // .. so for this project we assume that the current device data rules over the cloud data.
        // If they change device and some of the reminders are not what they like then
        // they need to manually change the reminders on that device.
        // BUT!! if the device has no reminders AND the cloud does have reminders
        // then we CAN insert them on the device assuming that the device is a new on for the patient
        // .. this is the only situation where we copy from cloud to device for reminders
        int numReminders = howManyRemindersOnDevice(context, patient.getId());
        if (numReminders < 0) Log.e(LOG_TAG, "Invalid PATIENT ID when updating reminders.");
        if (numReminders == 0) {
            Log.d(LOG_TAG, "Adding the Reminders stored in the cloud on this device.");
            updateRemindersToCP(context, patient);
        } else {
            Log.d(LOG_TAG, "We won't overwrite the reminders on this device.");
        }

        // what about physicians?  We don't use the physician information at this time
        // we aren't using the prefs either so not bothering to update them  ..  these have the
        // same problem that the reminders have and the local storage should
        // rule over the cloud storage
    }

    /**
     * This one is the important one for saving the stored CP information to a patient
     * record that can be sent back out to the cloud for storage
     *
     * @param mContext
     * @param patient
     */
    public synchronized static void processCPtoPatient(Context mContext, Patient patient) {

        // updating the last login information in case it has changed
        updateLastLoginFromCP(mContext, patient);

        // updating the logs
        getLogsFromCP(mContext, patient);

        // updating reminders which are stored in the prefs
        if (patient.getPrefs() == null) patient.setPrefs(new PatientPrefs());
        patient.getPrefs().setAlerts(getUpdatedReminders(mContext, patient.getId()));
    }

    public static synchronized void getLogsFromCP(Context context, Patient patient) {
        patient.setPainLog(getUpdatedPainLogs(context, patient.getId()));
        patient.setMedLog(getUpdatedMedLogs(context, patient.getId()));
        patient.setStatusLog(getUpdatedStatusLogs(context, patient.getId()));
    }

    private static synchronized Set<PainLog> getUpdatedPainLogs(Context context, String id) {
        Set<PainLog> logs = new HashSet<PainLog>();
        String selection = PatientCPContract.PainLogEntry.COLUMN_PATIENT_ID + "=" + "\'" + id + "\'";
        Cursor cursor = context.getContentResolver().query(
                PatientCPContract.PainLogEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            PainLog log = new PainLog();
            log.setEating(PainLog.Eating.findByValue(cursor.getInt(cursor.getColumnIndex(PatientCPContract.PainLogEntry.COLUMN_EATING))));
            log.setSeverity(PainLog.Severity.findByValue(cursor.getInt(cursor.getColumnIndex(PatientCPContract.PainLogEntry.COLUMN_SEVERITY))));
            log.setCreated(cursor.getLong(cursor.getColumnIndex(PatientCPContract.PainLogEntry.COLUMN_CREATED)));
            logs.add(log);
        }
        cursor.close();
        return logs;
    }

    private static synchronized Set<MedicationLog> getUpdatedMedLogs(Context context, String id) {
        Set<MedicationLog> logs = new HashSet<MedicationLog>();
        String selection = PatientCPContract.MedLogEntry.COLUMN_PATIENT_ID + "=" + "\'" + id + "\'";
        Cursor cursor = context.getContentResolver().query(
                PatientCPContract.MedLogEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            MedicationLog log = new MedicationLog();
            log.setMed(new Medication());
            log.getMed().setId(cursor.getString(cursor.getColumnIndex(PatientCPContract.MedLogEntry.COLUMN_MED_ID)));
            log.getMed().setName(cursor.getString(cursor.getColumnIndex(PatientCPContract.MedLogEntry.COLUMN_MED_NAME)));
            log.setTaken(cursor.getLong(cursor.getColumnIndex(PatientCPContract.MedLogEntry.COLUMN_TAKEN)));
            log.setCreated(cursor.getLong(cursor.getColumnIndex(PatientCPContract.MedLogEntry.COLUMN_CREATED)));
            logs.add(log);
        }
        cursor.close();
        return logs;
    }

    private static synchronized Set<StatusLog> getUpdatedStatusLogs(Context context, String id) {
        Set<StatusLog> logs = new HashSet<StatusLog>();
        String selection = PatientCPContract.StatusLogEntry.COLUMN_PATIENT_ID + "=" + "\'" + id + "\'";
        Cursor cursor = context.getContentResolver().query(
                PatientCPContract.StatusLogEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            StatusLog log = new StatusLog();
            log.setNote(cursor.getString(cursor.getColumnIndex(PatientCPContract.StatusLogEntry.COLUMN_NOTE)));
            log.setImage_location(cursor.getString(cursor.getColumnIndex(PatientCPContract.StatusLogEntry.COLUMN_IMAGE)));
            log.setCreated(cursor.getLong(cursor.getColumnIndex(PatientCPContract.StatusLogEntry.COLUMN_CREATED)));
            logs.add(log);
        }
        cursor.close();
        return logs;
    }

    // if we don't have internet but patient has been logged in on this device previously we can keep working
    public static synchronized UserCredential getUserCredentials(Context context, String id) {
        if (id == null || id.isEmpty()) return null;
        UserCredential credential = null;
        String selection = PatientCPContract.CredentialEntry.COLUMN_USER_ID + "=" + "\'" + id + "\'";
        Cursor cursor = context.getContentResolver().query(
                PatientCPContract.CredentialEntry.CONTENT_URI, null, selection, null, null);
        if (cursor.moveToNext()) {
            credential = new UserCredential();
            credential.setDbId(cursor.getLong(cursor.getColumnIndex(PatientCPContract.CredentialEntry._ID)));
            credential.setUserId(cursor.getString(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_USER_ID)));
            credential.setUserName(cursor.getString(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_USER_NAME)));
            credential.setUserRoleValue(cursor.getInt(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_USER_TYPE_VALUE)));
            credential.setLastLogin(cursor.getLong(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_LAST_LOGIN)));
            credential.setPassword(cursor.getString(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_PASSWORD)));
            credential.setPassword("pass");  //TODO: check why password is not saved?
            credential.setUserType(UserCredential.UserRole.findByValue(credential.getUserRoleValue()));
            Log.d(LOG_TAG, "This credential is what we are working with now OKEY DOKEY.." + credential.toString());
        }
        cursor.close();
        return credential;
    }

    public static synchronized UserCredential getUserCredentials(Context context, String username, String password) {
        Log.d(LOG_TAG, "Trying to get the credentials...");
        if (username == null || username.isEmpty() || password == null ||password.isEmpty() ) return null;
        Log.d(LOG_TAG, "... Inputs are good.. now doing a query.");
        UserCredential credential = null;
        // get all the stored credentials on this device .. should not be significant number
        Cursor cursor = context.getContentResolver().query(
                PatientCPContract.CredentialEntry.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            credential = new UserCredential();
            credential.setDbId(cursor.getLong(cursor.getColumnIndex(PatientCPContract.CredentialEntry._ID)));
            credential.setUserId(cursor.getString(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_USER_ID)));
            credential.setUserName(cursor.getString(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_USER_NAME)));
            credential.setUserRoleValue(cursor.getInt(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_USER_TYPE_VALUE)));
            credential.setLastLogin(cursor.getLong(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_LAST_LOGIN)));
            credential.setPassword(cursor.getString(cursor.getColumnIndex(PatientCPContract.CredentialEntry.COLUMN_PASSWORD)));
            credential.setPassword("pass");  //TODO: check why password is not saved?
            credential.setUserType(UserCredential.UserRole.findByValue(credential.getUserRoleValue()));
            // find input has a match in the CP
            Log.d(LOG_TAG, "Checking this credential ... " + credential.toString());
            if (credential.getUserName().toLowerCase().contentEquals(username.toLowerCase())) {
                Log.d(LOG_TAG, "Credential User name matches.. " + credential.getUserName());
                //if(credential.getPassword().contentEquals(password)) {
                    //Log.d(LOG_TAG, "Password matches ... " + credential.getPassword());
                    // TODO: remove this debug before putting out there
                    return credential;
               // }
            } else {
                Log.d(LOG_TAG, "Credential Not a match for username and password.");
            }
        }
        cursor.close();
        return credential;
    }

    public synchronized static void addCredentialToCP(Context context, UserCredential credential) {
        if (credential == null) return;
        ContentValues cv = PatientCPcvHelper.createInsertValuesObject(credential.getUserId(), credential);
        Uri uri = context.getContentResolver().insert(PatientCPContract.CredentialEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        credential.setDbId(objectId);  // set the local CP ID .. different for every device!
        Log.d(LOG_TAG, "New Credential Inserted in local DB Id is : " + Long.toString(objectId));
    }

    private synchronized static void updateLogsToCP(Context context, Patient patient) {
        Log.d(LOG_TAG, "Updating patient LOGs to CP ...id is : " + patient.getId());
        updatePainLogToCP(context, patient);
        updateMedLogToCP(context, patient);
        updateStatusLogToCP(context, patient);
    }

    private synchronized static void updatePainLogToCP(Context context, Patient patient) {
        if (patient.getPainLog() == null) return;
        String id = patient.getId();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(patient.getPainLog().size());
        for (PainLog p: patient.getPainLog()) {
            ContentValues cv = PatientCPcvHelper.createValuesObject(id, p);
            cVVector.add(cv);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        context.getContentResolver().bulkInsert(PatientCPContract.PainLogEntry.CONTENT_URI, cvArray);
    }

    private synchronized static void updateMedLogToCP(Context context, Patient patient) {
        if (patient.getMedLog() == null) return;
        String id = patient.getId();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(patient.getMedLog().size());
        for (MedicationLog l: patient.getMedLog()) {
            ContentValues cv = PatientCPcvHelper.createValuesObject(id, l);
            cVVector.add(cv);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        context.getContentResolver().bulkInsert(PatientCPContract.MedLogEntry.CONTENT_URI, cvArray);
    }

    private synchronized static void updateStatusLogToCP(Context context, Patient patient) {
        if (patient.getStatusLog() == null) return;
        String id = patient.getId();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(patient.getStatusLog().size());
        for (StatusLog l: patient.getStatusLog()) {
            ContentValues cv = PatientCPcvHelper.createValuesObject(id, l);
            cVVector.add(cv);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        // db ensures that there are not duplicates
        context.getContentResolver().bulkInsert(PatientCPContract.StatusLogEntry.CONTENT_URI, cvArray);
    }

    public static synchronized Collection<Reminder> loadReminderList(Context context, String id) {
        Collection<Reminder> reminders = new HashSet<Reminder>();
        // search the local storage for the item id
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + id + "\'";
        Cursor cursor = context.getContentResolver()
                .query(PatientCPContract.ReminderEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            Reminder item = new Reminder();
            item.setDbId(cursor.getLong(cursor.getColumnIndex(PatientCPContract.ReminderEntry._ID)));
            item.setName(cursor.getString(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_NAME)));
            item.setHour(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_HOUR)));
            item.setMinutes(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_MINUTES)));
            item.setOn((cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_ON)) == 0 ? false : true));
            item.setCreated(cursor.getLong(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_CREATED)));
            reminders.add(item);
        }
        cursor.close();
        return reminders;
    }

    public static synchronized Collection<Reminder> loadSortedReminderList(Context context, String id) {
        Collection<Reminder> reminders = loadReminderList(context, id);
        if (reminders.size() > 0) {
            Collection<Reminder> sorted = ReminderManager.sortRemindersByTime(reminders);
            return sorted;
        }
        return reminders;
    }

    public static synchronized void updateRemindersToCP(Context context, Patient patient) {
        if (patient.getPrefs() == null || patient.getPrefs().getAlerts() == null) {
            Log.d(LOG_TAG, "There are no Reminders to update or Prefs for this patient.");
            return;
        }

        String id = patient.getId();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(patient.getPrefs().getAlerts().size());
        for (Reminder r: patient.getPrefs().getAlerts()) {
            if (isReminderInCP(context, r)) {
                updateSingleReminder(context, patient.getId(), r);
            } else {
                ContentValues cv = PatientCPcvHelper.createInsertValuesObject(id, r);
                cVVector.add(cv);
            }
        }
        if (cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            context.getContentResolver().bulkInsert(PatientCPContract.ReminderEntry.CONTENT_URI, cvArray);
        }
    }

    public static synchronized boolean isReminderInCP(Context context, Reminder reminder) {
        if (reminder == null || reminder.getCreated() <= 0 ) return false;
        String selection = PatientCPContract.ReminderEntry.COLUMN_CREATED + "=" + "\'"
                +  reminder.getCreated() + "\'";
        Cursor cursor = context.getContentResolver()
                .query(PatientCPContract.ReminderEntry.CONTENT_URI, null, selection, null,null);
        boolean found = (cursor.getCount() > 0) ? true : false;
        cursor.close();
        Log.d(LOG_TAG, "Does Reminder " + reminder + " exist in DB? " + Boolean.toString(found));
        return found;
    }

    public static synchronized int howManyRemindersOnDevice(Context context, String id) {
        if (id == null || id.isEmpty() ) return -1; // invalid id
        String selection = PatientCPContract.ReminderEntry.COLUMN_PATIENT_ID
                             + "=" + "\'"  +  id + "\'";
        Cursor cursor = context.getContentResolver()
                .query(PatientCPContract.ReminderEntry.CONTENT_URI, null, selection, null,null);
        int found = cursor.getCount();
        cursor.close();
        Log.d(LOG_TAG, "How Many Reminders  exist in DB for this patient [" + id +"]? "
                + Integer.toString(found));
        return found;
    }

    public static synchronized int updateSingleReminder(Context context, String id, Reminder reminder) {
        if (id == null || id.isEmpty() || reminder == null ) return 0;
        Log.d(LOG_TAG, "Updating a single REMINDER for this patient." + id
                + " reminder name is " + reminder.getName());
        int rowsUpdated = 0;
        if (reminder.getCreated() >= 0) {
            ContentValues cv = PatientCPcvHelper.createValuesObject(id, reminder);
            String selection =
                    PatientCPContract.ReminderEntry.COLUMN_CREATED + "=" + Long.toString(reminder.getCreated());
             rowsUpdated = context.getContentResolver()
                    .update(PatientCPContract.ReminderEntry.CONTENT_URI, cv, selection, null);
            Log.v(LOG_TAG, "Reminder rows updated : " + Integer.toString(rowsUpdated));
        }
        return rowsUpdated;
    }

    private static synchronized Collection<Reminder> getUpdatedReminders(Context context, String id) {
        Set<Reminder> reminders = new HashSet<Reminder>();
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + id + "\'";
        Cursor cursor = context.getContentResolver().query(
                PatientCPContract.ReminderEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            Reminder log = new Reminder();
            log.setHour(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_HOUR)));
            log.setMinutes(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_MINUTES)));
            log.setOn(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_ON)) == 1);
            log.setName(cursor.getString(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_NAME)));
            log.setReminderType(Reminder.ReminderType.findByValue(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_TYPE)));
            log.setCreated(cursor.getLong(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_CREATED)));
            reminders.add(log);
        }
        cursor.close();
        return reminders;
    }

    /**
     * Take the prescriptions from the cloud patient, remove the ones from the local patient
     * and put the new ones in the CP.. just in case the doctor updated them.
     * <p/>
     *
     * @param prescriptions
     */
    private static synchronized void updatePrescriptionsToCP(Context context, Collection<Medication> prescriptions) {
        if (prescriptions == null) return;

        String mPatientId = LoginUtility.getLoginId(context);
        Log.d(LOG_TAG, "SYNC is Updating Prescriptions for patient : "  + mPatientId);
//        // delete all of the patient's prescriptions DON'T NEED ANYMORE WITH UPDATE TABLES
//        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + mPatientId + "\'";
//        int deleted = mContext.getContentResolver().delete(PrescriptionEntry.CONTENT_URI, selection, null);
//        Log.d(LOG_TAG, "Deleted prescription count is :" + Integer.toString(deleted));
        //insert all of the prescriptions at once
        Vector<ContentValues> cVVector = new Vector<ContentValues>(prescriptions.size());
        for (Medication m : prescriptions) {
            Log.d(LOG_TAG, "Adding a prescription : " + m.toDebugString());
            ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, m);
            cVVector.add(cv);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        Log.d(LOG_TAG, "We have this many prescriptions to bulk insert : " + cVVector.size());
        context.getContentResolver().bulkInsert(PatientCPContract.PrescriptionEntry.CONTENT_URI, cvArray);
    }

    public static synchronized Collection<Medication> getPrescriptionsFromCP(Context context, String id) {

        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'"  + id+ "\'";
        Cursor cursor = context.getContentResolver()
                .query(PatientCPContract.PrescriptionEntry.CONTENT_URI, null, selection, null, null);
        Collection<Medication> prescriptions = new HashSet<Medication>();
        Log.d(LOG_TAG, "Number of prescriptions found: " + Integer.toString(cursor.getCount()));
        while (cursor.moveToNext()) {
            Medication med = new Medication();
            med.setName(cursor.getString
                    (cursor.getColumnIndex(PatientCPContract.PrescriptionEntry.COLUMN_NAME)));
            Log.d(LOG_TAG, "Adding this prescription : " + med.toDebugString());
            prescriptions.add(med);
        }
        cursor.close();
        return prescriptions;
    }


    public static synchronized void updateLastLoginFromCP(Context context, Patient patient) {
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "="
                + "\'" + patient.getId() + "\'";
        Cursor cursor = context.getContentResolver()
                .query(PatientCPContract.PatientEntry.CONTENT_URI, null, selection, null, null);
        if (cursor.moveToFirst()) {
            patient.setLastLogin(cursor.getLong(cursor.getColumnIndex(
                    PatientCPContract.PatientEntry.COLUMN_LAST_LOGIN)));
            Log.v(LOG_TAG, "==>Last Login RESET to: " + Long.toString(patient.getLastLogin()));
        }
        cursor.close();
    }

    public static synchronized HistoryLog[] createLogList(Patient mPatient) {
        HistoryLogSorter sorter = new HistoryLogSorter();
        TreeSet<HistoryLog> sortedLogs = new TreeSet<HistoryLog>(
                Collections.reverseOrder(sorter));
        Collection<PainLog> plogs = mPatient.getPainLog();
        if (plogs != null) {
            for (PainLog p : plogs) {
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
        Collection<MedicationLog> mlogs = mPatient.getMedLog();
        if (mlogs != null) {
            for (MedicationLog m : mlogs) {
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
        Collection<StatusLog> slogs = mPatient.getStatusLog();
        if (slogs != null) {
            for (StatusLog s : slogs) {
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

    public static synchronized Patient findPatient(Context context, String id) {
        Log.d(LOG_TAG, "Is this Patient in the local database? " + id);
        Patient patient = new Patient();
        patient.setId(id); // cloud id
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'"  + id + "\'";
        Cursor cursor = context.getContentResolver()
                .query(PatientCPContract.PatientEntry.CONTENT_URI, null, selection, null,null);
        if (cursor.getCount() > 1) {
            Log.d(LOG_TAG, "There are multiple entries for the same DB patient! " +
                    "This really should not occur! id : " + id);
        }
        if (cursor.moveToFirst()) {
            // copy the data from the CV to the patient object
            patient.setDbId(cursor.getLong(cursor.getColumnIndex(PatientCPContract.PatientEntry._ID))); // local CP id
            patient.setFirstName(cursor.getString(cursor.getColumnIndex(PatientCPContract.PatientEntry
                    .COLUMN_FIRST_NAME)));
            patient.setLastName(cursor.getString(cursor.getColumnIndex(PatientCPContract.PatientEntry
                    .COLUMN_LAST_NAME)));
            patient.setLastLogin(cursor.getLong(cursor.getColumnIndex(PatientCPContract.PatientEntry
                    .COLUMN_LAST_LOGIN)));
            Log.v(LOG_TAG, "Last Login Originally set to: " + Long.toString(patient.getLastLogin()));
            patient.setBirthdate(cursor.getString(cursor.getColumnIndex(PatientCPContract.PatientEntry
                    .COLUMN_BIRTHDATE)));
            Log.d(LOG_TAG, "Yes.. found this PATIENT id : " + id
                    + " record : " + patient.toDebugString());
        }
        cursor.close();
        // I am assuming we have both first and last name ... not great but can change later
        if (patient.getId() == null || patient.getId().isEmpty()
                || patient.getFirstName() == null || patient.getFirstName().isEmpty()
                || patient.getLastName() == null || patient.getLastName().isEmpty()) {
            Log.d(LOG_TAG, "ALERT!!! " +
                    "This seems to happen on a new patient load.." +
                    "And I am forcing a restriction of first and last name on patients");
            // this will force the sync adapter to find the patient again
            return null;
        }
        return patient;
    }

    public static synchronized void storePatient(Context context, Patient patient) {
        if (patient == null || patient.getId() == null || patient.getId().isEmpty()) return;
        Log.d(LOG_TAG, "INSERTING/Updating to CP with this PATIENT id : " + patient.getId()
                + " record : " + patient.toDebugString());
        if (isPatientInCP(context, patient)) {
            updatePatientToCP(context, patient);
        } else {
            insertPatientToCP(context, patient);
        }
    }

    public static synchronized boolean isPatientInCP(Context context, Patient patient) {
        if (patient == null || patient.getId() == null || patient.getId().isEmpty()) return false;
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'"
                +  patient.getId() + "\'";
        Cursor cursor = context.getContentResolver()
                .query(PatientCPContract.PatientEntry.CONTENT_URI, null, selection, null,null);
        boolean found = (cursor.getCount() > 0) ? true : false;
        cursor.close();
        Log.d(LOG_TAG, "Does Patient " + patient + " exist in DB? " + Boolean.toString(found));
        return found;
    }

    private static synchronized void insertPatientToCP(Context context, Patient patient) {
        Log.d(LOG_TAG, "Inserting Patient information to CP - " + patient);
        if (patient == null || patient.getId() == null || patient.getId().isEmpty()) return;
        ContentValues cvPatient = PatientCPcvHelper.createInsertValuesObject(patient.getId(), patient);
        Uri uri = context.getContentResolver()
                .insert(PatientCPContract.PatientEntry.CONTENT_URI, cvPatient);
        long objectId = ContentUris.parseId(uri);
        patient.setDbId(objectId);  // set the local CP ID .. different for every device!
        Log.d(LOG_TAG, "New Patient Inserted in local DB Id is : " + Long.toString(objectId));
    }

    private static synchronized void updatePatientToCP(Context context, Patient patient) {
        Log.d(LOG_TAG, "Updating Patient information to CP - " + patient);
        if (patient == null || patient.getId() == null || patient.getId().isEmpty()) return;
        ContentValues cvPatient = PatientCPcvHelper.createValuesObject(patient.getId(), patient);
        Log.d(LOG_TAG, "Using this dbId to do the update :" + patient.getDbId());
        String selection = PatientCPContract.PatientEntry._ID + "=" + Long.toString(patient.getDbId());
        // table set to replace on conflict.. should update no matter what if we got the id OK
        int updated = context.getContentResolver()
                .update(PatientCPContract.PatientEntry.CONTENT_URI, cvPatient, selection, null);
        Log.d(LOG_TAG, "Update Patient to local DB was "
                + (updated > 0 ? "SUCCESSFUL" : " UNSUCCESSFUL"));
        if ( updated <= 0) {
            Log.d(LOG_TAG, "Trying to insert it instead because this table should REPLACE on conflict.");
            insertPatientToCP(context, patient);
        }
    }
}
