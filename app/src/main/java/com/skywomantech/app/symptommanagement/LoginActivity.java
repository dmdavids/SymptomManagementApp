package com.skywomantech.app.symptommanagement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;
import com.skywomantech.app.symptommanagement.admin.AdminMain;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.data.UserCredential;
import com.skywomantech.app.symptommanagement.patient.PatientMainActivity;
import com.skywomantech.app.symptommanagement.physician.PhysicianListPatientsActivity;
import com.skywomantech.app.symptommanagement.physician.PhysicianPatientDetailFragment;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.Collection;
import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private UserLoginTask mAuthTask = null;

    @InjectView(R.id.username)
    EditText mUsernameView;
    @InjectView(R.id.sm_password)
    EditText mPasswordView;
    @InjectView(R.id.login_form)
    View mProgressView;
    @InjectView(R.id.login_progress)
    View mLoginFormView;

    private String mUsername;
    private String mPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);
        ButterKnife.inject(this);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.goto_login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (LoginUtility.isLoggedIn(this)) {
            Log.d(LOG_TAG, "We are already logged in so we just need to redirect.");
            processLoginRedirect(LoginUtility.getUserRole(this));
        }
        else {
            // just make sure that we are completely logged out
            LoginUtility.logout(this);
        }
    }

    /**
     * Logout and remove everything on the backstack so LoginActivity
     * is the only screen
     *
     * @param context
     */
    public static void restartLoginActivity(Context context) {
        LoginUtility.logout(context);
        context.startActivity(new Intent(context, LoginActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @OnClick(R.id.username_sign_in_button)
    public void onClick(View view) {
        attemptLogin();
    }

    public void attemptLogin() {
        Log.d(LOG_TAG, "Attempting to Login  mAuthTask is " + ((mAuthTask != null) ? "Not NULL" : "NULL"));

        if (mAuthTask != null) return;

        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // always lowercase the user name
        String username = mUsernameView.getText().toString().toLowerCase();
        String password = mPasswordView.getText().toString();
        Log.d(LOG_TAG, "Username: " + username + " Password: " + password);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError("This username is invalid.");
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        return (username != null && !username.isEmpty());
    }

    // I guess we don't have to have a password so we'll just make sure that its not null
    private boolean isPasswordValid(String password) {
        return (password != null);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * An asynchronous login/registration task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private String mUsername;
        private String mPassword;

        UserLoginTask(String username, String password) {
            Log.d(LOG_TAG, "creating thread with username "
                    + username + "and password " + password);
            mUsername = username.toLowerCase();
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(LOG_TAG, "attempting an actually login to the server with username "
                    + mUsername + " password " + mPassword);
            // this is the version that actually does a logging into the service
            SymptomManagementApi svc = SymptomManagementService.getService(mUsername, mPassword);
            Log.d(LOG_TAG, "Service is : " +
                    (svc == null ? " NOT connected! " : " Successfully connected."));
            return (svc != null);
        }

        // process login results
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Log.d(LOG_TAG, "Server connection was SUCCESSFUL! " +
                        "But we still need the credentials.");
                LoginUtility.setUsername(getApplicationContext(), mUsername);
                getCredentialsAndRedirect(mUsername, mPassword);
            } else {
                Log.d(LOG_TAG, "Server connection FAILED!");
                // check the credentials via CP to see if they are a patient and we stored the credentials
                // if so then we can OK them without the internet
                Log.d(LOG_TAG, "Checking for local storage of the credentials.");
                UserCredential credential = checkCredentialsOnDevice(mUsername, mPassword);
                if (credential != null) {
                    Log.d(LOG_TAG, "Found credential in CP :" + credential.toString());
                    LoginUtility.setUsername(getApplicationContext(), mUsername);
                    LoginUtility.setLoggedIn(getApplicationContext(), credential);
                } else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    restartLoginActivity(getApplicationContext());
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private UserCredential checkCredentialsOnDevice(String username, String password) {
        Log.d(LOG_TAG, "Looking for username " + username + " password " + password);
        UserCredential credential =
                PatientDataManager.getUserCredentials(getApplicationContext(), username, password );
        return credential;
    }

    public void getCredentialsAndRedirect(String username, String password) {
        if(LoginUtility.isLoggedIn(this)) {
            // already logged in
            processLoginRedirect(LoginUtility.getUserRole(this));
        } else {
            Log.d(LOG_TAG, "We have to retrieve the user credential FROM THE SERVICE first.");
            getUserCredentialsAndProcessLogin(username, password);
        }
    }

    private void processLoginRedirect(UserCredential.UserRole role) {
        Log.d(LOG_TAG, "Process Login REDIRECTing to appropriate screen flow " +
                "for " + role.toString());
        if (role == UserCredential.UserRole.ADMIN) {
            Log.d(LOG_TAG, "Starting Admin screen flow");
            LoginUtility.setCheckin(getApplicationContext(), false);
            startActivity(new Intent(this, AdminMain.class));
        } else if (role == UserCredential.UserRole.PATIENT) {
            SymptomManagementSyncAdapter.syncImmediately(this);
            startActivity(new Intent(this, PatientMainActivity.class));
        } else if (role == UserCredential.UserRole.PHYSICIAN) {
            Log.d(LOG_TAG, "Starting Doctor screen flow");
            LoginUtility.setCheckin(getApplicationContext(), false);
            // pass the physician's id to the next activity
            String id = LoginUtility.getLoginId(this);
            Bundle arguments = new Bundle();
            arguments.putString(PhysicianPatientDetailFragment.PHYSICIAN_ID_KEY, id);
            Intent physicianIntent = new Intent(this, PhysicianListPatientsActivity.class);
            physicianIntent.putExtras(arguments);
            startActivity(physicianIntent);
            SymptomManagementSyncAdapter.syncImmediately(this);
        } else {
            Log.d(LOG_TAG, "INVALID ROLE ASSIGNED!!!");
            Toast.makeText(
                    this,
                    "Invalid Login. Please See Your Administrator for assistance.",
                    Toast.LENGTH_LONG).show();
            restartLoginActivity(this);
        }
    }

    private void getUserCredentialsAndProcessLogin(final String username, final String password) {
        Log.d(LOG_TAG, "Attempting to get the credentials to be fully logged in.");
        // we need the credentials or we can't be considered logged in!
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<UserCredential>>() {

                @Override
                public Collection<UserCredential> call() throws Exception {
                    String username = LoginUtility.getUsername(getApplicationContext());
                    Log.d(LOG_TAG, "getting user credential for username: " + username);
                    if (username == null || username.isEmpty()) return null;
                    return svc.findByUserName(username);
                }
            }, new TaskCallback<Collection<UserCredential>>() {

                @Override
                public void success(Collection<UserCredential> result) {
                    Log.d(LOG_TAG, "GOT user credentials");
                    if (result != null && result.size() == 1) {
                        UserCredential cred = result.iterator().next();
                        // This is a work around for the enum value in the credential
                        cred.setUserType(UserCredential.UserRole.findByValue(cred.getUserRoleValue()));
                        Log.d(LOG_TAG, "Credential Received is : " + cred.toString());
                        // store the information from the credential
                         if (LoginUtility.setLoggedIn(getApplicationContext(), cred)) {
                             // now we finally have enough information to redirect
                             LoginUtility.savePatientCredential(getApplicationContext(), cred);
                             processLoginRedirect(LoginUtility.getUserRole(getApplicationContext()));
                         }
                        else {
                             Log.d(LOG_TAG, "ERROR saving the user credentials.");
                             Toast.makeText(
                                     getApplicationContext(),
                                     "Invalid Login. Please See Your Administrator.",
                                     Toast.LENGTH_LONG).show();
                             restartLoginActivity(getApplicationContext());
                         }
                    } else {
                        Log.d(LOG_TAG, "ERROR getting user credentials.");
                        Toast.makeText(
                                getApplicationContext(),
                                "Invalid Login. Please Try Again.",
                                Toast.LENGTH_LONG).show();
                        restartLoginActivity(getApplicationContext());
                    }
                }

                @Override
                public void error(Exception e) {
                    Log.d(LOG_TAG, "Cloud Credential Check FAILED... so we can try the CP!!");
                    UserCredential credential = checkCredentialsOnDevice(username, password);
                    if (credential != null) {
                        Log.d(LOG_TAG, "Found credential in CP :" + credential.toString());
                        LoginUtility.setUsername(getApplicationContext(), mUsername);
                        LoginUtility.setLoggedIn(getApplicationContext(), credential);
                        processLoginRedirect(LoginUtility.getUserRole(getApplicationContext()));
                    } else {
                        Log.d(LOG_TAG, "CP Credential CHECK failed TOO! Just have to restart app.");
                        Toast.makeText(
                                getApplicationContext(),
                                "Unable to Login. Please check Internet connection.",
                                Toast.LENGTH_LONG).show();
                        restartLoginActivity(getApplicationContext());
                    }
                }
            });
        }
    }
}



