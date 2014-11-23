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
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

/**
 * This is the main controlling activity for the Patient version of the app.
 * It is currently the only activity for this purpose so it also manages callbacks for
 * for all the related fragments
 * <p/>
 * This patient app utilizes a local persistent storage with SQLite and has a content provider
 * to manage access to the storage.  A first-time login on a device must have internet access
 * to obtain the patient information from the server.  After that it uses the local storage
 * to store all patient tracking and check-in data so it can keep working offline.
 * <p/>
 * The SyncAdapter will sync the local storage with the server storage when internet is available.
 */
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
    private String mPatientId;  // server login db id
    private Patient mPatient;   // current patient who is logged in
    private Context mContext;   // this activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_patient_main);
        mContext = this;  // save this

        // want this to look like the top activity so disable Up in the Action bar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(false);

        // if the patient isn't in the db starts the sync process to get it immediately
        getPatient();

        // if this is a new instance then check to see if we are doing a check-in right now
        // this decides which fragment will be shown first
        if (savedInstanceState == null) {
            Log.d(LOG_TAG, "Are we doing Checkin? "
                    + (LoginUtility.isCheckin(this) ? "YES" : "NO"));
            if (LoginUtility.isCheckin(this)) {
                getFragmentManager().beginTransaction()
                        .add(R.id.patient_main_container,
                                new PatientPainLogFragment(),
                                PatientPainLogFragment.FRAGMENT_TAG)
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.patient_main_container,
                                new PatientMainFragment(),
                                PatientMainFragment.FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    /**
     * only show certain items in the option menu depending on which fragment is currently
     * be displayed.
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //the history log and reminder fragments don't want the history log in the menu
        Fragment hist_frag = getFragmentManager().findFragmentByTag(HistoryLogFragment.FRAGMENT_TAG);
        Fragment rem_frag = getFragmentManager().findFragmentByTag(ReminderFragment.FRAGMENT_TAG);
        if (hist_frag != null || rem_frag != null) {
            menu.removeItem(R.id.action_patient_history_log);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.patient_main, menu);
        // show the reminder option in the menu
        menu.findItem(R.id.action_settings).setVisible(true);
        return true;
    }

    /**
     * Process the option menu choice
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Reminder option menu was selected start the Reminder fragment
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_main_container,
                            new ReminderFragment(),
                            ReminderFragment.FRAGMENT_TAG)
                    .commit();
            return true;
        }
        // history log option was selected
        else if (id == R.id.action_patient_history_log) {
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
        // user is logging out let the LoginActivity handle this
        else if (id == R.id.patient_logout) {
            LoginActivity.restartLoginActivity(this);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * In the patient app we want to leave the app if back button is press
     * but only if the main fragment is displayed
     */
    @Override
    public void onBackPressed() {
        if (getFragmentManager().findFragmentById(R.id.patient_main_container)
                instanceof PatientMainFragment) {
            startActivity(new Intent()
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME));
        } else super.onBackPressed();
    }

    /**
     * Gets the currently logged in patient data
     * This app is able support multiple patients using the same device
     */
    private void getPatient() {
        // find the logged in patient ID
        if (LoginUtility.isLoggedIn(this)
                && LoginUtility.getUserRole(this) == UserCredential.UserRole.PATIENT) {
            mPatientId = LoginUtility.getLoginId(mContext);
        } else {
            Log.d(LOG_TAG, "UNABLE to get Patient because login properties are " +
                    "not completed or they are not correct.");
        }

        // go to the Content Provider first to see if this patient has already logged in
        // on this device and has data stored locally
        mPatient = null; // clear the patient object because this might be a different patient
        if (mPatientId != null && !mPatientId.isEmpty()) {
            mPatient = PatientDataManager.findPatient(mContext, mPatientId);
        }

        // if there is no locally stored patient information then we need to
        // immediately go to the server and download it
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
     * <p/>
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
                            new PatientMedicationLogFragment(),
                            PatientMedicationLogFragment.FRAGMENT_TAG)
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

    /**
     * Callback
     * if anything needs to be done when the status log has been completed then do it here
     *
     * @return
     */
    @Override
    public boolean onStatusLogComplete() {
        //setLastLoggedTimestamp();
        return true;
    }

    /**
     * Callback
     * if anything needs to be done when a single medication log has been completed then
     * do it here
     *
     * @return
     */
    @Override
    public boolean onMedicationLogComplete() {
        // setLastLoggedTimestamp();
        return true;
    }

    /**
     * Try to keep track of the last login or action that the patient entered .. not
     * required for this project .. has bugs so is not currently implemented
     */
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

    /**
     * Callback to display the medication time taken dialog
     *
     * @param position the position in the medication list
     */
    @Override
    public void onRequestDateTime(int position) {
        FragmentManager fm = getFragmentManager();
        MedicationTimeDialog timeDialog = MedicationTimeDialog.newInstance(position);
        timeDialog.show(fm, MedicationTimeDialog.FRAGMENT_TAG);
    }

    /**
     * Callback when the user saves the medication time taken changes from the dialog
     *
     * @param msTime
     * @param position
     */
    @Override
    public void onPositiveResult(long msTime, int position) {
        PatientMedicationLogFragment frag =
                (PatientMedicationLogFragment) getFragmentManager()
                        .findFragmentById(R.id.patient_main_container);
        frag.updateMedicationLogTimeTaken(msTime, position);
    }

    /**
     * Callback for when the user cancels the medication time taken dialog
     *
     * @param msTime
     * @param position
     */
    @Override
    public void onNegativeResult(long msTime, int position) {
        PatientMedicationLogFragment frag =
                (PatientMedicationLogFragment) getFragmentManager()
                        .findFragmentById(R.id.patient_main_container);
        frag.updateMedicationLogTimeTaken(0L, position);
    }

    /**
     * Callback to display the Add a Reminder Dialog
     *
     * @param reminder
     */
    @Override
    public void onRequestReminderAdd(Reminder reminder) {
        FragmentManager fm = getFragmentManager();
        ReminderAddEditDialog reminderDialog = ReminderAddEditDialog.newInstance(-1, reminder);
        reminderDialog.show(fm, ReminderAddEditDialog.FRAGMENT_TAG);
    }

    /**
     * Callback to display the Edit a Reminder Dialog
     *
     * @param position
     * @param reminder
     */
    @Override
    public void onRequestReminderEdit(int position, Reminder reminder) {
        FragmentManager fm = getFragmentManager();
        ReminderAddEditDialog reminderDialog = ReminderAddEditDialog.newInstance(position, reminder);
        reminderDialog.show(fm, ReminderAddEditDialog.FRAGMENT_TAG);
    }

    /**
     * Callback to process the adding of a reminder from the Add reminder Dialog back to the fragment
     *
     * @param newReminder
     */
    @Override
    public void onReminderAdd(Reminder newReminder) {
        ReminderFragment frag =
                (ReminderFragment) getFragmentManager()
                        .findFragmentById(R.id.patient_main_container);
        frag.addReminder(newReminder);
    }

    /**
     * Callback to update the reminder from the Edit reminder dialog back to the fragment
     *
     * @param position position of the original reminder in the list
     * @param reminder
     */
    @Override
    public void onReminderUpdate(int position, Reminder reminder) {
        ReminderFragment frag =
                (ReminderFragment) getFragmentManager()
                        .findFragmentById(R.id.patient_main_container);
        frag.updateReminder(position, reminder);
    }

    /**
     * Callback from the reminder list adapter
     * Display Confirmation Dialog when the patient request to delete a reminder
     *
     * @param position of the reminder in the reminder list
     * @param reminder
     */
    @Override
    public void onReminderDelete(final int position, Reminder reminder) {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_reminder_delete_title))
                .setMessage(getString(R.string.confirm_reminder_delete_message))
                .setPositiveButton(getString(R.string.answer_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // tell the fragment to delete the reminder
                                ReminderFragment frag =
                                        (ReminderFragment) getFragmentManager()
                                                .findFragmentById(R.id.patient_main_container);
                                frag.deleteReminder(position);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getString(R.string.answer_no),
                        new DialogInterface.OnClickListener() {
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
            ReminderManager.cancelSingleReminderAlarm(this, reminder); // in case it exists
            ReminderManager.setSingleReminderAlarm(this, reminder);
        } else {
            Log.d(LOG_TAG, "deactivating Reminder " + reminder.getName());
            ReminderManager.cancelSingleReminderAlarm(this, reminder);
        }
        ReminderManager.printAlarms(this, LoginUtility.getLoginId(this)); // DEBUG USE ONLY
        PatientDataManager.updateSingleReminder(mContext, mPatientId, reminder);
    }

    /**
     * Callback to Check the Content Provider for a Patient does not go to the server if
     * it is not found
     *
     * @return Patient
     */
    public Patient getPatientCallback() {
        return PatientDataManager.findPatient(mContext, mPatientId); // only check the CP
    }

    /**
     * Callback to obtain the patient information needed to display the history log
     * <p/>
     * Note that the history log processing is used by both the Patient and Physician version
     * of the app and that is why getting the patient is handled in the callbacks.
     * The patient version can access the logs via the content provider and then must
     * put them into a Patient object to work with the history log processing
     * the physician version MUST go to the server for the most current records available and
     * the server already returns the patient object needed no extra processing.
     *
     * @return Patient containing history logs for display
     */
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
