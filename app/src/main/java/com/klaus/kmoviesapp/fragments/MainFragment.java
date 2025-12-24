package com.klaus.kmoviesapp.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.core.content.ContextCompat;

import com.klaus.kmoviesapp.MovieDetailActivity;
import com.klaus.kmoviesapp.R;
import com.klaus.kmoviesapp.SettingsActivity;
import com.klaus.kmoviesapp.models.Movie;
import com.klaus.kmoviesapp.presenters.CardPresenter;
import com.klaus.kmoviesapp.scraper.ScraperTask;

import java.util.List;

/**
 * Main fragment for the home screen, using BrowseSupportFragment for the Netflix-style UI.
 */
public class MainFragment extends BrowseSupportFragment {
    private static final String TAG = "MainFragment";
    private ArrayObjectAdapter mRowsAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        setupUIElements();
        loadRows();
        setupEventListeners();
    }

    private void setupUIElements() {
        setTitle(getString(R.string.app_name));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // Set fastLane (side menu) background color
        setBrandColor(ContextCompat.getColor(requireContext(), R.color.fastlane_background));
        // Set search icon color
        setSearchAffordanceColor(ContextCompat.getColor(requireContext(), R.color.search_opaque));
    }

    private void loadRows() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        // 1. Add Featured Banner Row (Placeholder for now)
        // In a real app, this would be a custom Presenter for a single item
        // We will use a regular ListRow for simplicity, but a custom BannerPresenter would be better
        HeaderItem bannerHeader = new HeaderItem(0, "Featured");
        ArrayObjectAdapter bannerAdapter = new ArrayObjectAdapter(new CardPresenter());
        // Use a high-quality image URL for the featured item
        Movie featuredMovie = new Movie("13 Reasons Why", "Season 3 Coming Friday", "https://i.imgur.com/g0t4j3e.jpg", "https://en.fmovies24-to.com/series/13-reasons-why-season-3-2019-12345");
        featuredMovie.setStudio("Netflix Original");
        featuredMovie.setDescription("Months after the Spring Fling, Liberty High is hit with a new shock when Bryce Walker is murdered the night of homecoming ... and everyone is a suspect.");
        featuredMovie.setRating("97% Match");
        featuredMovie.setYear("2018");
        bannerAdapter.add(featuredMovie);
        mRowsAdapter.add(new ListRow(bannerHeader, bannerAdapter));

        // 2. Load Content Rows (e.g., Home, Movies, TV Shows)
        loadContentRows();

        setAdapter(mRowsAdapter);
    }

    private void loadContentRows() {
        // Use ScraperTask to fetch content for different categories
        // This is a simplified example, a real implementation would handle multiple categories

        // Load "Coming This Week" (Home content)
        loadCategoryRow(ScraperTask.ScraperType.HOME, "Coming This Week", 1);

        // Load "Movies"
        loadCategoryRow(ScraperTask.ScraperType.MOVIES, "Movies", 2);

        // Load "TV Shows"
        loadCategoryRow(ScraperTask.ScraperType.TV_SHOWS, "TV Shows", 3);
    }

    private void loadCategoryRow(ScraperTask.ScraperType type, String title, int id) {
        new ScraperTask(type, new ScraperTask.ScraperCallback() {
            @Override
            public void onScrapingComplete(List<Movie> movies) {
                if (isAdded()) {
                    ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
                    for (Movie movie : movies) {
                        listRowAdapter.add(movie);
                    }
                    HeaderItem header = new HeaderItem(id, title);
                    mRowsAdapter.add(new ListRow(header, listRowAdapter));
                    mRowsAdapter.notifyArrayItemRangeChanged(id, 1);
                }
            }

            @Override
            public void onScrapingError(String error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error loading " + title + ": " + error, Toast.LENGTH_LONG).show();
                }
            }
        }).execute();
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Log.d(TAG, "Item: " + movie.getTitle());
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                intent.putExtra("movie", movie);
                startActivity(intent);
            } else if (item instanceof String) {
                // Handle settings or other custom clicks
                if (item.equals("Settings")) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Movie) {
                // Update background or show details for selected movie
                // For the Netflix-style, the background would change to the movie's backdrop
            }
        }
    }

    /**
     * Placeholder CardPresenter for demonstration.
     * This needs to be replaced with a proper implementation that uses CardView and Glide.
     */
// The custom CardPresenter is now in a separate file (presenters/CardPresenter.java)
}
