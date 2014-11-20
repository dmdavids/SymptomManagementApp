package com.skywomantech.app.symptommanagement.physician;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

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
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.data.StatusLog;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;

public class PhysicianListPatientsActivity extends Activity  implements
        PhysicianPatientDetailFragment.Callbacks,
        PrescriptionAdapter.Callbacks,
        PatientMedicationFragment.Callbacks,
        MedicationListFragment.Callbacks,
        MedicationAddEditDialog.Callbacks,
        HistoryLogFragment.Callbacks,
        PatientGraphicsFragment.Callbacks,
        PhysicianManager.Callbacks,
        PatientManager.Callbacks,
        MedicationManager.Callbacks,
        PatientSearchDialog.Callbacks {

    public final static String LOG_TAG = PhysicianListPatientsActivity.class.getSimpleName();

    private static String PHYSICIAN_ID_KEY;
    private static String PATIENT_ID_KEY;

    private static String mPhysicianId;
    private static Physician mPhysician = new Physician();

    private static String mPatientId;
    private static Patient mPatient = new Patient();

    private static Collection<Medication> mMedications = new HashSet<Medication>();

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PHYSICIAN_ID_KEY = getString(R.string.physician_id_key);
        PATIENT_ID_KEY = getString(R.string.patient_id_key);

        // once the physician is logged in then this is the top level so
        // so it is considered home and we need to negate the actionbar arrow
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(false);

        // check to see if we were handed any of this information to work with from login.
        mPatientId = null;  // we should not have a patient id yet when starting this activity.

        // we should have an doctor id from the login process
        mPhysicianId = getIntent().getStringExtra(PHYSICIAN_ID_KEY);
        mPhysician = null;
        PhysicianManager.getPhysician(this, mPhysicianId);

        // so this is how we figure out if we are using a 2-pane layout or not... if the
        // patient detail container exists in the layout then we are two pane so set flag
        mTwoPane = false;
        setContentView(R.layout.activity_physician_patient_list);
        if (findViewById(R.id.physician_patient_detail_container) != null) {
            mTwoPane = true;
            // setup list item highlight when selected
            ((PhysicianListPatientsFragment) getFragmentManager()
                    .findFragmentById(R.id.physician_patient_list))
                    .setActivateOnItemClick(true);
            // if this is a new instance of this activity in 2-pane mode then
            // we need to activate the details fragment and the graphics fragment
            if (savedInstanceState == null) {
                // create a bundle with the information we have and send to both fragments
                Bundle arguments = new Bundle();
                arguments.putString(PHYSICIAN_ID_KEY, mPhysicianId);

                // this is the patient details window that shows at the top
                PhysicianPatientDetailFragment fragment = new PhysicianPatientDetailFragment();
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .add(R.id.physician_patient_detail_container, fragment)
                        .commit();

                // start by showing the history log in the graphics fragment for now
                HistoryLogFragment graphicsFragment = new HistoryLogFragment();
                graphicsFragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.patient_graphics_container, graphicsFragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTwoPane) {
            getMenuInflater().inflate(R.menu.physician_patient_twopane_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.physician_patient_list_menu, menu);
        }
        return true;
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
        if (id == R.id.action_patient_search) {
            Log.d(LOG_TAG, "Displaying Patient Search Dialog");
            FragmentManager fm = getFragmentManager();
            PatientSearchDialog searchDialog = PatientSearchDialog.newInstance();
            searchDialog.show(fm, "patient_search_dialog");
        } else if (id == R.id.action_sync_alerts) {
            SymptomManagementSyncAdapter.syncImmediately(this);
            return true;
        }  else if (id == R.id.action_medication_list) {
            Bundle arguments = new Bundle();
            arguments.putString(PATIENT_ID_KEY, mPatientId);
            arguments.putString(PHYSICIAN_ID_KEY, mPhysicianId);
            PatientMedicationFragment fragment = new PatientMedicationFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_history_log) {
            Bundle arguments = new Bundle();
            arguments.putString(PATIENT_ID_KEY, mPatientId);
            arguments.putString(PHYSICIAN_ID_KEY, mPhysicianId);
            HistoryLogFragment fragment = new HistoryLogFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_chart) {
            Bundle arguments = new Bundle();
            arguments.putString(PATIENT_ID_KEY, mPatientId);
            arguments.putString(PHYSICIAN_ID_KEY, mPhysicianId);
            PatientGraphicsFragment fragment = new PatientGraphicsFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.physician_logout) {
            LoginActivity.restartLoginActivity(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent()
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME));
    }

    public void onItemSelected(String physicianId, String patientId) {
        mPatientId = patientId;
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(PATIENT_ID_KEY, patientId);
            arguments.putString(PHYSICIAN_ID_KEY, physicianId);
            PhysicianPatientDetailFragment fragment = new PhysicianPatientDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.physician_patient_detail_container, fragment)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, PhysicianPatientDetailActivity.class);
            detailIntent.putExtra(PATIENT_ID_KEY, patientId);
            detailIntent.putExtra(PHYSICIAN_ID_KEY, physicianId);
            startActivity(detailIntent);
        }
    }

    /**
     * Callback from the Patient Search Dialog.  It uses the entered name to do a
     * search by name ON the server side.  The name must be an exact match.
     *
     * @param lastName
     * @param firstName
     */
    @Override
    public void onNameSelected(String lastName, String firstName) {
        Log.e(LOG_TAG, "THE NAME SELECTED IS : " + firstName + " " + lastName);
        findByNameFromCloud(lastName, firstName);
    }

    private Patient findByNameFromCloud(final String last, final String first) {
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<Patient>>() {

                @Override
                public Collection<Patient> call() throws Exception {
                    Log.d(LOG_TAG, "Searching for last name : " + last);
                    return svc.findByPatientLastName(last);
                }
            }, new TaskCallback<Collection<Patient>>() {

                @Override
                public void success(Collection<Patient> result) {
                    // check for first name match
                    String patientId = null;
                    for (Patient p : result) {
                        Log.d(LOG_TAG, "Checking patient first name for match : " + p.getFirstName());
                        if (p.getFirstName().toLowerCase().contentEquals(first.toLowerCase())) {
                            // found a match on both first and last
                            patientId = p.getId();
                        }
                    }
                    if (patientId == null) {
                        Toast.makeText(getApplicationContext(),
                                "There are no patients with the name " + first + " " + last + ".",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Intent detailIntent = new Intent(getApplicationContext(),
                                PhysicianPatientDetailActivity.class);
                        if (mTwoPane) {
                            Bundle arguments = new Bundle();
                            arguments.putString(PATIENT_ID_KEY, patientId);
                            PhysicianPatientDetailFragment fragment = new PhysicianPatientDetailFragment();
                            fragment.setArguments(arguments);
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.physician_patient_detail_container, fragment)
                                    .commit();
                        } else {
                            detailIntent.putExtra(PATIENT_ID_KEY, patientId);
                            startActivity(detailIntent);
                        }
                    }
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "There are no patients with the last name " + last + ".",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        return null;
    }

    /**
     *  Called by the Physician Manager when it gets a Physician from the server
     *
     * @param physician from server
     */
    @Override
    public void setPhysician(Physician physician) {
        if (physician == null) {
            Log.e(LOG_TAG, "Trying to set physician to null value");
            return;
        }
        Log.d(LOG_TAG, "Current Selected Physician is : " + physician.toString());
        mPhysician = physician;
    }

    /**
     *  Called by the Patient Manager when it gets a patient from the server
     *
     * @param patient from server
     */
    @Override
    public void setPatient(Patient patient) {
        if (patient == null) {
            Log.e(LOG_TAG, "Trying to set patient to null value");
            return;
        }
        Log.d(LOG_TAG, "Current Selected Patient is : " + patient.toString());
        mPatient = patient;
        updatePatientForFragments(mPatient);
    }


    /**
     * If the patient object was received from the server then we need to tell all the fragments
     * about the new patient so they can be updated appropriately
     *
     * @param patient from server for update to fragments
     */
    private void updatePatientForFragments(Patient patient) {
        // try the details fragment first
        Fragment frag;
        frag = getFragmentManager().findFragmentByTag(PhysicianPatientDetailFragment.FRAGMENT_TAG);
        if (frag != null) {
            ((PhysicianPatientDetailFragment) frag).updatePatient(patient);
        }

        // now the history log fragment
        frag = getFragmentManager().findFragmentByTag(HistoryLogFragment.FRAGMENT_TAG);
        if (frag != null) {
            ((HistoryLogFragment) frag).updatePatient(patient);
        }

        frag = getFragmentManager().findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
        if (frag != null) {
            ((PatientMedicationFragment) frag).updatePatient(patient);
        }
    }

    /**
     * Callback for the Details Fragment to request a patient
     *
     * @return Patient
     */
    @Override
    public Patient getPatientForDetails() {
        Log.d(LOG_TAG, "GETTING Selected Patient for Details : " + mPatient);
        return mPatient;
    }

    /**
     * Callback for the Graphing Fragment to request Patient data
     *
     * @return Patient
     */
    @Override
    public Patient getPatientDataForGraphing() {
        Log.d(LOG_TAG, "GETTING Selected Patient for Graphing : " + mPatient);
        return mPatient;
    }

    /**
     * Callback for the patient history log to obtain a patient to use
     *
     * @return Patient
     */
    @Override
    public Patient getPatientForHistory() {
        Log.d(LOG_TAG, "GETTING Selected Patient for History Log : " + mPatient);
        return mPatient;
    }

    /**
     * Callback for the Medication List fragment to obtain a patient to use
     *
     * @return Patient
     */
    @Override
    public Patient getPatientForPrescriptions() {
        Log.d(LOG_TAG, "GETTING Selected Patient for Prescriptions : " + mPatient);
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

    /**
     * Called from the prescription adapter when the delete icon for a prescription has been
     * clicked.  This confirms the delete and then tells the fragment that it needs to update
     * its patient's list and save the updated patient to the server.
     *
     * @param position
     * @param medication
     */
    @Override
    public void onPrescriptionDelete(final int position, Medication medication) {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_delete_prescription))
                .setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PatientMedicationFragment frag =
                                (PatientMedicationFragment) getFragmentManager()
                                        .findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
                        if (frag != null) {
                            frag.deletePrescription(position);
                        } else {
                            Log.e(LOG_TAG, "Bad error .. could not find the medication fragment!");
                        }
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

    /**
     * Called from Prescription fragment when choosing the item from the options menu when the patient
     * prescription fragment is activated.  This brings up the complete list of medications
     * from the server to allow the physician to choose one.
     *
     * @return
     */
    @Override
    public void onRequestPrescriptionAdd() {
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_graphics_container,
                        new MedicationListFragment(), MedicationListFragment.FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Callback from the prescription fragment to add a prescription that was chosen
     * from the whole medication list
     *
     * @param medication selected medication to add as a prescription
     */
    @Override
    public void onMedicationSelected(Medication medication) {
        // let the detail fragment update the patient's prescription list
        onBackPressed();
        PatientMedicationFragment frag =
                (PatientMedicationFragment) getFragmentManager()
                        .findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
        frag.addPrescription(medication);
    }

    /**
     * tell the medication list fragment if editing options are activated or not
     *
     * @return boolean true is show option menu
     */
    @Override
    public boolean showAddMedicationOptionsMenu() {
        Log.d(LOG_TAG, "Detail Activity is showing the add medication options menu.");
        return true;
    }

    /**
     * Callback for the Medication List fragment to get the list of medications that it needs to
     * display
     *
     * @return Collection of Medications
     */
    @Override
    public Collection<Medication> getMedications() {
        return mMedications;
    }

    /**
     * Callback for the Medication List fragment if the add medication was chosen from options
     * Displays a custom dialog fragment for adding a new medication
     */
    @Override
    public void onAddMedication() {
        FragmentManager fm = getFragmentManager();
        MedicationAddEditDialog medicationDialog = MedicationAddEditDialog.newInstance(new Medication());
        medicationDialog.show(fm, MedicationAddEditDialog.FRAGMENT_TAG);
    }

    /**
     * Callback for the Medication Add Edit Dialog
     * The user OK'd the new medication add so we need to add it to the server database
     * and then get the full list back from the database
     *
     * @param medication
     */
    @Override
    public void onSaveMedicationResult(final Medication medication) {
        // no name to work with so we aren't gonna do anything here
        if (medication.getName() == null || medication.getName().isEmpty()) {
            Log.d(LOG_TAG, "The user didn't really put a valid name so we aren't doing anything.");
            return;
        }
        MedicationManager.saveMedication(this, medication);
    }

    /**
     * Callback from the Medication Add Edit Dialog .. do nothing if it was cancelled
     */
    @Override
    public void onCancelMedicationResult() {
        Log.d(LOG_TAG, "Add/Edit Medication was cancelled.");
    }

    /**
     *  Callback from the Medication Manager when it obtains a list of all the medications.
     *  Saves the list.
     *  Then call the medication list fragment to pass it the updated list
     *
     * @param medications Collection<Medication>
     */
    @Override
    public void setMedicationList(Collection<Medication> medications) {
        mMedications = medications;
        // call the medication list fragment to update the medication list there
        Fragment frag;
        frag = getFragmentManager().findFragmentByTag(MedicationListFragment.FRAGMENT_TAG);
        if (frag != null) {
            ((MedicationListFragment) frag).updateMedications(medications);
        }
    }

    @Override
    public void failedSearch(String message) {
        Toast.makeText(getApplication(),message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void successfulSearch(Patient patient) {
        Intent detailIntent = new Intent(getApplication(), PhysicianPatientDetailActivity.class);
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(PATIENT_ID_KEY, patient.getId());
            PhysicianPatientDetailFragment fragment = new PhysicianPatientDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.physician_patient_detail_container, fragment,
                            PhysicianPatientDetailFragment.FRAGMENT_TAG)
                    .commit();

            getFragmentManager().beginTransaction()
                    .replace(R.id.physician_patient_detail_container,
                            new PhysicianPatientDetailFragment(),
                            PhysicianPatientDetailFragment.FRAGMENT_TAG)
                    .commit();
        } else {
            detailIntent.putExtra(PATIENT_ID_KEY, patient.getId());
            startActivity(detailIntent);
        }
    }

}
