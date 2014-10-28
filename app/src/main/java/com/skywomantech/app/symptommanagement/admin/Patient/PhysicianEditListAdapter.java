package com.skywomantech.app.symptommanagement.admin.Patient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Physician;


public class PhysicianEditListAdapter extends ArrayAdapter<Physician> {

    private final Context context;
    private final Physician[] physicians;

    public static class ViewHolder {
        public final ImageView deleteView;
        public final TextView textView;

        public ViewHolder(View view) {
            deleteView = (ImageView) view.findViewById(R.id.physician_list_delete_item);
            deleteView
                    .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //TODO:
                                                // delete the physician from
                                                // the patients physician list
                                                // then update the patient in the db
                                                Physician p = (Physician) view.getTag();
                                            }
                                        }

                    );
            textView = (TextView) view.findViewById(R.id.physician_list_name_item);
        }

        Physician physician;
    }

    public PhysicianEditListAdapter(Context context, Physician[] physicians) {
        super(context, R.layout.list_item_physician_edit, physicians);
        this.context = context;
        this.physicians = physicians;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_item_physician_edit, parent, false);
            final ViewHolder viewHolder = new ViewHolder(rowView);
            viewHolder.physician = physicians[position];
            viewHolder.textView.setText(physicians[position].toString());
            rowView.setTag(viewHolder);
            viewHolder.deleteView.setTag(viewHolder.physician);
            view = rowView;
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.textView.setText(physicians[position].getName());
        holder.physician = physicians[position];
        return view;
    }
}
