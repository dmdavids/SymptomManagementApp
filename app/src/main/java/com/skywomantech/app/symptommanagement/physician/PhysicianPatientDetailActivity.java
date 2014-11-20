package com.skywomantech.app.symptommanagement.physician;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.data.StatusLog;

import java.util.Collection;
import java.util.HashSet;

/**
 * This activity is called to handle this information when the device cannot handle two-pane
 * on the screen size.
 *
 * This activity runs after physician is logged in and has selected a patient from a patient list
 * So this activity expects a physician id and patient id are available for its use.
 * <p/>
 * This activity manages the patient option menu and two fragments including
 * the patient details fragment which shows patient information
 * and then the patient graphics fragment which displays graphs, history logs and medication data
 * Each fragment that it manages expects this activity to give them the patient information
 * <p/>
 * The details fragment is the one that finds the patient and then gives it to this activity
 * The graphic fragment switches between 3 possible fragments : history, prescription & graphs
 *
 * This uses mostly PhysicianActivity to process things.
 */
public class PhysicianPatientDetailActivity extends PhysicianActivity  {

    private static final String LOG_TAG = PhysicianPatientDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setups the physician and patient id's and then starts the server get's for them
        super.onCreate(savedInstanceState);

        // sets up the layout for the small screen version .. this activity doesn't
        // handle dual pane stuff
        setContentView(R.layout.activity_physicianpatient_detail);
        if (savedInstanceState == null) {   // started new by adding the new fragments
            Log.v(LOG_TAG, "Are the fragments are working with empty objects?");
            getFragmentManager().beginTransaction()
                    .add(R.id.physician_patient_detail_container,
                            new PhysicianPatientDetailFragment(),
                            PhysicianPatientDetailFragment.FRAGMENT_TAG)
                    .commit();
            // put the history log in the graphics fragment for starters
            // could change this to graphs or history logs or medications if preferred.
            getFragmentManager().beginTransaction()
                    .add(R.id.patient_graphics_container,
                            new HistoryLogFragment(), HistoryLogFragment.FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.physician_patient_detail_menu, menu);
        return true;
    }

 }
