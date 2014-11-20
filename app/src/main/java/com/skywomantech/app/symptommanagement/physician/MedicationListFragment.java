package com.skywomantech.app.symptommanagement.physician;

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
import com.skywomantech.app.symptommanagement.data.Medication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;


public class MedicationListFragment extends ListFragment {

    private static final String LOG_TAG = MedicationListFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "fragment_medication_list";
    public interface Callbacks {
        public void onMedicationSelected(Medication medication);
        public void onAddMedication();
        public boolean showAddMedicationOptionsMenu();
    }

    public MedicationListFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes

        // see if the activity want the ADD icon to display in the options menu
        setHasOptionsMenu(((Callbacks) getActivity()).showAddMedicationOptionsMenu());

        setEmptyText(getString(R.string.empty_list_text));
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
                ((Callbacks) getActivity()).onAddMedication();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAllMedications();
    }


    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Medication med = (Medication) getListAdapter().getItem(position);

        Log.d(LOG_TAG, "Medication name selected is " + med.getName()
                + " id is : " + med.getId());

        String medId = med.getId();
        Log.d(LOG_TAG, " String id value is : " + medId);
        ((Callbacks) getActivity()).onMedicationSelected(med);
    }

    public void refreshAllMedications() {

        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<Medication>>() {

                @Override
                public Collection<Medication> call() throws Exception {
                    Log.d(LOG_TAG,"getting all medications");
                    return svc.getMedicationList();
                }
            }, new TaskCallback<Collection<Medication>>() {

                @Override
                public void success(Collection<Medication> result) {
                    Log.d(LOG_TAG,"creating list of all medications");
                    if(result != null) {
                        Log.d(LOG_TAG, "REFRESHING MEDICATION LIST!");
                        setListAdapter(new ArrayAdapter<Medication>(
                                getActivity(),
                                android.R.layout.simple_list_item_activated_1,
                                android.R.id.text1,
                                new ArrayList(result)));
                    }
                }
                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch the Medications please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }

}
