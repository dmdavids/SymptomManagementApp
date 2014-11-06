package com.skywomantech.app.symptommanagement.admin.Patient;

import android.app.AlertDialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.R;

import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.Physician;

import java.util.ArrayList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PatientAddEditFragment extends Fragment  {
    private static final String LOG_TAG = PatientAddEditFragment.class.getSimpleName();

    public final static String PATIENT_ID_KEY = AdminPatientListActivity.PATIENT_ID_KEY;

    private static Patient mPatient;
    private String mPatientId;

    @InjectView(R.id.edit_first_name)  EditText mFirstName;
    @InjectView(R.id.edit_last_name)  EditText mLastName;
    @InjectView(R.id.display_birthdate)  TextView mBirthdate;
    @InjectView(R.id.admin_patient_physician_listview)  ListView mPhysiciansListView;

    public PatientAddEditFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mPatientId = savedInstanceState.getString(PATIENT_ID_KEY);
        }
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // fragment has menu items to display

        // else editing
        View rootView = inflater.inflate(R.layout.fragment_admin_patient_add_edit, container, false);
        ButterKnife.inject(this, rootView);

        mPatient = new Patient();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
            loadPatientFromAPI();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PATIENT_ID_KEY, mPatientId);
        super.onSaveInstanceState(outState);
    }

    private void loadPatientFromAPI() {
        if (mPatientId == null) return;
        Log.d(LOG_TAG, "Physician ID Key is : " + mPatientId);

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting single patient with id : " + mPatientId);
                    return svc.getPatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Log.d(LOG_TAG, "Found Patient :" + result.toString());
                    mPatient = result;
                    mFirstName.setText(mPatient.getFirstName());
                    mLastName.setText(mPatient.getLastName());
                    mBirthdate.setText(mPatient.getBirthdate());
                    displayPhysicians(mPatient.getPhysicians());
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch Patient for editing. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }

    @OnClick(R.id.save_patient_button)
    public void savePatient(Button button) {
        if ( mFirstName.getText().toString().trim().length() == 0 &&
                mLastName.getText().toString().trim().length() == 0)  {
            DialogFragment errorSaving =
                    new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(getActivity());
                            builder.setMessage("Unable to Save patient. Please Enter a valid first and last name.");
                            builder.setPositiveButton("OK", null);
                            return builder.create();
                        }
                    };
            errorSaving.show(getFragmentManager(), "Error saving/updating patient");
            return;
        }

        final SymptomManagementApi svc = SymptomManagementService.getService();
        final String successMsg = (mPatientId == null ? "ADDED" : "UPDATED");
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    mPatient.setId(mPatientId);
                    mPatient.setFirstName(mFirstName.getText().toString());
                    mPatient.setLastName(mLastName.getText().toString());
                    if (mPatientId == null) {
                        Log.d(LOG_TAG, "adding patient :" + mPatient.toDebugString());
                        return svc.addPatient(mPatient);
                    }else {
                        Log.d(LOG_TAG, "updating patient :" + mPatient.toDebugString());
                        return svc.updatePatient(mPatientId, mPatient);
                    }
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Toast.makeText(
                            getActivity(),
                            "Patient [" + result.getName() + "] " + successMsg + " successfully.",
                            Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to SAVE Patient. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }


    private void displayPhysicians(Collection<Physician> physicians) {
        if (physicians == null || physicians.size() == 0) {
            final List<Physician> emptyList = new ArrayList<Physician>();
            Physician emptyPhysician = new Physician("No Physicians for this Patient.", "");
            emptyList.add(emptyPhysician);
            Physician[] plist =  emptyList.toArray(new Physician[1]);
            mPhysiciansListView
                    .setAdapter(new PhysicianEditListAdapter(getActivity(), plist));
        } else {
            Physician[] plist =  physicians.toArray(new Physician[physicians.size()]);
            mPhysiciansListView
                    .setAdapter(new PhysicianEditListAdapter(getActivity(), plist));
        }
    }

    public void updatePatient() {
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "updating patient :" + mPatient.toDebugString());
                    if (mPatientId == null || mPatient.getId() == null ||
                            mPatientId.isEmpty() || mPatient.getId().isEmpty()) {
                        return svc.addPatient(mPatient);
                    }
                    return svc.updatePatient(mPatientId, mPatient);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    Toast.makeText(
                            getActivity(),
                            "Patient [" + result.getName() + "] added/updated successfully.",
                            Toast.LENGTH_SHORT).show();
                    mPatient = result;
                    mPatientId = mPatient.getId();
                    onResume();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to Update Patient. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @OnClick(R.id.add_physician_button)
    public void addPhysician(Button button) {
        Intent intent = new Intent(getActivity(), PatientPhysicianListActivity.class);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(LOG_TAG, "OnActivityResult for Request code: " + Integer.toString(requestCode));
        if(requestCode == 2) {
            Log.v(LOG_TAG, "Saving the new physician information.");
            String physicianId = data.getStringExtra(PatientPhysicianListActivity.PHYSICIAN_ID_KEY);
            String physicianFirstName = data.getStringExtra(PatientPhysicianListActivity.PHYSICIAN_FIRST_NAME_KEY);
            String physicianLastName = data.getStringExtra(PatientPhysicianListActivity.PHYSICIAN_LAST_NAME_KEY);
            Physician p = new Physician();
            p.setId(physicianId);
            p.setFirstName(physicianFirstName);
            p.setLastName(physicianLastName);
            if (mPatient.getPhysicians() != null) {
                mPatient.getPhysicians().add(p);
            } else {
                final Set<Physician> newSet = new HashSet<Physician>();
                newSet.add(p);
                mPatient.setPhysicians(newSet);
            }
            // if there hasn't been any input yet then don't bother to save or update yet
            // do it on a later saving opportunity
            if (physicianId != null && !physicianId.isEmpty() && physicianFirstName != null &&
                    !physicianFirstName.isEmpty() && physicianLastName != null &&
                    !physicianLastName.isEmpty()) {
                updatePatient();
                loadAndSavePhysicianFromAPI(physicianId);
            }
        }
    }

    private static Physician mPhysician;
    private void loadAndSavePhysicianFromAPI(final String physicianId) {
        Log.d(LOG_TAG, "Getting Physician to Update Patients - Physician ID is : " + physicianId);

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Physician>() {

                @Override
                public Physician call() throws Exception {
                    Log.d(LOG_TAG, "getting single Physician id : " + physicianId);
                    return svc.getPhysician(physicianId);
                }
            }, new TaskCallback<Physician>() {

                @Override
                public void success(Physician result) {
                    Log.d(LOG_TAG, "Found Physician :" + result.toString());
                    mPhysician = result;
                    Patient justPatient = new Patient(mPatient);
                    if (mPhysician.getPatients() != null) {
                        mPhysician.getPatients().add(justPatient);
                    }else  {
                        final Set<Patient> newSet = new HashSet<Patient>();
                        newSet.add(justPatient);
                        mPhysician.setPatients(newSet);
                    }
                    savePhysician();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch Physician to update Patients. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }

    public void savePhysician() {

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Physician>() {

                @Override
                public Physician call() throws Exception {
                        Log.d(LOG_TAG, "updating physician with patient :" + mPhysician.toDebugString());
                        return svc.updatePhysician(mPhysician.getId(), mPhysician);
                }
            }, new TaskCallback<Physician>() {

                @Override
                public void success(Physician result) {
                    Toast.makeText(
                            getActivity(),
                            "Updated Physician with Patient Successfully.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to UPDATE Physician with Patients. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
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

    @OnClick(R.id.pick_birthdate)
    public void showDatePickerDialog(View v) {
        BirthdateDialog newFragment = BirthdateDialog.newInstance(mPatient.getBirthdate());
        newFragment.show(getFragmentManager(), "birthdayPicker");
    }

    public void onPositiveResult(String bday) {
        mPatient.setBirthdate(bday);
        mBirthdate.setText(mPatient.getBirthdate());
    }

    public void onNegativeResult() {
        mBirthdate.setText(mPatient.getBirthdate());
    }
}
