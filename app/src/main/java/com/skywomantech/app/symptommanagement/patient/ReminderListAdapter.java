package com.skywomantech.app.symptommanagement.patient;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Reminder;

import java.util.Calendar;

/**
 * This Array Adapter manages the Reminder List for the Patient
 *
 */
public class ReminderListAdapter extends ArrayAdapter<Reminder> {

    public final static String LOG_TAG = ReminderAddEditDialog.class.getSimpleName();

    public interface Callbacks {
        public void onRequestReminderEdit(int position, Reminder reminder);
        public void onReminderDelete(int position, Reminder reminder);
        public void onRequestReminderActivate(Reminder reminder);
    }

    private final Activity activity;  // need activity for callbacks
    private final Context context;
    private final Reminder[] reminders;

    public static class ReminderHolder {
        Reminder reminder;
        int position;
    }

    public static class ViewHolder {
        Switch isActive;
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
        View view;
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_reminder, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.reminderHolder = new ReminderHolder();
            holder.reminderName = (TextView) view.findViewById(R.id.reminder_name);
            holder.reminderSummary = (TextView) view.findViewById(R.id.reminder_time_summary);
            holder.isActive = (Switch) view.findViewById(R.id.reminder_switch);
            holder.isActive.setChecked(reminders[position].isOn());
            // processing for when the checkbox is clicked
            holder.isActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reminder reminder = (Reminder) buttonView.getTag();
                    if (reminder.isOn() != isChecked) {
                        reminder.setOn(isChecked);
                        Log.d(LOG_TAG, "Reminder switch has changed ... it is now "
                                + (reminder.isOn() ? "ON" : "OFF"));
                        //Activity process the activation/deactivation
                        ((Callbacks) activity).onRequestReminderActivate(reminder);
                    }
                }
            });

            holder.deleteView = (ImageView) view.findViewById(R.id.reminder_delete_button);
            holder.deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReminderHolder remHolder = (ReminderHolder) holder.deleteView.getTag();
                    // activity manages the deletion
                    ((Callbacks) activity).onReminderDelete(remHolder.position, remHolder.reminder);
                }
            });
            holder.editView = (ImageView) view.findViewById(R.id.reminder_edit_button);
            holder.editView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReminderHolder remHolder = (ReminderHolder) holder.editView.getTag();
                    // activity manages the edit
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
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, reminders[position].getHour());
            cal.set(Calendar.MINUTE, reminders[position].getMinutes());
            int hour = cal.get(Calendar.HOUR);
            if (hour == 0 || hour == 23) hour = 12;
            int min = cal.get(Calendar.MINUTE);
            String minString = "";
            if (min < 10)  minString += "0";
            minString += Integer.toString(min);
            int am_pm = cal.get(Calendar.AM_PM);
            summary = Integer.toString(hour) + ":" + minString + (am_pm == 1 ? "PM" : "AM");
        }
        holder.reminderSummary.setText(summary);
        holder.isActive.setChecked(reminders[position].isOn());
        return view;
    }
}
