package com.skywomantech.app.symptommanagement.physician;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.view.Menu;
import android.view.MenuItem;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.SetPreferenceActivity;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.patient.PatientPainLogFragment;
import com.skywomantech.app.symptommanagement.patient.ReminderFragment;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

/**
 * An activity representing a single PhysicianPatient detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PhysicianListPatientsActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link PhysicianPatientDetailFragment}.
 */
public class PhysicianPatientDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physicianpatient_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(PhysicianPatientDetailFragment.PATIENT_ID_KEY,
                    getIntent().getStringExtra(PhysicianPatientDetailFragment.PATIENT_ID_KEY));
            PhysicianPatientDetailFragment fragment = new PhysicianPatientDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.physician_patient_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.physician_patient_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, PhysicianListPatientsActivity.class));
            return true;
        } else if (id == R.id.action_medication_list) {
            Bundle arguments = new Bundle();
            arguments.putString(PhysicianPatientDetailFragment.PATIENT_ID_KEY,
                    getIntent().getStringExtra(PhysicianPatientDetailFragment.PATIENT_ID_KEY));
            PhysicianPatientDetailFragment fragment = new PhysicianPatientDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.physician_patient_detail_container, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_history_log) {
            Bundle arguments = new Bundle();
            arguments.putString(PhysicianPatientDetailFragment.PATIENT_ID_KEY,
                    getIntent().getStringExtra(PhysicianPatientDetailFragment.PATIENT_ID_KEY));
            HistoryLogFragment fragment = new HistoryLogFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.physician_patient_detail_container, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
