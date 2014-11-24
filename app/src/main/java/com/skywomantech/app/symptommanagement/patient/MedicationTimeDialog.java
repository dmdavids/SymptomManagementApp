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

/**
 * Requests the date and time a medication was taken from the user
 * uses a time picker and date picker for this purpose
 */

public class MedicationTimeDialog extends DialogFragment {
    public final static String FRAGMENT_TAG = "patient_medication_time_dialog";
    private final static String logPositionKey = "logPosition";

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
        args.putInt(logPositionKey, position);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mLogPosition = getArguments().getInt(logPositionKey);
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_medication_taken_entry, null);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.med_time_taken_question))
                .setView(view)
                .setPositiveButton(getActivity().getString(R.string.Ok_button_text),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        msTime = convertSelectionToMilliseconds();
                        ((Callbacks) getActivity()).onPositiveResult(msTime, mLogPosition);
                    }
                })
                .setNegativeButton(getActivity().getString(R.string.cancel_button_text),
                        new DialogInterface.OnClickListener() {
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
