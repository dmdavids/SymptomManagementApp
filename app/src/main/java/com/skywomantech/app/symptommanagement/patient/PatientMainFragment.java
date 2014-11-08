package com.skywomantech.app.symptommanagement.patient;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.DataUtility;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.physician.PatientDataManager;

import java.util.Calendar;
import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * A placeholder fragment containing a simple view.
 */
public class PatientMainFragment extends Fragment {

    private final static String LOG_TAG = PatientMainFragment.class.getSimpleName();

    public interface Callbacks {
        public Patient getPatientCallback();
    }

    private Collection<Reminder> reminders;

    @InjectView(R.id.check_ins_completed)
    TextView numCheckIns;
    @InjectView(R.id.next_check_in)
    TextView nextCheckIn;
    @InjectView(R.id.main_patient_name) TextView patientName;

    Patient mPatient;

    public PatientMainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patient_main, container, false);
        ButterKnife.inject(this, rootView);

//        TODO: This is not working... just extra for now maybe fix later
       mPatient = ((Callbacks) getActivity()).getPatientCallback();
        String displayName = "For " +
                ((mPatient != null && mPatient.getName() != null ) ? mPatient.toString() : "Patient");
        Log.e(LOG_TAG, "THIS IS THE DISPLAY NAME!!! " + displayName);
        patientName.setText(displayName);

        // how many check-ins done today?
        String howMany = getNumCheckIns();
        numCheckIns.setText(howMany);

        // what time is next check-in?
        String when = getNextCheckIn();
        nextCheckIn.setText(when);
        return rootView;
    }

    @OnClick(R.id.pain_log_button)
    public void enterPainLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new PatientPainLogFragment())
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.medication_log_button)
    public void enterMedicationLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new PatientMedicationLogFragment())
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.status_log_button)
    public void enterStatusLog() {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new PatientStatusLogFragment())
                .addToBackStack(null)
                .commit();
    }

    private String getNextCheckIn() {
        reminders = PatientDataManager.loadReminderList(getActivity());
        Calendar rightNow = Calendar.getInstance();
        int checkValue = rightNow.get(Calendar.HOUR_OF_DAY) * 60 + rightNow.get(Calendar.MINUTE);
        int savedHour = -1;
        int savedMinute = -1;
        if (reminders.size() > 0 ) {
            Collection<Reminder> sorted = DataUtility.sortRemindersByTime(reminders);
            for (Reminder r: sorted) {
                int timeCheck = r.getHour() * 60 + r.getMinutes();
                if (savedHour == -1 ) {
                    savedHour = r.getHour();
                    savedMinute = r.getMinutes();
                }
                if(timeCheck > checkValue) {
                    savedHour = r.getHour();
                    savedMinute = r.getMinutes();
                    break;
                }
            }
        } else return "No Check-Ins are Scheduled At This Time.";
        String am_pm = (savedHour < 12) ? " AM" : " PM";
        String hours = (savedHour <= 12)
                ? Integer.valueOf(savedHour).toString()
                : Integer.valueOf(savedHour - 12).toString() ;
        String minutes =  (savedMinute < 10)
                ? "0" + Integer.toString(savedMinute)
                : Integer.toString(savedMinute);
        return  "Next Scheduled Check-In is at "+ hours + ":" + minutes + am_pm;
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
        long start_of_day = DataUtility.getStartOfToday();
        int count = 0;
        // consider status logs as the indicator of a check-in occurring
        for (HistoryLog h: logList) {
            if (h.getType() == HistoryLog.LogType.PAIN_LOG &&
                    h.getCreated() >= start_of_day) {
                count++;
            }
        }
        if (count == 0) return "You have not yet checked in today.";

        return "You have checked in " + count + " times today.";
    }

}
