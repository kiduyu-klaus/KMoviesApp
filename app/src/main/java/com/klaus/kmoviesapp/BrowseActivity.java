package com.klaus.kmoviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.klaus.kmoviesapp.adapters.MovieAdapter;
import com.klaus.kmoviesapp.models.Movie;
import com.klaus.kmoviesapp.scraper.ScraperTask;

import java.util.ArrayList;
import java.util.List;

public class BrowseActivity extends FragmentActivity {
    private static final String TAG = "BrowseActivity";

    private TextView titleTextView;
    private RecyclerView moviesRecyclerView;
    private ProgressBar progressBar;
    
    private MovieAdapter movieAdapter;
    private List<Movie> movies;
    
    private String categoryName;
    private String categoryUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        categoryName = getIntent().getStringExtra("category");
        categoryUrl = getIntent().getStringExtra("url");

        initializeViews();
        loadContent();
    }

    private void initializeViews() {
        titleTextView = findViewById(R.id.browseTitle);
        moviesRecyclerView = findViewById(R.id.browseRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        titleTextView.setText(categoryName);

        // Setup RecyclerView with Grid Layout
        moviesRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        movies = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, movies, new MovieAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(Movie movie) {
                openMovieDetails(movie);
            }
        });
        moviesRecyclerView.setAdapter(movieAdapter);
    }

    private void loadContent() {
        showLoading(true);

        ScraperTask task = new ScraperTask(
            ScraperTask.ScraperType.CATEGORY,
            new ScraperTask.ScraperCallback() {
                @Override
                public void onScrapingComplete(List<Movie> loadedMovies) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        movies.clear();
                        movies.addAll(loadedMovies);
                        movieAdapter.notifyDataSetChanged();
                    });
                }

                @Override
                public void onScrapingError(String error) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(BrowseActivity.this, error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        );
        task.execute(categoryUrl);
    }

    private void openMovieDetails(Movie movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        moviesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
