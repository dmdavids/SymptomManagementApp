package com.skywomantech.app.symptommanagement.patient;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.MedicationLog;

/**
 * Manage the list items for Tracking Medications .. Asks if the medication was taken?
 * then it gets the time taken via the main activity and finally updates the view with
 * the entered time taken
 *
 * The yes/no answer to the question is represented as a clicked/un-clicked checkbox
 *
 */
public class MedicationLogListAdapter extends ArrayAdapter<MedicationLog> {

    /**
     * Get the main activity to display the date and time dialog and return it here
     */
    public interface Callbacks {
        public void onRequestDateTime(int position);
    }

    private final Activity activity;  // need activity for the dialog callback
    private final Context context;
    private final MedicationLog[] logs;

    public static class ViewHolder {
        CheckBox isTaken;  // this holds the YES or NO answer to the pain medication question
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
            // instead of YES or NO answer this uses a CHECKBOX and clicked = YES/ unclicked = NO
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
        // Instead of generic Did you take your pain medicine? this displays
        // an actual medication name to replace pain medicine
        String question = "Did you take " + logs[position].getMed().getName() + "?";
        holder.question.setText(question);
        holder.summary.setText("");
        if (logs[position].getTaken() > 0) {
            String summaryText = "Taken " +
                    logs[position].getTakenDateFormattedString("E, MMM d yyyy 'at' hh:mm a");
            holder.summary.setText(summaryText);
            holder.isTaken.setVisibility(View.INVISIBLE);
        }
        holder.isTaken.setChecked(logs[position].getTaken() > 0);

        return view;
    }
}
