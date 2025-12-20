package com.klaus.kmoviesapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.klaus.kmoviesapp.R;
import com.klaus.kmoviesapp.models.Movie;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    
    private final Context context;
    private final List<Movie> movies;
    private final OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public MovieAdapter(Context context, List<Movie> movies, OnMovieClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        
        holder.titleTextView.setText(movie.getTitle());
        
        if (movie.getYear() != null && !movie.getYear().isEmpty()) {
            holder.yearTextView.setText(movie.getYear());
            holder.yearTextView.setVisibility(View.VISIBLE);
        } else {
            holder.yearTextView.setVisibility(View.GONE);
        }
        
        if (movie.getRating() != null && !movie.getRating().isEmpty()) {
            holder.ratingTextView.setText("★ " + movie.getRating());
            holder.ratingTextView.setVisibility(View.VISIBLE);
        } else {
            holder.ratingTextView.setVisibility(View.GONE);
        }
        
        if (movie.getQuality() != null && !movie.getQuality().isEmpty()) {
            holder.qualityTextView.setText(movie.getQuality());
            holder.qualityTextView.setVisibility(View.VISIBLE);
        } else {
            holder.qualityTextView.setVisibility(View.GONE);
        }
        
        if (movie.getDuration() != null && !movie.getDuration().isEmpty()) {
            holder.durationTextView.setText(movie.getDuration());
            holder.durationTextView.setVisibility(View.VISIBLE);
        } else {
            holder.durationTextView.setVisibility(View.GONE);
        }

        // Load thumbnail with Glide
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

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });

        // Focus handling for TV
        holder.cardView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView yearTextView;
        TextView ratingTextView;
        TextView qualityTextView;
        TextView durationTextView;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.movieCard);
            thumbnailImageView = itemView.findViewById(R.id.movieThumbnail);
            titleTextView = itemView.findViewById(R.id.movieTitle);
            yearTextView = itemView.findViewById(R.id.movieYear);
            ratingTextView = itemView.findViewById(R.id.movieRating);
            qualityTextView = itemView.findViewById(R.id.movieQuality);
            durationTextView = itemView.findViewById(R.id.movieDuration);
        }
    }
}
