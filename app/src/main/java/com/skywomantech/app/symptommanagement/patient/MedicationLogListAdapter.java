package com.skywomantech.app.symptommanagement.patient;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.MedicationLog;

public class MedicationLogListAdapter extends ArrayAdapter<MedicationLog> {

    public interface Callbacks {
        public void onRequestDateTime(int position);
    }

    private final Activity activity;  // need activity for the dialog callback
    private final Context context;
    private final MedicationLog[] logs;

    public static class ViewHolder {
        CheckBox isTaken;
        TextView question;
        TextView summary;
        int savePosition;
    }

    public MedicationLogListAdapter(Activity activity, MedicationLog[] logs) {
        super(activity.getApplicationContext(),
                R.layout.list_item_patient_medication_log, logs);
        this.context = activity.getApplicationContext();
        this.logs = logs;
        this.activity = activity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_patient_medication_log, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.question = (TextView) view.findViewById(R.id.patient_medication_check_question);
            holder.summary = (TextView) view.findViewById(R.id.patient_medication_check_summary);
            holder.isTaken = (CheckBox) view.findViewById(R.id.patient_medication_check_answer);
            // processing for when the checkbox is clicked
            holder.isTaken
                    .setOnClickListener(new CompoundButton.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MedicationLog log = (MedicationLog) holder.isTaken.getTag();
                            if (holder.isTaken.isChecked()) {
                                ((Callbacks) activity).onRequestDateTime(position);
                                if (log.getTaken() > 0) {
                                    String summaryText = "Taken on " +
                                            log.getTakenDateFormattedString("E, MMM d yyyy 'at' hh:mm a");
                                    holder.summary.setText(summaryText);
                                }
                            } else {
                                log.setTaken(0L);
                                holder.summary.setText("");
                            }
                        }
                    });
            view.setTag(holder);
            holder.isTaken.setTag(logs[position]);
        } else {  // this saves some processing time since most of the above is already done
            view = convertView;
            ((ViewHolder) view.getTag()).isTaken.setTag(logs[position]);
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.savePosition = position;
        String question = "Did you take " + logs[position].getMed().getName() + "?";
        holder.question.setText(question);
        holder.summary.setText("");
        if (logs[position].getTaken() > 0) {
            String summaryText = "Last Taken on " +
                    logs[position].getTakenDateFormattedString("E, MMM d yyyy 'at' hh:mm a");
            holder.summary.setText(summaryText);
            holder.isTaken.setVisibility(View.INVISIBLE);
        }
        holder.isTaken.setChecked(logs[position].getTaken() > 0);

        return view;
    }
}
