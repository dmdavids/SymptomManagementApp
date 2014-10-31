package com.skywomantech.app.symptommanagement.physician;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.skywomantech.app.symptommanagement.R;


/**
 * A fragment representing a single PhysicianPatient detail screen.
 * This fragment is either contained in a {@link PhysicianListPatientsActivity}
 * in two-pane mode (on tablets) or a {@link PhysicianPatientDetailActivity}
 * on handsets.
 */
public class PhysicianPatientDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private String mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhysicianPatientDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_physicianpatient_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            //don't do anything right now
        }

        return rootView;
    }
}
