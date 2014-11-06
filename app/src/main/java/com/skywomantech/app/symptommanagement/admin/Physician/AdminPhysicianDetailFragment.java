package com.skywomantech.app.symptommanagement.admin.Physician;

import android.os.Bundle;
 import android.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
 import android.widget.Toast;


 import com.skywomantech.app.symptommanagement.R;
 import com.skywomantech.app.symptommanagement.client.CallableTask;
 import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
 import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
 import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.Physician;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

 import butterknife.ButterKnife;
 import butterknife.InjectView;


 public class AdminPhysicianDetailFragment extends Fragment {
     private static final String LOG_TAG = AdminPhysicianDetailFragment.class.getSimpleName();

    public final static String PHYSICIAN_ID_KEY = AdminPhysicianListActivity.PHYSICIAN_ID_KEY;

    private String mPhysicianId;
    private Physician mPhysician;

     public interface Callbacks {
         // called when user selects Edit from options menu
         public void onEditPhysician(String id);
     }

    @InjectView(R.id.admin_physician_detail) TextView mTextView;
    @InjectView(R.id.physician_patients_list) ListView mListView;

     public AdminPhysicianDetailFragment() {
     }

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         Bundle arguments = getArguments();
         if (arguments != null) {
             mPhysicianId = arguments.getString(PHYSICIAN_ID_KEY);
         } else if (savedInstanceState != null) {
             mPhysicianId = savedInstanceState.getString(PHYSICIAN_ID_KEY);
         }
         View rootView = inflater.inflate(R.layout.fragment_admin_physician_detail, container, false);
         setHasOptionsMenu(true);
         ButterKnife.inject(this, rootView);
         return rootView;
     }

     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         inflater.inflate(R.menu.admin_edit_delete_menu, menu);
     }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_edit:
                 ((Callbacks) getActivity()).onEditPhysician(mPhysicianId);
                 return true;
             case R.id.action_delete:
                 deletePhysician();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }

     @Override
     public void onResume() {
         super.onResume();
         Bundle arguments = getArguments();
         if (arguments != null && arguments.containsKey(PHYSICIAN_ID_KEY)) {
             mPhysicianId = arguments.getString(PHYSICIAN_ID_KEY);
             loadPhysicianFromAPI();
         }
     }

     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putString(PHYSICIAN_ID_KEY, mPhysicianId);
         super.onSaveInstanceState(outState);
     }

     private void loadPhysicianFromAPI() {
         Log.d(LOG_TAG, "Physician ID is : " + mPhysicianId);

         final SymptomManagementApi svc =  SymptomManagementService.getService();
         if (svc != null) {
             CallableTask.invoke(new Callable<Physician>() {

                 @Override
                 public Physician call() throws Exception {
                     Log.d(LOG_TAG, "getting single Physician id : " + mPhysicianId);
                     return svc.getPhysician(mPhysicianId);
                 }
             }, new TaskCallback<Physician>() {

                 @Override
                 public void success(Physician result) {
                     Log.d(LOG_TAG, "Found Physician :" + result.toString());
                     mPhysician = result;
                     mTextView.setText(mPhysician.toString());
                     displayPatientList(mPhysician.getPatients());
                 }

                 @Override
                 public void error(Exception e) {
                     Toast.makeText(
                             getActivity(),
                             "Unable to fetch Selected Physician. " +
                                     "Please check Internet connection.",
                             Toast.LENGTH_LONG).show();
                     getActivity().onBackPressed();
                 }
             });
         }
     }

     public void deletePhysician() {

         final SymptomManagementApi svc = SymptomManagementService.getService();
         if (svc != null) {
             CallableTask.invoke(new Callable<Physician>() {

                 @Override
                 public Physician call() throws Exception {
                     Log.d(LOG_TAG, "deleting Physician id : " + mPhysicianId);
                     return svc.deletePhysician(mPhysicianId);
                 }
             }, new TaskCallback<Physician>() {

                 @Override
                 public void success(Physician result) {
                     Toast.makeText(
                             getActivity(),
                             "Physician [" + result.getName() + "] deleted successfully.",
                             Toast.LENGTH_SHORT).show();
                     // re-GET the medications list .. shouldn't have the medication in it any more
                     getActivity().onBackPressed();
                 }

                 @Override
                 public void error(Exception e) {
                     Toast.makeText(
                             getActivity(),
                             "Unable to delete Physician. Please check Internet connection.",
                             Toast.LENGTH_LONG).show();
                     //re-GET the medications list ... medication should still be in the list
                     getActivity().onBackPressed();
                 }
             });
         }
     }

    private void displayPatientList(Collection<Patient> patients) {
        if (patients == null || patients.size() == 0){
            final List<Patient> emptyList = new ArrayList<Patient>();
            Patient emptyPatient = new Patient("No Patients for this Physician.", "");
            emptyList.add(emptyPatient);
            mListView.setAdapter(new ArrayAdapter<Patient>(
                    getActivity(),
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1,
                    new ArrayList(emptyList)));
        } else {
            mListView.setAdapter(new ArrayAdapter<Patient>(
                    getActivity(),
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1,
                    new ArrayList(patients)));
        }
    }

     @Override
     public void onDestroyView() {
         super.onDestroyView();
         ButterKnife.reset(this);
     }
 }
