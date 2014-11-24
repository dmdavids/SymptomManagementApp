package com.skywomantech.app.symptommanagement.physician;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.StatusLog;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * This fragment displays detail information about the current patient
 * <p/>
 * The focus of this fragment is getting the patient and displaying the main details.
 * The activity is in charge of passing this fragment the patient to work with.  This happens
 * through a method call in the activity.
 * <p/>
 * This fragment also has an option menu item attached to it.
 * If this fragment is running then the physician can set a status log indicating
 * that the patient was contacted.  If the patient was severe then the alert notifications
 * are turned off until the next time the patient appears severe. Just a way to turn off the
 * notifications.  The patient will still appear severe in the patient list.
 * <p/>
 * Future Enhancement: Can add the physician status logs to the history list
 */
public class PhysicianPatientDetailFragment extends Fragment {

    public final static String LOG_TAG = PhysicianPatientDetailFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "fragment_details";

    // Notifies the activity about the following events
    // getPatientForDetails - return the current patient to work with
    // onPatientContacted - physician want to add a status log to the patient
    public interface Callbacks {
        public Patient getPatientForDetails();

        public void onPatientContacted(String patientId, StatusLog statusLog);
    }

    private static Patient mPatient;

    @InjectView(R.id.physician_patient_detail_name)
    TextView mNameView;
    @Optional @InjectView(R.id.physician_patient_detail_birthdate)
    TextView mBDView;
    @Optional  @InjectView(R.id.patient_medical_id)
    TextView mRecordId;

    public PhysicianPatientDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(activity.getString(R.string.callbacks_message));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_physician_patient_detail, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // check for patient data and display it if we have any
        mPatient = ((Callbacks) getActivity()).getPatientForDetails();
        displayPatient();
    }

    /**
     * Called by the hosting activity to update the details display with a new patient
     *
     * @param patient
     */
    public void updatePatient(Patient patient) {
        if (patient == null) {
            Log.e(LOG_TAG, "Trying to set details patient to null.");
            return;
        }
        Log.d(LOG_TAG, "New Patient has arrived!" + patient.toString());
        mPatient = patient;
        displayPatient();
    }

    private void displayPatient() {
        if (mPatient != null) {
            // update the display
            mNameView.setText(mPatient.getName());
            if (mBDView != null)  mBDView.setText(mPatient.getBirthdate());
            if (mRecordId != null) mRecordId.setText(mPatient.getId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * When a patient is selected then the physician has the option to
     * put a status long into his record saying that he contacted the patient
     * this is basically a way to turn-off the alert notifications until the patient is
     * severe again
     * <p/>
     * This will only affect the physician logged in. Other physicians who have this patient
     * will still get a notification.
     * <p/>
     * Note: this does not change the listview status of severe just turns off notifications
     * for a little while.
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // add the "Contacted Patient" menu item
        inflater.inflate(R.menu.physician_patient_contact_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mPatient == null) return true;  // just don't do anything cause there is no patient

        int id = item.getItemId();
        if (id == R.id.action_add_status) {
            if (mPatient != null) {
                Log.d(LOG_TAG, "Adding a Physician Status Log/Contacted Patient");
                AlertDialog alert = new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getString(R.string.confirm_patient_contacted_title))
                        .setMessage(getActivity().getString(R.string.ask_patient_contacted))
                        .setPositiveButton(getActivity().getString(R.string.answer_yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((Callbacks) getActivity())
                                                // tell the activity to handle the work of
                                                // adding a status log to the physician's patient
                                                .onPatientContacted(mPatient.getId(),
                                                        new StatusLog(
                                                                getActivity()
                                                                        .getString(R.string.patient_contact_status),
                                                                System.currentTimeMillis()));
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(getActivity().getString(R.string.answer_no),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { // do nothing
                                        dialog.dismiss();
                                    }
                                }).create();
                alert.show();
            } else {
                Log.e(LOG_TAG, "No patient loaded for physician to contact.");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
