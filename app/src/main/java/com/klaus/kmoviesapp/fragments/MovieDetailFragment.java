package com.klaus.kmoviesapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.app.DetailsSupportFragmentBackgroundController;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnActionClickedListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.klaus.kmoviesapp.PlayerActivity;
import com.klaus.kmoviesapp.R;
import com.klaus.kmoviesapp.models.Movie;
import com.klaus.kmoviesapp.presenters.CardPresenter;
import com.klaus.kmoviesapp.presenters.DetailsDescriptionPresenter;
import com.klaus.kmoviesapp.scraper.MovieDetailTask;
import com.klaus.kmoviesapp.scraper.StreamUrlTask;

import java.util.Collections;
import java.util.List;

/**
 * Fragment for displaying movie details using Leanback's DetailsSupportFragment.
 */
public class MovieDetailFragment extends DetailsSupportFragment {
    private static final String TAG = "MovieDetailFragment";

    private static final int ACTION_PLAY = 1;
    private static final int ACTION_RELATED = 2;

    private Movie mSelectedMovie;
    private DetailsSupportFragmentBackgroundController mBackgroundController;
    private ArrayObjectAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBackgroundController = new DetailsSupportFragmentBackgroundController(this);
        mSelectedMovie = (Movie) getActivity().getIntent().getSerializableExtra("movie");

        if (mSelectedMovie != null) {
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setupRelatedContentRow();
            setupEventListeners();
        } else {
            closeActivity();
        }
    }

    private void closeActivity() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void setupDetailsOverviewRow() {
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedMovie);
        
        // Load image for the overview row
        Glide.with(requireContext())
                .asBitmap()
                .load(mSelectedMovie.getThumbnailUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        row.setImageBitmap(requireContext(), resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Do nothing
                    }
                });

        // Add actions
        ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter();
        actionAdapter.add(new Action(ACTION_PLAY, getString(R.string.play), "Start Streaming"));
        actionAdapter.add(new Action(ACTION_RELATED, getString(R.string.related_movies), "View Related"));
        row.setActionsAdapter(actionAdapter);

        // Load detailed info
        loadDetailedInfo(row);

        mAdapter = new ArrayObjectAdapter(new ClassPresenterSelector());
        mAdapter.add(row);
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail presenter
        FullWidthDetailsOverviewRowPresenter detailsPresenter = new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.detail_background));
        detailsPresenter.setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_HALF);

        // Set up event listener for the details presenter
        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_PLAY) {
                    loadStreamUrl();
                } else if (action.getId() == ACTION_RELATED) {
                    Toast.makeText(requireContext(), "Related Movies Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAdapter.registerPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    private void setupRelatedContentRow() {
        // Placeholder for related content
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        // Add a dummy movie for demonstration
        listRowAdapter.add(new Movie("Related Movie 1", "Action", "https://example.com/related1.jpg", "https://example.com/related1.html"));
        listRowAdapter.add(new Movie("Related Movie 2", "Comedy", "https://example.com/related2.jpg", "https://example.com/related2.html"));

        HeaderItem header = new HeaderItem(0, getString(R.string.related_movies));
        mAdapter.add(new ListRow(header, listRowAdapter));

        mAdapter.registerPresenter(ListRow.class, new ListRowPresenter());
        setAdapter(mAdapter);
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                intent.putExtra("movie", movie);
                startActivity(intent);
            }
        });
    }

    private void loadDetailedInfo(DetailsOverviewRow row) {
        // Load detailed information if we have a detail URL
        if (mSelectedMovie.getDetailUrl() != null && !mSelectedMovie.getDetailUrl().isEmpty()) {
            new MovieDetailTask(new MovieDetailTask.MovieDetailCallback() {
                @Override
                public void onDetailLoaded(Movie detailedMovie) {
                    if (isAdded()) {
                        // Update movie object with detailed info
                        mSelectedMovie.setDescription(detailedMovie.getDescription() != null ? detailedMovie.getDescription() : mSelectedMovie.getDescription());
                        mSelectedMovie.setGenre(detailedMovie.getGenre() != null ? detailedMovie.getGenre() : mSelectedMovie.getGenre());
                        mSelectedMovie.setCountry(detailedMovie.getCountry() != null ? detailedMovie.getCountry() : mSelectedMovie.getCountry());
                        mSelectedMovie.setDirector(detailedMovie.getDirector() != null ? detailedMovie.getDirector() : mSelectedMovie.getDirector());
                        mSelectedMovie.setBackdropUrl(detailedMovie.getBackdropUrl() != null ? detailedMovie.getBackdropUrl() : mSelectedMovie.getBackdropUrl());

                        // Update the row and background
                        row.setItem(mSelectedMovie);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                        updateBackground(mSelectedMovie.getBackdropUrl());
                    }
                }

                @Override
                public void onDetailError(String error) {
                    Log.e(TAG, "Error loading details: " + error);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error loading details: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            }).execute(mSelectedMovie.getDetailUrl());
        }
    }

    private void updateBackground(String url) {
        if (url != null) {
            mBackgroundController.enableParallax();
            mBackgroundController.setCoverBitmap(null); // Clear previous image
            Glide.with(requireContext())
                    .asBitmap()
                    .load(url)
                    .centerCrop()
                    .into(mBackgroundController.getTarget());
        }
    }

    private void loadStreamUrl() {
        if (mSelectedMovie.getDetailUrl() == null) {
            Toast.makeText(requireContext(), "Stream URL not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Loading stream URL...", Toast.LENGTH_SHORT).show();

        new StreamUrlTask(new StreamUrlTask.StreamUrlCallback() {
            @Override
            public void onStreamUrlExtracted(String streamUrl) {
                if (isAdded()) {
                    mSelectedMovie.setStreamUrl(streamUrl);
                    playMovie(streamUrl);
                }
            }

            @Override
            public void onStreamUrlError(String error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Unable to load stream: " + error, Toast.LENGTH_LONG).show();
                }
            }
        }).execute(mSelectedMovie.getDetailUrl());
    }

    private void playMovie(String streamUrl) {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra("stream_url", streamUrl);
        intent.putExtra("movie_title", mSelectedMovie.getTitle());
        startActivity(intent);
    }
}
