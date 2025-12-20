package com.klaus.kmoviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.klaus.kmoviesapp.models.Movie;
import com.klaus.kmoviesapp.scraper.MovieDetailTask;
import com.klaus.kmoviesapp.scraper.StreamUrlTask;

public class MovieDetailActivity extends FragmentActivity {
    private static final String TAG = "MovieDetailActivity";

    private ImageView backdropImageView;
    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView yearTextView;
    private TextView ratingTextView;
    private TextView durationTextView;
    private TextView genreTextView;
    private TextView countryTextView;
    private TextView directorTextView;
    private TextView descriptionTextView;
    private Button playButton;
    private ProgressBar progressBar;

    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        initializeViews();
        loadMovieData();
    }

    private void initializeViews() {
        backdropImageView = findViewById(R.id.backdropImage);
        posterImageView = findViewById(R.id.posterImage);
        titleTextView = findViewById(R.id.movieTitle);
        yearTextView = findViewById(R.id.movieYear);
        ratingTextView = findViewById(R.id.movieRating);
        durationTextView = findViewById(R.id.movieDuration);
        genreTextView = findViewById(R.id.movieGenre);
        countryTextView = findViewById(R.id.movieCountry);
        directorTextView = findViewById(R.id.movieDirector);
        descriptionTextView = findViewById(R.id.movieDescription);
        playButton = findViewById(R.id.playButton);
        progressBar = findViewById(R.id.progressBar);

        playButton.setOnClickListener(v -> loadStreamUrl());
    }

    private void loadMovieData() {
        movie = (Movie) getIntent().getSerializableExtra("movie");
        
        if (movie == null) {
            Toast.makeText(this, "Error loading movie", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display initial data
        displayMovieInfo(movie);

        // Load detailed information if we have a detail URL
        if (movie.getDetailUrl() != null && !movie.getDetailUrl().isEmpty()) {
            loadDetailedInfo(movie.getDetailUrl());
        }
    }

    private void displayMovieInfo(Movie movie) {
        titleTextView.setText(movie.getTitle());

        if (movie.getYear() != null) {
            yearTextView.setText(movie.getYear());
            yearTextView.setVisibility(View.VISIBLE);
        }

        if (movie.getRating() != null) {
            ratingTextView.setText("★ " + movie.getRating());
            ratingTextView.setVisibility(View.VISIBLE);
        }

        if (movie.getDuration() != null) {
            durationTextView.setText(movie.getDuration());
            durationTextView.setVisibility(View.VISIBLE);
        }

        if (movie.getGenre() != null) {
            genreTextView.setText("Genre: " + movie.getGenre());
            genreTextView.setVisibility(View.VISIBLE);
        }

        if (movie.getCountry() != null) {
            countryTextView.setText("Country: " + movie.getCountry());
            countryTextView.setVisibility(View.VISIBLE);
        }

        if (movie.getDirector() != null) {
            directorTextView.setText("Director: " + movie.getDirector());
            directorTextView.setVisibility(View.VISIBLE);
        }

        if (movie.getDescription() != null) {
            descriptionTextView.setText(movie.getDescription());
            descriptionTextView.setVisibility(View.VISIBLE);
        }

        // Load images
        if (movie.getThumbnailUrl() != null) {
            Glide.with(this)
                .load(movie.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_movie)
                .into(posterImageView);
        }

        if (movie.getBackdropUrl() != null) {
            Glide.with(this)
                .load(movie.getBackdropUrl())
                .placeholder(R.drawable.placeholder_backdrop)
                .into(backdropImageView);
        } else if (movie.getThumbnailUrl() != null) {
            Glide.with(this)
                .load(movie.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_backdrop)
                .into(backdropImageView);
        }
    }

    private void loadDetailedInfo(String detailUrl) {
        progressBar.setVisibility(View.VISIBLE);

        MovieDetailTask task = new MovieDetailTask(new MovieDetailTask.MovieDetailCallback() {
            @Override
            public void onDetailLoaded(Movie detailedMovie) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    // Update movie object with detailed info
                    if (detailedMovie.getDescription() != null) {
                        movie.setDescription(detailedMovie.getDescription());
                    }
                    if (detailedMovie.getGenre() != null) {
                        movie.setGenre(detailedMovie.getGenre());
                    }
                    if (detailedMovie.getCountry() != null) {
                        movie.setCountry(detailedMovie.getCountry());
                    }
                    if (detailedMovie.getDirector() != null) {
                        movie.setDirector(detailedMovie.getDirector());
                    }
                    if (detailedMovie.getBackdropUrl() != null) {
                        movie.setBackdropUrl(detailedMovie.getBackdropUrl());
                    }

                    displayMovieInfo(movie);
                });
            }

            @Override
            public void onDetailError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading details: " + error);
                });
            }
        });
        task.execute(detailUrl);
    }

    private void loadStreamUrl() {
        if (movie.getDetailUrl() == null) {
            Toast.makeText(this, "Stream URL not available", Toast.LENGTH_SHORT).show();
            return;
        }

        playButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        StreamUrlTask task = new StreamUrlTask(new StreamUrlTask.StreamUrlCallback() {
            @Override
            public void onStreamUrlExtracted(String streamUrl) {
                runOnUiThread(() -> {
                    playButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    
                    movie.setStreamUrl(streamUrl);
                    playMovie(streamUrl);
                });
            }

            @Override
            public void onStreamUrlError(String error) {
                runOnUiThread(() -> {
                    playButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MovieDetailActivity.this, 
                        "Unable to load stream: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
        task.execute(movie.getDetailUrl());
    }

    private void playMovie(String streamUrl) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("stream_url", streamUrl);
        intent.putExtra("movie_title", movie.getTitle());
        startActivity(intent);
    }
}
