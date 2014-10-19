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
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    public final static String PATIENT_ID_KEY = AdminPatientListActivity.PATIENT_ID_KEY;

    private String mPatientId;
    private Patient mPatient;

    public interface Callbacks {
        // called when user selects Edit from options menu
        public void onEditPatient(String id);
    }

    @InjectView(R.id.admin_patient_detail) TextView mTextView;
    @InjectView(R.id.patient_physicians_list)   ListView mPhysiciansListView;


    public AdminPatientDetailFragment() {
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
        setHasOptionsMenu(true);
        ButterKnife.inject(this, rootView);
        return rootView;
    }


    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.admin_edit_delete_menu, menu);
    }


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
            mPatientId = arguments.getString(PATIENT_ID_KEY);
            loadPatientFromAPI();
            displayPhysicians();
        }
    }

    private void displayPhysicians() {
        if (mPatient == null || mPatient.getPhysicians() == null) {
            final ArrayList<String> emptyList = new ArrayList<String>();
            emptyList.add("No Physicians for this Patient.");
            mPhysiciansListView
                    .setAdapter(new ArrayAdapter<String>(
                            getActivity(),
                            android.R.layout.simple_list_item_activated_1,
                            android.R.id.text1,
                            new ArrayList(emptyList)));
        } else {
            mPhysiciansListView
                    .setAdapter(new ArrayAdapter<Physician>(
                            getActivity(),
                            android.R.layout.simple_list_item_activated_1,
                            android.R.id.text1,
                            new ArrayList(mPatient.getPhysicians())));
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
