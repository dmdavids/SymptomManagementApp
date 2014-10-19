package com.skywomantech.app.symptommanagement.admin.Medication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;


/**
 * An activity representing a single admin_medication detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link AdminMedicationListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link AdminMedicationDetailFragment}.
 */
public class AdminMedicationDetailActivity extends Activity
        implements AdminMedicationDetailFragment.Callbacks{

    private static final String LOG_TAG = AdminMedicationDetailActivity.class.getSimpleName();
    public final static String MED_ID_KEY = AdminMedicationListActivity.MED_ID_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_medication_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle arguments = new Bundle();
            // store the medication id so that the detail fragment can use it
            String medId = getIntent().getStringExtra(MED_ID_KEY);
            Log.d(LOG_TAG, "Med ID Key is : " + medId);
            Fragment fragment;
            if (medId != null) {
                arguments.putString(MED_ID_KEY, medId);
                 fragment = new AdminMedicationDetailFragment();
            } else {
                 fragment = new AdminMedicationAddEditFragment();
            }
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.adminmedication_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, AdminMedicationListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditMedication(String medId) {
        // switch out the fragments
        Bundle arguments = new Bundle();
        arguments.putString(MED_ID_KEY, medId);
        AdminMedicationAddEditFragment fragment = new AdminMedicationAddEditFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .replace(R.id.adminmedication_detail_container, fragment)
                .commit();
    }
}
