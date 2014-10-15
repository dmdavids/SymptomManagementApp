package com.skywomantech.app.symptommanagement;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;


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
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_enable_auto_login_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_enable_med_reminders_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_enable_notifications_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_enable_reminders_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_login_name_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_login_password_key)));
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
            if ( preference.getKey().equals(getString(R.string.pref_enable_reminders_key))
                || preference.getKey().equals(getString(R.string.pref_enable_med_reminders_key))) {
                // TODO: process changing of the alerts here
                Log.v(LOG_TAG, "Resetting the alert timers because of preference changes.");

            } else if (preference.getKey().equals(getString(R.string.pref_enable_notifications_key))) {
                Log.v(LOG_TAG, "Resetting the notifications because of preference changes.");
            }if ( preference.getKey().equals(getString(R.string.pref_login_name_key))
                  || preference.getKey().equals(getString(R.string.pref_login_password_key))
                  || preference.getKey().equals(getString(R.string.pref_enable_auto_login_key))){
                //TODO: appropriately handle the change of login information or the automatic login
                // processing here
                Log.v(LOG_TAG, "Processing the automatic login due to preference changes.");
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
