package com.klaus.kmoviesapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Movie implements Serializable {
    private String id;
    private String title;
    private String year;
    private String rating;
    private String duration;
    private String quality;
    private String thumbnailUrl;
    private String backdropUrl;
    private String description;
    private String type; // "movie" or "tv"
    private String country;
    private String genre;
    private String director;
    private String detailUrl;
    private String streamUrl;
    private String actors;
    private String keywords;
    private List<StreamSource> streamSources;

    public static class StreamSource implements Serializable {
        private String label;
        private String url;
        private int index;

        public StreamSource(String label, String url, int index) {
            this.label = label;
            this.url = url;
            this.index = index;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return label + " (Server " + index + ")";
        }
    }

    public Movie() {
        this.streamSources = new ArrayList<>();
    }

    public Movie(String id, String title, String year, String thumbnailUrl) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.thumbnailUrl = thumbnailUrl;
        this.streamSources = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getBackdropUrl() {
        return backdropUrl;
    }

    public void setBackdropUrl(String backdropUrl) {
        this.backdropUrl = backdropUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public List<StreamSource> getStreamSources() {
        return streamSources;
    }

    public void setStreamSources(List<StreamSource> streamSources) {
        this.streamSources = streamSources;
    }

    public void addStreamSource(String label, String url, int index) {
        if (this.streamSources == null) {
            this.streamSources = new ArrayList<>();
        }
        this.streamSources.add(new StreamSource(label, url, index));
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", rating='" + rating + '\'' +
                ", quality='" + quality + '\'' +
                '}';
    }
}