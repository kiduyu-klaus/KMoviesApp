package com.klaus.kmoviesapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.klaus.kmoviesapp.fragments.MainFragment;

/**
 * Host activity for the MainFragment (BrowseSupportFragment).
 * This activity is minimal and primarily serves to load the main UI fragment.
 */
public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_browse_fragment, new MainFragment());
            ft.commit();
        }
    }

    // Helper methods to launch other activities, called from MainFragment
    public void openMovieDetails(Movie movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    public void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openBrowseActivity(Category category) {
        Intent intent = new Intent(this, BrowseActivity.class);
        intent.putExtra("category", category.getName());
        intent.putExtra("url", category.getUrl());
        startActivity(intent);
    }
}
