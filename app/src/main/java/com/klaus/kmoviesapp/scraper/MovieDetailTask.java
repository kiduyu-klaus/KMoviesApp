package com.klaus.kmoviesapp.scraper;

import android.os.AsyncTask;
import android.util.Log;

import com.klaus.kmoviesapp.models.Movie;

public class MovieDetailTask extends AsyncTask<String, Void, Movie> {
    private static final String TAG = "MovieDetailTask";

    public interface MovieDetailCallback {
        void onDetailLoaded(Movie movie);
        void onDetailError(String error);
    }

    private final MovieDetailCallback callback;

    public MovieDetailTask(MovieDetailCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Movie doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }

        String detailUrl = params[0];
        try {
            return FMoviesScraper.scrapeMovieDetails(detailUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error loading movie details: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Movie movie) {
        if (movie != null && movie.getTitle() != null) {
            callback.onDetailLoaded(movie);
        } else {
            callback.onDetailError("Failed to load movie details");
        }
    }
}
