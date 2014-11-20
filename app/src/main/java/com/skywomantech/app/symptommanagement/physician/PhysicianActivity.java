package com.skywomantech.app.symptommanagement.physician;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Patient;

import java.util.Collection;

public abstract class PhysicianActivity extends Activity {
    private static final String LOG_TAG = PhysicianActivity.class.getSimpleName();

    private static Patient mPatient;
    private static Collection<Medication> mMedications;


    /**
     * remove menu items if the related fragment is already displaying ..
     * so it doesn't look weird
     *
     * @param menu
     * @return
     */
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

    /**
     * Called by the Patient Manager when it gets a patient from the server
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
        updatePatientForAllFragments(mPatient);
    }

    /**
     * If the patient object was received from the server then we need to tell all the fragments
     * about the new patient so they can be updated appropriately
     *
     * @param patient from server for update to fragments
     */
    private void updatePatientForAllFragments(Patient patient) {
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
        // then patient medication fragment
        frag = getFragmentManager().findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
        if (frag != null) {
            ((PatientMedicationFragment) frag).updatePatient(patient);
        }
        // finally the patient graphing fragment
        frag = getFragmentManager().findFragmentByTag(PatientGraphicsFragment.FRAGMENT_TAG);
        if (frag != null) {
            ((PatientGraphicsFragment) frag).updatePatient(patient);
        }
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
     * Don't need these Callbacks for this activity so do nothing
     *
     * @param message
     */
    //@Override
    public void failedSearch(String message) {
    }

    //@Override
    public void successfulSearch(Patient patient) {
    }
}
