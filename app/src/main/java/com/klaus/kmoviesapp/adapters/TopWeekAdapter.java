package com.klaus.kmoviesapp.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.klaus.kmoviesapp.R;
import com.klaus.kmoviesapp.models.Movie;

import java.util.List;

public class TopWeekAdapter extends RecyclerView.Adapter<TopWeekAdapter.TopWeekViewHolder> {

    private final Context context;
    private final List<Movie> movies;
    private final OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public TopWeekAdapter(Context context, List<Movie> movies, OnMovieClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopWeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_week, parent, false);
        return new TopWeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopWeekViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.titleTextView.setText(movie.getTitle());

        if (movie.getYear() != null && !movie.getYear().isEmpty()) {
            holder.yearTextView.setText(movie.getYear());
            holder.yearTextView.setVisibility(View.VISIBLE);
        } else {
            holder.yearTextView.setVisibility(View.GONE);
        }

        if (movie.getGenre() != null && !movie.getGenre().isEmpty()) {
            holder.genreTextView.setText(movie.getGenre());
            holder.genreTextView.setVisibility(View.VISIBLE);
        } else {
            holder.genreTextView.setVisibility(View.GONE);
        }

        if (movie.getType() != null && !movie.getType().isEmpty()) {
            holder.typeTextView.setText(movie.getType().equals("tv") ? "TV Show" : "Movie");
            holder.typeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.typeTextView.setVisibility(View.GONE);
        }

        if (movie.getRating() != null && !movie.getRating().isEmpty()) {
            holder.ratingTextView.setText(movie.getRating());
            holder.ratingContainer.setVisibility(View.VISIBLE);
        } else {
            holder.ratingContainer.setVisibility(View.GONE);
        }

        // Load thumbnail
        if (movie.getThumbnailUrl() != null && !movie.getThumbnailUrl().isEmpty()) {
            Glide.with(context)
                    .load(movie.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder_movie)
                    .error(R.drawable.placeholder_movie)
                    .centerCrop()
                    .into(holder.thumbnailImageView);
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.placeholder_movie);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });

        // Focus handling for TV
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(200).start();
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class TopWeekViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView yearTextView;
        TextView genreTextView;
        TextView typeTextView;
        TextView ratingTextView;
        View ratingContainer;

        public TopWeekViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.topWeekThumbnail);
            titleTextView = itemView.findViewById(R.id.topWeekTitle);
            yearTextView = itemView.findViewById(R.id.topWeekYear);
            genreTextView = itemView.findViewById(R.id.topWeekGenre);
            typeTextView = itemView.findViewById(R.id.topWeekType);
            ratingTextView = itemView.findViewById(R.id.topWeekRating);
            ratingContainer = itemView.findViewById(R.id.topWeekRatingContainer);
        }
    }
}