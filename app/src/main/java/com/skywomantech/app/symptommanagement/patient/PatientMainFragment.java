package com.skywomantech.app.symptommanagement.patient;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.patient.Reminder.ReminderManager;
import com.skywomantech.app.symptommanagement.physician.PatientManager;

import java.util.Calendar;
import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * This fragment is the main screen when the check-in process is not currently active.
 *
 */
public class PatientMainFragment extends Fragment {

    private final static String LOG_TAG = PatientMainFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "patient_main_fragment";

    /**
     *  This allows this fragment to ask the main activity for the patient object
     *  it needs to display details
     */
    public interface Callbacks {
        public Patient getPatientCallback();
    }

    @InjectView(R.id.check_ins_completed)
    TextView numCheckIns;
    @InjectView(R.id.next_check_in)
    TextView nextCheckIn;
    @InjectView(R.id.main_patient_name)
    TextView patientName;

    Patient mPatient;
    private Collection<Reminder> reminders;

    public PatientMainFragment() {
    }

    /**
     * set up the patient status/details screen
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_main, container, false);
        ButterKnife.inject(this, rootView);

        // get the patient from the content provider if it exists
        mPatient = ((Callbacks) getActivity()).getPatientCallback();

        // if the patient doesn't exists locally yet (on first login)
        // then we don't display a name yet ..  we have to wait for the sync adapter
        // to load the data into the content provider but only first time the
        // patient uses this device
        String displayName = getActivity().getString(R.string.patient_welcome_message) +
                ((mPatient != null && mPatient.getName() != null) ? mPatient.toString() : "");

        // display all the information that is available
        patientName.setText(displayName);
        numCheckIns.setText(getNumCheckIns());
        nextCheckIn.setText(getNextCheckIn());

        setHasOptionsMenu(true);
        setRetainInstance(true);
        return rootView;
    }

    /**
     * when the track pain button is clicked display the pain log screen
     */
    @OnClick(R.id.pain_log_button)
    public void enterPainLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_main_container,
                        new PatientPainLogFragment(),
                        PatientPainLogFragment.FRAGMENT_TAG)
                .commit();
    }

    /**
     * when the track medication button is clicked display the medication log screen
     */
    @OnClick(R.id.medication_log_button)
    public void enterMedicationLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_main_container,
                        new PatientMedicationLogFragment(),
                        PatientMedicationLogFragment.FRAGMENT_TAG)
                .commit();
    }

    /**
     * when the add notes button is clicked display the status log screen
     */
    @OnClick(R.id.status_log_button)
    public void enterStatusLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_main_container,
                        new PatientStatusLogFragment(),
                        PatientStatusLogFragment.FRAGMENT_TAG)
                .commit();
    }

    /**
     * Figure out when the next check-in for TODAY occurs.  Does not indicate TOMORROW's check ins.
     *
     * @return String with message about the next check-in time.
     */
    private String getNextCheckIn() {
        if (mPatient == null) return "";
        reminders = PatientDataManager.loadSortedReminderList(getActivity(), mPatient.getId());
        if (reminders == null || reminders.size() <= 0) return "No Check-Ins are Scheduled.";

        Calendar rightNow = Calendar.getInstance();
        int checkValue = rightNow.get(Calendar.HOUR_OF_DAY) * 60 + rightNow.get(Calendar.MINUTE);
        int hour = -1;
        int minute = -1;
        for (Reminder r : reminders) {
            // if the time matches and the alarm is activated
            if ((r.getHour() * 60 + r.getMinutes()) > checkValue
                    && r.isOn()) {
                hour = r.getHour();
                minute = r.getMinutes();
                break;
            }
        }

        if (hour < 0 )
            return getActivity().getString(R.string.todays_check_ins_complete_message);

        String am_pm = (hour < 12) ? " AM" : " PM";
        String hours = (hour <= 12)
                ? Integer.valueOf(hour).toString()
                : Integer.valueOf(hour - 12).toString();
        String minutes = (minute < 10)
                ? "0" + Integer.toString(minute)
                : Integer.toString(minute);
        return "Next Check-In Today is at " + hours + ":" + minutes + am_pm;
    }

    /**
     * count the number of check-in logs since midnight and return the message
     *
     * @return String with message to display about the nubmer of check-ins TODAY
     */
    private String getNumCheckIns() {
        // retrieve the patient logs and store them in Patient Object
        Patient tempPatient = new Patient();
        String mPatientId = LoginUtility.getLoginId(getActivity());
        tempPatient.setId(mPatientId);
        // fill the logs from the CP
        PatientDataManager.getLogsFromCP(getActivity(), tempPatient);
        // Create a sorted history list from the logs
        HistoryLog[] logList = PatientManager.createLogList(tempPatient);
        long start_of_day = new ReminderManager().getStartOfToday();
        int count = 0;
        // consider pain logs as the indicator of a check-in occurring
        for (HistoryLog h : logList) {
            if (h.getType() == HistoryLog.LogType.CHECK_IN_LOG &&
                    h.getCreated() >= start_of_day) {
                count++;
            }
        }
        if (count == 0) return getActivity().getString(R.string.no_check_ins_today_message);

        return "You have checked in " + count + " times today.";
    }

}
