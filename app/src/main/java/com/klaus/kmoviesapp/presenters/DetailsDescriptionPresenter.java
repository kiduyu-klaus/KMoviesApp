package com.klaus.kmoviesapp.presenters;

import androidx.leanback.widget.Abstract='true'DetailsDescriptionPresenter;
import com.klaus.kmoviesapp.models.Movie;

/**
 * A custom DetailsDescriptionPresenter to display the details of a Movie object.
 * This is used in the DetailsOverviewRow of the MovieDetailActivity.
 */
public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Movie movie = (Movie) item;

        if (movie != null) {
            viewHolder.getTitle().setText(movie.getTitle());
            viewHolder.getSubtitle().setText(movie.getStudio());
            viewHolder.getBody().setText(movie.getDescription());
        }
    }
}
