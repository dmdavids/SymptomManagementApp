package com.skywomantech.app.symptommanagement;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.skywomantech.app.symptommanagement.admin.AdminMain;
import com.skywomantech.app.symptommanagement.patient.PatientMainActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.skywomantech.app.symptommanagement.R.id.patient_button;


public class Login extends Activity {

    public final static String LOG_TAG = Login.class.getSimpleName();
    boolean loggedIn = false;

    public final static String SERVER_ADDRESS = "http://192.168.0.34:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialize the preferences wherever the program has an entrance
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SetPreferenceActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_login, container, false);
            ButterKnife.inject(this, rootView);
            return rootView;
        }

        @OnClick(R.id.admin_button)
        public void loginAdministrator() {
            startActivity(new Intent(getActivity(), AdminMain.class));
        }

        // TODO: Doctor Login
        @OnClick(R.id.physician_button)
        public void loginPhysician() {
            // do nothing for now
        }


        /// TODO: Patient Login
        @OnClick(R.id.patient_button)
        public void loginPatient() {
            startActivity(new Intent(getActivity(), PatientMainActivity.class));
        }
    }



}
