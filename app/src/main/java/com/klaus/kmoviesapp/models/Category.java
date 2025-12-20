package com.klaus.kmoviesapp.models;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private String url;
    private List<Movie> movies;

    public Category(String name, String url) {
        this.name = name;
        this.url = url;
        this.movies = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public void addMovie(Movie movie) {
        this.movies.add(movie);
    }
}
