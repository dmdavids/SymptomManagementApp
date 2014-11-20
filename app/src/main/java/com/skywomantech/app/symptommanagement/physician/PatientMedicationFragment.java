package com.skywomantech.app.symptommanagement.physician;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Patient;

import java.util.Collection;
import java.util.HashSet;

/**
 * This fragment displays the patient's prescription list.
 * <p/>
 * This fragment expects the hosting activity to give it the patient with the prescriptions.
 * <p/>
 * This fragment uses a custom list adapter to process the prescription list.
 * <p/>
 * This fragment has an options menu that allow the physician to add a new prescription.
 * This fragment relies on the calling activity to do the add new prescription dialog processing.
 * <p/>
 * The prescription can be deleted by the physician by clicking on the list item delete icon.
 */
public class PatientMedicationFragment extends ListFragment {

    private static final String LOG_TAG = PatientMedicationFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "fragment_patient_medication";

    // Notifies the activity about the following events
    // onRequestPrescriptionAdd - adds a prescription to the patients med list
    // getPatientForPrescriptions - gets the patient from the activity
    public interface Callbacks {
        public void onRequestPrescriptionAdd();

        public Patient getPatientForPrescriptions(); // pulls the patient
    }

    private static Patient mPatient;  // current patient
    private static Medication[] meds; // holds the array list for the list adapter

    public PatientMedicationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(activity.getString(R.string.callbacks_message));
        }
    }

    /**
     * This fragment has an option menu with add prescription option
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.medication_add_menu, menu);
    }

    /**
     * The add prescription uses a dialog that is managed by the hosting activity
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                ((Callbacks) getActivity()).onRequestPrescriptionAdd();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.empty_list_text));
        setRetainInstance(true); // save fragment across config changes
    }

    /**
     * Get the patient from the activity and refresh the list
     */
    @Override
    public void onResume() {
        super.onResume();
        mPatient = ((Callbacks) getActivity()).getPatientForPrescriptions();
        displayPrescriptions(mPatient);
    }

    /**
     * Called by the hosting activity to update the patient and refresh the list
     *
     * @param patient with prescriptions to be displayed
     */
    public void updatePatient(Patient patient) {
        if (patient == null) {
            Log.e(LOG_TAG, "Trying to set patient medication patient to null.");
            return;
        }
        Log.d(LOG_TAG, "New Patient has arrived!" + patient.toString());
        mPatient = patient;
        displayPrescriptions(mPatient);
    }

    /**
     * Called to refresh the prescription list for display
     *
     * @param patient
     */
    private void displayPrescriptions(Patient patient) {
        Log.d(LOG_TAG, "We are updating the display list for Prescriptions.");
        if (patient.getPrescriptions() == null)
            patient.setPrescriptions(new HashSet<Medication>());
        if (patient.getPrescriptions() != null) {
            meds = patient.getPrescriptions()
                    .toArray(new Medication[patient.getPrescriptions().size()]);
        }
        setListAdapter(new PrescriptionAdapter(getActivity(), meds));
    }

    /**
     * called by the hosting activity to add the new prescription to the patient list
     * and then to send the updated patient to the server, don't display here
     * because we wait for the server to send it back to us via the updatePatient()
     * and then we display it
     *
     * @param medication this is the new one to add
     */
    public void addPrescription(Medication medication) {
        if (mPatient == null || medication == null) {
            Log.d(LOG_TAG, "No current patient or medication to process.");
            return;
        }
        if (mPatient.getPrescriptions() == null) {
            mPatient.setPrescriptions(new HashSet<Medication>());
        }
        mPatient.getPrescriptions().add(medication);
        Log.d(LOG_TAG, "Sending this updated patient to the server" + mPatient.toString());
        PatientManager.updatePatient(getActivity(), mPatient);
    }

    /**
     * called by the hosting activity to delete a prescription from the current patient
     * and then update the patient record on the server, wait to display until the
     * updated patient returns from the server
     *
     * @param position where in the meds display is the deleted prescription
     */
    public void deletePrescription(int position) {
        Collection<Medication> p = mPatient.getPrescriptions();
        p.remove(meds[position]);
        mPatient.setPrescriptions(new HashSet<Medication>(p));
        Log.d(LOG_TAG, "Sending this updated patient to the server" + mPatient.toString());
        PatientManager.updatePatient(getActivity(), mPatient);
    }
}
