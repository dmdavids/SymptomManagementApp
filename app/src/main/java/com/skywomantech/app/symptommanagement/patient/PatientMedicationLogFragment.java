package com.skywomantech.app.symptommanagement.patient;

import android.app.Fragment;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.MedicationLog;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.MedLogEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;

import java.util.Collection;
import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PatientMedicationLogFragment extends Fragment {

    public final static String LOG_TAG = PatientMedicationLogFragment.class.getSimpleName();

    private String mPatientId;
    MedicationLogListAdapter mAdapter;
    private Collection<MedicationLog> medicationLogs;
    MedicationLog[] mLogList;

    @InjectView(R.id.patient_medication_check_list)  ListView mLogListView;

    //TODO:  replace with actual patient's list of medications
    private Collection<Medication> dummyMedications = makeDummyMedicationList();

    public PatientMedicationLogFragment() {
        mPatientId = "123213123"; //TODO: get the real patient id
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
        // TODO: get actual Medications for this patient use dummy list for now
        if (mLogList == null) {
            createEmptyLogsList(dummyMedications);
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
        ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, mLogList[position]);
        Uri uri = getActivity().getContentResolver().insert(MedLogEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        if (objectId < 0) {
            Log.e(LOG_TAG, "Medication Log Insert Failed.");
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }



    private static Collection<Medication> makeDummyMedicationList() {
        Collection<Medication> meds = new HashSet<Medication>();

        Medication oxycontin = new Medication("OxyContin");
        oxycontin.setId("1234");
        meds.add(oxycontin);

        Medication lortab = new Medication("Lortab");
        lortab.setId("543212");
        meds.add(lortab);
        return meds;
    }

}
