package com.skywomantech.app.symptommanagement.admin.Medication;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;


/**
 * An activity representing a list of admin_medications. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link AdminMedicationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MedicationListFragment} and the item details
 * (if present) is a {@link AdminMedicationDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link MedicationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class AdminMedicationListActivity extends Activity
        implements MedicationListFragment.Callbacks {

    public final String LOG_TAG = AdminMedicationListActivity.class.getSimpleName();

    /**
     * The fragment argument representing the medication ID that this fragment
     * represents.
     */
    public static final String MED_ID_KEY = "med_id";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_medication_list);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // return if Activity is being restored, no need to recreate GUI
        if (savedInstanceState != null)
            return;

        if (findViewById(R.id.adminmedication_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((MedicationListFragment) getFragmentManager()
                    .findFragmentById(R.id.adminmedication_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
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
     * Callback method from {@link MedicationListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onMedicationSelected(String medId) {
        Log.d(LOG_TAG, "Saving Med ID: " + medId + " 2-pane is " + Boolean.toString(mTwoPane));
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(MED_ID_KEY, medId);
            AdminMedicationDetailFragment fragment = new AdminMedicationDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.adminmedication_detail_container, fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, AdminMedicationDetailActivity.class);
            detailIntent.putExtra(MED_ID_KEY, medId);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean showAddMedicationOptionsMenu() {
        return true;
    }

    @Override
    public void onAddMedication() {
        Log.d(LOG_TAG, "Changing to Add/Edit Fragment");
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            AdminMedicationAddEditFragment fragment = new AdminMedicationAddEditFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.adminmedication_detail_container, fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, AdminMedicationDetailActivity.class);
            startActivity(detailIntent);
        }
    }



}
