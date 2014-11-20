package com.skywomantech.app.symptommanagement.physician;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.Patient;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;

public class PatientMedicationFragment extends ListFragment {

    private static final String LOG_TAG = PatientMedicationFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "fragment_patient_medication";

    public interface Callbacks {
        public boolean onRequestPrescriptionAdd();
    }

    private static String PHYSICIAN_ID_KEY;
    private static String PATIENT_ID_KEY;

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private static String  mPatientId;
    private static Patient mPatient;
    private static Medication[] meds;

    public PatientMedicationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PHYSICIAN_ID_KEY = getString(R.string.physician_id_key);
        PATIENT_ID_KEY = getString(R.string.patient_id_key);
        if (getArguments().containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
        setHasOptionsMenu(true);
        this.setRetainInstance(true);  // save the fragment state with rotations
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.medication_add_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add:
                ((Callbacks) getActivity()).onRequestPrescriptionAdd();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        setEmptyText(getString(R.string.empty_list_text));
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
                setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
            }
            if (savedInstanceState.containsKey(PATIENT_ID_KEY)) {
                mPatientId =
                        savedInstanceState.getString(PATIENT_ID_KEY);
            }
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
        Bundle arguments = getArguments();
        if (mPatientId == null &&
                arguments != null
                && arguments.containsKey(PATIENT_ID_KEY) ) {
            mPatientId = arguments.getString(PATIENT_ID_KEY);
        }
        refreshPatientMeds();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
        if (mPatientId != null) {
            outState.putString(PATIENT_ID_KEY, mPatientId);
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


    public void addPrescription(Medication medication) {
        if (mPatient == null || medication == null) {
            Log.d(LOG_TAG, "No current patient or medication to process.");
        }
        if (mPatient != null && mPatient.getPrescriptions() == null) {
            // first prescription
            mPatient.setPrescriptions(new HashSet<Medication>());
        }
        mPatient.getPrescriptions().add(medication);
        sendPatientRecordToCloud(mPatient);
    }

    private void refreshPatientMeds() {

        if (mPatientId == null) return;

        final SymptomManagementApi svc = SymptomManagementService.getService();
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
                    Log.d(LOG_TAG, "getting Patient and all prescriptions");
                    mPatient = result;

                    if (mPatient.getPrescriptions() == null)
                        mPatient.setPrescriptions(new HashSet<Medication>());
                    if (mPatient.getPrescriptions() != null) {
                        meds = mPatient.getPrescriptions()
                                .toArray(new Medication[mPatient.getPrescriptions().size()]);
                    }
                    setListAdapter(new PrescriptionAdapter(getActivity(), meds));
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

    public void deletePrescription(int position) {
        Collection<Medication> p = mPatient.getPrescriptions();
        p.remove(meds[position]);
        mPatient.setPrescriptions(new HashSet<Medication>(p));
        sendPatientRecordToCloud(mPatient);
    }

    // The Sync Adapter for updating patients only works for a PATIENT
    // so the Physician needs to do this immediately here.
    private void sendPatientRecordToCloud(final Patient patientRecord) {

        if (patientRecord == null) return;
        final SymptomManagementApi svc =  SymptomManagementService.getService();

        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "Updating single Patient id : " + patientRecord.getId());
                    return svc.updatePatient(patientRecord.getId(), patientRecord);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "Returned Patient from Server:" + result.toDebugString());
                    mPatient = result;
                    if (mPatient.getPrescriptions() == null)
                        mPatient.setPrescriptions(new HashSet<Medication>());
                    if (mPatient.getPrescriptions() != null) {
                        meds = mPatient.getPrescriptions()
                                .toArray(new Medication[mPatient.getPrescriptions().size()]);
                    }
                    setListAdapter(new PrescriptionAdapter(getActivity(), meds));
                }

                @Override
                public void error(Exception e) {
                    Log.e(LOG_TAG, "Sync unable to UPDATE Patient record to Internet." +
                            "Prescription changes did not save. Try again later");
                }
            });
        }
    }
}
