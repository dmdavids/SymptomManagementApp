package com.skywomantech.app.symptommanagement.patient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.MedicationLog;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MedicationLogListAdapter extends ArrayAdapter<MedicationLog> {

    private final Context context;
    private final MedicationLog[] logs;

    public static class ViewHolder {
        CheckBox isTaken;
        TextView question;
        TextView summary;
    }

    public MedicationLogListAdapter(Context context, MedicationLog[] logs) {
        super(context, R.layout.patient_medication_log_item, logs);
        this.context = context;
        this.logs = logs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if(convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.patient_medication_log_item, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.question = (TextView) view.findViewById(R.id.patient_medication_check_question);
            holder.summary = (TextView) view.findViewById(R.id.patient_medication_check_summary);
            holder.isTaken = (CheckBox) view.findViewById(R.id.patient_medication_check_answer);
            // processing for when the checkbox is clicked
            holder.isTaken
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            MedicationLog log = (MedicationLog) holder.isTaken.getTag();
                            if (isChecked) {
                                // TODO: Bring up the alert dialog for the date and time
                                log.setTaken(System.currentTimeMillis());
                                    holder.summary.setText("TAKEN TODAY");
                            }
                            else {
                                log.setTaken(0L);
                                holder.summary.setText("");
                            }

                        }
                    });
            view.setTag(holder);
            holder.isTaken.setTag(logs[position]);
        }
        else {  // this saves some processing time since most of the above is already done
            view = convertView;
            ((ViewHolder) view.getTag()).isTaken.setTag(logs[position]);
        }


        ViewHolder holder = (ViewHolder) view.getTag();
            String question = "Did you take " + logs[position].getMed().getName() + "?";
            holder.question.setText(question);
            holder.summary.setText("");
        if( logs[position].getTaken() > 0) {
            holder.summary.setText("TAKEN TODAY");
        }
        holder.isTaken.setChecked(logs[position].getTaken() > 0);

        return view;
    }



}
