package com.skywomantech.app.symptommanagement.physician;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import android.view.MenuItem;
import com.skywomantech.app.symptommanagement.R;

/**
 * An activity representing a single PhysicianPatient detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PhysicianPatientListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link PhysicianPatientDetailFragment}.
 */
public class PhysicianPatientDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physicianpatient_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(PhysicianPatientDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(PhysicianPatientDetailFragment.ARG_ITEM_ID));
            PhysicianPatientDetailFragment fragment = new PhysicianPatientDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.physicianpatient_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, PhysicianPatientListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
