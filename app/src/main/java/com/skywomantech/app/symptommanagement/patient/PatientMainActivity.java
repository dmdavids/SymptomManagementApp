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

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.PatientEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.data.UserCredential;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.concurrent.Callable;


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
        ReminderListAdapter.Callbacks {

    public final static String LOG_TAG = PatientMainActivity.class.getSimpleName();
    private String mPatientId;  // cloud login db id
    private static Patient mPatient;
    private long mLastLogged = System.currentTimeMillis();
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);
        mContext = this;
        getPatient(); // may need to go to the cloud here
        if (savedInstanceState == null) {
            if (LoginUtility.isCheckin(this)) {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new PatientPainLogFragment())
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new PatientMainFragment())
                        .commit();
            }
        }
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
                    .replace(R.id.container, new ReminderFragment())
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
            LoginUtility.logout(this);
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private Patient getPatient() {
        if ( LoginUtility.isLoggedIn(this)
                && LoginUtility.getUserRole(this) == UserCredential.UserRole.PATIENT) {
            mPatientId = LoginUtility.getLoginId(mContext);
        } else {
            Log.d(LOG_TAG, "UNABLE to get Patient because login properties are " +
                    "not completed or they are not correct.");
            return null;
        }

        Log.d(LOG_TAG, "Attempting to get PATIENT from CP with id : " + mPatientId);
        mPatient = null;
        if (mPatientId != null && !mPatientId.isEmpty()) {
            mPatient = getPatientFromCP(mPatientId);
        }
        // CP didn't find it so try the internet
        // if found on the internet then save it to C
        if (mPatient == null ) {
            Log.d(LOG_TAG, "INSTEAD we are Getting PATIENT from CLOUD with id : " + mPatientId);
            getPatientFromCloud(mPatientId);  // this is asynchronous so don't expect immediate response
        }
        return mPatient;
    }

    public Patient getPatientFromCP(String id) {
        Log.d(LOG_TAG, "THIS IS THE CP attempt to get the Patient information.");
        Patient patient = new Patient();
        patient.setId(id); // cloud id
        String selection = PatientEntry.COLUMN_PATIENT_ID + "=" + "\'"  + id + "\'";
        Cursor cursor = getContentResolver()
                .query(PatientEntry.CONTENT_URI, null, selection, null,null); // where cloud id = mPatientId
        if (cursor.getCount() > 1) {
            Log.d(LOG_TAG, "There are multiple entries for the same DB patient! id : " + id);
        }
        if (cursor.moveToFirst()) {

            patient.setDbId(cursor.getLong(cursor.getColumnIndex(PatientEntry._ID))); // local CP id
            patient.setFirstName(cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_FIRST_NAME)));
            patient.setLastName(cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_LAST_NAME)));
            patient.setLastLogin(cursor.getLong(cursor.getColumnIndex(PatientEntry.COLUMN_LAST_LOGIN)));
            Log.v(LOG_TAG, "Last Login Originally set to: " + Long.toString(patient.getLastLogin()));

//            if (mLastLogged > patient.getLastLogin()) {
//                Log.v(LOG_TAG, "Last Login RESET to: " + Long.toString(mLastLogged));
//                if(mPatient != null)  mPatient.setLastLogin(mLastLogged);
//            }
            patient.setBirthdate(cursor.getString(cursor.getColumnIndex(PatientEntry.COLUMN_BIRTHDATE)));
            Log.d(LOG_TAG, "Working with this PATIENT\n\tid : " + id
                    + "\n\trecord : " + patient.toDebugString());
        }
        cursor.close();
        Log.d(LOG_TAG, "Found Patient in CP : " + patient.toDebugString());
        // something went wrong and a bad record is inserted.
        // TODO: I am assuming we have both first and last name ... not great
        if (patient.getId() == null || patient.getId().isEmpty()
                || patient.getFirstName() == null || patient.getFirstName().isEmpty()
                || patient.getLastName() == null || patient.getLastName().isEmpty())
            return null;
        return patient;
    }

    private Patient getPatientFromCloud(final String id) {

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting Patient from Internet");
                    mPatient = null;
                    return svc.getPatient(id);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "got the Patient!");
                    mPatient = result;
                    if (mPatient != null) {
//                        Log.v(LOG_TAG, "Last Login set to: " + Long.toString(mLastLogged));
//                        mPatient.setLastLogin(mLastLogged);
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
        if (mPatientId != null && !mPatientId.isEmpty() &&  patient != null) {
            Log.d(LOG_TAG, "INSERTING to CP with this PATIENT id : " + mPatientId
                    + " record : " + patient.toDebugString());
            // test to see if it is already in the database
            String selection = PatientEntry.COLUMN_PATIENT_ID + "=" + "\'"  + mPatientId + "\'";
            Cursor cursor = getContentResolver()
                    .query(PatientEntry.CONTENT_URI, null, selection, null,null); // where cloud id = mPatientId
            if (cursor.getCount() > 0) {
                Log.d(LOG_TAG, "UPDATING because This Patient Id already exists in the CP : " + mPatientId);
                updatePatientToCP(patient);
            } else {
                // its a new patient in the CP so we can go ahead and insert
                ContentValues cvPatient = PatientCPcvHelper.createInsertValuesObject(mPatientId, patient);
                Uri uri = getContentResolver().insert(PatientEntry.CONTENT_URI, cvPatient);
                long objectId = ContentUris.parseId(uri);
                patient.setDbId(objectId);  // set the local CP ID
                Log.d(LOG_TAG, "New Patient DB Id is : " + Long.toString(objectId));
            }
            cursor.close();
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
            Log.d(LOG_TAG, "Updated Patients : " + Integer.toString(updated));
        }
        else {
            Log.d(LOG_TAG, "Patient is not Updateable.");
        }
    }

    @Override
    public boolean onPainLogComplete() {
        if (LoginUtility.isCheckin(this)) {
            // replace fragment with the medication log fragment
            LoginUtility.setCheckin(this, false);  // we go to the med logs and then back to the regular process
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
        // TODO: what the heck?
        getPatient(); // may need to go to cloud don't expect immediate response
//        if (mPatient != null) {
//            Log.v(LOG_TAG, "Last Login RESET to: " + Long.toString(mLastLogged));
//            mPatient.setLastLogin(mLastLogged);
//            updatePatientToCP(mPatient);
//        }
    }

    // this should be called when the timer goes off
    // timer can set the CheckIn value to be true too
    public void startCheckInProcess() {
        LoginUtility.setCheckin(this, true);
        getFragmentManager().beginTransaction()
                    .replace(R.id.container, new PatientPainLogFragment())
                    .commit();
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


    public Patient getPatientCallback() {
        return getPatientFromCP(mPatientId);
    }
}
