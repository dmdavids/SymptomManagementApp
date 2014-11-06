package com.skywomantech.app.symptommanagement.admin.Physician;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;

public class AdminPhysicianDetailActivity extends Activity
        implements AdminPhysicianDetailFragment.Callbacks {

    private static final String LOG_TAG = AdminPhysicianDetailActivity.class.getSimpleName();
    public final static String PHYSICIAN_ID_KEY = AdminPhysicianListActivity.PHYSICIAN_ID_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_physician_detail);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            String physicianId = getIntent().getStringExtra(PHYSICIAN_ID_KEY);
            Log.d(LOG_TAG, "Physician ID Key is : " + physicianId);
            Fragment fragment;
            if (physicianId != null) {
                arguments.putString(PHYSICIAN_ID_KEY, physicianId);
                fragment = new AdminPhysicianDetailFragment();
            } else {
                fragment = new AdminPhysicianAddEditFragment();
            }
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.adminphysician_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, AdminPhysicianListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
