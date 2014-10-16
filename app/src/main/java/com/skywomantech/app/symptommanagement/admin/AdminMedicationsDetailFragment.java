package com.skywomantech.app.symptommanagement.admin;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    private BigInteger mMedId;
    private Medication mMedication;

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
            String x = savedInstanceState.getString(MED_ID_KEY);
            mMedId = new BigInteger(x);
            Log.d(LOG_TAG, "onActivityCreated 1-Med ID Key is : " + mMedId.toString() );
        }
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(MED_ID_KEY)) {
            String x = getArguments().getString(MED_ID_KEY);
            mMedId = new BigInteger(x);
            Log.d(LOG_TAG, "onActivityCreated 1b-Med ID Key is : " + mMedId.toString() );
            loadMedicationFromAPI();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(MED_ID_KEY)) {
            String x = getArguments().getString(MED_ID_KEY);
            mMedId = new BigInteger(x);
            Log.d(LOG_TAG, "onCreate-Med ID Key is : " + mMedId.toString() );
            loadMedicationFromAPI();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            String x = arguments.getString(MED_ID_KEY);
            mMedId = new BigInteger(x);
            Log.d(LOG_TAG, "onCreateView 2-Med ID Key is : " + mMedId.toString());
        }
        else if (savedInstanceState != null) {
            String x  = savedInstanceState.getString(MED_ID_KEY);
            mMedId = new BigInteger(x);
            Log.d(LOG_TAG, "onCreateView 3-Med ID Key is : " + mMedId.toString());
        }

        View rootView = inflater.inflate(R.layout.fragment_admin_medication_detail, container, false);

        if (mMedication != null) {
            ((TextView) rootView.findViewById(R.id.admin_medication_detail)).setText(mMedication.toString());
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        Log.d(LOG_TAG, "onResume-Med ID Key is : " + mMedId.toString());
        if (arguments != null && arguments.containsKey(MED_ID_KEY) && mMedId != null) {
            String x = getArguments().getString(MED_ID_KEY);
            mMedId = new BigInteger(x);
            Log.d(LOG_TAG, "onResume 1b-Med ID Key is : " + mMedId.toString() );
            loadMedicationFromAPI();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(MED_ID_KEY, mMedId.toString());
        Log.d(LOG_TAG, "onSaveInstanceState-Med ID Key is : " + mMedId.toString());
        super.onSaveInstanceState(outState);
    }

    private void loadMedicationFromAPI() {
        Log.d(LOG_TAG, "LoadFromAPI - Med ID Key is : " + mMedId.toString());
        // hardcoded for my local host (see ipconfig for values) at port 8080
        // need to put this is prefs or somewhere it can me modified
        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        if (svc != null) {
            CallableTask.invoke(new Callable<Medication>() {

                @Override
                public Medication call() throws Exception {
                    Log.d(LOG_TAG, "getting single medication with id : "
                            + mMedId.toString());
                    return svc.getMedication(mMedId);
                }
            }, new TaskCallback<Medication>() {

                @Override
                public void success(Medication result) {
                    Log.d(LOG_TAG, "Found Medication :" + result.toString());
                    mMedication = result;
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch Selected Medication. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getActivity(), AdminMedicationsListActivity.class));
                }
            });
        }
    }
}
