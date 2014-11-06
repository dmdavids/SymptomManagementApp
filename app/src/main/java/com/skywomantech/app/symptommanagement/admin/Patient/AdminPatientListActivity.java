package com.skywomantech.app.symptommanagement.admin.Patient;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;


public class AdminPatientListActivity extends Activity
        implements PatientListFragment.Callbacks {

    public final String LOG_TAG = AdminPatientListActivity.class.getSimpleName();

    public static final String PATIENT_ID_KEY = "patient_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_patient_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method from {@link PatientListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onPatientSelected(String id) {
        Log.d(LOG_TAG, "Saving Patient ID: " + id);
        Intent detailIntent = new Intent(this, AdminPatientDetailActivity.class);
        detailIntent.putExtra(PATIENT_ID_KEY, id);
        startActivity(detailIntent);
    }

    @Override
    public void onAddPatient() {
        Log.d(LOG_TAG, "Changing to Add/Edit Fragment");
        // In single-pane mode, simply start the detail activity
        // for the selected item ID.
        Intent detailIntent = new Intent(this, AdminPatientDetailActivity.class);
        startActivity(detailIntent);
    }
}
