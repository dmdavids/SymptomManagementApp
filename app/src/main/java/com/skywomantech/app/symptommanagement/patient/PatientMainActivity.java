package com.skywomantech.app.symptommanagement.patient;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.data.UserCredential;
import com.skywomantech.app.symptommanagement.physician.HistoryLogFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);
        mContext = this;
        getPatient(); // if the patient isn't in the db kicks off a sync to get it
        if (savedInstanceState == null) {
            Log.d(LOG_TAG, "Are we doing Checkin? "
                    + (LoginUtility.isCheckin(this) ? "YES" : "NO"));
            if (LoginUtility.isCheckin(this)) {
                getFragmentManager().beginTransaction()
                        .add(R.id.patient_main_container, new PatientPainLogFragment())
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.patient_main_container, new PatientMainFragment())
                        .commit();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Fragment frag = getFragmentManager().findFragmentByTag("history_log_frag");
        if (frag != null) {
            Log.e(LOG_TAG, "FOUND HISTORY LOG SO WE ARE REMOVING THE ACTION ITEM.");
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
                    .replace(R.id.patient_main_container, new ReminderFragment())
                    .commit();
            return true;
        }
        else if (id == R.id.action_prefs) {
            startActivity(new Intent(this, SetPreferenceActivity.class));
            return true;
        } else if (id == R.id.action_refresh) {
            SymptomManagementSyncAdapter.syncImmediately(this);
            return true;
        } else if (id == R.id.patient_logout) {
            LoginActivity.restartLoginActivity(this);
        } else if (id == R.id.action_patient_history_log) {
            Bundle arguments = new Bundle();
            if (mPatientId != null && !mPatientId.isEmpty()) {
                arguments.putString(PhysicianPatientDetailFragment.PATIENT_ID_KEY, mPatientId);
            }
            HistoryLogFragment fragment = new HistoryLogFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_main_container, fragment, "history_log_frag")
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO: ?? not sure if this is needed?
    private void getPatient() {
        if ( LoginUtility.isLoggedIn(this)
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
        // TODO: What if no internet?
        if (mPatient == null ) {
            SymptomManagementSyncAdapter.syncImmediately(this);
        }
    }

    @Override
    public boolean onPainLogComplete() {
        if (LoginUtility.isCheckin(this)) {
            // replace fragment with the medication log fragment
            LoginUtility.setCheckin(this, false);  // we go to the med logs now
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_main_container, new PatientMedicationLogFragment())
                    .commit();
            return true;
        }
        setLastLoggedTimestamp();
        return false;
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
        if (mPatient != null ) {
            mPatient.setLastLogin(System.currentTimeMillis());
            PatientDataManager.updateLastLoginFromCP(mContext, mPatient);
        } else {
            Log.d(LOG_TAG, "Could not upate the last login ... no patient found.");
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
    public void onReminderDelete(int position, Reminder reminder) {
        ReminderFragment frag =
                (ReminderFragment) getFragmentManager().findFragmentById(R.id.patient_main_container);
        frag.deleteReminder(position);
    }

    public Patient getPatientCallback() {
        return PatientDataManager.findPatient(mContext, mPatientId); // only check the CP
    }

    @Override
    public Patient getPatientForHistory(String id) {
        // we need a valid patient id
        if ( LoginUtility.isLoggedIn(this)
                && LoginUtility.getUserRole(this) == UserCredential.UserRole.PATIENT) {
            mPatientId = LoginUtility.getLoginId(mContext);
        } else {
            Log.d(LOG_TAG, "UNABLE to get Patient because login properties are " +
                    "not completed or they are not correct.");
            return null;
        }
        // create an new empty patient and just load the logs into it
        Patient p = new Patient();
        p.setId(id);
        PatientDataManager.getLogsFromCP(this, p);
        return p;
    }
}
