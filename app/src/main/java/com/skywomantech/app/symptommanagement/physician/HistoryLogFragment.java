package com.skywomantech.app.symptommanagement.physician;

import android.app.ActionBar;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;

import java.util.concurrent.Callable;

public class HistoryLogFragment extends ListFragment {

    private static final String LOG_TAG = HistoryLogFragment.class.getSimpleName();

    public interface Callbacks {
        public Patient getPatientForHistory(String id);
        //public String getPatientIdForHistory();
    }

    private String  mPatientId = null;
    private Patient mPatient;

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private int mActivatedPosition = ListView.INVALID_POSITION;

    public HistoryLogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (getArguments().containsKey(PhysicianPatientDetailFragment.PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PhysicianPatientDetailFragment.PATIENT_ID_KEY);
        }
        this.setRetainInstance(true);  // save the fragment state with rotations
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        setEmptyText(getString(R.string.empty_list_text));
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
                setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
            }
            if (savedInstanceState.containsKey(PhysicianPatientDetailFragment.PATIENT_ID_KEY)) {
                mPatientId =
                        savedInstanceState.getString(PhysicianPatientDetailFragment.PATIENT_ID_KEY);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.empty_list_text));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (mPatientId == null &&
                arguments != null
                && arguments.containsKey(PhysicianPatientDetailFragment.PATIENT_ID_KEY) ) {
            mPatientId = arguments.getString(PhysicianPatientDetailFragment.PATIENT_ID_KEY);
        }
        // get your patient information from the calling activity
        mPatient = ((Callbacks) getActivity()).getPatientForHistory(mPatientId);
        // if mPatient is null it will go to cloud to find it
        refreshAllLogs(mPatient);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
        if (mPatientId != null) {
            outState.putString(PhysicianPatientDetailFragment.PATIENT_ID_KEY, mPatientId);
        }
    }


    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }
        mActivatedPosition = position;
    }

    private void refreshAllLogs(Patient patient) {
        // we were given the patient to work with (PATIENT USER)
        if (mPatientId == null) {
            // ask the activity to give it to us again.

        }
        if (patient != null) {
            HistoryLog[] logList = PatientDataManager.createLogList(mPatient);
            setListAdapter(new HistoryLogAdapter(getActivity(), logList));
        }
        // we need to go find the patient in the cloud (PHYSICIAN USER)
        else  if (mPatientId != null) { // try to find the history in the cloud
            final SymptomManagementApi svc = SymptomManagementService.getService();
            if (svc != null) {
                CallableTask.invoke(new Callable<Patient>() {

                    @Override
                    public Patient call() throws Exception {
                        Log.d(LOG_TAG, "getting patient");
                        return svc.getPatient(mPatientId);
                    }
                }, new TaskCallback<Patient>() {

                    @Override
                    public void success(Patient result) {
                        Log.d(LOG_TAG, "getting Patient and all logs");
                        mPatient = result;
                        HistoryLog[] logList = PatientDataManager.createLogList(mPatient);
                        setListAdapter(new HistoryLogAdapter(getActivity(), logList));
                    }

                    @Override
                    public void error(Exception e) {
                        Toast.makeText(
                                getActivity(),
                                "Unable to fetch the Patient Logs. Please check Internet connection.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
