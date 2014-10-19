package com.skywomantech.app.symptommanagement.admin.Medication;

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
import com.skywomantech.app.symptommanagement.data.Medication;

import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * A fragment representing a single admin_medication detail screen.
 * This fragment is either contained in a {@link AdminMedicationListActivity}
 * in two-pane mode (on tablets) or a {@link AdminMedicationDetailActivity}
 * on handsets.
 */
public class AdminMedicationAddEditFragment extends Fragment {
    private static final String LOG_TAG = AdminMedicationAddEditFragment.class.getSimpleName();

    public final static String MED_ID_KEY = AdminMedicationListActivity.MED_ID_KEY;

    private Medication mMedication;
    private String mMedId;

    @InjectView(R.id.admin_medication_edit_name)  EditText mMedName;
    @InjectView(R.id.save_medication_button)  Button mSaveButton;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdminMedicationAddEditFragment() {
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

        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // fragment has menu items to display

        // else editing
        View rootView = inflater.inflate(R.layout.fragment_admin_medication_add_edit, container, false);
        mMedName = (EditText) rootView.findViewById(R.id.admin_medication_edit_name);
        ButterKnife.inject(this, rootView);

        mMedication = new Medication();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(MED_ID_KEY) && mMedId != null) {
            mMedId = getArguments().getString(MED_ID_KEY);
            Log.d(LOG_TAG, "onResume 1b-Med ID Key is : " + mMedId );
            loadMedicationFromAPI();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        Log.d(LOG_TAG, "onResume-Med ID Key is : " + mMedId);
        if (arguments != null && arguments.containsKey(MED_ID_KEY) && mMedId != null) {
            mMedId = getArguments().getString(MED_ID_KEY);
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
        if (mMedId == null) return;
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
                    mMedName.setText(mMedication.toString());
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch Medication for editing. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }

    @OnClick(R.id.save_medication_button)
    public void saveMedication(Button button) {
        if (mMedName.getText().toString().trim().length() == 0) {
            DialogFragment errorSaving =
                    new DialogFragment()
                    {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState)
                        {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(getActivity());
                            builder.setMessage("Please Enter a Medication Name to Save.");
                            builder.setPositiveButton("OK", null);
                            return builder.create();
                        }
                    };

            errorSaving.show(getFragmentManager(), "error saving medication");
            return;
        }

        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        final String successMsg = (mMedId == null ? "ADDED" : "UPDATED");
        if (svc != null) {
            CallableTask.invoke(new Callable<Medication>() {

                @Override
                public Medication call() throws Exception {
                    mMedication.setId(mMedId);
                    mMedication.setName(mMedName.getText().toString());
                    if (mMedId == null) {
                        Log.d(LOG_TAG, "adding medication :" + mMedication.toDebugString());
                        return svc.addMedication(mMedication);
                    }else {
                        Log.d(LOG_TAG, "updating medication :" + mMedication.toDebugString());
                        return svc.updateMedication(mMedId, mMedication);
                    }
                }
            }, new TaskCallback<Medication>() {

                @Override
                public void success(Medication result) {
                    Toast.makeText(
                            getActivity(),
                            "Medication [" + result.getName() + "] " + successMsg + " successfully.",
                            Toast.LENGTH_SHORT).show();
                    // re-GET the medications list .. shouldn't have the medication in it any more
                    getActivity().onBackPressed();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to SAVE Medication. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    //re-GET the medications list ... medication should still be in the list
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
