package com.skywomantech.app.symptommanagement.admin.Physician;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;


/**
 * An activity representing a single AdminPhysician detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link AdminPhysicianListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link AdminPhysicianDetailFragment}.
 */
public class AdminPhysicianDetailActivity extends Activity
        implements AdminPhysicianDetailFragment.Callbacks{

    private static final String LOG_TAG = AdminPhysicianDetailActivity.class.getSimpleName();
    public final static String PHYSICIAN_ID_KEY = AdminPhysicianListActivity.PHYSICIAN_ID_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_physician_detail);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            String physicianId = getIntent().getStringExtra(PHYSICIAN_ID_KEY);
            Log.d(LOG_TAG, "Physician ID Key is : " + physicianId);
            Fragment fragment;
            if(physicianId != null) {
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
