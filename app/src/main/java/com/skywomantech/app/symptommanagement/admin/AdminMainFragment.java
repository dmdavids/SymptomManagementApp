package com.skywomantech.app.symptommanagement.admin;

import android.app.Fragment;
import android.content.Intent;
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
public class AdminMainFragment extends Fragment {

    public AdminMainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_admin_main, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @OnClick(R.id.edit_patients_button)
    public void addEditPatients() {
        startActivity(new Intent(getActivity(), AdminPatientListActivity.class));
    }

    @OnClick(R.id.edit_physicians_button)
    public void addEditPhysicians() {
        startActivity(new Intent(getActivity(), AdminPhysicianListActivity.class));
    }

    @OnClick(R.id.edit_medications_button)
    public void addEditMedications() {
        startActivity(new Intent(getActivity(), AdminMedicationsListActivity.class));
    }

}
