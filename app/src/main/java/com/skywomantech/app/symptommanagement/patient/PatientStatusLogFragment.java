package com.skywomantech.app.symptommanagement.patient;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skywomantech.app.symptommanagement.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PatientStatusLogFragment extends Fragment {

    public PatientStatusLogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status_log_entry, container, false);
        return rootView;
    }
}
