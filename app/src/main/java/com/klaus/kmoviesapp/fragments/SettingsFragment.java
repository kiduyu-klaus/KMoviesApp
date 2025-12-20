package com.klaus.kmoviesapp.fragments;

import android.os.Bundle;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.klaus.kmoviesapp.R;

/**
 * Fragment for displaying application settings.
 * Uses LeanbackPreferenceFragmentCompat for TV-optimized settings UI.
 */
public class SettingsFragment extends LeanbackPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Example of handling a specific preference click
        Preference aboutPreference = findPreference(getString(R.string.pref_key_about));
        if (aboutPreference != null) {
            aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Handle about screen navigation or dialog here
                    // For now, just log or show a toast
                    return true;
                }
            });
        }
    }

    /**
     * Helper method to navigate back to the main screen or close the settings.
     */
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.pref_key_back))) {
            // Close the settings fragment
            if (getActivity() != null) {
                getActivity().finish();
            }
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
}
