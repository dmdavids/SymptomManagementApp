package com.skywomantech.app.symptommanagement.admin.Physician;

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
import com.skywomantech.app.symptommanagement.data.Physician;

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
public class AdminPhysicianAddEditFragment extends Fragment {
    private static final String LOG_TAG = AdminPhysicianAddEditFragment.class.getSimpleName();

    public final static String PHYSICIAN_ID_KEY = AdminPhysicianListActivity.PHYSICIAN_ID_KEY;

    private Physician mPhysician;
    private String mPhysicianId;

    @InjectView(R.id.admin_physician_edit_name)  EditText mPhysicianName;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdminPhysicianAddEditFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mPhysicianId = savedInstanceState.getString(PHYSICIAN_ID_KEY);
        }
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PHYSICIAN_ID_KEY)) {
            mPhysicianId = getArguments().getString(PHYSICIAN_ID_KEY);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(PHYSICIAN_ID_KEY)) {
            mPhysicianId = getArguments().getString(PHYSICIAN_ID_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // fragment has menu items to display

        // else editing
        View rootView = inflater.inflate(R.layout.fragment_admin_physician_add_edit, container, false);
        ButterKnife.inject(this, rootView);

        mPhysician = new Physician();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PHYSICIAN_ID_KEY) && mPhysicianId != null) {
            mPhysicianId = getArguments().getString(PHYSICIAN_ID_KEY);
            loadPhysicianFromAPI();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PHYSICIAN_ID_KEY) && mPhysicianId != null) {
            mPhysicianId = getArguments().getString(PHYSICIAN_ID_KEY);
            loadPhysicianFromAPI();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PHYSICIAN_ID_KEY, mPhysicianId);
        super.onSaveInstanceState(outState);
    }

    private void loadPhysicianFromAPI() {
        if (mPhysicianId == null) return;
        Log.d(LOG_TAG, "LoadFromAPI - Physician ID Key is : " + mPhysicianId);
        // hardcoded for my local host (see ipconfig for values) at port 8080
        // need to put this is prefs or somewhere it can me modified
        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        if (svc != null) {
            CallableTask.invoke(new Callable<Physician>() {

                @Override
                public Physician call() throws Exception {
                    Log.d(LOG_TAG, "getting single physician with id : " + mPhysicianId);
                    return svc.getPhysician(mPhysicianId);
                }
            }, new TaskCallback<Physician>() {

                @Override
                public void success(Physician result) {
                    Log.d(LOG_TAG, "Found Physician :" + result.toString());
                    mPhysician = result;
                    mPhysicianName.setText(mPhysician.toString());
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch Physician for editing. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }

    @OnClick(R.id.save_physician_button)
    public void savePhysician(Button button) {
        if (mPhysicianName.getText().toString().trim().length() == 0) {
            DialogFragment errorSaving =
                    new DialogFragment()
                    {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState)
                        {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(getActivity());
                            builder.setMessage("Please Enter a Physician Name to Save.");
                            builder.setPositiveButton("OK", null);
                            return builder.create();
                        }
                    };

            errorSaving.show(getFragmentManager(), "error saving physician");
            return;
        }

        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        final String successMsg = (mPhysicianId == null ? "ADDED" : "UPDATED");
        if (svc != null) {
            CallableTask.invoke(new Callable<Physician>() {

                @Override
                public Physician call() throws Exception {
                    mPhysician.setId(mPhysicianId);
                    mPhysician.setName(mPhysicianName.getText().toString());
                    if (mPhysicianId == null) {
                        Log.d(LOG_TAG, "adding physician :" + mPhysician.toDebugString());
                        return svc.addPhysician(mPhysician);
                    }else {
                        Log.d(LOG_TAG, "updating physician :" + mPhysician.toDebugString());
                        return svc.updatePhysician(mPhysicianId, mPhysician);
                    }
                }
            }, new TaskCallback<Physician>() {

                @Override
                public void success(Physician result) {
                    Toast.makeText(
                            getActivity(),
                            "Physician [" + result.getName() + "] " + successMsg + " successfully.",
                            Toast.LENGTH_SHORT).show();
                    // re-GET the physicians list .. shouldn't have the medication in it any more
                    getActivity().onBackPressed();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to SAVE Physician. Please check Internet connection.",
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
