package com.skywomantech.app.symptommanagement.physician;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.concurrent.Callable;

public class PhysicianPatientDetailActivity extends Activity implements
        PrescriptionAdapter.Callbacks,
        PatientMedicationFragment.Callbacks,
        MedicationListFragment.Callbacks,
        MedicationAddEditDialog.Callbacks,
        HistoryLogFragment.Callbacks {

    private static final String LOG_TAG = PhysicianPatientDetailActivity.class.getSimpleName();

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
        getMenuInflater().inflate(R.menu.physician_patient_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Fragment frag =
                getFragmentManager().findFragmentById(R.id.physician_patient_detail_container);
        if(frag instanceof PatientMedicationFragment) {
            menu.removeItem(R.id.action_medication_list);
        } else if (frag instanceof HistoryLogFragment) {
            menu.removeItem(R.id.action_history_log);
        } else if (frag instanceof MedicationListFragment) {
            menu.removeItem(R.id.action_medication_list);
            menu.removeItem(R.id.action_history_log);
        }
        return super.onPrepareOptionsMenu(menu);
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
            PatientMedicationFragment fragment = new PatientMedicationFragment();
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
        } else if (id == R.id.action_sync_alerts) {
            SymptomManagementSyncAdapter.syncImmediately(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrescriptionDelete(int position, Medication medication) {
        PatientMedicationFragment frag =
                (PatientMedicationFragment) getFragmentManager()
                        .findFragmentById(R.id.physician_patient_detail_container);
        frag.deletePrescription(position);
    }

    @Override
    public boolean onRequestPrescriptionAdd() {
        MedicationListFragment fragment = new MedicationListFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.physician_patient_detail_container, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    @Override
    public void onMedicationSelected(Medication medication) {
        // let the detail fragment update the patient's prescription list
        onBackPressed();
        PatientMedicationFragment frag =
                (PatientMedicationFragment) getFragmentManager()
                        .findFragmentById(R.id.physician_patient_detail_container);
        frag.addPrescription(medication);
    }

    @Override
    public boolean showAddMedicationOptionsMenu() {
        // display the menu to add new items
        return true;
    }

    @Override
    public void onAddMedication() {
        Log.d(LOG_TAG, "Displaying Medication Add/Edit Dialog");
        FragmentManager fm = getFragmentManager();
        MedicationAddEditDialog medicationDialog = MedicationAddEditDialog.newInstance(new Medication());
        medicationDialog.show(fm, "med_add_dialog");
    }


    @Override
    public void onSaveMedicationResult(final Medication medication) {
        // no name to work with so we aren't gonna do anything here
        if (medication.getName() == null || medication.getName().isEmpty()) return;

        // we have a name so now we can get some work done
        final SymptomManagementApi svc =  SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Medication>() {

                @Override
                public Medication call() throws Exception {
                    if (medication.getId() == null  || medication.getId().isEmpty()) {
                        Log.d(LOG_TAG, "adding mMedication :" + medication.toDebugString());
                        return svc.addMedication(medication);
                    } else {
                        Log.d(LOG_TAG, "updating mMedication :" + medication.toDebugString());
                        return svc.updateMedication(medication.getId(), medication);
                    }
                }
            }, new TaskCallback<Medication>() {

                @Override
                public void success(Medication result) {
                    Log.d(LOG_TAG, "Medication change was successful.");
                    // if we are still in the medication list view then update the list
                    Fragment fragment = getFragmentManager()
                            .findFragmentById(R.id.physician_patient_detail_container);
                    if (fragment instanceof MedicationListFragment) {
                        ((MedicationListFragment) fragment).refreshAllMedications();
                    }
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "Unable to SAVE Medication. " +
                                    "Please check Internet connection and try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onCancelMedicationResult() {
        Log.d(LOG_TAG, "Add/Edit Medication was cancelled.");
    }

    @Override
    public Patient getPatientForHistory(String id) {
        // force the history log to go to the cloud
        return null;
    }
}
