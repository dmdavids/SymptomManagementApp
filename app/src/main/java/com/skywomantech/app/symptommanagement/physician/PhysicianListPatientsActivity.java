package com.skywomantech.app.symptommanagement.physician;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

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
            startActivity(new Intent(this, LoginActivity.class));
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
    public void onNameSelected(String name) {
        Log.e(LOG_TAG, "THE NAME SELECTED IS : " + name);
    }
}
