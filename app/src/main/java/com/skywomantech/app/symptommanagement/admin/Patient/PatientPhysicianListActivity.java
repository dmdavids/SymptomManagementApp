package com.skywomantech.app.symptommanagement.admin.Patient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.admin.Physician.PhysicianListFragment;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

public class PatientPhysicianListActivity extends Activity
        implements PhysicianListFragment.Callbacks {

    public final String LOG_TAG = PatientPhysicianListActivity.class.getSimpleName();
    public static final String PHYSICIAN_ID_KEY = "physician_id";
    public static final String PHYSICIAN_FIRST_NAME_KEY = "physician_first_name";
    public static final String PHYSICIAN_LAST_NAME_KEY = "physician_last_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminphysician_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method from {@link com.skywomantech.app.symptommanagement.admin.Physician.PhysicianListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onPhysicianSelected(String physicianId, String firstName, String lastName) {
        Log.d(LOG_TAG, "id selected is " + physicianId + " name is " + firstName + " " + lastName);
        Intent intent=new Intent();
        intent.putExtra(PHYSICIAN_ID_KEY, physicianId);
        intent.putExtra(PHYSICIAN_FIRST_NAME_KEY, firstName);
        intent.putExtra(PHYSICIAN_LAST_NAME_KEY, lastName);
        setResult(RESULT_OK, intent);
        onBackPressed();
    }

    @Override
    public boolean showAddPhysicianOptionsMenu() {
        return false;
    }

    @Override
    public void onAddPhysician() {
        // do nothing because we aren't allowing the add physician capability
    }

}
