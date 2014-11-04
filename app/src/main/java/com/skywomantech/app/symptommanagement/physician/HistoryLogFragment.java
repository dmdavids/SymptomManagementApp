package com.skywomantech.app.symptommanagement.physician;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.Login;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.data.MedicationLog;
import com.skywomantech.app.symptommanagement.data.PainLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.Physician;
import com.skywomantech.app.symptommanagement.data.StatusLog;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * A list fragment representing a list of PhysicianPatients. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link com.skywomantech.app.symptommanagement.physician.PhysicianPatientDetailFragment}.
 * <p>
 */
public class HistoryLogFragment extends ListFragment {

    private static final String LOG_TAG = HistoryLogFragment.class.getSimpleName();
    private String  mPatientId;
    private Patient mPatient;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";


    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistoryLogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(PhysicianPatientDetailFragment.PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PhysicianPatientDetailFragment.PATIENT_ID_KEY);
        }
        this.setRetainInstance(true);  // save the fragment state with rotations
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
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAllLogs();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
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

    private void refreshAllLogs() {

        final SymptomManagementApi svc =
                SymptomManagementService.getService();

        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting patient");
                    return svc.getPatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "getting Patient and all logs");
                    mPatient = result;
                    HistoryLog[] logList = createLogList(mPatient);
                    setListAdapter(new HistoryLogAdapter(getActivity(), logList));
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch the Patient Logs. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private HistoryLog[] createLogList(Patient mPatient) {
        HistoryLogSorter sorter = new HistoryLogSorter();
        TreeSet<HistoryLog> sortedLogs = new TreeSet<HistoryLog>(
                Collections.reverseOrder(sorter));
        Collection<PainLog> plogs = mPatient.getPainLog();
        if (plogs != null) {
            for (PainLog p : plogs) {
                HistoryLog h = new HistoryLog();
                h.setCreated(p.getCreated());
                h.setType(HistoryLog.LogType.PAIN_LOG);
                String severity = (p.getSeverity() == PainLog.Severity.SEVERE) ? "SEVERE"
                        : (p.getSeverity() == PainLog.Severity.MODERATE) ? "Moderate"
                        : "Well-Defined";
                String eating = (p.getEating() == PainLog.Eating.NOT_EATING) ? "NOT EATING"
                        : (p.getEating() == PainLog.Eating.SOME_EATING) ? "Some Eating" : "Eating";
                String info = "Pain : " + severity + " -- " + eating;
                h.setInfo(info);
                sortedLogs.add(h);
            }
        }
        Collection<MedicationLog> mlogs = mPatient.getMedLog();
        if (mlogs != null) {
            for (MedicationLog m : mlogs) {
                HistoryLog h = new HistoryLog();
                h.setCreated(m.getCreated());
                h.setType(HistoryLog.LogType.MED_LOG);
                String name = m.getMed().getName();
                String taken = m.getTakenDateFormattedString(" hh:mm a 'on' E, MMM d yyyy" );
                String info = name + " taken " +  taken;
                h.setInfo(info);
                sortedLogs.add(h);
            }
        }
        Collection<StatusLog> slogs = mPatient.getStatusLog();
        if (slogs != null) {
            for (StatusLog s : slogs) {
                HistoryLog h = new HistoryLog();
                h.setCreated(s.getCreated());
                h.setType(HistoryLog.LogType.STATUS_LOG);
                String image = (s.getImage_location() != null && !s.getImage_location().isEmpty())
                        ? "Image Taken" : "";
                String info = "Note: " + s.getNote() + " " + image;
                h.setInfo(info);
                sortedLogs.add(h);
            }
        }

        if (sortedLogs.size() <= 0) return new HistoryLog[0];
        return sortedLogs.toArray(new HistoryLog[sortedLogs.size()]);
    }

    private class HistoryLogSorter implements Comparator<HistoryLog> {

        public int compare(HistoryLog x, HistoryLog y) {
            return Long.compare(x.getCreated(), y.getCreated());
        }
    }
}
