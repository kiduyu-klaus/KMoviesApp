package com.klaus.kmoviesapp;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.klaus.kmoviesapp.fragments.MovieDetailFragment;

/**
 * Host activity for the MovieDetailFragment.
 */
public class MovieDetailActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.details_fragment_container, new MovieDetailFragment())
                .commit();
        }
    }
}
