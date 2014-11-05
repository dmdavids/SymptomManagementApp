package com.skywomantech.app.symptommanagement.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.Login;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;

public class SymptomManagementSyncAdapter extends AbstractThreadedSyncAdapter {

    // use the class name for logging purposes
    private final String LOG_TAG = SymptomManagementSyncAdapter.class.getSimpleName();

    // keep track of our application environment context
    private final Context mContext;

    // Try to sync the data at approximately 5 minute intervals
    // it can range plus or minus 6ish minutes
    private static final int SYNC_INTERVAL = 60 * 5;  //  5 minutes
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static boolean mPatientDevice = true;
    private boolean mPhysicianLoggedIn = false;
    private String mPatientId = ""; // = "2084098340928"; // TODO: needs a real id from db to work
    private Patient mPatient;
    private String mPhysicianId;
    private Collection<Alert> mAlerts;


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
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
        if (mPatientDevice) {
            mPatientId = Login.getLoginId(mContext);
            if (mPatientId != null && !mPatientId.isEmpty()) {
                processPatientSync();
            } else {
                Log.v(LOG_TAG, "Skipping Patient Sync - no Patient identified.");
            }
        } else {
            processPhysicianSync();
        }
    }

    private void processPatientSync() {
        if (!mPatientDevice && mPhysicianLoggedIn) return;
        Log.d(LOG_TAG, "Processing Patient sync");

        Patient patientRecord = getPatientRecordFromCloud();
        if (patientRecord == null) {
            Log.d(LOG_TAG, "No Patient identified yet.");
            return;
        }

        if (patientRecord.getPrescriptions() != null ) {
            updatePrescriptionsToCP(patientRecord.getPrescriptions()); // warning! do this first before updating the server
        }

        updateLastLoginFromCP(patientRecord);
        updateLogsFromCP(patientRecord);

        // update the reminders
        if (patientRecord.getPrefs() == null) {
            patientRecord.setPrefs(new PatientPrefs());
        }
        patientRecord.getPrefs().setAlerts(getUpdatedReminders());
        sendPatientRecordToCloud(patientRecord);
    }

    private void updateLastLoginFromCP(Patient patientRecord) {
        Cursor cursor = mContext.getContentResolver()
                .query(PatientCPContract.PatientEntry.CONTENT_URI, null, null, null,null);
        if (cursor.moveToFirst()) {
           patientRecord
                   .setLastLogin(cursor
                           .getLong(cursor
                                   .getColumnIndex(PatientCPContract.PatientEntry.COLUMN_LAST_LOGIN)));
            Log.v(LOG_TAG, "Last Login RESET to: " + Long.toString(patientRecord.getLastLogin()));
        }
        cursor.close();
    }

    private Patient getPatientRecordFromCloud() {
        mPatientId = Login.getLoginId(mContext);
        Log.d(LOG_TAG, "getPatientRecordFromCloud - Patient ID is : " + mPatientId);

        final SymptomManagementApi svc =
                SymptomManagementService.getService();

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
                            " Try again later");
                }
            });
        }
        return mPatient;
    }

    private void updatePrescriptionsToCP(Collection<Medication> prescriptions) {
        if (prescriptions == null) return;

        mPatientId = Login.getLoginId(mContext);
        // delete all of the patient's prescriptions
        mContext.getContentResolver().delete(PrescriptionEntry.CONTENT_URI, null, null);

        //insert all of the prescriptions at once
        Vector<ContentValues> cVVector = new Vector<ContentValues>(prescriptions.size());
        for(Medication m : prescriptions) {
            ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, m);
            cVVector.add(cv);
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        mContext.getContentResolver().bulkInsert(PrescriptionEntry.CONTENT_URI, cvArray);
    }

    private void updateLogsFromCP(Patient patientRecord) {
        patientRecord.setPainLog(getUpdatedPainLogs());
        patientRecord.setMedLog(getUpdatedMedLogs());
        patientRecord.setStatusLog(getUpdatedStatusLogs());
    }

    private Set<PainLog> getUpdatedPainLogs() {
        Set<PainLog> logs = new HashSet<PainLog>();
        Cursor cursor = mContext.getContentResolver().query(
                PatientCPContract.PainLogEntry.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()){
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
        Cursor cursor = mContext.getContentResolver().query(
                PatientCPContract.MedLogEntry.CONTENT_URI, null, null, null, null);
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
        Cursor cursor = mContext.getContentResolver().query(
                PatientCPContract.StatusLogEntry.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()){
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
        Cursor cursor = mContext.getContentResolver().query(
                PatientCPContract.ReminderEntry.CONTENT_URI, null, null, null, null);
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
    private void sendPatientRecordToCloud(final Patient patientRecord) {
        mPatientId = Login.getLoginId(mContext);

        final SymptomManagementApi svc =
                SymptomManagementService.getService();

        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "Updating single Patient id : " + mPatientId);
                    Log.v(LOG_TAG, "Last Login SET to before Sent to Cloud: " + Long.toString(patientRecord.getLastLogin()));
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
                            " Try again later");
                }
            });
        }
    }

    private void processPhysicianSync() {
        if (mPatientDevice) return;
        Log.d(LOG_TAG, "Physician sync");
        Collection<Alert> alerts = getPhysicianAlerts();
//        notifyPhysicianAlerts(alerts);
    }

    private Collection<Alert> getPhysicianAlerts() {
        mPhysicianId = Login.getLoginId(mContext);
        Log.d(LOG_TAG, "get Alerts for Physician : " + mPhysicianId);

        final SymptomManagementApi svc =
                SymptomManagementService.getService();

        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<Alert>>() {

                @Override
                public Collection<Alert> call() throws Exception {
                    Log.d(LOG_TAG, "getting patient alerts for physician : " + mPhysicianId);
                    return svc.getPatientAlerts(mPhysicianId);
                }
            }, new TaskCallback<Collection<Alert>>() {

                @Override
                public void success(Collection<Alert> result) {
                    if (result != null) {
                        Log.d(LOG_TAG, "Found Alerts size of :" + result.size());
                    }
                    mAlerts = result;
                }

                @Override
                public void error(Exception e) {
                    Log.e(LOG_TAG, "Sync unable to get patients alerts from internet." +
                            " Check your internet.");
                }
            });
        }
        return mAlerts;
    }

    public static void setPatientDevice(boolean isPatient) {
        mPatientDevice = isPatient;
    }

}
