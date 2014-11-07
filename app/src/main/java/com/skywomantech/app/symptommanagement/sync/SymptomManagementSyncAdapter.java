package com.skywomantech.app.symptommanagement.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Alert;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.MedicationLog;
import com.skywomantech.app.symptommanagement.data.PainLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientCPContract;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.PrescriptionEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.data.PatientPrefs;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.data.StatusLog;
import com.skywomantech.app.symptommanagement.data.UserCredential;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;

public class SymptomManagementSyncAdapter extends AbstractThreadedSyncAdapter {

    // use the class name for logging purposes
    private final static String LOG_TAG = SymptomManagementSyncAdapter.class.getSimpleName();

    private static final int SYMPTOM_MANAGEMENT_NOTIFICATION_ID = 1111;

    // keep track of our application environment context
    private final Context mContext;

    // Try to sync the data at approximately 5 minute intervals
    // it can range plus or minus 6ish minutes
    private static final int SYNC_INTERVAL = 60 * 15;  //  15 minutes syncs
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private Patient mPatient;
    private String mPhysicianId;
    private String mPatientId;
    private static Collection<Alert> mAlerts;


    public SymptomManagementSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one if the
     * fake account doesn't exist yet.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {
            // Add the account and account type, no password or user data
            // If successful, return the Account object, otherwise report an error.
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            // If you don't set android:syncable="true" in your <provider> element in the manifest,
            // then call context.setIsSyncable(account, AUTHORITY, 1) here.
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Configure a periodic sync of the data then Sync data immediately.
     * Display a Toast message to let the user know what is happening.
     *
     * @param newAccount account to configure sync on
     * @param context    current application environment context
     */
    private static void onAccountCreated(Account newAccount, Context context) {

        configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.content_authority), true);

        // sync the initial data
        syncImmediately(context);
        Toast.makeText(context, "Retrieving Data...", Toast.LENGTH_LONG).show();
    }

    /**
     * @param context current application environment context
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Set up the sync to run at periodic intervals.
     *
     * @param context      current application environment context
     * @param syncInterval how often to sync in seconds
     * @param flexTime     how many seconds can it run before syncInterval (inexact timer for versions greater
     *                     than KITKAT
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        // we can enable inexact timers in our periodic sync
        SyncRequest request = new SyncRequest.Builder().
                syncPeriodic(syncInterval, flexTime).
                setSyncAdapter(account, authority).build();
        ContentResolver.requestSync(request);
    }

    /**
     * Initialize the SyncAdapter.
     *
     * @param context current application environment context
     */
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    /**
     * When a sync occurs gets the JSON data from the API and then parses and processes it. Also
     * cleans old data from the storage so that the storage is maintained and does not keep growing
     * and growing.
     *
     * @param account               the account that should be synced
     * @param bundle                SyncAdapter-specific parameters
     * @param authority             the authority of this sync request
     * @param contentProviderClient a ContentProviderClient that points to the ContentProvider for this authority
     * @param syncResult            SyncAdapter-specific parameters
     */
    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting onPerformSync");
        if (LoginUtility.isLoggedIn(getContext())) {
            // only process if patient or physician... no processing for ADMIN
            if (LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PATIENT) {
                Log.d(LOG_TAG, "SYNC Processing for PATIENT.");
                // app is logged in as a PATIENT
                processPatientSync();
            } else if (LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PHYSICIAN) {
                Log.d(LOG_TAG, "SYNC Processing for PHYSICIAN.");
                // app is logged in as a PHYSICIAN
                processPhysicianSync();
            }
        } else {
            Log.d(LOG_TAG, "Not Logged In so we won't need to anything for SYNC.");
        }
    }

    private void processPatientSync() {

        if (LoginUtility.isLoggedIn(getContext())
                && LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PATIENT) {
            mPatientId = LoginUtility.getLoginId(mContext);
        } else return;

        Log.d(LOG_TAG, "Processing Patient sync");

        // first we update the device with the cloud data
        Patient patientRecord = getPatientRecordFromCloud();
        if (patientRecord == null) {
            Log.d(LOG_TAG, "No Patient Record Found yet.");
            return;
        }
        if (patientRecord.getPrescriptions() != null) {
            updatePrescriptionsToCP(patientRecord.getPrescriptions());
        }
        updateLogsToCP(patientRecord);
        updateRemindersToCP(patientRecord);

        // then we update the cloud with the information from this device
        updateLastLoginFromCP(patientRecord);
        updateLogsFromCP(patientRecord);

        // update the reminders
        if (patientRecord.getPrefs() == null) {
            patientRecord.setPrefs(new PatientPrefs());
        }
        patientRecord.getPrefs().setAlerts(getUpdatedReminders());
        sendPatientRecordToCloud(patientRecord);
    }

    private void updateRemindersToCP(Patient patient) {
        if (patient.getPrefs() == null || patient.getPrefs().getAlerts() == null) return;
        String id = patient.getId();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(patient.getPrefs().getAlerts().size());
        for (Reminder l: patient.getPrefs().getAlerts()) {
            ContentValues cv = PatientCPcvHelper.createValuesObject(id, l);
            cVVector.add(cv);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        mContext.getContentResolver().bulkInsert(PatientCPContract.ReminderEntry.CONTENT_URI, cvArray);
    }

    private void updateLogsToCP(Patient patient) {
        Log.d(LOG_TAG, "Updating patient LOGs to CP Checking.. id is : " + patient.getId()
        + " Logged in id is : " + mPatientId);
        updatePainLogToCP(patient);
        updateMedLogToCP(patient);
        updateStatusLogToCP(patient);
    }

    private void updatePainLogToCP(Patient patient) {
        if (patient.getPainLog() == null) return;
        String id = patient.getId();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(patient.getPainLog().size());
        for (PainLog p: patient.getPainLog()) {
           ContentValues cv = PatientCPcvHelper.createValuesObject(id, p);
           cVVector.add(cv);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        mContext.getContentResolver().bulkInsert(PatientCPContract.PainLogEntry.CONTENT_URI, cvArray);
    }

    private void updateMedLogToCP(Patient patient) {
        if (patient.getMedLog() == null) return;
        String id = patient.getId();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(patient.getMedLog().size());
        for (MedicationLog l: patient.getMedLog()) {
            ContentValues cv = PatientCPcvHelper.createValuesObject(id, l);
            cVVector.add(cv);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        mContext.getContentResolver().bulkInsert(PatientCPContract.MedLogEntry.CONTENT_URI, cvArray);
    }

    private void updateStatusLogToCP(Patient patient) {
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
        mContext.getContentResolver().bulkInsert(PatientCPContract.StatusLogEntry.CONTENT_URI, cvArray);
    }


    private void updateLastLoginFromCP(Patient patientRecord) {
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + mPatientId + "\'";
        Cursor cursor = mContext.getContentResolver()
                .query(PatientCPContract.PatientEntry.CONTENT_URI, null, selection, null, null);
        if (cursor.moveToFirst()) {
            patientRecord
                    .setLastLogin(cursor
                            .getLong(cursor
                                    .getColumnIndex(PatientCPContract.PatientEntry.COLUMN_LAST_LOGIN)));
            Log.v(LOG_TAG, "==>Last Login RESET to: " + Long.toString(patientRecord.getLastLogin()));
        }
        cursor.close();
    }

    private Patient getPatientRecordFromCloud() {

        // are we a logged in patient?  don't bother the server if we aren't
        if (LoginUtility.isLoggedIn(getContext())
                && LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PATIENT) {
            mPatientId = LoginUtility.getLoginId(mContext);
            mPhysicianId = null;
        } else
            return null;

        Log.d(LOG_TAG, "getPatientRecordFromCloud - Patient ID is : " + mPatientId);
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting single Patient id : " + mPatientId);
                    return svc.getPatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "Found Patient :" + result.toDebugString());
                    mPatient = result;
                }

                @Override
                public void error(Exception e) {
                    Log.e(LOG_TAG, "Sync unable to UPDATE Patient record from internet." +
                            " No Internet? Try again later");
                }
            });
        } else {
            // something is wrong .. maybe we don't have internet .. just keep on trying
            Log.d(LOG_TAG, "NO SERVICE... is the internet offline?");
        }
        return mPatient;
    }

    /**
     * Take the prescriptions from the cloud patient, remove the ones from the local patient
     * and put the new ones in the CP.. just in case the doctor updated them.
     * <p/>
     *
     * @param prescriptions
     */
    private void updatePrescriptionsToCP(Collection<Medication> prescriptions) {
        if (prescriptions == null) return;

        mPatientId = LoginUtility.getLoginId(mContext);
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
        mContext.getContentResolver().bulkInsert(PrescriptionEntry.CONTENT_URI, cvArray);
    }

    /**
     * Put the logs that are on the local device into the cloud patient record
     * Warning.. this pretty much assumes the patient is only use this device and
     * not another one too... otherwise we would just put them in one by one
     * and make sure that we aren't overwriting them.  We would need to put IDs in them
     * on the cloud server too... too much work for this project
     *
     * @param patientRecord
     */
    private void updateLogsFromCP(Patient patientRecord) {
        patientRecord.setPainLog(getUpdatedPainLogs());
        patientRecord.setMedLog(getUpdatedMedLogs());
        patientRecord.setStatusLog(getUpdatedStatusLogs());
    }

    private Set<PainLog> getUpdatedPainLogs() {
        Set<PainLog> logs = new HashSet<PainLog>();
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + mPatientId + "\'";
        Cursor cursor = mContext.getContentResolver().query(
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

    private Set<MedicationLog> getUpdatedMedLogs() {
        Set<MedicationLog> logs = new HashSet<MedicationLog>();
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + mPatientId + "\'";
        Cursor cursor = mContext.getContentResolver().query(
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

    private Set<StatusLog> getUpdatedStatusLogs() {
        Set<StatusLog> logs = new HashSet<StatusLog>();
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + mPatientId + "\'";
        Cursor cursor = mContext.getContentResolver().query(
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

    private Collection<Reminder> getUpdatedReminders() {
        Set<Reminder> reminders = new HashSet<Reminder>();
        String selection = PatientCPContract.PatientEntry.COLUMN_PATIENT_ID + "=" + "\'" + mPatientId + "\'";
        Cursor cursor = mContext.getContentResolver().query(
                PatientCPContract.ReminderEntry.CONTENT_URI, null, selection, null, null);
        while (cursor.moveToNext()) {
            Reminder log = new Reminder();
            log.setHour(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_HOUR)));
            log.setMinutes(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_MINUTES)));
            log.setOn(cursor.getInt(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_ON)) == 1);
            log.setName(cursor.getString(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_NAME)));
            log.setReminderType(Reminder.ReminderType.findByValue(cursor.getColumnIndex(PatientCPContract.ReminderEntry.COLUMN_TYPE)));
            reminders.add(log);
        }
        cursor.close();
        return reminders;
    }

    /**
     * Now that the cloud patient has all the new information from this device we need to
     * save it back out to the cloud
     *
     * @param patientRecord
     */
    private void sendPatientRecordToCloud(final Patient patientRecord) {
        mPatientId = LoginUtility.getLoginId(mContext);

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "Updating single Patient id : " + mPatientId);
                    Log.v(LOG_TAG, "Last Login SET to before Sent to Cloud: "
                            + Long.toString(patientRecord.getLastLogin()));
                    return svc.updatePatient(mPatientId, patientRecord);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "Returned Patient from Server:" + result.toDebugString());
                    mPatient = result;

                }

                @Override
                public void error(Exception e) {
                    Log.e(LOG_TAG, "Sync unable to UPDATE Patient record to Internet." +
                            " Maybe no internet? Try again later");
                }
            });
        }
    }

    private void processPhysicianSync() {
        if (LoginUtility.isLoggedIn(getContext())
                && LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PHYSICIAN) {
            mPhysicianId = LoginUtility.getLoginId(mContext);
            mPatientId = null;
        } else return;

        Log.d(LOG_TAG, "Processing Physician sync for id: " + mPhysicianId);
        getPhysicianAlerts();
        //TODO: have to figure out physician alarm manager yet.
    }

    /**
     * go to cloud and find any alerts for the doctor who is logged in
     *
     * @return set of alerts for the logged in physician
     */
    private void getPhysicianAlerts() {

        if (LoginUtility.isLoggedIn(getContext())
                && LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PHYSICIAN) {
            mPhysicianId = LoginUtility.getLoginId(mContext);
            mPatientId = null;
        } else
            return ;

        Log.d(LOG_TAG, "Get Alerts for Physician : " + mPhysicianId);
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<Alert>>() {

                @Override
                public Collection<Alert> call() throws Exception {
                    Log.d(LOG_TAG, "Getting patient alerts for physician : " + mPhysicianId);
                    return svc.getPatientAlerts(mPhysicianId);
                }
            }, new TaskCallback<Collection<Alert>>() {

                @Override
                public void success(Collection<Alert> result) {
                    if (result != null) {
                        Log.d(LOG_TAG, "Found Alerts :" + result.size());
                    }
                    mAlerts = result;
                    createPhysicianNotification(mAlerts);
                }

                @Override
                public void error(Exception e) {
                    Log.e(LOG_TAG, "Sync unable to get physician alerts from internet." +
                            " Internet may not be available. Check your internet.");
                }
            });
        } else {
            Log.d(LOG_TAG, "No SERVICE available? Is the internet gone?");
        }
    }

    private void createPhysicianNotification(Collection<Alert> alerts) {

        if (alerts == null || alerts.size() <= 0) return;

        String title = "Symptom Management";

        int numberOfAlerts = alerts.size();
        String contentText = " Patient Alerts!";
        if (numberOfAlerts == 1) {
            Alert a = alerts.iterator().next();
            if (a != null) {
                contentText = a.getFormattedMessage();
            }
        } else {
            contentText = "There are " + Integer.toString(numberOfAlerts)
                    + "Severe Patients requiring attention.";
        }

        Log.d(LOG_TAG, "SENDING ALERT message : " + contentText);
        int iconId = R.drawable.ic_launcher;
        // set the notification to clear after a click
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setContentText(contentText)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true);

        // Open the app when the user clicks on the notification.
        Intent resultIntent = new Intent(getContext(), LoginActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext())
                .addParentStack(LoginActivity.class)
                .addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(SYMPTOM_MANAGEMENT_NOTIFICATION_ID, mBuilder.build());
    }

    public static synchronized int getPatientSeverityLevel(Patient patient) {
        Log.d(LOG_TAG, "Checking Patient for Severity Level: " + patient.getId());
        if(mAlerts == null || mAlerts.size() <= 0)  return Alert.PAIN_SEVERITY_LEVEL_0;
        for(Alert a: mAlerts) {
            Log.d(LOG_TAG, "Checking Alert for Patient ID :" + a.getPatientId());
            if(a.getPatientId().contentEquals(patient.getId())) {
                Log.d(LOG_TAG, "MATCHES PATIENT .. High Severity Level found :" + a.getSeverityLevel());
                patient.setSeverityLevel(a.getSeverityLevel());
                return a.getSeverityLevel();
            }
        }
        return Alert.PAIN_SEVERITY_LEVEL_0;
    }

}
