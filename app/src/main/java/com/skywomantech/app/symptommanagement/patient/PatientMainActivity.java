package com.skywomantech.app.symptommanagement.patient;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.CheckInLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientCPContract;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.data.UserCredential;
import com.skywomantech.app.symptommanagement.patient.Reminder.ReminderManager;
import com.skywomantech.app.symptommanagement.physician.HistoryLogFragment;
import com.skywomantech.app.symptommanagement.physician.PatientGraphicsFragment;
import com.skywomantech.app.symptommanagement.physician.PhysicianPatientDetailFragment;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

public class PatientMainActivity extends Activity
        implements
        PatientMainFragment.Callbacks,
        MedicationLogListAdapter.Callbacks,
        MedicationTimeDialog.Callbacks,
        PatientPainLogFragment.Callbacks,
        PatientMedicationLogFragment.Callbacks,
        PatientStatusLogFragment.Callbacks,
        ReminderFragment.Callbacks,
        ReminderAddEditDialog.Callbacks,
        ReminderListAdapter.Callbacks,
        HistoryLogFragment.Callbacks {

    public final static String LOG_TAG = PatientMainActivity.class.getSimpleName();
    private String mPatientId;  // cloud login db id
    private Patient mPatient;
    private Context mContext;
    //private ReminderManager reminderManager = new ReminderManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        mContext = this;
        getPatient(); // if the patient isn't in the db kicks off a sync to get it
        if (savedInstanceState == null) {
            Log.d(LOG_TAG, "Are we doing Checkin? "
                    + (LoginUtility.isCheckin(this) ? "YES" : "NO"));
            if (LoginUtility.isCheckin(this)) {
                getFragmentManager().beginTransaction()
                        .add(R.id.patient_main_container,
                                new PatientPainLogFragment(), "patient_pain_frag")
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.patient_main_container,
                                new PatientMainFragment(), "patient_main_frag")
                        .commit();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Fragment hist_frag = getFragmentManager().findFragmentByTag("history_log_frag");
        Fragment rem_frag = getFragmentManager().findFragmentByTag("reminder_frag");
        if (hist_frag != null || rem_frag != null) {
            menu.removeItem(R.id.action_patient_history_log);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.patient_main, menu);
        menu.findItem(R.id.action_settings).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_main_container, new ReminderFragment(), "reminder_frag")
                    .commit();
            return true;
        } else if (id == R.id.patient_logout) {
            LoginActivity.restartLoginActivity(this);
        } else if (id == R.id.action_patient_history_log) {
            Bundle arguments = new Bundle();
            arguments.putBoolean(HistoryLogFragment.BACKUP_KEY, true); // allow home as up
            HistoryLogFragment historyLogFragment = new HistoryLogFragment();
            historyLogFragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_main_container,
                            historyLogFragment,
                            HistoryLogFragment.FRAGMENT_TAG)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().findFragmentById(R.id.patient_main_container)
                instanceof PatientMainFragment) {
            startActivity(new Intent()
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME));
        } else super.onBackPressed();
    }

    private void getPatient() {
        if (LoginUtility.isLoggedIn(this)
                && LoginUtility.getUserRole(this) == UserCredential.UserRole.PATIENT) {
            mPatientId = LoginUtility.getLoginId(mContext);
        } else {
            Log.d(LOG_TAG, "UNABLE to get Patient because login properties are " +
                    "not completed or they are not correct.");
        }

        Log.d(LOG_TAG, "Attempting to get PATIENT from CP with id : " + mPatientId);
        mPatient = null; // clear the patient object because this might be a different patient
        if (mPatientId != null && !mPatientId.isEmpty()) {
            mPatient = PatientDataManager.findPatient(mContext, mPatientId);
        }
        // CP didn't find it so start a sync and let it find it
        if (mPatient == null) {
            SymptomManagementSyncAdapter.syncImmediately(this);
        }
    }

    /**
     * When the pain log is completed it has to check to see if there is a checkin
     * in process and if so it needs to go directly to the medication logs... and it
     * needs to pass the checkin id to the med logs so they are associated with the
     * same checkin process that the pain log just associated with.  If its not a
     * check in process then the med logs us 0 for the checkin id.
     *
     * Note that when the med logs complete they immediately try to sync with the server
     * and so the doctor will get this new data if logged on
     *
     * @return
     */
    @Override
    public boolean onPainLogComplete(long checkinId) {
        // when we are doing the checkin process we have to force the screen flow
        // to go the medication logs
        if (LoginUtility.isCheckin(getApplication())) {
            // create a checkinLog to match the status log id
            createCheckInLog(checkinId);
            // complete the last step in the checkin process by starting the med logs
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_main_container,
                            new PatientMedicationLogFragment(), "patient_medlog_frag")
                    .commit();
            return true;
        }
        setLastLoggedTimestamp();
        return false;
    }

    /**
     * creates a check in log .. could be used by history logs or for data graphing
     * is associated with pain and medication logs.
     *
     * @param checkinId
     */
    private void createCheckInLog(long checkinId) {
        CheckInLog cLog = new CheckInLog();
        cLog.setCheckinId(checkinId);
        cLog.setCreated(checkinId);
        ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, cLog);
        Log.d(LOG_TAG, "Saving this Checkin Log : " + cLog.toString());
        Uri uri = getContentResolver().insert(PatientCPContract.CheckInLogEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        if (objectId < 0) {
            Log.e(LOG_TAG, "Check-in Log Insert Failed.");
        }
    }

    @Override
    public boolean onStatusLogComplete() {
        setLastLoggedTimestamp();
        return true;
    }

    @Override
    public boolean onMedicationLogComplete() {
        setLastLoggedTimestamp();
        return true;
    }

    private void setLastLoggedTimestamp() {
        Log.d(LOG_TAG, "Should be updating the last logged timestamp in the patient record.");
        getPatient(); // use this to make sure that we are logged in, etc.
        if (mPatient != null) {
            mPatient.setLastLogin(System.currentTimeMillis());
            PatientDataManager.updateLastLoginFromCP(mContext, mPatient);
        } else {
            Log.d(LOG_TAG, "Could not update the last login ... no patient found.");
        }
    }

    @Override
    public void onRequestDateTime(int position) {
        FragmentManager fm = getFragmentManager();
        MedicationTimeDialog timeDialog = MedicationTimeDialog.newInstance(position);
        timeDialog.show(fm, "med_time_date_dialog");
    }

    @Override
    public void onPositiveResult(long msTime, int position) {
        PatientMedicationLogFragment frag =
                (PatientMedicationLogFragment) getFragmentManager().findFragmentById(R.id.patient_main_container);
        frag.updateMedicationLogTimeTaken(msTime, position);
    }

    @Override
    public void onNegativeResult(long msTime, int position) {
        PatientMedicationLogFragment frag =
                (PatientMedicationLogFragment) getFragmentManager().findFragmentById(R.id.patient_main_container);
        frag.updateMedicationLogTimeTaken(0L, position);
    }

    @Override
    public void onRequestReminderAdd(Reminder reminder) {
        FragmentManager fm = getFragmentManager();
        ReminderAddEditDialog reminderDialog = ReminderAddEditDialog.newInstance(-1, reminder);
        reminderDialog.show(fm, "reminder_dialog");
    }

    @Override
    public void onRequestReminderEdit(int position, Reminder reminder) {
        FragmentManager fm = getFragmentManager();
        ReminderAddEditDialog reminderDialog = ReminderAddEditDialog.newInstance(position, reminder);
        reminderDialog.show(fm, "reminder_dialog");
    }

    @Override
    public void onReminderAdd(Reminder newReminder) {
        ReminderFragment frag =
                (ReminderFragment) getFragmentManager().findFragmentById(R.id.patient_main_container);
        frag.addReminder(newReminder);
    }

    @Override
    public void onReminderUpdate(int position, Reminder reminder) {
        ReminderFragment frag =
                (ReminderFragment) getFragmentManager().findFragmentById(R.id.patient_main_container);
        frag.updateReminder(position, reminder);
    }

    @Override
    public void onReminderDelete(final int position, Reminder reminder) {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Do you want to delete this reminder?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ReminderFragment frag =
                                (ReminderFragment) getFragmentManager().findFragmentById(R.id.patient_main_container);
                        frag.deleteReminder(position);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                }).create();
        alert.show();
    }

    /**
     * Callback method for the Reminder List Adapter
     * gets called when the switch value for the reminder has been toggled
     *
     * @param reminder
     */
    @Override
    public void onRequestReminderActivate(Reminder reminder) {
        if (reminder == null) {
            Log.e(LOG_TAG, "Null Reminder value attempting to be activated.");
            return;
        }
        Log.d(LOG_TAG, "Attempting to activate the alarm for reminder " + reminder.toString());
        if (reminder.isOn()) {
            Log.d(LOG_TAG, "activating Reminder " + reminder.getName());
            ReminderManager.cancelSingleReminderAlarm(this, reminder);
            ReminderManager.setSingleReminderAlarm(this, reminder);
        } else {
            Log.d(LOG_TAG, "deactivating Reminder " + reminder.getName());
            ReminderManager.cancelSingleReminderAlarm(this, reminder);
        }
        ReminderManager.printAlarms(this, LoginUtility.getLoginId(this));
        PatientDataManager.updateSingleReminder(mContext, mPatientId, reminder);
    }

    public Patient getPatientCallback() {
        return PatientDataManager.findPatient(mContext, mPatientId); // only check the CP
    }

    @Override
    public Patient getPatientForHistory() {
        // we need a valid patient id
        if (LoginUtility.isLoggedIn(this)
                && LoginUtility.getUserRole(this) == UserCredential.UserRole.PATIENT) {
            mPatientId = LoginUtility.getLoginId(mContext);
        } else {
            Log.d(LOG_TAG, "UNABLE to get Patient because login properties are " +
                    "not completed or they are not correct.");
            return null;
        }
        // create an new empty patient and just load the logs into it
        Patient p = new Patient();
        p.setId(mPatientId);
        PatientDataManager.getLogsFromCP(this, p);
        return p;
    }
}
