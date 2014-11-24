package com.skywomantech.app.symptommanagement.admin;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.LoginActivity;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.patient.PatientMainFragment;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

/**
 * The admin stuff was written when I first started this app
 * and its not a requirement.  Due to the limited amount of time
 * I choose not to go back and fix it up because its adequate for what it
 * needs to do at this time.
 *
 */
public class AdminMain extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.admin_main_container, new AdminMainFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            LoginActivity.restartLoginActivity(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().findFragmentById(R.id.admin_main_container)
                instanceof AdminMainFragment) {
            startActivity(new Intent()
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME));
        } else super.onBackPressed();
    }
}
