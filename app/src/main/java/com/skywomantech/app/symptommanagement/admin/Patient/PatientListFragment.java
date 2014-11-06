package com.skywomantech.app.symptommanagement.admin.Patient;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

public class PatientListFragment extends ListFragment {

    private static final String LOG_TAG = PatientListFragment.class.getSimpleName();

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {
        // called when user selects a Patient
        public void onPatientSelected(String id);

        // called when user wants to add a patient
        public void onAddPatient();
    }

    public PatientListFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        setHasOptionsMenu(true); // this fragment has menu items to display
        setEmptyText(getString(R.string.empty_list_text));
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.empty_list_text));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
    }
    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.admin_add_menu, menu);
    }

    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add:
                ((Callbacks) getActivity()).onAddPatient();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAllPatients();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Patient patient =
                (Patient) getListAdapter().getItem(position);
        Log.d(LOG_TAG, "Patient name selected is " + patient.getName()
                + " id is : " + patient.getId());
        String patientId = patient.getId();
        Log.d(LOG_TAG, " String id value is : " + patientId);
        ((Callbacks) getActivity()).onPatientSelected(patientId);
        setActivatedPosition(position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }
        mActivatedPosition = position;
    }

    private void refreshAllPatients() {

        final SymptomManagementApi svc =
                SymptomManagementService.getService();

        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<Patient>>() {

                @Override
                public Collection<Patient> call() throws Exception {
                    Log.d(LOG_TAG, "getting all patients");
                    return svc.getPatientList();
                }
            }, new TaskCallback<Collection<Patient>>() {

                @Override
                public void success(Collection<Patient> result) {
                    Log.d(LOG_TAG, "creating list of all patients");
                    setListAdapter(new ArrayAdapter<Patient>(
                            getActivity(),
                            android.R.layout.simple_list_item_activated_1,
                            android.R.id.text1,
                            new ArrayList(result)));
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch the Patients. Please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
