package com.skywomantech.app.symptommanagement.patient;

import android.app.Activity;
import android.app.FragmentManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.SetPreferenceActivity;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PatientMainActivity extends Activity
        implements
        MedicationLogListAdapter.Callbacks,
        MedicationTimeDialog.Callbacks,
        PatientPainLogFragment.Callbacks,
        ReminderFragment.Callbacks,
        ReminderAddEditDialog.Callbacks,
        ReminderListAdapter.Callbacks {


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
        }
        return super.onOptionsItemSelected(item);
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
        return false;
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
