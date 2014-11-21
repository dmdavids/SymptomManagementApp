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

/**
 * This fragment displays a list of all the available medications on the server.
 *
 * This fragment allows the user to select a medication or add a new medication.
 * This fragment does not allow deleting any medications from the server.  This is not allowed
 * in this design.  All medications are expected to remain in the database.
 *
 *
 */
public class MedicationListFragment extends ListFragment {

    private static final String LOG_TAG = MedicationListFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "fragment_medication_list";

    private static Collection<Medication> medications;

    // Notifies the activity about the following events
    // onAddMedication - adds a medication to the server database
    // onMedicationSelected - lets the activity know which medication was selected
    // showAddMedicationOptionsMenu - asks activity if the options menu is needed or not
    public interface Callbacks {
        public void onMedicationSelected(Medication medication);
        public void onAddMedication();
        public boolean showAddMedicationOptionsMenu();
        public Collection<Medication> getMedications();
    }

    public MedicationListFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes
        // see if the activity want the ADD icon to display in the options menu
        setHasOptionsMenu(((Callbacks) getActivity()).showAddMedicationOptionsMenu());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.empty_list_text));
        setRetainInstance(true); // save fragment across config changes
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(activity.getString(R.string.callbacks_message));
        }
    }

    /**
     * If the activity allows, the user can have access to an Add medication option
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.admin_add_menu, menu);
    }

    /**
     * If the add medication option is chosen, allow the hosting activity to process it
     * via a dialog
     *
     * @param item
     * @return
     */
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

    /**
     * attempt to display the list of medications
     */
    @Override
    public void onResume() {
        super.onResume();
        medications = ((Callbacks) getActivity()).getMedications();
        displayMedications(medications);
    }

    /**
     * Called by the hosting activity to update the medication list from the server
     *
     * @param meds list of medications
     */
    public void updateMedications(Collection<Medication> meds) {
        medications = meds;
        displayMedications(medications);
    }

    /**
     * When a list item is selected send it to the hosting activity to process
     *
     * @param listView
     * @param view
     * @param position
     * @param id
     */

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Medication med = (Medication) getListAdapter().getItem(position);
        ((Callbacks) getActivity()).onMedicationSelected(med);
    }

    public void displayMedications(final Collection<Medication> medications) {
        if(medications == null) {
            Log.e(LOG_TAG, "Trying to display a null medication list.");
            return;
        }
        setListAdapter(new ArrayAdapter<Medication>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                new ArrayList(medications)));
    }
}
