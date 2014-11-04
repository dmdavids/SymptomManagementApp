package com.skywomantech.app.symptommanagement.admin.Patient;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.physician.PatientMedicationFragment;

import java.util.HashSet;
import java.util.Set;


/**
 * An activity representing a single AdminPatient detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link AdminPatientListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link PatientDetailFragment}.
 */
public class AdminPatientDetailActivity extends Activity
            implements PatientDetailFragment.Callbacks,
        BirthdateDialog.Callbacks {
    private static final String LOG_TAG = AdminPatientDetailActivity.class.getSimpleName();
    public final static String PATIENT_ID_KEY = AdminPatientListActivity.PATIENT_ID_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_patient_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            Bundle arguments = new Bundle();
            String id = getIntent().getStringExtra(PATIENT_ID_KEY);
            Log.d(LOG_TAG, "Patient ID Key is : " + id);
            Fragment fragment;
            if(id != null) {
                arguments.putString(PATIENT_ID_KEY, id);
                fragment = new PatientDetailFragment();
            } else {
                fragment = new PatientAddEditFragment();
            }
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.adminpatient_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, AdminPatientListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditPatient(String id) {
        // switch out the fragments
        Bundle arguments = new Bundle();
        arguments.putString(PATIENT_ID_KEY, id);
        PatientAddEditFragment fragment = new PatientAddEditFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .replace(R.id.adminpatient_detail_container, fragment)
                .commit();
    }

    @Override
    public void onPositiveResult(long time) {
        PatientAddEditFragment frag =
                (PatientAddEditFragment) getFragmentManager()
                        .findFragmentById(R.id.adminpatient_detail_container);
        frag.onPositiveResult(time);
    }

    @Override
    public void onNegativeResult() {
        PatientAddEditFragment frag =
                (PatientAddEditFragment) getFragmentManager()
                        .findFragmentById(R.id.adminpatient_detail_container);
        frag.onNegativeResult();
    }
}
