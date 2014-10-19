package com.skywomantech.app.symptommanagement.admin.Patient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import butterknife.OnClick;

/**
 * A fragment representing a single admin_medication detail screen.
 * This fragment is either contained in a {@link com.skywomantech.app.symptommanagement.admin.Medication.AdminMedicationListActivity}
 * in two-pane mode (on tablets) or a {@link com.skywomantech.app.symptommanagement.admin.Medication.AdminMedicationDetailActivity}
 * on handsets.
 */
public class AdminPatientAddEditFragment extends Fragment {
    private static final String LOG_TAG = AdminPatientAddEditFragment.class.getSimpleName();

    public final static String PATIENT_ID_KEY = AdminPatientListActivity.PATIENT_ID_KEY;

    private Patient mPatient;
    private String mPatientId;

    @InjectView(R.id.admin_patient_edit_name)  EditText mPatientName;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdminPatientAddEditFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mPatientId = savedInstanceState.getString(PATIENT_ID_KEY);
        }
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
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

        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // fragment has menu items to display

        // else editing
        View rootView = inflater.inflate(R.layout.fragment_admin_patient_add_edit, container, false);
        ButterKnife.inject(this, rootView);

        mPatient = new Patient();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PATIENT_ID_KEY) && mPatientId != null) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
            loadPatientFromAPI();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PATIENT_ID_KEY) && mPatientId != null) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
            loadPatientFromAPI();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PATIENT_ID_KEY, mPatientId);
        super.onSaveInstanceState(outState);
    }

    private void loadPatientFromAPI() {
        if (mPatientId == null) return;
        Log.d(LOG_TAG, "LoadFromAPI - Physician ID Key is : " + mPatientId);
        // hardcoded for my local host (see ipconfig for values) at port 8080
        // need to put this is prefs or somewhere it can me modified
        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting single patient with id : " + mPatientId);
                    return svc.getPatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "Found Patient :" + result.toString());
                    mPatient = result;
                    mPatientName.setText(mPatient.toString());
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch Patient for editing. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }

    @OnClick(R.id.save_patient_button)
    public void savePatient(Button button) {
        if (mPatientName.getText().toString().trim().length() == 0) {
            DialogFragment errorSaving =
                    new DialogFragment()
                    {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState)
                        {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(getActivity());
                            builder.setMessage("Please Enter a Patient Name to Save.");
                            builder.setPositiveButton("OK", null);
                            return builder.create();
                        }
                    };

            errorSaving.show(getFragmentManager(), "error saving patient");
            return;
        }

        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        final String successMsg = (mPatientId == null ? "ADDED" : "UPDATED");
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    mPatient.setId(mPatientId);
                    mPatient.setName(mPatientName.getText().toString());
                    if (mPatientId == null) {
                        Log.d(LOG_TAG, "adding patient :" + mPatient.toDebugString());
                        return svc.addPatient(mPatient);
                    }else {
                        Log.d(LOG_TAG, "updating patient :" + mPatient.toDebugString());
                        return svc.updatePatient(mPatientId, mPatient);
                    }
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Toast.makeText(
                            getActivity(),
                            "Patient [" + result.getName() + "] " + successMsg + " successfully.",
                            Toast.LENGTH_SHORT).show();
                    // re-GET the physicians list .. shouldn't have the medication in it any more
                    getActivity().onBackPressed();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to SAVE Patient. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    //re-GET the physicians list ... medication should still be in the list
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
