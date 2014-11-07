package com.skywomantech.app.symptommanagement.physician;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PatientSearchDialog extends DialogFragment {

    public interface Callbacks {
        public void onNameSelected(String lastName, String firstName);
    }

    @InjectView(R.id.patient_search_last_name)
    EditText mLastName;

    @InjectView(R.id.patient_search_first_name)
    EditText mFirstName;
    String lastName = "";
    String firstName = "";

    static String mTitle = "Search All Patients By Name";

    public PatientSearchDialog() {
    }

    public static PatientSearchDialog newInstance() {
        PatientSearchDialog frag = new PatientSearchDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_patient_search, null);
        ButterKnife.inject(this, view);
        mLastName.setText(lastName);
        mFirstName.setTag(firstName);

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setView(view)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        lastName = mLastName.getText().toString();
                        firstName = mFirstName.getText().toString();
                        ((Callbacks) getActivity()).onNameSelected(lastName, firstName);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing here
                    }
                });

        return builder.create();
    }
}
