package com.klaus.kmoviesapp.scraper;

import android.util.Log;

import com.klaus.kmoviesapp.models.Movie;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FMoviesScraper {
    private static final String TAG = "FMoviesScraper";
    private static final String BASE_URL = "https://en.fmovies24-to.com";
    private static final int TIMEOUT = 10000; // 10 seconds

    /**
     * Scrape movies from the home page
     */
    public static List<Movie> scrapeHomePage() {
        return scrapeMoviesFromUrl(BASE_URL + "/home");
    }

    /**
     * Scrape movies from a specific category
     */
    public static List<Movie> scrapeCategory(String categoryPath) {
        return scrapeMoviesFromUrl(BASE_URL + categoryPath);
    }

    /**
     * Scrape movies page
     */
    public static List<Movie> scrapeMovies() {
        return scrapeMoviesFromUrl(BASE_URL + "/movie");
    }

    /**
     * Scrape TV shows page
     */
    public static List<Movie> scrapeTVShows() {
        return scrapeMoviesFromUrl(BASE_URL + "/tv-show");
    }

    /**
     * Scrape Top IMDb page
     */
    public static List<Movie> scrapeTopIMDb() {
        return scrapeMoviesFromUrl(BASE_URL + "/top-imdb");
    }

    /**
     * Generic method to scrape movies from any URL
     */
    private static List<Movie> scrapeMoviesFromUrl(String url) {
        List<Movie> movies = new ArrayList<>();
        
        try {
            Log.d(TAG, "Scraping URL: " + url);
            Document doc = Jsoup.connect(url)
                    .timeout(TIMEOUT)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            // Find all movie cards - adjust selectors based on actual HTML structure
            Elements movieCards = doc.select("div.film_list-wrap div.flw-item");
            
            if (movieCards.isEmpty()) {
                // Try alternative selectors
                movieCards = doc.select("article.item");
            }

            Log.d(TAG, "Found " + movieCards.size() + " movie cards");

            for (Element card : movieCards) {
                try {
                    Movie movie = parseMovieCard(card);
                    if (movie != null) {
                        movies.add(movie);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing movie card: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Error scraping movies: " + e.getMessage());
        }

        return movies;
    }

    /**
     * Parse a single movie card element
     */
    private static Movie parseMovieCard(Element card) {
        Movie movie = new Movie();

        try {
            // Extract detail URL
            Element linkElement = card.selectFirst("a[href]");
            if (linkElement != null) {
                String detailUrl = linkElement.attr("href");
                if (!detailUrl.startsWith("http")) {
                    detailUrl = BASE_URL + detailUrl;
                }
                movie.setDetailUrl(detailUrl);
                
                // Extract ID from URL
                String[] urlParts = detailUrl.split("/");
                if (urlParts.length > 0) {
                    movie.setId(urlParts[urlParts.length - 1]);
                }
            }

            // Extract thumbnail
            Element imgElement = card.selectFirst("img");
            if (imgElement != null) {
                String thumbnail = imgElement.attr("data-src");
                if (thumbnail.isEmpty()) {
                    thumbnail = imgElement.attr("src");
                }
                movie.setThumbnailUrl(thumbnail);
            }

            // Extract title
            Element titleElement = card.selectFirst("h3.film-name a, h2.title a, a.title");
            if (titleElement != null) {
                movie.setTitle(titleElement.text().trim());
            }

            // Extract quality badge
            Element qualityElement = card.selectFirst("span.quality, div.quality");
            if (qualityElement != null) {
                movie.setQuality(qualityElement.text().trim());
            }

            // Extract rating
            Element ratingElement = card.selectFirst("span.rating, div.rating");
            if (ratingElement != null) {
                movie.setRating(ratingElement.text().trim());
            }

            // Extract year
            Element yearElement = card.selectFirst("span.year, div.year");
            if (yearElement != null) {
                movie.setYear(yearElement.text().trim());
            }

            // Extract duration
            Element durationElement = card.selectFirst("span.duration, div.duration");
            if (durationElement != null) {
                movie.setDuration(durationElement.text().trim());
            }

            // Set default type
            movie.setType("movie");

            Log.d(TAG, "Parsed movie: " + movie.getTitle());
            return movie;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing movie card: " + e.getMessage());
            return null;
        }
    }

    /**
     * Scrape detailed information for a specific movie
     */
    public static Movie scrapeMovieDetails(String detailUrl) {
        Movie movie = new Movie();
        
        try {
            Log.d(TAG, "Scraping movie details: " + detailUrl);
            Document doc = Jsoup.connect(detailUrl)
                    .timeout(TIMEOUT)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            // Extract title
            Element titleElement = doc.selectFirst("h1.title, h1.heading-name");
            if (titleElement != null) {
                movie.setTitle(titleElement.text().trim());
            }

            // Extract description
            Element descElement = doc.selectFirst("div.description, div.synopsis");
            if (descElement != null) {
                movie.setDescription(descElement.text().trim());
            }

            // Extract metadata
            Elements metaElements = doc.select("div.meta div.row");
            for (Element meta : metaElements) {
                String label = meta.selectFirst("strong, span.label")
                        .text().toLowerCase().trim();
                String value = meta.selectFirst("span.value, a")
                        .text().trim();

                if (label.contains("country")) {
                    movie.setCountry(value);
                } else if (label.contains("genre")) {
                    movie.setGenre(value);
                } else if (label.contains("director")) {
                    movie.setDirector(value);
                } else if (label.contains("release")) {
                    movie.setYear(value);
                } else if (label.contains("duration")) {
                    movie.setDuration(value);
                }
            }

            // Extract rating
            Element ratingElement = doc.selectFirst("span.rating, div.rating");
            if (ratingElement != null) {
                movie.setRating(ratingElement.text().trim());
            }

            // Extract quality
            Element qualityElement = doc.selectFirst("span.quality, div.quality");
            if (qualityElement != null) {
                movie.setQuality(qualityElement.text().trim());
            }

            // Extract backdrop image
            Element backdropElement = doc.selectFirst("div.backdrop img, div.cover img");
            if (backdropElement != null) {
                String backdrop = backdropElement.attr("src");
                movie.setBackdropUrl(backdrop);
            }

            movie.setDetailUrl(detailUrl);

            Log.d(TAG, "Scraped movie details: " + movie.getTitle());

        } catch (IOException e) {
            Log.e(TAG, "Error scraping movie details: " + e.getMessage());
        }

        return movie;
    }

    /**
     * Extract streaming URL from movie page
     * This method needs to be adapted based on the actual player implementation
     */
    public static String extractStreamUrl(String detailUrl) {
        try {
            Log.d(TAG, "Extracting stream URL from: " + detailUrl);
            Document doc = Jsoup.connect(detailUrl)
                    .timeout(TIMEOUT)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            // Look for iframe sources
            Elements iframes = doc.select("iframe[src]");
            for (Element iframe : iframes) {
                String src = iframe.attr("src");
                if (src.contains("embed") || src.contains("player")) {
                    Log.d(TAG, "Found potential stream URL: " + src);
                    return src;
                }
            }

            // Look for video sources
            Elements videos = doc.select("video source[src]");
            if (!videos.isEmpty()) {
                String src = videos.first().attr("src");
                Log.d(TAG, "Found video source: " + src);
                return src;
            }

            // Look for data attributes that might contain video URLs
            Elements dataElements = doc.select("[data-src], [data-url]");
            for (Element elem : dataElements) {
                String dataSrc = elem.attr("data-src");
                if (dataSrc.isEmpty()) {
                    dataSrc = elem.attr("data-url");
                }
                if (dataSrc.contains(".m3u8") || dataSrc.contains(".mp4")) {
                    Log.d(TAG, "Found stream URL in data attribute: " + dataSrc);
                    return dataSrc;
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Error extracting stream URL: " + e.getMessage());
        }

        return null;
    }

    /**
     * Search for movies
     */
    public static List<Movie> searchMovies(String query) {
        String searchUrl = BASE_URL + "/search?keyword=" + query.replace(" ", "+");
        return scrapeMoviesFromUrl(searchUrl);
    }
}
