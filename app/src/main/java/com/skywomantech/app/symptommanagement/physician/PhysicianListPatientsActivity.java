package com.skywomantech.app.symptommanagement.physician;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

public class PhysicianListPatientsActivity extends PhysicianActivity {
    public final static String LOG_TAG = PhysicianListPatientsActivity.class.getSimpleName();

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // this is going to get the physician and patient id's and
        // the start the server sessions for getting the needed data
        super.onCreate(savedInstanceState);

        // once the physician is logged in then this is the top level so
        // so it is considered home and we need to negate the actionbar arrow
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(false);

        // Setting up the layout depending if it is large screen or not
        // ...so this is how we figure out if we are using a 2-pane layout or not...if the
        // patient detail container exists in the layout then we are two pane so set flag
        mTwoPane = false;
        setContentView(R.layout.activity_physician_patient_list);
        if (findViewById(R.id.physician_patient_detail_container) != null) { // dual pane!
            mTwoPane = true;
            //force a landscape view ... hack because of bugs with rotation of fragments
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            // setup list item highlight when selected for this situation
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
                PhysicianPatientDetailFragment detailsFragment = new PhysicianPatientDetailFragment();
                detailsFragment.setArguments(arguments);
                // start by showing the history log in the graphics fragment for now
                PatientGraphicsFragment graphicsFragment = new PatientGraphicsFragment();
                graphicsFragment.setArguments(arguments);
                getFragmentManager().beginTransaction()
                        .replace(R.id.physician_patient_detail_container,
                                detailsFragment,
                                PhysicianPatientDetailFragment.FRAGMENT_TAG)
                        .replace(R.id.patient_graphics_container,
                                graphicsFragment,
                                PatientGraphicsFragment.FRAGMENT_TAG)
                        .commit();
            }
        }
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
            // menu.removeItem(R.id.action_history_log);
        } else if (frag instanceof PatientGraphicsFragment) {
            menu.removeItem(R.id.action_chart);
        }
        return super.onPrepareOptionsMenu(menu);
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
     * process the patient list ones then process the common ones with super
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_patient_search) {
            FragmentManager fm = getFragmentManager();
            PatientSearchDialog searchDialog = PatientSearchDialog.newInstance();
            searchDialog.show(fm, PatientSearchDialog.FRAGMENT_TAG);
        } else if (id == R.id.action_sync_alerts) {
            SymptomManagementSyncAdapter.syncImmediately(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This makes the list activity, the top activity so you can't go back to login
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
        if (mTwoPane) {
            super.onItemSelected(physicianId, patient);
        } else {
            // we are in the small screen path so we just send this patient information to
            // detail activity and let it take over the processing from now on
            Bundle arguments = new Bundle();
            arguments.putString(PHYSICIAN_ID_KEY, physicianId);
            arguments.putString(PATIENT_ID_KEY, patient.getId());
            Intent detailIntent = new Intent(this, PhysicianPatientDetailActivity.class);
            detailIntent.putExtras(arguments);
            startActivity(detailIntent);
        }
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
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_graphics_container,
                        new PatientMedicationFragment(),
                        PatientMedicationFragment.FRAGMENT_TAG)
                .commit();

        PatientMedicationFragment frag =
                (PatientMedicationFragment) getFragmentManager()
                        .findFragmentByTag(PatientMedicationFragment.FRAGMENT_TAG);
        if (frag != null) {
            frag.addPrescription(medication);
        } else {
            Log.e(LOG_TAG, "Unable to add patient prescription.");
        }
    }

    /**
     * Callback from the Patient Search Dialog.
     * It uses the entered name to do a search by name ON the server side.
     * The name must be an exact match.
     * <p/>
     * Also has a failed and successful search methods for processing the result
     *
     * @param lastName
     * @param firstName
     */
    @Override
    public void onNameSelected(String lastName, String firstName) {
        Log.e(LOG_TAG, "THE NAME SELECTED IS : " + getName(lastName, firstName));
        PatientManager.findPatientByName(this, getName(lastName, firstName));
    }

    /**
     * If the patient dialog search was successful and the onNameSelected() runs
     * then this is called if the patient was found on servers so make this patient the
     * current patient ... it is a fully-formed already
     * <p/>
     * if dual pane then just set the patient and tell all the fragments to update their
     * displays
     * <p/>
     * if this is the classic path then start the patient detail activity
     * and tell it who the physician and patient are to work with.
     *
     * @param patient Patient found via the search dialog
     */
    @Override
    public void successfulSearch(Patient patient) {
        if (mTwoPane) {
            setPatient(patient);  // tells all the fragments that it has a new patient
            // temporarily put the searched patient in the patient list for display purposes only
            // the patient will not be in the list permanently and will disappear if the
            // list is remade such as when the device is rotate probably
            Fragment frag = getFragmentManager()
                    .findFragmentByTag(PhysicianListPatientsFragment.FRAGMENT_TAG);
            if (frag != null) {
                ((PhysicianListPatientsFragment) frag).temporaryAddToList(patient);
            }
        } else {
            // this is a small screen so just let the details activity handle it instead
            // they will have to look up the patient from the server again
            Intent detailIntent = new Intent(getApplication(), PhysicianPatientDetailActivity.class);
            detailIntent.putExtra(PATIENT_ID_KEY, patient.getId());
            detailIntent.putExtra(PHYSICIAN_ID_KEY, mPhysicianId);
            startActivity(detailIntent);
        }
    }


}
