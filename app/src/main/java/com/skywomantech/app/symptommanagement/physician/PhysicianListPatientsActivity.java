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

public class PhysicianListPatientsActivity extends Activity
        implements PhysicianListPatientsFragment.Callbacks,
        PatientSearchDialog.Callbacks,
        PhysicianPatientDetailFragment.Callbacks,
        PrescriptionAdapter.Callbacks,
        PatientMedicationFragment.Callbacks,
        MedicationListFragment.Callbacks,
        MedicationAddEditDialog.Callbacks,
        HistoryLogFragment.Callbacks,
        PatientGraphicsFragment.Callbacks,
        PhysicianManager.Callbacks {

    public final static String LOG_TAG = PhysicianListPatientsActivity.class.getSimpleName();

    private static String PHYSICIAN_ID_KEY;
    private static String PATIENT_ID_KEY;

    private static String mPhysicianId;
    private static Physician mPhysician;

    private String mPatientId;
    private static Patient mPatient;

    private PhysicianManager physicianManager = new PhysicianManager(); // to handle physician stuff

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
        physicianManager.getPhysician(this, mPhysicianId);

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
        } else if (id == R.id.physician_logout) {
            LoginActivity.restartLoginActivity(this);
        } else if (id == R.id.action_medication_list) {
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent()
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME));
    }

    @Override
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

    @Override
    public void onPrescriptionDelete(final int position, Medication medication) {

        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Do you want to delete this prescription?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PatientMedicationFragment frag =
                                (PatientMedicationFragment) getFragmentManager()
                                        .findFragmentById(R.id.patient_graphics_container);
                        frag.deletePrescription(position);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                }).create();
        alert.show();
    }

    @Override
    public boolean onRequestPrescriptionAdd() {
        MedicationListFragment fragment = new MedicationListFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_graphics_container, fragment)
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
                        .findFragmentById(R.id.patient_graphics_container);
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
                            .findFragmentById(R.id.patient_graphics_container);
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

    @Override
    public Patient getPatient() {
        return mPatient;
    }

    @Override
    public void onPatientContacted(String patientId, StatusLog statusLog) {
        if (mPhysician == null || mPhysician.getPatients() == null
                || patientId == null || patientId.isEmpty()) {
            Log.e(LOG_TAG, "Unable to update the Patient Status Log");
            return;
        }
        // attach the status log to the appropriate patient and then
        // tag some physician information onto the note
        String s = statusLog.getNote() + " [" + mPhysician.getName() + "] ";
        statusLog.setNote(s);
        boolean added = false;
        for (Patient p : mPhysician.getPatients()) {
            if (p.getId().contentEquals(patientId)) {
                if (p.getStatusLog() == null) {
                    p.setStatusLog(new HashSet<StatusLog>());
                }
                p.getStatusLog().add(statusLog);
                added = true;
                break;
            }
        }
        if (added) {
            savePhysician(mPhysician);
            Toast.makeText(getApplicationContext(),
                    "Saved Patient Contacted Status.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Unable to Save Patient Contacted Status.",
                    Toast.LENGTH_LONG).show();
        }
    }


    private void savePhysician(final Physician physician) {
        if (physician == null) return;
        Log.d(LOG_TAG, "Saving Physician ID Key is : " + physician.getId());

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Physician>() {

                @Override
                public Physician call() throws Exception {
                    Log.d(LOG_TAG, "Saving physician with status notes : " + physician.getId());
                    return svc.updatePhysician(physician.getId(), physician);
                }
            }, new TaskCallback<Physician>() {

                @Override
                public void success(Physician result) {
                    Log.d(LOG_TAG, "Updated Physician :" + result.toString());
                    mPhysician = result;
                }

                @Override
                public void error(Exception e) {
                    Log.d(LOG_TAG,
                            "Unable to update status logs for the Physician. " +
                                    "Please check Internet connection.");
                }
            });
        }
    }

    private static synchronized void getPhysician(final String id) {
        if (id == null) return;
        Log.d(LOG_TAG, "Getting Physician ID Key is : " + id);

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Physician>() {

                @Override
                public Physician call() throws Exception {
                    Log.d(LOG_TAG, "getting single physician with id : " + id);
                    return svc.getPhysician(id);
                }
            }, new TaskCallback<Physician>() {

                @Override
                public void success(Physician result) {
                    Log.d(LOG_TAG, "Found Physician :" + result.toString());
                    mPhysician = result;
                }

                @Override
                public void error(Exception e) {
                    Log.d(LOG_TAG,
                            "Unable to fetch Physician to update the status logs. " +
                                    "Please check Internet connection.");
                }
            });
        }
    }

    @Override
    public Patient getPatientDataForGraphing() {
        Log.d(LOG_TAG, "GETTING Selected Patient for Graphing : " + mPatient);
        return mPatient;
    }

    @Override
    public void setPhysician(Physician physician) {
        mPhysician = physician;
    }
}
