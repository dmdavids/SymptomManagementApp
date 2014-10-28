package com.skywomantech.app.symptommanagement.patient;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Reminder;

public class ReminderListAdapter extends ArrayAdapter<Reminder> {

    public interface Callbacks {
        public void onRequestReminderEdit(int position, Reminder reminder);
        public void onReminderDelete(int position, Reminder reminder);
    }

    private final Activity activity;  // need activity for callbacks
    private final Context context;
    private final Reminder[] reminders;

    public static class ReminderHolder {
        Reminder reminder;
        int position;
    }

    public static class ViewHolder {
        CheckBox isActive;
        TextView reminderName;
        TextView reminderSummary;
        ImageView deleteView;
        ImageView editView;
        ReminderHolder reminderHolder;
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
            holder.reminderHolder = new ReminderHolder();
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

            holder.deleteView = (ImageView) view.findViewById(R.id.reminder_delete_button);
            holder.deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReminderHolder remHolder = (ReminderHolder) holder.deleteView.getTag();
                    ((Callbacks) activity).onReminderDelete(remHolder.position, remHolder.reminder);
                }
            });
            holder.editView = (ImageView) view.findViewById(R.id.reminder_edit_button);
            holder.editView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReminderHolder remHolder = (ReminderHolder) holder.editView.getTag();
                    ((Callbacks) activity).onRequestReminderEdit(remHolder.position, remHolder.reminder);
                }
            });
            view.setTag(holder);
            holder.isActive.setTag(reminders[position]);
            holder.editView.setTag(holder.reminderHolder);
            holder.deleteView.setTag(holder.reminderHolder);
        } else {  // this saves some processing time since most of the above is already done
            view = convertView;
            ((ViewHolder) view.getTag()).isActive.setTag(reminders[position]);
        }

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.reminderHolder.position = position;
        holder.reminderHolder.reminder = reminders[position];

        holder.reminderName.setText(reminders[position].getName());
        String summary = "";
        if (reminders[position].getHour() >= 0 && reminders[position].getMinutes() >= 0) {
            // time is stored in 24 hour format - change to 12 hour with AM/PM
            //TODO: Time needs to be formatted better than this
            String am_pm = (reminders[position].getHour() < 12) ? " AM" : " PM";
            String hours = (reminders[position].getHour() < 12)
                    ? new Integer(reminders[position].getHour()).toString()
                    : new Integer(reminders[position].getHour() - 12).toString() ;
            String minutes =  (reminders[position].getMinutes() < 10)
                    ? "0" + Integer.toString(reminders[position].getMinutes())
                    : Integer.toString(reminders[position].getMinutes());
            summary = hours + ":" + minutes + am_pm;
        }
        holder.reminderSummary.setText(summary);
        holder.isActive.setChecked(reminders[position].isOn());

        return view;
    }
}
