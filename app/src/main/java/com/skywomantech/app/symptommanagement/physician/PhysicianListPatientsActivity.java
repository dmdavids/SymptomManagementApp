package com.skywomantech.app.symptommanagement.physician;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.view.Menu;
import android.view.MenuItem;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

/**
 * An activity representing a list of PhysicianPatients. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PhysicianPatientDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PhysicianListPatientsFragment} and the item details
 * (if present) is a {@link PhysicianPatientDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PhysicianListPatientsFragment.Callbacks} interface
 * to listen for item selections.
 */
public class PhysicianListPatientsActivity extends Activity
        implements PhysicianListPatientsFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physician_patient_list);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.physician_patient_detail_container) != null) {
            mTwoPane = true;

            ((PhysicianListPatientsFragment) getFragmentManager()
                    .findFragmentById(R.id.physician_patient_list))
                    .setActivateOnItemClick(true);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.physician_patient_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpFromSameTask(this);
            return true;
        } else if (id == R.id.action_sync_alerts) {
            SymptomManagementSyncAdapter.syncImmediately(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method from {@link PhysicianListPatientsFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(PhysicianPatientDetailFragment.PATIENT_ID_KEY, id);
            PhysicianPatientDetailFragment fragment = new PhysicianPatientDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.physician_patient_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, PhysicianPatientDetailActivity.class);
            detailIntent.putExtra(PhysicianPatientDetailFragment.PATIENT_ID_KEY, id);
            startActivity(detailIntent);
        }
    }

    public void onAddMedication(String id) {
    }
}
