package com.skywomantech.app.symptommanagement.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
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
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.UserCredential;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;

import java.util.Collection;
import java.util.concurrent.Callable;

public class SymptomManagementSyncAdapter extends AbstractThreadedSyncAdapter {

    // use the class name for logging purposes
    private final static String LOG_TAG = SymptomManagementSyncAdapter.class.getSimpleName();

    private static final int SYMPTOM_MANAGEMENT_NOTIFICATION_ID = 1111;

    // keep track of our application environment context
    private final Context mContext;

    // Try to sync the data at approximately 20 minute intervals
    // it can range plus or minus 6ish minutes
    private static final int SYNC_INTERVAL = 60 * 20;  //  abt 20 minutes sync intervals
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    // Sync'd data for PATIENT
    private String mPatientId;
    private Patient mPatient;

    // Sync'd data for Physician
    private String mPhysicianId;
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
        // Note: Admin does not need any sync processing
        if (LoginUtility.isLoggedIn(getContext())) {
            if (LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PATIENT) {
                Log.d(LOG_TAG, "SYNC Processing for PATIENT.");
                processPatientSync();
            } else if (LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PHYSICIAN) {
                Log.d(LOG_TAG, "SYNC Processing for PHYSICIAN.");
                processPhysicianSync();
            }
        } else {
            Log.d(LOG_TAG, "Not Logged In so we won't need to anything for SYNC.");
        }
    }

    /**
     * PATIENT Sync
     */
    private void processPatientSync() {
        if (LoginUtility.isLoggedIn(getContext())
                && LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PATIENT) {
            mPatientId = LoginUtility.getLoginId(mContext);
        } else return;

        Log.d(LOG_TAG, "Logged In and Processing Patient sync : " + mPatientId);

        // we are calling out for the patient..it will process when it gets here
        getPatientRecordFromCloud();
    }

    private Patient getPatientRecordFromCloud() {
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
                    Log.d(LOG_TAG, "Got a patient now we can process.");
                    processPatientFromCloud(mPatient);
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

    private void processPatientFromCloud(Patient patient) {
        // patient record has been received.. save it via CP
        PatientDataManager.processPatientToCP(mContext, patient);

        // then we update the cloud with the information from this device
        PatientDataManager.processCPtoPatient(mContext, patient);

        // All updates are done so send it back for storing in cloud
        sendPatientRecordToCloud(patient);
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

    /**
     * Physician Sync
     */
    private void processPhysicianSync() {
        if (LoginUtility.isLoggedIn(getContext())
                && LoginUtility.getUserRole(getContext()) == UserCredential.UserRole.PHYSICIAN) {
            mPhysicianId = LoginUtility.getLoginId(mContext);
            mPatientId = null;
        } else return;

        Log.d(LOG_TAG, "Logged In and Processing Physician sync: " + mPhysicianId);
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

        String contentText = " Patient Alerts!";
        if (alerts.size() == 1) {
            Alert a = alerts.iterator().next();
            if (a != null) contentText = a.getFormattedMessage();
        } else {
            contentText = "There are " + Integer.toString(alerts.size())
                    + "Severe Patients requiring attention.";
        }

        Log.d(LOG_TAG, "SENDING ALERT message : " + contentText);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Symptom Management")
                        .setContentText(contentText)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(
                TaskStackBuilder.create(getContext())
                .addParentStack(LoginActivity.class)
                .addNextIntent(new Intent(getContext(), LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
        ((NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE))
               .notify(SYMPTOM_MANAGEMENT_NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Sync Adapter has the physician Alerts... Check them for a specific patient
     * and return the alert severity level
     *
     * @param patient to check for an alert if not found return severity level 0
     * @return int indicating the level of the alert for this patient
     */
    public static synchronized int findPatientAlertSeverityLevel(Patient patient) {
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
