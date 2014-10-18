package com.skywomantech.app.symptommanagement.admin;

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
 * in a {@link com.skywomantech.app.symptommanagement.admin.AdminMedicationsListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link AdminMedicationsDetailFragment}.
 */
public class AdminMedicationDetailActivity extends Activity
        implements AdminMedicationsDetailFragment.Callbacks{

    private static final String LOG_TAG = AdminMedicationDetailActivity.class.getSimpleName();
    public final static String MED_ID_KEY = AdminMedicationsListActivity.MED_ID_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_medication_detail);

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
            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle arguments = new Bundle();
            // store the medication id so that the detail fragment can use it
            String medId = getIntent().getStringExtra(MED_ID_KEY);
            Log.d(LOG_TAG, "Med ID Key is : " + medId);
            Fragment fragment;
            if (medId != null) {
                arguments.putString(MED_ID_KEY, medId);
                 fragment = new AdminMedicationsDetailFragment();
            } else {
                 fragment = new AdminMedicationsAddEditFragment();
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
            navigateUpTo(new Intent(this, AdminMedicationsListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditMedication(String medId) {
        // switch out the fragments
        Bundle arguments = new Bundle();
        arguments.putString(MED_ID_KEY, medId);
        AdminMedicationsAddEditFragment fragment = new AdminMedicationsAddEditFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .replace(R.id.adminmedication_detail_container, fragment)
                .commit();
    }
}
