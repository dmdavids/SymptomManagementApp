package com.skywomantech.app.symptommanagement.patient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Reminder;


public class ReminderAddEditDialog extends DialogFragment {

    public interface Callbacks {
        public void onReminderAdd(Reminder newReminder);
        public void onReminderUpdate(int position, Reminder reminder);
    }

    TimePicker timePicker;
    EditText reminderName;

    private static int mHour;
    private static int mMinutes;
    private static String mName;
    private static long mDbId;
    private static Reminder mReminder;
    private static int mPosition;

    public ReminderAddEditDialog() {
        // required empty constructor
    }

    public static ReminderAddEditDialog newInstance(int position, Reminder reminder) {
        ReminderAddEditDialog frag = new ReminderAddEditDialog();
        mReminder = reminder;
        mPosition = position;
        mHour = reminder.getHour();
        mMinutes = reminder.getMinutes();
        mName = reminder.getName();
        mDbId = reminder.getDbId();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_reminder_add_edit, null);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setTitle("Reminder Configuration")
                .setView(view)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mName = reminderName.getText().toString();
                        mHour = timePicker.getCurrentHour();
                        mMinutes = timePicker.getCurrentMinute();
                        if (mReminder.getHour() == -1 && mReminder.getMinutes() == -1) {
                            mReminder.setHour(mHour);
                            mReminder.setMinutes(mMinutes);
                            mReminder.setName(mName);
                            ((Callbacks) getActivity()).onReminderAdd(mReminder);
                        } else {
                            mReminder.setHour(mHour);
                            mReminder.setMinutes(mMinutes);
                            mReminder.setName(mName);
                            mReminder.setDbId(mDbId);
                            ((Callbacks) getActivity()).onReminderUpdate(mPosition, mReminder);
                        }
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                });

        // set up the widgets to get the selections from
        timePicker = (TimePicker) view.findViewById(R.id.reminder_timePicker);
        if (mHour >= 0) timePicker.setCurrentHour(mHour);
        if (mMinutes >= 0) timePicker.setCurrentMinute(mMinutes);
        reminderName = (EditText) view.findViewById(R.id.reminder_name_edit);
        if (mName != null) reminderName.setText(mName);
        return builder.create();
    }

}
