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
import java.util.HashSet;
import java.util.concurrent.Callable;

/**
 * This fragment processes the physician's patient list
 *
 */
public class PhysicianListPatientsFragment extends ListFragment {

    private static final String LOG_TAG = PhysicianListPatientsFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "fragment_patient_list";

    // Notifies the activity about the following events
    // onItemSelected - return the current physician and patient information to work with
    public interface Callbacks {
        public void onItemSelected(String physicianId, Patient patient);
        public Physician getPhysicianForPatientList();
    }

    static String mPhysicianId;
    static Physician mPhysician;
    static Patient[] mPatientList = new Patient[0];
    static Collection<Patient> mTempList = new HashSet<Patient>();

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public PhysicianListPatientsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);
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
            throw new IllegalStateException(activity.getString(R.string.callbacks_message));
        }
    }

    /**
     * try to redisplay the physician's patient list
     */
    @Override
    public void onResume() {
        super.onResume();
        mPhysician = ((Callbacks) getActivity()).getPhysicianForPatientList();
        displayPatientList(mPhysician);
    }

    /**
     * Allows the hosting activity to push a new physician record that was received from
     * the server to this fragment
     *
     * @param physician
     */
    public void updatePhysician(Physician physician) {
        if (physician == null) {
            Log.e(LOG_TAG, "Trying to set physician to null.");
            return;
        }
        Log.d(LOG_TAG, "New Physician has arrived!" + physician.toString());
        mPhysician = physician;
        displayPatientList(mPhysician);
    }

    /**
     * Give the list adapter the physician list of patients to display
     *
     * @param physician
     */
    private void displayPatientList(Physician physician) {
        if(physician == null) {
            Log.e(LOG_TAG, "Trying to display a null physician.");
            return;
        }
        Log.d(LOG_TAG, "Creating list of all patients assigned to physician");
        if (physician.getPatients() != null ) {
            mTempList = physician.getPatients();
            mPatientList = mTempList.toArray(new Patient[mTempList.size()]);
        }
        setListAdapter(new PatientListAdapter(getActivity(), mPatientList));
    }

    /**
     * temporarily add a patient from the patient search to the list of patients for this physician
     * so that the dual pane stuff doesn't look weird... will be removed if the physician is updated
     * or if this fragment is resumed .. might need to do some testing on this though
     * TODO: test how this works when the device is rotated
     * this patient is not added to the physician's patient list
     *
     * @param patient
     */
    public void temporaryAddToList(Patient patient) {
        // use the current patient list and add one to it
        if(mTempList != null && patient != null) {
            mTempList.add(patient);
        }
        mPatientList = mTempList.toArray(new Patient[mTempList.size()]);
        setListAdapter(new PatientListAdapter(getActivity(), mPatientList));
    }

    /**
     * When the user clicks on a patient from the list then it lets the activity know
     * which patient was selected
     * WARNING!! The patients that are stored in the Physician record are not fully formed!
     * The fully-formed patients are in a different set of records.  So the activity will need
     * to get the fully-formed patient if they need it.
     *
     * @param listView
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Patient patient =  (Patient) getListAdapter().getItem(position);
        Log.d(LOG_TAG, "Patient selected is " + patient.toString());
        mPhysicianId = LoginUtility.getLoginId(getActivity());
        ((Callbacks) getActivity()).onItemSelected(mPhysicianId, patient);
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
}
