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

import java.util.Calendar;
import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PatientMainFragment extends Fragment {

    private final static String LOG_TAG = PatientMainFragment.class.getSimpleName();

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_main, container, false);
        ButterKnife.inject(this, rootView);
        mPatient = ((Callbacks) getActivity()).getPatientCallback(); // only checks the CP
        String displayName = "Welcome " +
                ((mPatient != null && mPatient.getName() != null) ? mPatient.toString() : "");
        patientName.setText(displayName);
        numCheckIns.setText(getNumCheckIns());
        nextCheckIn.setText(getNextCheckIn());
        setHasOptionsMenu(true);
        setRetainInstance(true); // save fragment across config changes
        return rootView;
    }

    @OnClick(R.id.pain_log_button)
    public void enterPainLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_main_container, new PatientPainLogFragment())
             //   .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.medication_log_button)
    public void enterMedicationLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_main_container, new PatientMedicationLogFragment())
              //  .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.status_log_button)
    public void enterStatusLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.patient_main_container, new PatientStatusLogFragment())
             //   .addToBackStack(null)
                .commit();
    }

    private String getNextCheckIn() {
        if (mPatient == null) return "";
        reminders = PatientDataManager.loadSortedReminderList(getActivity(), mPatient.getId());
        if (reminders == null || reminders.size() <= 0) return "No Check-Ins are Scheduled At This Time.";

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

        if (hour < 0 ) return "Today's Scheduled Check-Ins are Completed.";

        String am_pm = (hour < 12) ? " AM" : " PM";
        String hours = (hour <= 12)
                ? Integer.valueOf(hour).toString()
                : Integer.valueOf(hour - 12).toString();
        String minutes = (minute < 10)
                ? "0" + Integer.toString(minute)
                : Integer.toString(minute);
        return "Next Scheduled Check-In Today is at " + hours + ":" + minutes + am_pm;
    }

    private String getNumCheckIns() {
        // retrieve the patient logs and store them in Patient Object
        Patient tempPatient = new Patient();
        String mPatientId = LoginUtility.getLoginId(getActivity());
        tempPatient.setId(mPatientId);
        // fill the logs from the CP
        PatientDataManager.getLogsFromCP(getActivity(), tempPatient);
        // Create a sorted history list from the logs
        HistoryLog[] logList = PatientDataManager.createLogList(tempPatient);
        long start_of_day = ReminderManager.getStartOfToday();
        int count = 0;
        // consider pain logs as the indicator of a check-in occurring
        for (HistoryLog h : logList) {
            if (h.getType() == HistoryLog.LogType.PAIN_LOG &&
                    h.getCreated() >= start_of_day) {
                count++;
            }
        }
        if (count == 0) return "You have not yet checked in today.";

        return "You have checked in " + count + " times today.";
    }

}
