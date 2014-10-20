package com.skywomantech.app.symptommanagement.admin.Medication;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

/**
 * A fragment representing a single admin_medication detail screen.
 * This fragment is either contained in a {@link AdminMedicationListActivity}
 * in two-pane mode (on tablets) or a {@link AdminMedicationDetailActivity}
 * on handsets.
 */
public class AdminMedicationDetailFragment extends Fragment {
    private static final String LOG_TAG = AdminMedicationDetailFragment.class.getSimpleName();

    public final static String MED_ID_KEY = AdminMedicationListActivity.MED_ID_KEY;

    private String mMedId;
    private Medication mMedication;

    public interface Callbacks {

        //indicates if the options menu should be shown or nor
        public boolean showEditMedicationOptionsMenu();

        // called when user selects Edit from options menu
        public void onEditMedication(String medId);
    }

    @InjectView(R.id.admin_medication_detail) TextView mTextView;

    public AdminMedicationDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMedId = arguments.getString(MED_ID_KEY);
        }
        else if (savedInstanceState != null) {
            mMedId = savedInstanceState.getString(MED_ID_KEY);
        }

        // set up the option menu according to the activity's choice
        setHasOptionsMenu(((Callbacks) getActivity()).showEditMedicationOptionsMenu());

        View rootView =
                inflater.inflate(R.layout.fragment_admin_medication_detail, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.admin_edit_delete_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_edit:
                ((Callbacks) getActivity()).onEditMedication(mMedId);
                return true;
            case R.id.action_delete:
                deleteMedication();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(MED_ID_KEY)) {
            mMedId = arguments.getString(MED_ID_KEY);
            loadMedicationFromAPI();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(MED_ID_KEY, mMedId);
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

    public void deleteMedication() {

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

    /**
     * required by ButterKnife to null out the view when destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
