package com.skywomantech.app.symptommanagement.physician;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.patient.ReminderFragment;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.Collection;
import java.util.concurrent.Callable;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

public class PhysicianListPatientsActivity extends Activity
        implements PhysicianListPatientsFragment.Callbacks,
                    PatientSearchDialog.Callbacks {

    public final String LOG_TAG = PhysicianListPatientsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physician_patient_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.physician_patient_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_patient_search){
            Log.d(LOG_TAG, "Displaying Patient Search Dialog");
            FragmentManager fm = getFragmentManager();
            PatientSearchDialog searchDialog = PatientSearchDialog.newInstance();
            searchDialog.show(fm, "patient_search_dialog");
        } else if (id == R.id.action_sync_alerts) {
            SymptomManagementSyncAdapter.syncImmediately(this);
            return true;
        } else if (id == R.id.physician_logout) {
            LoginUtility.logout(this);
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String id) {
            Intent detailIntent = new Intent(this, PhysicianPatientDetailActivity.class);
            detailIntent.putExtra(PhysicianPatientDetailFragment.PATIENT_ID_KEY, id);
            startActivity(detailIntent);
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
                    }
                    else {
                        Intent detailIntent = new Intent(getApplicationContext(),
                                PhysicianPatientDetailActivity.class);
                        detailIntent.putExtra(PhysicianPatientDetailFragment.PATIENT_ID_KEY, patientId);
                        startActivity(detailIntent);
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
}
