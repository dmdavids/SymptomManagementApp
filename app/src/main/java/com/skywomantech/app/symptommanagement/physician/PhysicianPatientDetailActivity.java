package com.skywomantech.app.symptommanagement.physician;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.data.StatusLog;

import java.util.Collection;
import java.util.HashSet;

/**
 * This activity is called to handle this information when the device cannot handle two-pane
 * on the screen size.
 * <p/>
 * This task is a holder of the physician and patient information that the fragments need to
 * do their processing.
 * <p/>
 * This activity runs after physician is logged in and has selected a patient from a patient list
 * So this activity expects a physician id and patient id are available for its use.
 * <p/>
 * This activity manages the patient option menu and two fragments including
 * the patient details fragment which shows patient information
 * and then the patient graphics fragment which displays graphs, history logs and medication data
 * Each fragment that it manages expects this activity to give them the patient information
 * <p/>
 * The details fragment is the one that finds the patient and then gives it to this activity
 * The graphic fragment switches between 3 possible fragments : history, prescription & graphs
 */
public class PhysicianPatientDetailActivity extends PhysicianActivity implements
        PhysicianPatientDetailFragment.Callbacks,
        PrescriptionAdapter.Callbacks,
        PatientMedicationFragment.Callbacks,
        MedicationListFragment.Callbacks,
        MedicationAddEditDialog.Callbacks,
        HistoryLogFragment.Callbacks,
        PatientGraphicsFragment.Callbacks,
        PhysicianManager.Callbacks,
        PatientManager.Callbacks,
        MedicationManager.Callbacks {

    private static final String LOG_TAG = PhysicianPatientDetailActivity.class.getSimpleName();

    private static String PHYSICIAN_ID_KEY;
    private static String PATIENT_ID_KEY;

    private static String mPhysicianId;
    private static Physician mPhysician = new Physician();
    private static String mPatientId;

    private static Collection<Medication> mMedications = new HashSet<Medication>();

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

        if (savedInstanceState != null) { // restart
            if (mPhysicianId == null)
                mPhysicianId = savedInstanceState.getString(PHYSICIAN_ID_KEY);
            if (mPatientId == null)
                mPatientId = savedInstanceState.getString(PATIENT_ID_KEY);
        } else {  // started new by adding the new fragments
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
        PhysicianManager.getPhysician(this, mPhysicianId);
        PatientManager.getPatient(this, mPatientId);
        MedicationManager.getAllMedications(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.physician_patient_detail_menu, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPatientId != null) outState.putString(PATIENT_ID_KEY, mPatientId);
        if (mPhysicianId != null) outState.putString(PHYSICIAN_ID_KEY, mPhysicianId);
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
        } else if (id == R.id.physician_logout) {
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

}
