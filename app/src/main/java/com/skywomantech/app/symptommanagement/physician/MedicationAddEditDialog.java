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

public class MedicationAddEditDialog extends DialogFragment {

    public interface Callbacks {
        public void onSaveMedicationResult(Medication medication);
        public void onCancelMedicationResult();
    }

    @InjectView(R.id.medication_add_edit_name)
    EditText mName;
    Medication mMedication = new Medication();

    static String mTitle = "Edit Medication";

    public MedicationAddEditDialog() {
    }


    public static MedicationAddEditDialog newInstance(Medication medication) {
        if (medication.getId() == null || medication.getId().isEmpty()) {
            mTitle = "Add Medication";
        }
        MedicationAddEditDialog frag = new MedicationAddEditDialog();
        Bundle args = new Bundle();
        args.putString("med_id", medication.getId());
        args.putString("med_name", medication.getName());
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mMedication.setId(getArguments().getString("med_id"));
        mMedication.setName(getArguments().getString("med_name"));
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_medication_add_edit, null);

        ButterKnife.inject(this, view);
        if (mMedication.getId() != null && !mMedication.getId().isEmpty()) {
            mName.setText(mMedication.getName());
        }

        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setView(view)

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mMedication.setName(mName.getText().toString());
                        ((Callbacks) getActivity()).onSaveMedicationResult(mMedication);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((Callbacks) getActivity()).onCancelMedicationResult();
                    }
                });

        return builder.create();
    }
}
