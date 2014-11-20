package com.skywomantech.app.symptommanagement.physician;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
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
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

public class PhysicianListPatientsActivity extends PhysicianActivity implements
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

    public final static String LOG_TAG = PhysicianListPatientsActivity.class.getSimpleName();

    private static String PHYSICIAN_ID_KEY;
    private static String PATIENT_ID_KEY;

    private static String mPhysicianId;
    private static Physician mPhysician;
    private static String mPatientId;

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

    /**
     * uses different menus for the two pane vs classic display
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTwoPane) {
            getMenuInflater().inflate(R.menu.physician_patient_twopane_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.physician_patient_list_menu, menu);
        }
        return true;
    }

    /**
     * process the option menu items, some are for both menus and
     * some are only available for dual pane option menu
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_patient_search) { // both menus need special processing for both
            FragmentManager fm = getFragmentManager();
            PatientSearchDialog searchDialog = PatientSearchDialog.newInstance();
            searchDialog.show(fm, PatientSearchDialog.FRAGMENT_TAG);
        } else if (id == R.id.action_sync_alerts) { // both menus no special processing
            SymptomManagementSyncAdapter.syncImmediately(this);
            return true;
        } else if (id == R.id.action_medication_list) { // dual pane only
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container,
                            new PatientMedicationFragment(), PatientMedicationFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_history_log) { //dual pane only
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container,
                            new HistoryLogFragment(), HistoryLogFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.action_chart) { //dual pane only
            getFragmentManager().beginTransaction()
                    .replace(R.id.patient_graphics_container,
                            new PatientGraphicsFragment(), HistoryLogFragment.FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.physician_logout) { // both menus no special processing
            LoginActivity.restartLoginActivity(this);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This makes the list activity, the top activity so you can't go back to login
     * TODO: Does this work differently with dual pane?  don't think so but ...
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent()
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME));
    }

    /**
     * Callback from the patient list fragment for when an item is selected from the list
     * need to process according to how the display layout is set up
     *
     * @param physicianId
     * @param patient
     */
    @Override
    public void onItemSelected(String physicianId, Patient patient) {
        mPatientId = patient.getId();
        //mPhysicianId = physicianId;
        if (mTwoPane) {
            // Go to the server and get the fully-formed patient object
            // the patient manager will call the setPatient callback when done
            // and that will inform all the fragments of the change
            PatientManager.getPatient(this, mPatientId);
        } else {
            // we are in the classic path so we just send this patient information to
            // detail activity and it handles it instead of this activity
            Bundle arguments = new Bundle();
            arguments.putString(PHYSICIAN_ID_KEY, physicianId);
            arguments.putString(PATIENT_ID_KEY, patient.getId());
            Intent detailIntent = new Intent(this, PhysicianPatientDetailActivity.class);
            detailIntent.putExtras(arguments);
            startActivity(detailIntent);
        }
    }

    /**
     * Callback from the Patient Search Dialog.  It uses the entered name to do a
     * search by name ON the server side.  The name must be an exact match.
     * <p/>
     * Also has a failed and successful search methods for processing the result
     *
     * @param lastName
     * @param firstName
     */
    @Override
    public void onNameSelected(String lastName, String firstName) {
        Log.e(LOG_TAG, "THE NAME SELECTED IS : " + firstName + " " + lastName);
        PatientManager.findPatientByName(this, lastName, firstName);
    }

    /**
     * If the patient dialog search was successful then make this patient the
     * current patient
     * if dual pane then just set the patient and tell all the fragments to update their
     * displays
     * if this is the classic path then start the patient detail activity
     * and tell it who the physician and patient are to work with.
     *
     * @param patient Patient found via the search dialog
     */
    @Override
    public void successfulSearch(Patient patient) {
        if (mTwoPane) {
            setPatient(patient);
            // temporarily put the searched patient in the patient list for display purposes only
            // the patient will not be in the list permanently and will disappear if the
            // list is remade such as when the device is rotate probably
            Fragment frag = getFragmentManager()
                    .findFragmentByTag(PhysicianListPatientsFragment.FRAGMENT_TAG);
            if (frag != null) {
                ((PhysicianListPatientsFragment) frag).temporaryAddToList(patient);
            }
        } else {
            Intent detailIntent = new Intent(getApplication(), PhysicianPatientDetailActivity.class);
            detailIntent.putExtra(PATIENT_ID_KEY, patient.getId());
            detailIntent.putExtra(PHYSICIAN_ID_KEY, mPhysicianId);
            startActivity(detailIntent);
        }
    }

    /**
     * If the search failed put up a toast message and ignore
     *
     * @param message detailed message for failed results
     */
    @Override
    public void failedSearch(String message) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Called by the Physician Manager when it gets a Physician from the server
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

        // now we notify the list patients fragment so it can update the patient list adapter
        Fragment frag = getFragmentManager()
                .findFragmentByTag(PhysicianListPatientsFragment.FRAGMENT_TAG);
        if (frag != null) {
            ((PhysicianListPatientsFragment) frag).updatePhysician(mPhysician);
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
        Log.d(LOG_TAG, "GETTING Selected Physician for Patient list : " + mPhysician);
        return mPhysician;
    }

    /**
     * DUAL PANE
     * Callback from the prescription fragment to add a prescription that was chosen
     * from the whole medication list
     *
     * @param medication selected medication to add as a prescription
     */
    @Override
    public void onMedicationSelected(Medication medication) {
        // let the detail fragment update the patient's prescription list
        //onBackPressed();
        PatientMedicationFragment frag =
                (PatientMedicationFragment) getFragmentManager()
                        .findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
        frag.addPrescription(medication);
    }

    /**
     * DUAL PANE
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
