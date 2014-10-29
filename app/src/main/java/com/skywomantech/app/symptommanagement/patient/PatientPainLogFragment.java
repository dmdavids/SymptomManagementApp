package com.skywomantech.app.symptommanagement.patient;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.PainLog;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.PainLogEntry;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class PatientPainLogFragment extends Fragment {

    public final static String LOG_TAG = PatientPainLogFragment.class.getSimpleName();

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
        // save Pain Log to the CP
        ContentValues cv = createValuesObject(mLog);
        Uri uri = getActivity().getContentResolver().insert(PainLogEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        if (objectId < 0) {
            Log.e(LOG_TAG, "Pain Log Insert Failed.");
        }
        // tell the activity we're done and if check-in put up the med log fragment
        boolean isCheckIn = ((Callbacks) getActivity()).onPainLogComplete();
        if (!isCheckIn) {
            getActivity().onBackPressed();
        }
    }

    private ContentValues createValuesObject(PainLog log) {
        ContentValues cv = new ContentValues();
        cv.put(PainLogEntry.COLUMN_EATING, log.getEating().getValue());
        cv.put(PainLogEntry.COLUMN_SEVERITY, log.getSeverity().getValue());
        cv.put(PainLogEntry.COLUMN_PATIENT_ID, mPatientId);
        cv.put(PainLogEntry.COLUMN_CREATED, System.currentTimeMillis());
        return cv;
    }
}
