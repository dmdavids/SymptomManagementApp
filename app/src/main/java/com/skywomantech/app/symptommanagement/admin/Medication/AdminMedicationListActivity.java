package com.skywomantech.app.symptommanagement.admin.Medication;

import android.app.Fragment;
import android.app.FragmentManager;

import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.physician.MedicationAddEditDialog;
import com.skywomantech.app.symptommanagement.physician.MedicationListFragment;

import java.util.concurrent.Callable;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;


public class AdminMedicationListActivity extends Activity
        implements
        MedicationListFragment.Callbacks,
        MedicationAddEditDialog.Callbacks {

    public final String LOG_TAG = AdminMedicationListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_medication_list);
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


    @Override
    public void onMedicationSelected(Medication medication) {
        Log.d(LOG_TAG, "Displaying Medication Edit Dialog for medication : "
                + medication.toDebugString());
        FragmentManager fm = getFragmentManager();
        MedicationAddEditDialog medicationDialog = MedicationAddEditDialog.newInstance(medication);
        medicationDialog.show(fm, "med_edit_dialog");
    }

    @Override
    public boolean showAddMedicationOptionsMenu() {
        return true;
    }

    @Override
    public void onAddMedication() {
        Log.d(LOG_TAG, "Displaying Medication Add Dialog");
        FragmentManager fm = getFragmentManager();
        MedicationAddEditDialog medicationDialog = MedicationAddEditDialog.newInstance(new Medication());
        medicationDialog.show(fm, "med_add_dialog");
    }

    @Override
    public void onSaveMedicationResult(final Medication medication) {
        // no name to work with so we aren't gonna do anything here
        if (medication.getName() == null || medication.getName().isEmpty()) return;

        // we have a name so now we can get some work done
        final SymptomManagementApi svc =
                SymptomManagementService.getService();

        if (svc != null) {
            CallableTask.invoke(new Callable<Medication>() {

                @Override
                public Medication call() throws Exception {
                    if (medication.getId() == null || medication.getId().isEmpty()) {
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
                            .findFragmentById(R.id.adminmedication_list);
                    if (fragment instanceof MedicationListFragment) {
                        // refreshing medications
                        ((MedicationListFragment) fragment).refreshAllMedications();
                    }

                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "Unable to SAVE Medication. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onCancelMedicationResult() {
        Log.d(LOG_TAG, "Add/Edit Medication was cancelled.");
    }
}
