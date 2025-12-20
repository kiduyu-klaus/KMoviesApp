package com.klaus.kmoviesapp.scraper;

import android.os.AsyncTask;
import android.util.Log;

import com.klaus.kmoviesapp.models.Movie;

import java.util.List;

public class ScraperTask extends AsyncTask<String, Void, List<Movie>> {
    private static final String TAG = "ScraperTask";
    
    public interface ScraperCallback {
        void onScrapingComplete(List<Movie> movies);
        void onScrapingError(String error);
    }

    private final ScraperCallback callback;
    private final ScraperType type;

    public enum ScraperType {
        HOME,
        MOVIES,
        TV_SHOWS,
        TOP_IMDB,
        CATEGORY,
        SEARCH
    }

    public ScraperTask(ScraperType type, ScraperCallback callback) {
        this.type = type;
        this.callback = callback;
    }

    @Override
    protected List<Movie> doInBackground(String... params) {
        try {
            switch (type) {
                case HOME:
                    return FMoviesScraper.scrapeHomePage();
                    
                case MOVIES:
                    return FMoviesScraper.scrapeMovies();
                    
                case TV_SHOWS:
                    return FMoviesScraper.scrapeTVShows();
                    
                case TOP_IMDB:
                    return FMoviesScraper.scrapeTopIMDb();
                    
                case CATEGORY:
                    if (params.length > 0) {
                        return FMoviesScraper.scrapeCategory(params[0]);
                    }
                    break;
                    
                case SEARCH:
                    if (params.length > 0) {
                        return FMoviesScraper.searchMovies(params[0]);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in scraping task: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        if (movies != null && !movies.isEmpty()) {
            callback.onScrapingComplete(movies);
        } else {
            callback.onScrapingError("No movies found or error occurred");
        }
    }
}
