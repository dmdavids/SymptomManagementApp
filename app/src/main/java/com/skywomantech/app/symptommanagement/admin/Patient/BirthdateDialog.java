package com.skywomantech.app.symptommanagement.admin.Patient;

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


public class BirthdateDialog extends DialogFragment {

    public interface Callbacks {
        public void onPositiveResult(long time);
        public void onNegativeResult();
    }
    DatePicker datePicker;

    private long birthday = 0L;

    public BirthdateDialog() {
        // required empty constructor

    }

    public static BirthdateDialog newInstance(long thisDay) {
        BirthdateDialog frag = new BirthdateDialog();
        Bundle args = new Bundle();
        args.putLong("bday_long", thisDay);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        birthday = getArguments().getLong("bday_long");
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_birthday_entry, null);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setTitle("Enter Patient's Birthday")
                .setView(view)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        birthday = convertSelectionToMilliseconds();
                        ((Callbacks) getActivity()).onPositiveResult(birthday);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        birthday = 0L;
                        ((Callbacks) getActivity()).onNegativeResult();
                    }
                });

        // set up the widgets to get the selections from
        datePicker = (DatePicker) view.findViewById(R.id.birthday_date_picker);

        return builder.create();
    }

    private long convertSelectionToMilliseconds() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                     0, 0, 0);
        return calendar.getTimeInMillis();
    }

}
