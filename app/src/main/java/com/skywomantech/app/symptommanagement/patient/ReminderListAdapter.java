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
import com.skywomantech.app.symptommanagement.data.Reminder;

public class ReminderListAdapter extends ArrayAdapter<Reminder> {

    private final Activity activity;  // need activity for callbacks
    private final Context context;
    private final Reminder[] reminders;

    public static class ViewHolder {
        CheckBox isActive;
        TextView reminderName;
        TextView reminderSummary;
        int savePosition;
    }

    public ReminderListAdapter(Activity activity, Reminder[] reminders) {
        super(activity.getApplicationContext(),
                R.layout.list_item_reminder, reminders);
        this.context = activity.getApplicationContext();
        this.reminders = reminders;
        this.activity = activity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_reminder, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.reminderName = (TextView) view.findViewById(R.id.reminder_name);
            holder.reminderSummary = (TextView) view.findViewById(R.id.reminder_time_summary);
            holder.isActive = (CheckBox) view.findViewById(R.id.reminder_turned_on);
            // processing for when the checkbox is clicked
            holder.isActive
                    .setOnClickListener(new CompoundButton.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Reminder reminder = (Reminder) holder.isActive.getTag();
                            if (holder.isActive.isChecked()) {
                                reminder.setOn(true);
                                // do something here to let the alarm manager know
                            } else {
                                reminder.setOn(false);
                                // do something here to let the alarm manager know
                            }
                        }
                    });
            view.setTag(holder);
            holder.isActive.setTag(reminders[position]);
        } else {  // this saves some processing time since most of the above is already done
            view = convertView;
            ((ViewHolder) view.getTag()).isActive.setTag(reminders[position]);
        }


        ViewHolder holder = (ViewHolder) view.getTag();
        holder.savePosition = position;

        holder.reminderName.setText(reminders[position].getName());
        String summary = "";
        if (reminders[position].getHour() >= 0 && reminders[position].getMinutes() >= 0) {
            summary = Integer.toString(reminders[position].getHour()) + ":" +
                    Integer.toString(reminders[position].getMinutes());
        }
        holder.reminderSummary.setText(summary);
        holder.isActive.setChecked(reminders[position].isOn());

        return view;
    }
}
