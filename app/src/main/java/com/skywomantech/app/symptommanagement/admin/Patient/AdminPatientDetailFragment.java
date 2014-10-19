package com.skywomantech.app.symptommanagement.admin.Patient;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.skywomantech.app.symptommanagement.Login;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.Physician;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A fragment representing a single AdminPatient detail screen.
 * This fragment is either contained in a {@link AdminPatientListActivity}
 * in two-pane mode (on tablets) or a {@link AdminPatientDetailActivity}
 * on handsets.
 */
public class AdminPatientDetailFragment extends Fragment {
    private static final String LOG_TAG = AdminPatientDetailFragment.class.getSimpleName();

    public interface Callbacks {
        // called when user selects Edit from options menu
        public void onEditPatient(String id);
    }

    public final static String PATIENT_ID_KEY = AdminPatientListActivity.PATIENT_ID_KEY;

    private String mPatientId;
    private Patient mPatient;


    @InjectView(R.id.admin_patient_detail) TextView mTextView;
    // only show a max of 6 physicians.. no user requirements so I can do whatever
    @InjectView(R.id.admin_patient_physician_name_1) TextView mPhysician1;
    @InjectView(R.id.admin_patient_physician_name_2) TextView mPhysician2;
    @InjectView(R.id.admin_patient_physician_name_3) TextView mPhysician3;
    @InjectView(R.id.admin_patient_physician_name_4) TextView mPhysician4;
    @InjectView(R.id.admin_patient_physician_name_5) TextView mPhysician5;
    @InjectView(R.id.admin_patient_physician_name_6) TextView mPhysician6;


    public AdminPatientDetailFragment() {
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mPatientId = savedInstanceState.getString(PATIENT_ID_KEY);
        }
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mPatientId = arguments.getString(PATIENT_ID_KEY);
        } else if (savedInstanceState != null) {
            mPatientId = savedInstanceState.getString(PATIENT_ID_KEY);
        }

        View rootView = inflater.inflate(R.layout.fragment_admin_patient_detail, container, false);
        setHasOptionsMenu(true); // this fragment has menu items to display
        mTextView = (TextView) rootView.findViewById(R.id.admin_patient_detail);
        if (mPatient != null) {
            mTextView.setText(mPatient.toString());
        }
        ButterKnife.inject(this, rootView);
        return rootView;
    }


    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.admin_edit_delete_menu, menu);
    }


    // handle menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                ((Callbacks) getActivity()).onEditPatient(mPatientId);
                return true;
            case R.id.action_delete:
                deletePatient();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PATIENT_ID_KEY)) {
            loadPatientFromAPI();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PATIENT_ID_KEY, mPatientId);
        super.onSaveInstanceState(outState);
    }

    private void loadPatientFromAPI() {
        Log.d(LOG_TAG, "LoadFromAPI - Patient ID is : " + mPatientId);
        // hardcoded for my local host (see ipconfig for values) at port 8080
        // need to put this is prefs or somewhere it can me modified
        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting single Patient id : " + mPatientId);
                    return svc.getPatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "Found Patient :" + result.toString());
                    mPatient = result;
                    mTextView.setText(mPatient.getName());
                    if (mPatient != null && mPatient.getPhysicians() != null &&
                            mPatient.getPhysicians().size() > 0) {
                       displayPhysicians(mPatient.getPhysicians());
                    }
                    else  {
                        mPhysician1.setText("No Physicians Assigned to Patient.");
                        mPhysician2.setText("");
                        mPhysician3.setText("");
                        mPhysician4.setText("");
                        mPhysician5.setText("");
                        mPhysician6.setText("");
                    }
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch Selected Patient. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void displayPhysicians(Set<Physician> physicians) {
        int numberOf = physicians.size();
        Physician[] drs = physicians.toArray(new Physician[numberOf]);
        mPhysician1.setText(drs[0].getName());
        if (numberOf > 1) mPhysician2.setText(drs[1].getName());
        if (numberOf > 2) mPhysician3.setText(drs[2].getName());
        if (numberOf > 3) mPhysician4.setText(drs[3].getName());
        if (numberOf > 4) mPhysician5.setText(drs[4].getName());
        if (numberOf > 5) mPhysician6.setText(drs[5].getName());
    }

    public void deletePatient() {

        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "deleting Physician id : " + mPatientId);
                    return svc.deletePatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Toast.makeText(
                            getActivity(),
                            "Patient [" + result.getName() + "] deleted successfully.",
                            Toast.LENGTH_SHORT).show();
                    // re-GET the medications list .. shouldn't have the medication in it any more
                    getActivity().onBackPressed();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to delete Patient. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    //re-GET the medications list ... medication should still be in the list
                    getActivity().onBackPressed();
                }
            });
        }
    }

    /**
     * required by ButterKnife to null out the view when destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
