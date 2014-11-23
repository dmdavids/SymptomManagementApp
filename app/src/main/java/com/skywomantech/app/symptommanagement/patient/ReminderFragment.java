package com.skywomantech.app.symptommanagement.patient;


import android.app.ActionBar;
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


import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;

import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientCPContract;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.ReminderEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.data.Reminder;
import com.skywomantech.app.symptommanagement.patient.Reminder.ReminderManager;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

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
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
        menu.findItem(R.id.action_settings).setVisible(false);
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
            reminders = PatientDataManager
                    .loadSortedReminderList(getActivity(), LoginUtility.getLoginId(getActivity()));
            mReminders = reminders.toArray(new Reminder[reminders.size()]);
        }
        mAdapter = new ReminderListAdapter(getActivity(), mReminders);
        mReminderView.setAdapter(mAdapter);
    }

    public void addReminder(Reminder newReminder) {
        // add to database first
        String mPatientId = LoginUtility.getLoginId(getActivity());
        newReminder.setCreated(System.currentTimeMillis());
        ContentValues cv = PatientCPcvHelper.createInsertValuesObject(mPatientId, newReminder);
        Uri uri = getActivity().getContentResolver().insert(ReminderEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        if (objectId < 0) {
            Log.e(LOG_TAG, "New Reminder Insert Failed.");
            Toast.makeText(getActivity(), "Failed to Add Reminder.", Toast.LENGTH_LONG).show();

        } else {
            newReminder.setDbId(objectId);  // this is the local CP id
            // if database add successful then
            reminders.add(newReminder);
            mReminders = reminders.toArray(new Reminder[reminders.size()]);

            // start this alarm right now.
            Log.d(LOG_TAG, "adding a Reminder " + newReminder.getName());
            ReminderManager.printAlarms(getActivity(), mPatientId);
            ReminderManager.setSingleReminderAlarm(getActivity(), newReminder);
            ReminderManager.printAlarms(getActivity(), mPatientId);
            mAdapter = new ReminderListAdapter(getActivity(), mReminders);
            mReminderView.setAdapter(mAdapter);
        }
        SymptomManagementSyncAdapter.syncImmediately(getActivity());
    }

    public void deleteReminder(int position ) {
        if (mReminders[position].getDbId() >= 0) {
            String selection =
                    ReminderEntry._ID + "=" + Long.toString(mReminders[position].getDbId());
            int rowsDeleted = getActivity().getContentResolver()
                    .delete(ReminderEntry.CONTENT_URI, selection, null);
            Log.v(LOG_TAG, "Reminder rows deleted : " + Integer.toString(rowsDeleted));
        }
        Log.d(LOG_TAG, "deleting a Reminder " + mReminders[position].getName());
        ReminderManager.printAlarms(getActivity(), LoginUtility.getLoginId(getActivity()));
        ReminderManager.cancelSingleReminderAlarm(getActivity(), mReminders[position]);
        ReminderManager.printAlarms(getActivity(), LoginUtility.getLoginId(getActivity()));
        reminders.remove(mReminders[position]);
        mReminders = reminders.toArray(new Reminder[reminders.size()]);
        mAdapter = new ReminderListAdapter(getActivity(), mReminders);
        mReminderView.setAdapter(mAdapter);
        SymptomManagementSyncAdapter.syncImmediately(getActivity());
        mAdapter.notifyDataSetChanged();
    }

    // called by Add Edit Dialog
    public void updateReminder(int position, Reminder temp) {
        if (mReminders[position].getDbId() >= 0) {
            temp.setDbId(mReminders[position].getDbId());
            int rowsUpdated = PatientDataManager.updateSingleReminder(getActivity(),
                    LoginUtility.getLoginId(getActivity()), temp);
            Log.v(LOG_TAG, "Reminder rows updated : " + Integer.toString(rowsUpdated));
        }
        // cancel and restart the alarm related to this reminder
        Log.d(LOG_TAG, "updating a Reminder " + mReminders[position].getName());
        ReminderManager.printAlarms(getActivity(), LoginUtility.getLoginId(getActivity()));
        ReminderManager.cancelSingleReminderAlarm(getActivity(), mReminders[position]);
        ReminderManager.setSingleReminderAlarm(getActivity(), mReminders[position]);
        ReminderManager.printAlarms(getActivity(), LoginUtility.getLoginId(getActivity()));

        SymptomManagementSyncAdapter.syncImmediately(getActivity());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
