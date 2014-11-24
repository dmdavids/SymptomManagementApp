package com.skywomantech.app.symptommanagement.physician;


import android.app.ActionBar;
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
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.data.StatusLog;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * This abstract class provides many of the methods and fields that are common to the details
 * activity and the patient list activity when it is in dual pane mode.
 *
 * Should never be instantiated though.
 *
 * Many of these methods are callbacks for the fragments that are duplicated otherwise.
 *
 * This task is a holder of the physician and patient information that the fragments need to
 * do their processing.
 *
 *
 */
public abstract class PhysicianActivity
        extends     Activity
        implements
                    PhysicianListPatientsFragment.Callbacks,
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

    private static final String LOG_TAG = PhysicianActivity.class.getSimpleName();

    protected static String PHYSICIAN_ID_KEY;
    protected static String PATIENT_ID_KEY;

    protected static String mPhysicianId;
    protected static Physician mPhysician = new Physician();

    protected static String mPatientId;
    protected static Patient mPatient = new Patient();

    protected static Collection<Medication> mMedications = new HashSet<Medication>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(false);

        PHYSICIAN_ID_KEY = getString(R.string.physician_id_key);
        PATIENT_ID_KEY = getString(R.string.patient_id_key);

        // we should have a physician from the login process for both activities
        mPhysicianId = getIntent().getStringExtra(PHYSICIAN_ID_KEY);
        if (mPhysicianId == null)
            Log.e(LOG_TAG, "This activity should not have been started without the DOCTOR's id!!");

        mPatientId = getIntent().getStringExtra(PATIENT_ID_KEY);
        if (mPatientId == null)
            Log.e(LOG_TAG, "IN case you are interested the patient id is Null.");

        // now look to see if we are restarting
        if (savedInstanceState != null &&
                (mPhysicianId == null || mPatientId == null)) {
            if (mPhysicianId == null)
                mPhysicianId = savedInstanceState.getString(PHYSICIAN_ID_KEY);
            if (mPatientId == null)
                mPatientId = savedInstanceState.getString(PATIENT_ID_KEY);
        }

        // start the server requests for the data that we need
        PhysicianManager.getPhysician(this, mPhysicianId);
        MedicationManager.getAllMedications(this);
        if (mPatientId != null) {
            Log.d(LOG_TAG, "onCreate is getting the patient from the server id :" + mPatientId);
            PatientManager.getPatient(this, mPatientId);
        } else {
            Log.d(LOG_TAG, "NO patient id so we don't need to go get it from the server.");
        }
    }

    /**
     * Stores the parameters that we need if we restart
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPatientId != null) outState.putString(PATIENT_ID_KEY, mPatientId);
        if (mPhysicianId != null) outState.putString(PHYSICIAN_ID_KEY, mPhysicianId);
    }

    /**
     * remove menu items if the related fragment is already displaying ..
     * so it doesn't look weird
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Fragment frag = getActiveFragment();
        if (frag == null) {
            Log.e(LOG_TAG, "Active Fragment is not found!");
            return super.onPrepareOptionsMenu(menu);
        }
        if (frag instanceof PatientMedicationFragment) {
            menu.removeItem(R.id.action_medication_list);
        } else if (frag instanceof HistoryLogFragment) {
            menu.removeItem(R.id.action_history_log);
        } else if (frag instanceof MedicationListFragment) {
            menu.removeItem(R.id.action_medication_list);
            // when add medication remove the history stuff too
            menu.removeItem(R.id.action_history_log);
        } else if (frag instanceof PatientGraphicsFragment) {
            menu.removeItem(R.id.action_chart);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public Fragment getActiveFragment() {
        Fragment medicationFrag;
        medicationFrag = getFragmentManager().findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
        if (medicationFrag != null && medicationFrag.isVisible()) {
            Log.d(LOG_TAG, "Medication Frag is visible.");
            return medicationFrag;
        }
        Fragment graphicsFrag;
        graphicsFrag = getFragmentManager().findFragmentByTag(PatientGraphicsFragment.FRAGMENT_TAG);
        if (graphicsFrag != null && graphicsFrag.isVisible()) {
            Log.d(LOG_TAG, "Graphics Frag is visible.");
            return graphicsFrag;
        }
        Fragment historyFrag;
        historyFrag = getFragmentManager().findFragmentByTag(HistoryLogFragment.FRAGMENT_TAG);
        if (historyFrag != null && historyFrag.isVisible()) {
            Log.d(LOG_TAG, "History Frag is visible.");
            return historyFrag;
        }
        Fragment medicationListFrag;
        medicationListFrag = getFragmentManager().findFragmentByTag(MedicationListFragment.FRAGMENT_TAG);
        if (medicationListFrag != null && medicationListFrag.isVisible()) {
            Log.d(LOG_TAG, "Medication List Frag is visible.");
            return medicationListFrag;
        }
        return null;
    }

    /**
     * process the option menu items that are common between both activities
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_medication_list) { // dual pane only
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container,
                            new PatientMedicationFragment(),
                            PatientMedicationFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            invalidateOptionsMenu(); // force to rebuild the options menu
            return true;
        } else if (id == R.id.action_history_log) { //dual pane only
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container,
                            new HistoryLogFragment(),
                            HistoryLogFragment.FRAGMENT_TAG)
                    .commit();
            invalidateOptionsMenu(); // force to rebuild the options menu
            return true;
        } else if (id == R.id.action_chart) { //dual pane only
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container,
                            new PatientGraphicsFragment(),
                            PatientGraphicsFragment.FRAGMENT_TAG)
                    .commit();
            invalidateOptionsMenu();// force to rebuild the options menu
            return true;
        } else if (id == R.id.physician_logout) { // both menus no special processing
            LoginActivity.restartLoginActivity(this);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called by the Physician Manager when it gets a Physician from the server
     *
     * @param physician from server
     */
    public void setPhysician(Physician physician) {
        if (physician == null) {
            Log.e(LOG_TAG, "Trying to set physician to null value");
            return;
        }
        Log.d(LOG_TAG, "Current Selected Physician is : " + physician.toString());
        mPhysician = physician;
        // send the new physician data to the list fragment to redisplay
        Fragment listFrag;
        listFrag = getFragmentManager().findFragmentByTag(PhysicianListPatientsFragment.FRAGMENT_TAG);
        if (listFrag != null && listFrag instanceof PhysicianListPatientsFragment) {
            ((PhysicianListPatientsFragment) listFrag).updatePhysician(mPhysician);
        }
    }

    /**
     * Callback for the List Fragment to request the physician with the patient list
     * for displaying
     *
     * @return Physician to process patient list for
     */
    @Override
    public Physician getPhysicianForPatientList() {
        Log.d(LOG_TAG, "I am now GETTING Physician for Patient list.");
        return mPhysician;
    }

    /**
     * Called by the Patient Manager when it gets a patient from the server
     * It sets the current patient and then informs all the fragments that they
     * need to update their displays with the current patient
     *
     * @param patient from server
     */
    //@Override
    public void setPatient(Patient patient) {
        if (patient == null) {
            Log.e(LOG_TAG, "Trying to set patient to null value");
            return;
        }
        Log.d(LOG_TAG, "Current Selected Patient is : " + patient.toString());
        mPatient = patient;
        sendPatientToFragments(mPatient);
    }

    /**
     * If the patient object was received from the server then we need to tell all the fragments
     * about the new patient so they can be updated appropriately
     *
     * @param patient from server for update to fragments
     */
    private void sendPatientToFragments(Patient patient) {
        Log.d(LOG_TAG, "Sending Patient to the detail frag+ ....");
        // try the details fragment first
        Fragment detailFrag;
        detailFrag = getFragmentManager().findFragmentByTag(PhysicianPatientDetailFragment.FRAGMENT_TAG);
        if (detailFrag != null && detailFrag.isVisible()
                && detailFrag instanceof PhysicianPatientDetailFragment) {
            Log.d(LOG_TAG, "Detail Frag is visible.");
            ((PhysicianPatientDetailFragment) detailFrag).updatePatient(patient);
        }
        Log.d(LOG_TAG, "Sending Patient to the medication frag+ ....");
        // then patient medication fragment
        Fragment medicationFrag;
        medicationFrag = getFragmentManager().findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
        if (medicationFrag != null && medicationFrag.isVisible() &&
                medicationFrag instanceof PatientMedicationFragment) {
            Log.d(LOG_TAG, "Medication Frag is visible.");
            ((PatientMedicationFragment) medicationFrag).updatePatient(patient);
        }
        Log.d(LOG_TAG, "Sending Patient to the graphics frag+ ....");
        // finally the patient graphing fragment
        Fragment graphicsFrag;
        graphicsFrag = getFragmentManager().findFragmentByTag(PatientGraphicsFragment.FRAGMENT_TAG);
        if (graphicsFrag != null && graphicsFrag.isVisible() &&
                graphicsFrag instanceof PatientGraphicsFragment) {
            Log.d(LOG_TAG, "Graphics Frag is visible.");
            ((PatientGraphicsFragment) graphicsFrag).updatePatient(patient);
        }
        Log.d(LOG_TAG, "Sending Patient to the history frag+ ....");

        // now the history log fragment
        // if the history log fragment is not the only fragment this causes a problem
        // this is a hack but I'm running out of time
        Fragment historyFrag;
        historyFrag = getFragmentManager().findFragmentByTag(HistoryLogFragment.FRAGMENT_TAG);
        if (historyFrag != null && historyFrag.isVisible() &&
                historyFrag instanceof HistoryLogFragment) {
            Log.d(LOG_TAG, "History Frag is visible.");
            ((HistoryLogFragment) historyFrag).updatePatient(patient);
        }
        Log.d(LOG_TAG, "Sending Patient to the fragments is DONE.");
    }

    /**
     * Callback for the Details Fragment to request a patient
     *
     * @return Patient
     */
    //@Override
    public Patient getPatientForDetails() {
        Log.d(LOG_TAG, "GETTING Selected Patient for Details : " + mPatient);
        return mPatient;
    }

    /**
     * Callback for the Graphing Fragment to request Patient data
     *
     * @return Patient
     */
    //@Override
    public Patient getPatientDataForGraphing() {
        Log.d(LOG_TAG, "GETTING Selected Patient for Graphing : " + mPatient);
        return mPatient;
    }

    /**
     * Callback for the patient history log to obtain a patient to use
     *
     * @return Patient
     */
    //@Override
    public Patient getPatientForHistory() {
        Log.d(LOG_TAG, "GETTING Selected Patient for History Log : " + mPatient);
        return mPatient;
    }

    /**
     * Callback for the Medication List fragment to obtain a patient to use
     *
     * @return Patient
     */
    //@Override
    public Patient getPatientForPrescriptions() {
        Log.d(LOG_TAG, "GETTING Selected Patient for Prescriptions : " + mPatient);
        return mPatient;
    }

    /**
     * Callback for the Medication List fragment to get the list of medications that it needs to
     * display
     *
     * @return Collection of Medications
     */
    //@Override
    public Collection<Medication> getMedications() {
        return mMedications;
    }

    /**
     * This is one that is called when the activity selects an item from a patient list
     * It could be use to reset the patient when there is a new one too
     *
     * it may not be a fully-formed patient so we need to go to the server and get
     * the latest and greatest version of the patient
     *
     * @param physicianId
     * @param patient
     */

    @Override
    public void onItemSelected(String physicianId, Patient patient) {
        if (patient == null || physicianId == null) {
            Log.d(LOG_TAG, "Invalid item selected.");
            return;
        }
        mPatientId = patient.getId();
        PatientManager.getPatient(this, mPatientId);
    }

    /**
     * Find the patient in the doctor's list and add a status log with the doctor's note.
     *
     * @param patientId patient that the status is for
     * @param statusLog note to add to the status log
     */
    //@Override
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
    // @Override
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
    //@Override
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
    //@Override
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
    //@Override
    public boolean showAddMedicationOptionsMenu() {
        Log.d(LOG_TAG, "Detail Activity is showing the add medication options menu.");
        return true;
    }

    /**
     * Callback for the Medication List fragment if the add medication was chosen from options
     * Displays a custom dialog fragment for adding a new medication
     */
    //@Override
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
    //@Override
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
    //@Override
    public void onCancelMedicationResult() {
        Log.d(LOG_TAG, "Add/Edit Medication was cancelled.");
    }

    /**
     * Callback from the Medication Manager when it obtains a list of all the medications.
     * Saves the list.
     * Then call the medication list fragment to pass it the updated list
     *
     * @param medications Collection<Medication>
     */
    //@Override
    public void setMedicationList(Collection<Medication> medications) {
        mMedications = medications;
        // call the medication list fragment to update the medication list there
        Fragment frag;
        frag = getFragmentManager().findFragmentByTag(MedicationListFragment.FRAGMENT_TAG);
        if (frag != null) {
            ((MedicationListFragment) frag).updateMedications(medications);
        }
    }


    /**
     * Patient search dialog search results when OK button pressed
     * normally this should go to the server and get the object
     * then call successful search or failed search methods
     *
     * @param lastName
     * @param firstName
     */
    @Override
    public void onNameSelected(String lastName, String firstName) {
        Toast.makeText(getApplication(),
                "Name selected is " + getName(lastName, firstName),
                Toast.LENGTH_LONG).show();
    }

    /**
     * Patient Search Dialog results processing if successfully
     * found on the servers side.
     *
     * Default to putting up a message .. this one should be over-ridden
     *
     * @param patient Patient found in the server search
     */
    //@Override
    public void successfulSearch(Patient patient) {
        Toast.makeText(getApplication(), patient.toString() + " Found.",
                Toast.LENGTH_LONG).show();
    }
    /**
     * Patient Search Dialog processing when server search fails
     * If the search failed put up a toast message and ignore
     *
     * @param message detailed message for failed results
     */
    @Override
    public void failedSearch(String message) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * This formats it the same way that the server formats it so they can compare
     * the same way.
     *
     * @param lastName
     * @param firstName
     * @return
     */
    public static String getName(String lastName, String firstName) {
        String name = "";
        if (firstName != null && !firstName.isEmpty()) name += firstName;
        if (!name.isEmpty()) name += " ";
        if (lastName != null  && !lastName.isEmpty()) name+= lastName;
        return name;
    }

}
