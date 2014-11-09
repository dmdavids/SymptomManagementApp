package com.skywomantech.app.symptommanagement.patient;

import android.app.Fragment;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.MedicationLog;
import com.skywomantech.app.symptommanagement.data.PatientCPContract;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.MedLogEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.Collection;
import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PatientMedicationLogFragment extends Fragment {

    public final static String LOG_TAG = PatientMedicationLogFragment.class.getSimpleName();

    MedicationLogListAdapter mAdapter;
    private Collection<MedicationLog> medicationLogs;
    MedicationLog[] mLogList;

    @InjectView(R.id.patient_medication_check_list)  ListView mLogListView;

    public interface Callbacks {
        public boolean onMedicationLogComplete();
    }

    public PatientMedicationLogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);  // save the fragment state with rotations
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medication_log, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    // load a list of empty log records for the patient to fill
    @Override
    public void onResume() {
        super.onResume();
        loadMedicationLogList();
    }

    private void loadMedicationLogList() {
        // make a blank list of possible entries for the patient to fill in
        Collection<Medication> prescriptions =
                PatientDataManager.getPrescriptionsFromCP(getActivity(),
                LoginUtility.getLoginId(getActivity()));
        if (mLogList == null) {
            createEmptyLogsList(prescriptions);
        }
        mAdapter = new MedicationLogListAdapter(getActivity(), mLogList);
        mLogListView.setAdapter(mAdapter);
    }

    private void createEmptyLogsList(Collection<Medication> medications) {
        medicationLogs = new HashSet<MedicationLog>();
        for(Medication m: medications) {
            MedicationLog ml = new MedicationLog();
            ml.setMed(m);
            medicationLogs.add(ml);
        }
        mLogList =  medicationLogs.toArray(new MedicationLog[medicationLogs.size()]);
    }

    // callback for the list adapter
    public void updateMedicationLogTimeTaken(long msTime, int position) {
        mLogList[position].setTaken(msTime);

        // save this one to the database
        String mPatientId = LoginUtility.getLoginId(getActivity());
        ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, mLogList[position]);
        Log.d(LOG_TAG, "Saving this Med Log : " + mLogList[position].toString());
        Uri uri = getActivity().getContentResolver().insert(MedLogEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        if (objectId < 0) {
            Log.e(LOG_TAG, "Medication Log Insert Failed.");
        } else {
            ((Callbacks) getActivity()).onMedicationLogComplete();
        }
        SymptomManagementSyncAdapter.syncImmediately(getActivity());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
