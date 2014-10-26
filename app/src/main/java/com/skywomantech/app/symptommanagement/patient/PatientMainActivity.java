package com.skywomantech.app.symptommanagement.patient;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.skywomantech.app.symptommanagement.R;


public class PatientMainActivity extends Activity  {

    public static final String PATIENT_ID_KEY = "patient_id_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(PATIENT_ID_KEY,
                    getIntent().getStringExtra(PATIENT_ID_KEY));
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PatientMainFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.patient_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
