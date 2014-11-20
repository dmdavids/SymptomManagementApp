package com.skywomantech.app.symptommanagement.physician;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;

/**
 * This fragment expects a patient to be obtained from the hosting activity.
 * <p/>
 * This fragment displays a list of the combined log records for the patient
 * <p/>
 * This list is used by both the patient and the physician apps.
 * <p/>
 * Clicking on a list item does nothing at the time being. This is a view only list.
 * <p/>
 * Future Enhancement is that it could combine the physician and patient logs.
 * Future Enhancement is that there could be other types of logs.
 * Future Enhancement is the ability to filter logs for display purposes.
 */
public class HistoryLogFragment extends ListFragment {

    private static final String LOG_TAG = HistoryLogFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "fragment_history_log";

    // Notifies the activity about the following events
    // getPatientForHistory - return the current patient to work with
    public interface Callbacks {
        public Patient getPatientForHistory();
    }

    private static Patient mPatient;

    public HistoryLogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: enables the back arrow ... may need to configure this one
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.empty_list_text));
        this.setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(activity.getString(R.string.callbacks_message));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPatient = ((Callbacks) getActivity()).getPatientForHistory();
        displayLogList(mPatient);
    }

    /**
     * Called by hosting activity to set the patient and redisplay the list
     *
     * @param patient object holding the logs to be displayed
     */
    public void updatePatient(Patient patient) {
        if (patient == null) {
            Log.e(LOG_TAG, "Trying to set history log patient to null.");
            return;
        }
        Log.d(LOG_TAG, "New Patient has arrived!" + patient.toString());
        mPatient = patient;
        displayLogList(mPatient);
    }

    private void displayLogList(Patient patient) {
        if (patient != null) {
            HistoryLog[] logList = PatientManager.createLogList(mPatient);
            setListAdapter(new HistoryLogAdapter(getActivity(), logList));
        }
    }
}
