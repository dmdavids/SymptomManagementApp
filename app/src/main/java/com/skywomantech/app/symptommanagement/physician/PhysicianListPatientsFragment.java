package com.skywomantech.app.symptommanagement.physician;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;

import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.data.UserCredential;


import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * A list fragment representing a list of PhysicianPatients. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link PhysicianPatientDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PhysicianListPatientsFragment extends ListFragment {

    private static final String LOG_TAG = PhysicianListPatientsFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "fragment_patient_list";

    String mPhysicianId;
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {
        public void onItemSelected(String physicianId, String patientId);
    }

    public PhysicianListPatientsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        setEmptyText(getString(R.string.empty_list_text));
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.empty_list_text));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAllPatients();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Patient patient =
                (Patient) getListAdapter().getItem(position);
        Log.d(LOG_TAG, "Patient name selected is " + patient.getName()
                + " id is : " + patient.getId());
        String patientId = patient.getId();
        Log.d(LOG_TAG, " String id value is : " + patientId);
        mPhysicianId = LoginUtility.getLoginId(getActivity());
        ((Callbacks) getActivity()).onItemSelected(mPhysicianId, patientId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }
        mActivatedPosition = position;
    }

    private void refreshAllPatients() {

        if ( LoginUtility.isLoggedIn(getActivity())
                && LoginUtility.getUserRole(getActivity()) == UserCredential.UserRole.PHYSICIAN) {
            mPhysicianId = LoginUtility.getLoginId(getActivity());
        } else {
            Log.d(LOG_TAG, "This user isn't a physician why are they here?");
            return;
        }

        final SymptomManagementApi svc =  SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Physician>() {

                @Override
                public Physician call() throws Exception {
                    Log.d(LOG_TAG, "getting physician");
                    return svc.getPhysician(mPhysicianId);
                }
            }, new TaskCallback<Physician>() {

                @Override
                public void success(Physician result) {
                    Log.d(LOG_TAG, "Creating list of all patients assigned to physician");
                    Patient[] plist = new Patient[0];
                    if (result != null && result.getPatients() != null ) {
                        plist = result.getPatients().toArray(new Patient[result.getPatients().size()]);
                    }
                    setListAdapter(new PatientListAdapter(getActivity(), plist));
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch the Physician data. " +
                                    "Please check Internet connection and try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
