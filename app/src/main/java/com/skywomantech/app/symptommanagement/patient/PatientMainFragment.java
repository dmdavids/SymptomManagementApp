package com.skywomantech.app.symptommanagement.patient;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skywomantech.app.symptommanagement.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class PatientMainFragment extends Fragment {

    public PatientMainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_main, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @OnClick(R.id.pain_log_button)
    public void enterPainLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new PatientPainLogFragment())
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.medication_log_button)
    public void enterMedicationLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new PatientMedicationLogFragment())
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.status_log_button)
    public void enterStatusLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new PatientStatusLogFragment())
                .addToBackStack(null)
                .commit();
    }

}
