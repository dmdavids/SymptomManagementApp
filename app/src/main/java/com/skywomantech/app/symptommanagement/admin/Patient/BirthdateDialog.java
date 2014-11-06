package com.skywomantech.app.symptommanagement.admin.Patient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.skywomantech.app.symptommanagement.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class BirthdateDialog extends DialogFragment {
    public final static String LOG_TAG = BirthdateDialog.class.getSimpleName();
    public interface Callbacks {
        public void onPositiveResult(String bday);
        public void onNegativeResult();
    }

    private DatePicker datePicker;

    private String birthday = "";

    public BirthdateDialog() {
        // required empty constructor
    }

    public static BirthdateDialog newInstance(String birthdate) {
        BirthdateDialog frag = new BirthdateDialog();
        Bundle args = new Bundle();
        args.putString("bday", birthdate);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        birthday = getArguments().getString("bday");
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_birthday_entry, null);
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setTitle("Enter Patient's Birthday")
                .setView(view)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        birthday = convertSelectionToString();
                        ((Callbacks) getActivity()).onPositiveResult(birthday);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        birthday = "";
                        ((Callbacks) getActivity()).onNegativeResult();
                    }
                });

        // set up the widgets to get the selections from
        datePicker = (DatePicker) view.findViewById(R.id.birthday_date_picker);

        // set the datePicker to display the birthday if available
        convertStringToSelection(birthday);

        return builder.create();
    }

    private String convertSelectionToString() {
        long dateTime = datePicker.getCalendarView().getDate();
        Date date = new Date(dateTime);
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        return dateFormat.format(date);
    }

    private void convertStringToSelection(String day) {
        if (day == null || day.isEmpty()) return;

        Date theDate;
        try {
            theDate = new SimpleDateFormat("MM-dd-yyyy").parse(day);
        } catch (ParseException e) {
            Log.d(LOG_TAG, "Birthdate String not in expected format MM-dd-yy. Ignoring.");
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(theDate);
        datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }
}
