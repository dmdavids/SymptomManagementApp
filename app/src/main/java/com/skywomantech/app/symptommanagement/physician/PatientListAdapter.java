package com.skywomantech.app.symptommanagement.physician;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.Physician;


public class PatientListAdapter extends ArrayAdapter<Patient> {

    private final Context context;
    private final Patient[] patients;

    public static class ViewHolder {
        public final ImageView alertIcon;
        public final TextView patientName;
        public final TextView lastLog;

        public ViewHolder(View view) {
            alertIcon = (ImageView) view.findViewById(R.id.patient_list_alert_icon);
            patientName = (TextView) view.findViewById(R.id.patient_list_name);
            lastLog = (TextView) view.findViewById(R.id.patient_list_last_log);
        }

        Patient patient;
    }

    public PatientListAdapter(Context context, Patient[] patients) {
        super(context, R.layout.list_item_patient_list, patients);
        this.context = context;
        this.patients = patients;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_item_patient_list, parent, false);
            final ViewHolder viewHolder = new ViewHolder(rowView);
            viewHolder.patient = patients[position];
            viewHolder.patientName.setText(patients[position].getName());
            viewHolder.lastLog.setText(patients[position].getFormattedLastLogged());
            rowView.setTag(viewHolder);
            view = rowView;
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.patientName.setText(patients[position].getName());
        holder.lastLog.setText(patients[position].getFormattedLastLogged());
        holder.patient = patients[position];
        return view;
    }
}
