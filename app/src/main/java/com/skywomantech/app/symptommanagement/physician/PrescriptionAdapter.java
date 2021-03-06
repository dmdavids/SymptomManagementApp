package com.skywomantech.app.symptommanagement.physician;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;

/**
 * This is a custom list adapter for the patient prescriptions.
 * <p/>
 * This adapter manages the display of the prescriptions / patient medication list.
 * The display contains a clickable delete icon that allows the physician to remove prescriptions.
 * This adapter expects the hosting activity to manage the delete of the prescription via
 * a callback method and the activity needs to implement the callbacks interface.
 * The hosting activity is passed to the adapter on creation.
 */
public class PrescriptionAdapter extends ArrayAdapter<Medication> {

    // Notifies the activity about the following events
    // onPrescriptionDelete - deletes the given medication/prescription in the array position
    public interface Callbacks {
        public void onPrescriptionDelete(int position, Medication medication);
    }

    private final Activity activity;  // need activity for callbacks
    private final Context context;
    private final Medication[] prescriptions;

    public static class ViewHolder {
        public final ImageView deletePrescription;
        public final TextView prescriptionName;

        public ViewHolder(View view) {
            deletePrescription = (ImageView) view.findViewById(R.id.prescription_delete);
            prescriptionName = (TextView) view.findViewById(R.id.prescription_name);
        }

        Medication prescription;
        int position;
    }

    public PrescriptionAdapter(Activity activity, Medication[] prescriptions) {
        super(activity, R.layout.list_item_prescription, prescriptions);
        this.activity = activity;
        this.context = activity;
        this.prescriptions = prescriptions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_item_prescription, parent, false);
            final ViewHolder viewHolder = new ViewHolder(rowView);
            viewHolder.prescription = prescriptions[position];
            viewHolder.prescriptionName.setText(prescriptions[position].getName());
            viewHolder.deletePrescription
                    .setOnClickListener(new CompoundButton.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Medication med = (Medication) viewHolder.deletePrescription.getTag();
                            ((Callbacks) activity)
                                    .onPrescriptionDelete(viewHolder.position, viewHolder.prescription);
                        }
                    });
            viewHolder.position = position;
            rowView.setTag(viewHolder);
            viewHolder.deletePrescription.setTag(prescriptions[position]);
            view = rowView;
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.prescriptionName.setText(prescriptions[position].getName());
        holder.prescription = prescriptions[position];
        holder.position = position;
        return view;
    }
}
