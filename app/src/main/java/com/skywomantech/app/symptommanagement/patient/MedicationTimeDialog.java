package com.skywomantech.app.symptommanagement.patient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.skywomantech.app.symptommanagement.R;

import java.util.Calendar;


public class MedicationTimeDialog extends DialogFragment {

    public interface Callbacks {
        public void onPositiveResult(long msTime, int position);
        public void onNegativeResult(long msTime, int position);
    }
    TimePicker timePicker;
    DatePicker datePicker;

    private long msTime = 0L;
    private int mLogPosition;

    public MedicationTimeDialog() {
        // required empty constructor

    }

    public static MedicationTimeDialog newInstance(int position) {
        MedicationTimeDialog frag = new MedicationTimeDialog();
        Bundle args = new Bundle();
        args.putInt("logPosition", position);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mLogPosition = getArguments().getInt("logPosition");
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_medication_taken_entry, null);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setTitle("When did you take your medication?")
                .setView(view)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        msTime = convertSelectionToMilliseconds();
                        ((Callbacks) getActivity()).onPositiveResult(msTime, mLogPosition);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        msTime = 0L;
                        ((Callbacks) getActivity()).onNegativeResult(msTime, mLogPosition);
                    }
                });

        // set up the widgets to get the selections from
        timePicker = (TimePicker) view.findViewById(R.id.medication_timePicker);
        datePicker = (DatePicker) view.findViewById(R.id.medication_datePicker);

        return builder.create();
    }

    private long convertSelectionToMilliseconds() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                     timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
        return calendar.getTimeInMillis();
    }

}
