package com.skywomantech.app.symptommanagement.patient;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.skywomantech.app.symptommanagement.R;


public class SettingsFragment extends PreferenceFragment
        implements OnPreferenceChangeListener {

    public final String LOG_TAG = SettingsFragment.class.getSimpleName();
    boolean mBindingPreference = false;

    /**
     * set up the display and putting the preferences in the summary
     *
     * @param savedInstanceState bundle of saved information
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        this.setRetainInstance(true);  // save the fragment state with rotations
    }

    /**
     * Put the preference in the summary
     *
     * @param preference map of preferences
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        mBindingPreference = true;
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
        mBindingPreference = false;
    }

    /**
     *  process any preference changes
     *
     * @param preference preference map
     * @param value summary text
     * @return true
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (!mBindingPreference) {
             if (preference.getKey().equals(getString(R.string.pref_enable_notifications_key))) {
                Log.v(LOG_TAG, "Resetting the notifications because of preference changes.");
            }
        }
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    }
}
