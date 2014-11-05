package com.skywomantech.app.symptommanagement.physician;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A fragment representing a single PhysicianPatient detail screen.
 * This fragment is either contained in a {@link PhysicianListPatientsActivity}
 * in two-pane mode (on tablets) or a {@link PhysicianPatientDetailActivity}
 * on handsets.
 */
public class PhysicianPatientDetailFragment extends Fragment {

    public final static String LOG_TAG = PhysicianPatientDetailFragment.class.getSimpleName();

    public static final String PATIENT_ID_KEY = "patient_id";

    private String  mPatientId;
    private Patient mPatient;

    @InjectView(R.id.physician_patient_detail_name)
    TextView mNameView;

    @InjectView(R.id.physician_patient_detail_birthdate)
    TextView mBDView;

    @InjectView(R.id.physician_patient_last_log)
    TextView mLastLog;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhysicianPatientDetailFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_physician_patient_detail, container, false);
        ButterKnife.inject(this, rootView);
        mPatient = getPatientFromCloud();
        return rootView;
    }

    private Patient getPatientFromCloud() {

        final SymptomManagementApi svc =
                SymptomManagementService.getService();

        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting Patient ID : " + mPatientId);
                    return svc.getPatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    mPatient = result;
                    Log.d(LOG_TAG, "got the Patient!" + mPatient.toDebugString());
                    if (mPatient != null) {
                        // set the views with the patient data
                        mNameView.setText(mPatient.getName());
                        mBDView.setText(mPatient.getBirthdate());
                        mLastLog.setText(mPatient.getFormattedLastLogged());  //TODO: needs real date here!
                    }
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(getActivity(),
                            "Unable to fetch the Patient data. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        return null;
    }
}
