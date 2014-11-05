package com.skywomantech.app.symptommanagement;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.admin.AdminMain;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;

import com.skywomantech.app.symptommanagement.data.UserCredential;
import com.skywomantech.app.symptommanagement.patient.PatientMainActivity;

import com.skywomantech.app.symptommanagement.physician.PhysicianListPatientsActivity;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.Collection;
import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class Login extends Activity {

    public final static String LOG_TAG = Login.class.getSimpleName();

    private static UserCredential mCredential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialize the preferences wherever the program has an entrance
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        String username = getIntent().getExtras().getString("username");
        Log.d(LOG_TAG, "Saving the username to preferences");
        setUsername(this, username);

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
    public static void setLoginId(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("login_id", value);
        editor.apply();
    }

    public static String getLoginId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("login_id", null);
    }

    public static void setUsername(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", value);
        editor.apply();
    }

    public static String getUsername(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("username", null);
    }

    public static void setUserRole(Context context, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("role", value);
        editor.apply();
    }

    public static int getUserRole(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("role", UserCredential.UserRole.NOT_ASSIGNED.getValue());
    }

    public static void setRememberMe(Context context, boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("remember_me", value);
        editor.apply();
    }

    public static boolean getRememberMe(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("remember_me", false);
    }

    public static void setLastDeviceLogin(Context context, long value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_device_login", value);
        editor.apply();
    }

    public static long getLastDeviceLogin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong("last_device_login", 0L);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LoginFragment extends Fragment {

        String mUserName;
        long mLastLogin = 0L;
        String mLoginId;
        UserCredential.UserRole mRole;
        boolean mRememberMe = false;

        public LoginFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_login, container, false);
            ButterKnife.inject(this, rootView);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            mUserName = Login.getUsername(getActivity());
            mLastLogin = Login.getLastDeviceLogin(getActivity());
            mLoginId = Login.getLoginId(getActivity());
            mRole = UserCredential.UserRole.findByValue(Login.getUserRole(getActivity()));
            mRememberMe = Login.getRememberMe(getActivity());

            // already logged in
            if (mRole != UserCredential.UserRole.NOT_ASSIGNED &&
                    mLoginId != null && !mLoginId.isEmpty()){
                // we can just continue on without a credential check
                Log.d(LOG_TAG, "We are just going right to the correct user screens.");
                mCredential = new UserCredential();
                mCredential.setUserId(mLoginId);
                mCredential.setUserType(mRole);
                mCredential.setUserRoleValue(mRole.getValue()); // STUPID WORK AROUND
                mCredential.setUserName(mUserName);
                processLoginRedirect(mCredential);
            } else {
                // doesn't matter what we have stored we need to go out and get the credentials
                // should have already got the token and the username successfully at this point
                Log.d(LOG_TAG, "We have to get the user credential before " +
                        "we can go to the correct user screens");
                getUserCredentialsAndProcessLogin(mUserName);
            }
        }

        private void processLoginRedirect(UserCredential credential) {
            Log.d(LOG_TAG, "Process Login REDIRECTing to appropriate screen flow.");
            if (credential == null) {
                Log.d(LOG_TAG, "Invalid credentials we need to login again");
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }

            if (credential.getUserType() == UserCredential.UserRole.ADMIN) {
                Log.d(LOG_TAG, "Starting Admin screen flow");
                SymptomManagementSyncAdapter.setPatientDevice(false);
                startActivity(new Intent(getActivity(), AdminMain.class));
            } else if (credential.getUserType() == UserCredential.UserRole.PATIENT) {
                Log.d(LOG_TAG, "Starting Patient screen flow");
                SymptomManagementSyncAdapter.setPatientDevice(true);
                startActivity(new Intent(getActivity(), PatientMainActivity.class));
            } else if (credential.getUserType() == UserCredential.UserRole.PHYSICIAN) {
                Log.d(LOG_TAG, "Starting Doctor screen flow");
                SymptomManagementSyncAdapter.setPatientDevice(false);
                startActivity(new Intent(getActivity(), PhysicianListPatientsActivity.class));
            } else {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        }

        private void getUserCredentialsAndProcessLogin(String username) {

            final String mUsername = username;

            final SymptomManagementApi svc =
                    SymptomManagementService.getService();

            if (svc != null) {
                CallableTask.invoke(new Callable<Collection<UserCredential>>() {

                    @Override
                    public Collection<UserCredential> call() throws Exception {
                        Log.d(LOG_TAG, "getting user credential for username: " + mUsername);
                        return svc.findByUserName(mUsername);
                    }
                }, new TaskCallback<Collection<UserCredential>>() {

                    @Override
                    public void success(Collection<UserCredential> result) {
                        Log.d(LOG_TAG, "getting user credentials");
                        if (result != null && result.size() == 1) {
                            mCredential = result.iterator().next();
                            Log.d(LOG_TAG, "Credential Received is : " + result.toString());

                            // STUPID WORK AROUND ... the enum value does not get set correctly ARGH!
                            // have to sent the int value over to make it work or write a gson converter
                            mCredential.setUserType(UserCredential.UserRole.findByValue(mCredential.getUserRoleValue()));

                            // store the credential  info needed for future processing
                            setLoginId(getActivity(), mCredential.getUserId());
                            setUserRole(getActivity(), mCredential.getUserRoleValue());


                            mUserName = Login.getUsername(getActivity());
                            mLastLogin = Login.getLastDeviceLogin(getActivity());
                            mLoginId = Login.getLoginId(getActivity());
                            mRole = UserCredential.UserRole.findByValue(Login.getUserRole(getActivity()));
                            processLoginRedirect(mCredential);
                        } else {
                            Log.d(LOG_TAG, "Error getting user credentials.");
                            // TODO: go to the LoginActivity class HERE
                        }
                    }

                    @Override
                    public void error(Exception e) {
                        Toast.makeText(
                                getActivity(),
                                "Unable to fetch the User Credentials. Please check Internet connection.",
                                Toast.LENGTH_LONG).show();
                        // TODO: go to the LoginActivity class HERE
                    }
                });
            }
        }
    }

}
