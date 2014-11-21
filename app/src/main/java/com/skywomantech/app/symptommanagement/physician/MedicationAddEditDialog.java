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

/**
 * This dialog fragment allows the user to add or edit a Medication
 *
 */
public class MedicationAddEditDialog extends DialogFragment {
    public final static String FRAGMENT_TAG = "fragment_medication_dialog";

    // Notifies the activity about the following events
    // onSaveMedicationResult - if the medication is being saved
    // onCancelMedicationResult - if the dialog is being cancelled
    public interface Callbacks {
        public void onSaveMedicationResult(Medication medication);
        public void onCancelMedicationResult();
    }

    @InjectView(R.id.medication_add_edit_name)
    EditText mName;

    Medication mMedication = new Medication();

    static String mTitle =  "Edit Medication";
    static String mAddTitle = "Add Medication";
    static final String medIdKey = "med_id";
    static final String medNameKey = "med_name";


    public MedicationAddEditDialog() {
    }

    /**
     * Custom new instance method so we can pass information to the dialog
     * @param medication
     * @return
     */
    public static MedicationAddEditDialog newInstance(Medication medication) {
        if (medication.getId() == null || medication.getId().isEmpty()) {
            mTitle = mAddTitle;
        }
        MedicationAddEditDialog frag = new MedicationAddEditDialog();
        Bundle args = new Bundle();
        args.putString(medIdKey, medication.getId());
        args.putString(medNameKey, medication.getName());
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mMedication.setId(getArguments().getString(medIdKey));
        mMedication.setName(getArguments().getString(medNameKey));
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_medication_add_edit, null);
        ButterKnife.inject(this, view);
        if (mMedication.getId() != null && !mMedication.getId().isEmpty()) {
            mName.setText(mMedication.getName());
        }
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setView(view)
                .setPositiveButton(getActivity().getString(R.string.Ok_button_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mName != null && mName.getText() != null) {
                                    mMedication.setName(mName.getText().toString());
                                    ((Callbacks) getActivity()).onSaveMedicationResult(mMedication);
                                } else {
                                    // assume canceling if no name added
                                    ((Callbacks) getActivity()).onCancelMedicationResult();
                                }
                            }
                        })
                .setNegativeButton(getActivity().getString(R.string.cancel_button_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((Callbacks) getActivity()).onCancelMedicationResult();
                            }
                        });
        return builder.create();
    }
}
