package com.skywomantech.app.symptommanagement.patient;


import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.Toast;


import com.skywomantech.app.symptommanagement.Login;
import com.skywomantech.app.symptommanagement.R;

import com.skywomantech.app.symptommanagement.data.PatientCPContract.ReminderEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.data.Reminder;

import java.util.Collection;
import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ReminderFragment extends Fragment {

    public final static String LOG_TAG = ReminderFragment.class.getSimpleName();

    public interface Callbacks {
        public void onRequestReminderAdd(Reminder reminder);
    }

    ReminderListAdapter mAdapter;
    private Collection<Reminder> reminders;
    Reminder[] mReminders;

    @InjectView(R.id.reminder_list)  ListView mReminderView;


    public ReminderFragment() {

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
        if (mReminders == null) {
            reminders = new HashSet<Reminder>();
            // search the local storage for the item id
            Cursor cursor = getActivity().getContentResolver()
                    .query(ReminderEntry.CONTENT_URI, null, null, null,null);
            while (cursor.moveToNext()) {
                Reminder item = new Reminder();
                item.setDbId(cursor.getLong(cursor.getColumnIndex(ReminderEntry._ID)));
                item.setName(cursor.getString(cursor.getColumnIndex(ReminderEntry.COLUMN_NAME)));
                item.setHour(cursor.getInt(cursor.getColumnIndex(ReminderEntry.COLUMN_HOUR)));
                item.setMinutes(cursor.getInt(cursor.getColumnIndex(ReminderEntry.COLUMN_MINUTES)));
                item.setOn((cursor.getInt(cursor.getColumnIndex(ReminderEntry.COLUMN_ON)) == 0 ? false : true));
                reminders.add(item);
            }
            cursor.close();
            mReminders = reminders.toArray(new Reminder[reminders.size()]);
        }
        mAdapter = new ReminderListAdapter(getActivity(), mReminders);
        mReminderView.setAdapter(mAdapter);
    }

    public void addReminder(Reminder newReminder) {
        // add to database first
        String mPatientId = Login.getPatientId(getActivity());
        ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, newReminder);
        Uri uri = getActivity().getContentResolver().insert(ReminderEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        if (objectId < 0) {
            Log.e(LOG_TAG, "New Reminder Insert Failed.");
            Toast.makeText(getActivity(), "Failed to Add Reminder.", Toast.LENGTH_LONG).show();

        } else {
            newReminder.setDbId(objectId);
            // if database add successful then
            reminders.add(newReminder);
            mReminders = reminders.toArray(new Reminder[reminders.size()]);
            mAdapter = new ReminderListAdapter(getActivity(), mReminders);
            mReminderView.setAdapter(mAdapter);
        }
    }

    public void deleteReminder(int position ) {
        // remove from database first
        if (mReminders[position].getDbId() >= 0) {
            // TODO: deletes all rows, need to only delete one, write testcase
            String selection =
                    ReminderEntry._ID + "=" + Long.toString(mReminders[position].getDbId());
            int rowsDeleted = getActivity().getContentResolver()
                    .delete(ReminderEntry.CONTENT_URI, selection, null);
            Log.v(LOG_TAG, "Reminder rows deleted : " + Integer.toString(rowsDeleted));
        }
        reminders.remove(mReminders[position]);
        mReminders = reminders.toArray(new Reminder[reminders.size()]);
        mAdapter = new ReminderListAdapter(getActivity(), mReminders);
        mReminderView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public void updateReminder(int position, Reminder temp) {
       // update the one in the database first
        if (mReminders[position].getDbId() >= 0) {
            String mPatientId = Login.getPatientId(getActivity());
            ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, temp);
            String selection =
                    ReminderEntry._ID + "=" + Long.toString(mReminders[position].getDbId());
            int rowsUpdated = getActivity().getContentResolver()
                    .update(ReminderEntry.CONTENT_URI, cv, selection, null);
            Log.v(LOG_TAG, "Reminder rows updated : " + Integer.toString(rowsUpdated));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
