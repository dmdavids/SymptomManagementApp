package com.skywomantech.app.symptommanagement;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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


public class Login extends Activity {

    public final static String LOG_TAG = Login.class.getSimpleName();

    public final static String SERVER_ADDRESS = "http://192.168.0.34:8080";
    private static String mPatientId = "9203420938402938403289";


    // TODO: needs better design
    private static boolean mIsLoggedIn = false;
    private static boolean mHasAdmin = false;
    private static boolean mIsPatient = true;

    public enum LoginType {
        PATIENT(100), PHYSICIAN(200), ADMIN(300), LOGGED_OUT(400);

        private final int value;

        private LoginType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static LoginType findByValue(int val){
            for(LoginType r : values()){
                if( r.getValue() == val ){
                    return r;
                }
            }
            return LOGGED_OUT;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialize the preferences wherever the program has an entrance
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new LoginFragment())
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

    private static void setCheckin(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isCheckin", value);
        editor.apply();
    }
    public static void setPatientId(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("patient_id", value);
        editor.apply();
    }

    public static String getPatientId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("patient_id", null);
    }

    public static void setPhysicianId(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("doctor_id", value);
        editor.apply();
    }

    public static String getPhysicianId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("doctor_id", null);
    }


    public static boolean isPatientLoggedIn(){
        return mIsPatient;
    }

    private void setUserType(LoginType type) {
        mIsPatient = false;
        if (type == LoginType.PATIENT) {
            setPatientId(this, "12323123123");
            mIsPatient = true;
        }
    }

    private static LoginType getUserType() {
        if (!mIsLoggedIn) return LoginType.LOGGED_OUT;
        if (mIsPatient) return LoginType.PATIENT;
        if (!mHasAdmin) return LoginType.PHYSICIAN;
        return LoginType.ADMIN;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LoginFragment extends Fragment {



        public LoginFragment() {
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
            mHasAdmin = true;
            mIsPatient = false;
            mPatientId = "";
            mIsLoggedIn = true;
            startActivity(new Intent(getActivity(), AdminMain.class));
        }

        // TODO: Doctor Login
        @OnClick(R.id.physician_button)
        public void loginPhysician() {
            mHasAdmin = false;
            mIsPatient = false;
            mIsLoggedIn = true;
        }

        /// TODO: Patient Login
        @OnClick(R.id.patient_button)
        public void loginPatient() {
            mHasAdmin = false;
            mIsLoggedIn = true;
            setCheckin(getActivity(), false);
            setPatientId(getActivity(), mPatientId);
            startActivity(new Intent(getActivity(), PatientMainActivity.class));
        }

        // Do the Check IN process
        @OnClick(R.id.checkin_button)
        public void loginPatientCheckin() {
            mHasAdmin = false;
            mIsLoggedIn = true;
            setCheckin(getActivity(), true);
            setPatientId(getActivity(), mPatientId);
            startActivity(new Intent(getActivity(), PatientMainActivity.class));
        }
    }

}
