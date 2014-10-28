package com.skywomantech.app.symptommanagement.patient;


import android.app.Fragment;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;


import com.skywomantech.app.symptommanagement.R;

import com.skywomantech.app.symptommanagement.data.PatientCPContract.ReminderEntry;
import com.skywomantech.app.symptommanagement.data.Reminder;

import java.util.Collection;
import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ReminderFragment extends Fragment {

    public interface Callbacks {
        public void onRequestReminderAdd(Reminder reminder);
    }

    private String mPatientId;
    ReminderListAdapter mAdapter;
    private Collection<Reminder> reminders;
    Reminder[] mReminders;

    @InjectView(R.id.reminder_list)  ListView mReminderView;

    //TODO:  replace with actual patient's list of reminders
    private Collection<Reminder> dummyData = makeDummyData();


    public ReminderFragment() {
        mPatientId = "123213123"; //TODO: get the real patient id
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.setRetainInstance(true);  // save the fragment state with rotations
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reminder, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.reminder_add_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add:
                ((Callbacks) getActivity()).onRequestReminderAdd(new Reminder());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // load a list of empty log records for the patient to fill
    @Override
    public void onResume() {
        super.onResume();
        loadReminderList();
    }

    private void loadReminderList() {
        // TODO: get actual Reminders for this patient use dummy list for now
        if (mReminders == null) {
            reminders = dummyData;
            mReminders = reminders.toArray(new Reminder[reminders.size()]);
        }
        mAdapter = new ReminderListAdapter(getActivity(), mReminders);
        mReminderView.setAdapter(mAdapter);
    }

    public void addReminder(Reminder newReminder) {
        reminders.add(newReminder);
        mReminders = reminders.toArray(new Reminder[reminders.size()]);
        mAdapter = new ReminderListAdapter(getActivity(), mReminders);
        mReminderView.setAdapter(mAdapter);
        //mAdapter.notifyDataSetChanged();
    }

    public void deleteReminder(int position ) {
        reminders.remove(mReminders[position]);
        mReminders = reminders.toArray(new Reminder[reminders.size()]);
        mAdapter = new ReminderListAdapter(getActivity(), mReminders);
        mReminderView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public void updateReminder(int position, Reminder temp) {
       mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private ContentValues createValuesObject(Reminder rem) {
        ContentValues cv = new ContentValues();
        cv.put(ReminderEntry.COLUMN_ON, rem.isOn());
        cv.put(ReminderEntry.COLUMN_HOUR, rem.getHour());
        cv.put(ReminderEntry.COLUMN_PATIENT_ID, mPatientId);
        cv.put(ReminderEntry.COLUMN_MINUTES, rem.getMinutes());
        cv.put(ReminderEntry.COLUMN_NAME, rem.getName());
        cv.put(ReminderEntry.COLUMN_CREATED, System.currentTimeMillis());
        return cv;
    }


    private static Collection<Reminder> makeDummyData() {
        Collection<Reminder> reminders = new HashSet<Reminder>();

        Reminder daytime = new Reminder("Daytime Alarm");
        daytime.setHour(6);
        daytime.setMinutes(30);
        daytime.setOn(true);
        reminders.add(daytime);

        Reminder night = new Reminder("Night Time Alarm");
        night.setHour(10);
        night.setMinutes(30);
        night.setOn(true);
        reminders.add(night);

        return reminders;
    }

}
