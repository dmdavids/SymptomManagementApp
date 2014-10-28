package com.skywomantech.app.symptommanagement.patient;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.PainLog;
import com.skywomantech.app.symptommanagement.data.PatientCPContract;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class PatientPainLogFragment extends Fragment {

    public interface Callbacks {
        public boolean onPainLogComplete();
    }

    private PainLog mLog;
    private String mPatientId;

    public PatientPainLogFragment() {
        mLog = new PainLog();
        mLog.setSeverity(PainLog.Severity.NOT_DEFINED);
        mLog.setEating(PainLog.Eating.NOT_DEFINED);
        mPatientId = "234234234"; //TODO: get the actual patient id
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);  // save the fragment state with rotations
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pain_log, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @OnClick({R.id.well_controlled_button, R.id.moderate_button, R.id.severe_button})
    public void onSeverityRadioGroup(View v) {
        switch (v.getId()) {
            case R.id.well_controlled_button:
                mLog.setSeverity(PainLog.Severity.WELL_CONTROLLED);
                break;
            case R.id.moderate_button:
                mLog.setSeverity(PainLog.Severity.MODERATE);
                break;
            case R.id.severe_button:
                mLog.setSeverity(PainLog.Severity.SEVERE);
                break;
        }
    }

    @OnClick({R.id.eating_ok_button, R.id.eating_some_button, R.id.not_eating_button})
    public void onEatingRadioGroup(View v) {
        switch (v.getId()) {
            case R.id.eating_ok_button:
                mLog.setEating(PainLog.Eating.EATING);
                break;
            case R.id.eating_some_button:
                mLog.setEating(PainLog.Eating.SOME_EATING);
                break;
            case R.id.not_eating_button:
                mLog.setEating(PainLog.Eating.NOT_EATING);
                break;
        }
    }

    @OnClick(R.id.pain_log_done_button)
    public void savePainLog() {
        ContentValues cv = createValuesObject(mLog);
        // TODO: Put the log into the database via the content provider
        // tell the activity we're done
        boolean isCheckIn = ((Callbacks) getActivity()).onPainLogComplete();
        if (!isCheckIn) {
            getActivity().onBackPressed();
        }
    }

    private ContentValues createValuesObject(PainLog log) {
        ContentValues cv = new ContentValues();
        cv.put(PatientCPContract.PainLogEntry.COLUMN_EATING, log.getEating().getValue());
        cv.put(PatientCPContract.PainLogEntry.COLUMN_SEVERITY, log.getSeverity().getValue());
        cv.put(PatientCPContract.PainLogEntry.COLUMN_PATIENT_ID, mPatientId);
        cv.put(PatientCPContract.PainLogEntry.COLUMN_CREATED, System.currentTimeMillis());
        return cv;
    }
}
