package com.skywomantech.app.symptommanagement.admin;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.skywomantech.app.symptommanagement.Login;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.dummy.DummyContent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * A fragment representing a single admin_medication detail screen.
 * This fragment is either contained in a {@link com.skywomantech.app.symptommanagement.admin.AdminMedicationsListActivity}
 * in two-pane mode (on tablets) or a {@link com.skywomantech.app.symptommanagement.admin.AdminMedicationDetailActivity}
 * on handsets.
 */
public class AdminMedicationsDetailFragment extends Fragment {
    private static final String LOG_TAG = AdminMedicationsDetailFragment.class.getSimpleName();

    /**
     * The fragment argument representing the medication ID that this fragment
     * represents.
     */
    public static final String MED_ID_KEY = "med_id";

    /**
     * The Medication that this fragment is presenting.
     */
    private String mMedId;
    private Medication mMedication;

    @InjectView(R.id.admin_medication_detail) TextView mTextView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdminMedicationsDetailFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mMedId = savedInstanceState.getString(MED_ID_KEY);
            Log.d(LOG_TAG, "onActivityCreated 1-Med ID Key is : " + mMedId );
        }
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(MED_ID_KEY)) {
            mMedId = getArguments().getString(MED_ID_KEY);
            Log.d(LOG_TAG, "onActivityCreated 1b-Med ID Key is : " + mMedId );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(MED_ID_KEY)) {
            mMedId = getArguments().getString(MED_ID_KEY);
            Log.d(LOG_TAG, "onCreate-Med ID Key is : " + mMedId );
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMedId = arguments.getString(MED_ID_KEY);
            Log.d(LOG_TAG, "onCreateView 2-Med ID Key is : " + mMedId);
        }
        else if (savedInstanceState != null) {
            mMedId = savedInstanceState.getString(MED_ID_KEY);
            Log.d(LOG_TAG, "onCreateView 3-Med ID Key is : " + mMedId);
        }

        View rootView = inflater.inflate(R.layout.fragment_admin_medication_detail, container, false);
        mTextView = (TextView) rootView.findViewById(R.id.admin_medication_detail);
        if (mMedication != null) {
            mTextView.setText(mMedication.toString());
        }
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        Log.d(LOG_TAG, "onResume-Med ID Key is : " + mMedId);
        if (arguments != null && arguments.containsKey(MED_ID_KEY) && mMedId != null) {
            String x = getArguments().getString(MED_ID_KEY);
            Log.d(LOG_TAG, "onResume 1b-Med ID Key is : " + mMedId );
            loadMedicationFromAPI();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(MED_ID_KEY, mMedId);
        Log.d(LOG_TAG, "onSaveInstanceState-Med ID Key is : " + mMedId);
        super.onSaveInstanceState(outState);
    }

    private void loadMedicationFromAPI() {
        Log.d(LOG_TAG, "LoadFromAPI - Med ID Key is : " + mMedId);
        // hardcoded for my local host (see ipconfig for values) at port 8080
        // need to put this is prefs or somewhere it can me modified
        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        if (svc != null) {
            CallableTask.invoke(new Callable<Medication>() {

                @Override
                public Medication call() throws Exception {
                    Log.d(LOG_TAG, "getting single medication with id : " + mMedId);
                    return svc.getMedication(mMedId);
                }
            }, new TaskCallback<Medication>() {

                @Override
                public void success(Medication result) {
                    Log.d(LOG_TAG, "Found Medication :" + result.toString());
                    mMedication = result;
                    mTextView.setText(mMedication.toString());
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch Selected Medication. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }

    @OnClick(R.id.update_medication_button)
    public void updateMedication(Button button) {
        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        if (svc != null) {
            CallableTask.invoke(new Callable<Medication>() {

                @Override
                public Medication call() throws Exception {
                    Log.d(LOG_TAG, "updating medication FROM " + mMedication.toDebugString());
                    mMedication.setName(mTextView.getText().toString());
                    Log.d(LOG_TAG, "updating medication TO " + mMedication.toDebugString());
                    return svc.updateMedication(mMedId, mMedication);
                }
            }, new TaskCallback<Medication>() {

                @Override
                public void success(Medication result) {
                    Toast.makeText(
                            getActivity(),
                            "Medication [" + result.getName() + "] updated successfully.",
                            Toast.LENGTH_SHORT).show();
                    // re-GET the medications list .. shouldn't have the medication in it any more
                    getActivity().onBackPressed();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to update Medication. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    //re-GET the medications list ... medication should still be in the list
                    getActivity().onBackPressed();
                }
            });
        }
    }

    @OnClick(R.id.delete_medication_button)
    public void deleteMedication(Button button) {

        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        if (svc != null) {
            CallableTask.invoke(new Callable<Medication>() {

                @Override
                public Medication call() throws Exception {
                    Log.d(LOG_TAG, "deleting medication with id : " + mMedId);
                    return svc.deleteMedication(mMedId);
                }
            }, new TaskCallback<Medication>() {

                @Override
                public void success(Medication result) {
                    Toast.makeText(
                            getActivity(),
                            "Medication [" + result.getName() + "] deleted successfully.",
                            Toast.LENGTH_SHORT).show();
                    // re-GET the medications list .. shouldn't have the medication in it any more
                    getActivity().onBackPressed();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to delete Medication. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    //re-GET the medications list ... medication should still be in the list
                    getActivity().onBackPressed();
                }
            });
        }
    }

    @OnClick(R.id.exit_medication_detail_button)
    public void exitMedicationDetails(Button button) {
        getActivity().onBackPressed();
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
