package com.klaus.kmoviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.klaus.kmoviesapp.adapters.CategoryAdapter;
import com.klaus.kmoviesapp.adapters.MovieAdapter;
import com.klaus.kmoviesapp.models.Category;
import com.klaus.kmoviesapp.models.Movie;
import com.klaus.kmoviesapp.scraper.ScraperTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";

    private RecyclerView categoryRecyclerView;
    private RecyclerView moviesRecyclerView;
    private ProgressBar progressBar;
    
    private CategoryAdapter categoryAdapter;
    private MovieAdapter movieAdapter;
    
    private List<Category> categories;
    private List<Movie> currentMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupCategories();
        loadHomeContent();
    }

    private void initializeViews() {
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Setup category RecyclerView
        categoryRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        // Setup movies RecyclerView
        moviesRecyclerView.setLayoutManager(
           //new GridLayoutManager(this, 4)
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        currentMovies = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, currentMovies, new MovieAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(Movie movie) {
                openMovieDetails(movie);
            }
        });
        moviesRecyclerView.setAdapter(movieAdapter);
    }

    private void setupCategories() {
        categories = new ArrayList<>();
        categories.add(new Category("Home", "/home"));
        categories.add(new Category("Movies", "/movie"));
        categories.add(new Category("TV Shows", "/tv-show"));
        categories.add(new Category("Top IMDb", "/top-imdb"));
        categories.add(new Category("Genre", "/genre"));
        categories.add(new Category("Country", "/country"));
        categories.add(new Category("Year", "/year"));
        categories.add(new Category("Settings", "settings"));

        categoryAdapter = new CategoryAdapter(this, categories, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                loadCategoryContent(category);
            }
        });
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void loadHomeContent() {
        showLoading(true);
        
        ScraperTask task = new ScraperTask(ScraperTask.ScraperType.HOME, new ScraperTask.ScraperCallback() {
            @Override
            public void onScrapingComplete(List<Movie> movies) {
                runOnUiThread(() -> {
                    showLoading(false);
                    currentMovies.clear();
                    currentMovies.addAll(movies);
                    movieAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + movies.size() + " movies");
                });
            }

            @Override
            public void onScrapingError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error: " + error);
                });
            }
        });
        task.execute();
    }

    private void loadCategoryContent(Category category) {
        showLoading(true);
        
        ScraperTask.ScraperType scraperType;
        String categoryPath = category.getUrl();

        // Determine scraper type based on category
        if (categoryPath.equals("/home")) {
            scraperType = ScraperTask.ScraperType.HOME;
        } else if (categoryPath.equals("/movie")) {
            scraperType = ScraperTask.ScraperType.MOVIES;
        } else if (categoryPath.equals("/tv-show")) {
            scraperType = ScraperTask.ScraperType.TV_SHOWS;
        } else if (categoryPath.equals("/top-imdb")) {
            scraperType = ScraperTask.ScraperType.TOP_IMDB;
        } else if (categoryPath.equals("settings")) {
            openSettingsActivity();
            showLoading(false);
            return;
        } else {
            // For Genre, Country, Year - open browse activity
            openBrowseActivity(category);
            showLoading(false);
            return;
        }

        ScraperTask task = new ScraperTask(scraperType, new ScraperTask.ScraperCallback() {
            @Override
            public void onScrapingComplete(List<Movie> movies) {
                runOnUiThread(() -> {
                    showLoading(false);
                    currentMovies.clear();
                    currentMovies.addAll(movies);
                    movieAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, 
                        "Loaded " + category.getName(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onScrapingError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
        task.execute();
    }

    private void openMovieDetails(Movie movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openBrowseActivity(Category category) {
        Intent intent = new Intent(this, BrowseActivity.class);
        intent.putExtra("category", category.getName());
        intent.putExtra("url", category.getUrl());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        moviesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
