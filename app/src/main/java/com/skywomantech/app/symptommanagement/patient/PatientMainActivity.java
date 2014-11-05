package com.skywomantech.app.symptommanagement.patient;

import android.app.Activity;
import android.app.FragmentManager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.Login;
import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.SetPreferenceActivity;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.PatientEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.physician.PatientListAdapter;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.concurrent.Callable;


public class PatientMainActivity extends Activity
        implements
        MedicationLogListAdapter.Callbacks,
        MedicationTimeDialog.Callbacks,
        PatientPainLogFragment.Callbacks,
        PatientMedicationLogFragment.Callbacks,
        PatientStatusLogFragment.Callbacks,
        ReminderFragment.Callbacks,
        ReminderAddEditDialog.Callbacks,
        ReminderListAdapter.Callbacks {

    public final static String LOG_TAG = PatientMainActivity.class.getSimpleName();
    private String mPatientId;
    private Patient mPatient;
    private long mLastLogged = System.currentTimeMillis();
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);
        if (savedInstanceState == null) {
            if (isCheckIn()) {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new PatientPainLogFragment())
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new PatientMainFragment())
                        .commit();
            }
        }
        mContext = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.patient_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // set up the reminders
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new ReminderFragment())
                    .addToBackStack(null)
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
            Login.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private Patient getPatientFromCP() {
        mPatientId = Login.getLoginId(this);
        if (mPatientId != null && !mPatientId.isEmpty()) {
            Cursor cursor = getContentResolver()
                    .query(PatientEntry.CONTENT_URI, null, null, null,null);
            if (cursor.moveToFirst()) {
                mPatient = new Patient();
                mPatientId = cursor.getString(cursor.getColumnIndex(PatientEntry._ID));
                mPatient.setId(mPatientId);
                mPatient.setDbId(cursor.getLong(cursor.getColumnIndex(PatientEntry._ID)));
                mPatient.setFirstName(cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_FIRST_NAME)));
                mPatient.setLastName(cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_LAST_NAME)));
                mPatient.setLastLogin(cursor.getLong(cursor.getColumnIndex(PatientEntry.COLUMN_LAST_LOGIN)));
                Log.v(LOG_TAG, "Last Login Originally set to: " + Long.toString(mPatient.getLastLogin()));
                if (mLastLogged > mPatient.getLastLogin()) {
                    Log.v(LOG_TAG, "Last Login RESET to: " + Long.toString(mLastLogged));
                    mPatient.setLastLogin(mLastLogged);
                }
                mPatient.setBirthdate(cursor.getLong(cursor.getColumnIndex(PatientEntry.COLUMN_BIRTHDATE)));
            }
            cursor.close();
        }
        // CP didn't find it so try the internet
        // if found on the internet then save it to C
        if (mPatient == null ) {
            mPatient = getPatientFromCloud();  // this is asynchronous so don't expect immediate response
        }
        return mPatient;
    }

    private Patient getPatientFromCloud() {

        final SymptomManagementApi svc =
                SymptomManagementService.getService();

        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting Patient from Internet");
                    return svc.getPatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "got the Patient!");
                    mPatient = result;
                    mPatientId = mPatient.getId();
                    if (mPatient != null) {
                        Log.v(LOG_TAG, "Last Login set to: " + Long.toString(mLastLogged));
                        mPatient.setLastLogin(mLastLogged);
                        savePatientToCP(mPatient);
                    }
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(mContext,
                            "Unable to fetch the Patient data. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        return null;
    }

    private void savePatientToCP(Patient patient) {
        if (mPatientId != null && !mPatientId.isEmpty()
                &&  patient != null && patient.getDbId() >= 0L) {
            ContentValues cvPatient = PatientCPcvHelper.createValuesObject(mPatientId, patient);
            Uri uri = getContentResolver().insert(PatientEntry.CONTENT_URI, cvPatient);
            long objectId = ContentUris.parseId(uri);
            patient.setDbId(objectId);
            Log.d(LOG_TAG, "New Patient DB Id is : " + Long.toString(objectId));
        }
        else {
            Log.d(LOG_TAG, "Patient is not Saveable.");
        }
    }

    private void updatePatientToCP(Patient patient) {
        if (mPatientId != null && !mPatientId.isEmpty()
                &&  patient != null && patient.getDbId() >= 0L) {
            ContentValues cvPatient = PatientCPcvHelper.createValuesObject(mPatientId, patient);
            String selection = PatientEntry._ID + "=" + Long.toString(patient.getDbId());
            int updated = getContentResolver()
                    .update(PatientEntry.CONTENT_URI, cvPatient, selection, null);
        }
        else {
            Log.d(LOG_TAG, "Patient is not Updateable.");
        }
    }

    // default to no checkin if the preference is not found
    private boolean isCheckIn() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("isCheckin", false);
    }

    public void setCheckIn(boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isCheckin", value);
        editor.apply();
    }

    @Override
    public boolean onPainLogComplete() {
        if (isCheckIn()) {
            // replace fragment with the medication log fragment
            setCheckIn(false);  // we go to the med logs and then back to the regular process
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new PatientMedicationLogFragment())
                    .commit();
            // do we add to backstack here?
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
        mPatient = getPatientFromCP();
        if (mPatient != null) {
            Log.v(LOG_TAG, "Last Login RESET to: " + Long.toString(mLastLogged));
            mPatient.setLastLogin(mLastLogged);
            updatePatientToCP(mPatient);
        }
    }

    // this should be called when the timer goes off
    // timer can set the CheckIn value to be true too
    public void startCheckInProcess() {
        setCheckIn(true);
        getFragmentManager().beginTransaction()
                    .replace(R.id.container, new PatientPainLogFragment())
                    .commit();
        // do we add to backstack here?
    }

    // Handling the Date Time Pickers in a Dialog
    // if the box is checked then the list adapter requests the date and
    // time for the log in "position"
    // when the time and date dialog ends then it tells the
    // activity to update the medication log at position with then
    // time entered or to put 0L for a cancel

    @Override
    public void onRequestDateTime(int position) {
        FragmentManager fm = getFragmentManager();
        MedicationTimeDialog timeDialog = MedicationTimeDialog.newInstance(position);
        timeDialog.show(fm, "med_time_date_dialog");
    }

    @Override
    public void onPositiveResult(long msTime, int position) {
        PatientMedicationLogFragment frag =
                (PatientMedicationLogFragment) getFragmentManager().findFragmentById(R.id.container);
        frag.updateMedicationLogTimeTaken(msTime, position);
    }

    @Override
    public void onNegativeResult(long msTime, int position) {
        PatientMedicationLogFragment frag =
                (PatientMedicationLogFragment) getFragmentManager().findFragmentById(R.id.container);
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
                (ReminderFragment) getFragmentManager().findFragmentById(R.id.container);
        frag.addReminder(newReminder);
    }

    @Override
         public void onReminderUpdate(int position, Reminder reminder) {
        ReminderFragment frag =
                (ReminderFragment) getFragmentManager().findFragmentById(R.id.container);
        frag.updateReminder(position, reminder);
    }

    @Override
    public void onReminderDelete(int position, Reminder reminder) {
        ReminderFragment frag =
                (ReminderFragment) getFragmentManager().findFragmentById(R.id.container);
        frag.deleteReminder(position);
    }


}
