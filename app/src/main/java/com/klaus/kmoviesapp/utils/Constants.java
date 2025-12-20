package com.klaus.kmoviesapp.utils;

public class Constants {
    
    // Base URLs
    public static final String BASE_URL = "https://en.fmovies24-to.com";
    
    // Endpoints
    public static final String HOME_URL = BASE_URL + "/home";
    public static final String MOVIES_URL = BASE_URL + "/movie";
    public static final String TV_SHOWS_URL = BASE_URL + "/tv-show";
    public static final String TOP_IMDB_URL = BASE_URL + "/top-imdb";
    public static final String GENRE_URL = BASE_URL + "/genre";
    public static final String COUNTRY_URL = BASE_URL + "/country";
    public static final String YEAR_URL = BASE_URL + "/year";
    public static final String SEARCH_URL = BASE_URL + "/search?keyword=";
    
    // Request timeouts
    public static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    public static final int READ_TIMEOUT = 15000; // 15 seconds
    
    // Cache settings
    public static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    public static final int MAX_CACHE_AGE = 60 * 60 * 24; // 24 hours
    
    // Player settings
    public static final int PLAYER_BUFFER_SIZE = 50 * 1024; // 50 KB
    public static final int PLAYER_MIN_BUFFER_MS = 15000; // 15 seconds
    public static final int PLAYER_MAX_BUFFER_MS = 50000; // 50 seconds
    
    // UI settings
    public static final int GRID_COLUMNS = 4;
    public static final int CARD_WIDTH_DP = 200;
    public static final int CARD_HEIGHT_DP = 280;
    
    // Error messages
    public static final String ERROR_NO_INTERNET = "No internet connection";
    public static final String ERROR_LOADING_CONTENT = "Error loading content";
    public static final String ERROR_INVALID_URL = "Invalid stream URL";
    public static final String ERROR_PLAYBACK = "Playback error occurred";
}
