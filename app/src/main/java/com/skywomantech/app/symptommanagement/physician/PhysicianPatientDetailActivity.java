package com.skywomantech.app.symptommanagement.physician;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.data.StatusLog;
import com.skywomantech.app.symptommanagement.patient.PatientMainFragment;

import java.util.concurrent.Callable;

/**
 * This activity is called to handle this information when the device cannot handle two-pane
 * on the screen size.
 *
 * This task is a holder of the physician and patient information that the fragments need to
 * do their processing.
 *
 * This activity runs after physician is logged in and has selected a patient from a patient list
 * So this activity expects a physician id and patient id are available for its use.
 *
 * This activity manages the patient option menu and two fragments including
 * the patient details fragment which shows patient information
 * and then the patient graphics fragment which displays graphs, history logs and medication data
 * Each fragment that it manages expects this activity to give them the patient information
 *
 * The details fragment is the one that finds the patient and then gives it to this activity
 * The graphic fragment switches between 3 possible fragments : history, prescription & graphs
 *
 */
public class PhysicianPatientDetailActivity extends Activity implements
        PhysicianPatientDetailFragment.Callbacks,
        PrescriptionAdapter.Callbacks,
        PatientMedicationFragment.Callbacks,
        MedicationListFragment.Callbacks,
        MedicationAddEditDialog.Callbacks,
        HistoryLogFragment.Callbacks,
        PatientGraphicsFragment.Callbacks,
        PhysicianManager.Callbacks,
        PatientManager.Callbacks {

    private static final String LOG_TAG = PhysicianPatientDetailActivity.class.getSimpleName();

    private static String PHYSICIAN_ID_KEY;
    private static String PATIENT_ID_KEY;

    private static String mPhysicianId;
    private static Physician mPhysician = new Physician();

    private static String mPatientId;
    private static Patient mPatient = new Patient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physicianpatient_detail);

        PHYSICIAN_ID_KEY = getString(R.string.physician_id_key);
        PATIENT_ID_KEY = getString(R.string.patient_id_key);

        mPhysicianId = getIntent().getStringExtra(PHYSICIAN_ID_KEY);
        if (mPhysicianId == null)
            Log.e(LOG_TAG, "This activity should not have been started without the DOCTOR's id!!");

        mPatientId = getIntent().getStringExtra(PATIENT_ID_KEY);
        if (mPatientId == null)
            Log.e(LOG_TAG, "This activity should not have been started without the PATIENT id!!");

        if (savedInstanceState != null ) { // restart
            if (mPhysicianId == null)
                mPhysicianId = savedInstanceState.getString(PHYSICIAN_ID_KEY);
            if(mPatientId == null)
                mPatientId = savedInstanceState.getString(PATIENT_ID_KEY);
        }
        else {  // started new by adding the new fragments
            Log.v(LOG_TAG, "for this case the fragments are working with empty objects.");
            getFragmentManager().beginTransaction()
                    .add(R.id.physician_patient_detail_container,
                            new PhysicianPatientDetailFragment(),
                            PhysicianPatientDetailFragment.FRAGMENT_TAG)
                    .commit();
            // put the history log in the graphics fragment for starters
            getFragmentManager().beginTransaction()
                    .add(R.id.patient_graphics_container,
                            new HistoryLogFragment(), HistoryLogFragment.FRAGMENT_TAG)
                    .commit();
        }
        // get physician and patient objects from server
        PhysicianManager.getPhysician(this, mPhysicianId );
        PatientManager.getPatient(this, mPatientId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.physician_patient_detail_menu, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPatientId != null)   outState.putString(PATIENT_ID_KEY, mPatientId);
        if (mPhysicianId != null) outState.putString(PHYSICIAN_ID_KEY, mPhysicianId);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Fragment frag =
                getFragmentManager().findFragmentById(R.id.patient_graphics_container);
        if (frag instanceof PatientMedicationFragment) {
            menu.removeItem(R.id.action_medication_list);
        } else if (frag instanceof HistoryLogFragment) {
            menu.removeItem(R.id.action_history_log);
        } else if (frag instanceof MedicationListFragment) {
            menu.removeItem(R.id.action_medication_list);
            menu.removeItem(R.id.action_history_log);
        } else if (frag instanceof PatientGraphicsFragment) {
            menu.removeItem(R.id.action_chart);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_medication_list) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container,
                            new PatientMedicationFragment(), PatientMedicationFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_history_log) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container,
                            new HistoryLogFragment(), HistoryLogFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_chart) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container,
                            new PatientGraphicsFragment(), HistoryLogFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            return true;
        }  else if (id == R.id.physician_logout) {
            LoginActivity.restartLoginActivity(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setPhysician(Physician physician) {
        if (physician == null) {
            Log.e(LOG_TAG, "Trying to set physician to null value");
            return;
        }
        Log.d(LOG_TAG, "Current Selected Physician is : " + physician.toString());
        mPhysician = physician;
    }

    @Override
    public void setPatient(Patient patient) {
        if (patient == null) {
            Log.e(LOG_TAG, "Trying to set patient to null value");
            return;
        }
        Log.d(LOG_TAG, "Current Selected Patient is : " + patient.toString());
        mPatient = patient;
    }

    @Override
    public Patient getPatient() {
        return mPatient;
    }


    @Override
    public Patient getPatientDataForGraphing() {
        Log.d(LOG_TAG, "GETTING Selected Patient for Graphing : " + mPatient);
        return mPatient;
    }

    @Override
    public Patient getPatientForHistory(String id) {
        // don't do a lookup with the id, just return the current patient
        return mPatient;
    }

    /**
     * Find the patient in the doctor's list and add a status log with the doctor's note.
     *
     * @param patientId patient that the status is for
     * @param statusLog note to add to the status log
     */
    @Override
    public void onPatientContacted(String patientId, StatusLog statusLog) {
        if (mPhysician == null || mPhysician.getPatients() == null
                || patientId == null || patientId.isEmpty()) {
            Log.e(LOG_TAG, "INVALID IDS -- Unable to update the Dr.'s Status Log");
            return;
        }
        if (PhysicianManager.attachPhysicianStatusLog(mPhysician, patientId, statusLog)) {
            PhysicianManager.savePhysician(this, mPhysician);
        }
    }

    @Override
    public void onPrescriptionDelete(final int position, Medication medication) {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Do you want to delete this prescription?")
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PatientMedicationFragment frag =
                                (PatientMedicationFragment) getFragmentManager()
                                        .findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
                        frag.deletePrescription(position);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.answer_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alert.show();
    }

    @Override
    public boolean onRequestPrescriptionAdd() {
        MedicationListFragment fragment = new MedicationListFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_graphics_container, fragment, MedicationListFragment.FRAGMENT_TAG)
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
                        .findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
        frag.addPrescription(medication);
    }

    @Override
    public boolean showAddMedicationOptionsMenu() {
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
        final SymptomManagementApi svc = SymptomManagementService.getService();
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
                            .findFragmentByTag(MedicationListFragment.FRAGMENT_TAG);
                    if (fragment != null && fragment instanceof MedicationListFragment) {
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
}
