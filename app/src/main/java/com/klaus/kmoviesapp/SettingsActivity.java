package com.klaus.kmoviesapp;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.klaus.kmoviesapp.fragments.SettingsFragment;

/**
 * Activity to host the SettingsFragment.
 */
public class SettingsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment_container, new SettingsFragment())
                .commit();
        }
    }
}
