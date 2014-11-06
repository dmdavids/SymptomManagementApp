package com.skywomantech.app.symptommanagement.admin.Physician;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

public class AdminPhysicianListActivity extends Activity
        implements PhysicianListFragment.Callbacks,
        AdminPhysicianDetailFragment.Callbacks {

    public final String LOG_TAG = AdminPhysicianListActivity.class.getSimpleName();

    public static final String PHYSICIAN_ID_KEY = "physician_id";


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

    @Override
    public void onPhysicianSelected(String physicianId, String firstName, String lastName) {
        Log.d(LOG_TAG, "Saving Physician ID: " + physicianId);

        Intent detailIntent = new Intent(this, AdminPhysicianDetailActivity.class);
        detailIntent.putExtra(PHYSICIAN_ID_KEY, physicianId);
        startActivity(detailIntent);
    }

    @Override
    public boolean showAddPhysicianOptionsMenu() {
        return true;
    }

    @Override
    public void onAddPhysician() {
        Log.d(LOG_TAG, "Changing to Add/Edit Fragment");
        Intent detailIntent = new Intent(this, AdminPhysicianDetailActivity.class);
        startActivity(detailIntent);
    }

    @Override
    public void onEditPhysician(String id) {
        // switch out the fragments
        Bundle arguments = new Bundle();
        arguments.putString(PHYSICIAN_ID_KEY, id);
        AdminPhysicianAddEditFragment fragment = new AdminPhysicianAddEditFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .replace(R.id.adminphysician_detail_container, fragment)
                .commit();
    }
}
