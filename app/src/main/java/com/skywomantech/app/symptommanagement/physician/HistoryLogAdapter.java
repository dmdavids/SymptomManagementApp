package com.skywomantech.app.symptommanagement.physician;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.data.Patient;


public class HistoryLogAdapter extends ArrayAdapter<HistoryLog> {

    private final Context context;
    private final HistoryLog[] logs;

    public static class ViewHolder {
        public final ImageView typeIcon;
        public final TextView info;
        public final TextView created;

        public ViewHolder(View view) {
            typeIcon = (ImageView) view.findViewById(R.id.log_type_icon);
            info = (TextView) view.findViewById(R.id.history_log_info);
            created = (TextView) view.findViewById(R.id.history_log_created);
        }
        HistoryLog log;
    }

    public HistoryLogAdapter(Context context, HistoryLog[] logs) {
        super(context, R.layout.list_item_patient_history_log, logs);
        this.context = context;
        this.logs = logs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_item_patient_history_log, parent, false);
            final ViewHolder viewHolder = new ViewHolder(rowView);
            viewHolder.log = logs[position];
            viewHolder.info.setText(logs[position].getInfo());
            viewHolder.created.setText(logs[position].getFormattedCreatedDate());
            viewHolder.typeIcon.setImageResource(getImageResourceForLogType(logs[position].getType()));
            rowView.setTag(viewHolder);
            view = rowView;
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.info.setText(logs[position].getInfo());
        holder.created.setText(logs[position].getFormattedCreatedDate());
        holder.log = logs[position];
        return view;
    }

    public static int getImageResourceForLogType(HistoryLog.LogType type) {
        if (type == HistoryLog.LogType.PAIN_LOG) return R.drawable.ic_action_red_pain_log;
        if (type == HistoryLog.LogType.MED_LOG) return R.drawable.ic_action_healthcare_pill_icon;
        if (type == HistoryLog.LogType.STATUS_LOG) return R.drawable.ic_action_status_log;
        return R.drawable.ic_action_red_border_pain_log;
    }
}
