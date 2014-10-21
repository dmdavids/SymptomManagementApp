package com.skywomantech.app.symptommanagement.admin.Physician;

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


import com.skywomantech.app.symptommanagement.Login;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Physician;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * A list fragment representing a list of AdminPhysicians. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link AdminPhysicianDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PhysicianListFragment extends ListFragment {

    private static final String LOG_TAG = PhysicianListFragment.class.getSimpleName();

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {

        // called when user selects a Physician
        public void onPhysicianSelected(String physicianId, String firstName, String lastName);

        // called when user wants to add a Physician
        public void onAddPhysician();

        public boolean showAddPhysicianOptionsMenu();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhysicianListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true); // save fragment across config changes

        // see if Activity wants the options menu shown
        setHasOptionsMenu(((Callbacks) getActivity()).showAddPhysicianOptionsMenu());
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
                ((Callbacks) getActivity()).onAddPhysician();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAllPhysicians();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Physician physician =
                (Physician) getListAdapter().getItem(position);
        Log.d(LOG_TAG, "Physician name selected is " + physician.getName()
                + " id is : " + physician.getId());
        String physicianId = physician.getId();
        Log.d(LOG_TAG, " String id value is : " + physicianId);
        ((Callbacks) getActivity()).onPhysicianSelected(physicianId,
                physician.getFirstName(), physician.getLastName());
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

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
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

    private void refreshAllPhysicians() {

        // hardcoded for my local host (see ipconfig for values) at port 8080
        // need to put this is prefs or somewhere it can me modified
        final SymptomManagementApi svc =
                SymptomManagementService.getService(Login.SERVER_ADDRESS);

        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<Physician>>() {

                @Override
                public Collection<Physician> call() throws Exception {
                    Log.d(LOG_TAG, "getting all physicians");
                    return svc.getPhysicianList();
                }
            }, new TaskCallback<Collection<Physician>>() {

                @Override
                public void success(Collection<Physician> result) {
                    Log.d(LOG_TAG, "creating list of all physicians");
                    setListAdapter(new ArrayAdapter<Physician>(
                            getActivity(),
                            android.R.layout.simple_list_item_activated_1,
                            android.R.id.text1,
                            new ArrayList(result)));
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(
                            getActivity(),
                            "Unable to fetch the Physicians please check Internet connection.",
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            });
        }
    }
}
