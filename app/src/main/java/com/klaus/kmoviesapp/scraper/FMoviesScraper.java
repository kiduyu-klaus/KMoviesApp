package com.klaus.kmoviesapp.scraper;

import android.util.Base64;
import android.util.Log;

import com.klaus.kmoviesapp.models.Movie;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class FMoviesScraper {
    private static final String TAG = "FMoviesScraper";
    private static final String BASE_URL = "https://ww4.fmovies.co";
    private static final int TIMEOUT = 10000; // 10 seconds

    private static SSLContext socketFactory;

    /**
     * Get SSL Socket Factory that trusts all certificates
     * WARNING: Only use this for testing/development
     */
    private static SSLContext getSSLSocketFactory() {
        if (socketFactory != null) {
            return socketFactory;
        }

        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            //socketFactory = sslContext.getSocketFactory();
            return sslContext;
        } catch (Exception e) {
            Log.e(TAG, "Error creating SSL Socket Factory: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create a secure connection with SSL handling
     */
    private static Connection createConnection(String url) {
        return Jsoup.connect(url)
                .sslContext(getSSLSocketFactory())
                .timeout(TIMEOUT)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .referrer("https://www.google.com")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1");
    }

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
        return scrapeMoviesFromUrl(BASE_URL + "/tv");
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
            Document doc = createConnection(url).get();

            // Find all movie cards - adjust selectors based on actual HTML structure
            Elements movieCards = doc.select("div.col");

            if (movieCards.isEmpty()) {
                // Try alternative selectors
                movieCards = doc.select("div.col");
            }

            if (movieCards.isEmpty()) {
                // Try more generic selectors
                movieCards = doc.select("div.col");
            }

            Log.d(TAG, "Found " + movieCards.size() + " movie cards");

            for (Element card : movieCards) {
                try {
                    Movie movie = parseMovieCard(card);
                    if (movie != null && movie.getTitle() != null) {
                        movies.add(movie);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing movie card: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Error scraping movies: " + e.getMessage());
            e.printStackTrace();
            System.setProperty("javax.net.debug", "ssl");
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        return movies;
    }

    /**
     * Parse a single movie card element
     */
    private static Movie parseMovieCard(Element card) {
        Movie movie = new Movie();

        try {
            // Extract detail URL from <a class="rounded poster">
            Element linkElement = card.selectFirst("a.rounded.poster[href], a[href]");
            if (linkElement != null) {
                String detailUrl = linkElement.attr("href");
                if (!detailUrl.startsWith("http")) {
                    detailUrl = BASE_URL + detailUrl;
                }
                movie.setDetailUrl(detailUrl);

                // Extract ID from URL (e.g., /film/wake-up-dead-man-a-knives-out-mystery-1630860264/)
                String[] urlParts = detailUrl.split("/");
                if (urlParts.length > 0) {
                    // Get the last non-empty part
                    for (int i = urlParts.length - 1; i >= 0; i--) {
                        if (!urlParts[i].isEmpty()) {
                            movie.setId(urlParts[i]);
                            break;
                        }
                    }
                }
            }

            // Extract thumbnail from <img> inside <picture>
            Element imgElement = card.selectFirst("picture img.lazy, picture img, img.lazy, img");
            if (imgElement != null) {
                // Try data-src first (lazy loading)
                String thumbnail = imgElement.attr("data-src");
                if (thumbnail.isEmpty()) {
                    thumbnail = imgElement.attr("src");
                }
                if (thumbnail.isEmpty()) {
                    thumbnail = imgElement.attr("data-original");
                }
                movie.setThumbnailUrl(thumbnail);

                // Extract title from alt attribute if available
                String altTitle = imgElement.attr("alt");
                if (!altTitle.isEmpty() && movie.getTitle() == null) {
                    movie.setTitle(altTitle.trim());
                }
            }

            // Extract quality badge from <span class="mlbq"> (e.g., "HD")
            Element qualityElement = card.selectFirst("span.mlbq");
            if (qualityElement != null) {
                movie.setQuality(qualityElement.text().trim());
            }

            // Extract title from <h2 class="card-title"> in <div class="card-body">
            Element titleElement = card.selectFirst("div.card-body h2.card-title, h2.card-title");
            if (titleElement != null) {
                movie.setTitle(titleElement.text().trim());
            }

            // Extract year and duration from URL or other elements if available
            // Note: The new HTML structure doesn't show year/duration in the example
            // You may need to extract these from the detail page or another source

            // Try to extract year from URL pattern (e.g., wake-up-dead-man-a-knives-out-mystery-1630860264)
            if (movie.getDetailUrl() != null) {
                String url = movie.getDetailUrl();
                // Look for a year pattern in the URL
                Pattern yearPattern = Pattern.compile("-(\\d{4})-|/(\\d{4})/");
                Matcher matcher = yearPattern.matcher(url);
                if (matcher.find()) {
                    String year = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                    if (year != null && year.matches("\\d{4}")) {
                        movie.setYear(year);
                    }
                }
            }

            // Try to determine type from icon or URL
            Element iconElement = card.selectFirst("i.fa-clapperboard, i.fa-tv");
            if (iconElement != null) {
                if (iconElement.hasClass("fa-tv")) {
                    movie.setType("tv");
                } else {
                    movie.setType("movie");
                }
            } else {
                // Check URL for type hints
                if (movie.getDetailUrl() != null && movie.getDetailUrl().contains("/film/")) {
                    movie.setType("movie");
                } else {
                    // Default to movie
                    movie.setType("movie");
                }
            }

            // Log successful parse
            if (movie.getTitle() != null) {
                Log.d(TAG, "Parsed movie: " + movie.getTitle() + " (" + movie.getYear() + ")");
            }
            return movie;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing movie card: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Scrape detailed information for a specific movie
     * Updated to match structure: <div class="card bg-dark mb-3 mov-info">
     */
    public static Movie scrapeMovieDetails(String detailUrl) {
        Movie movie = new Movie();

        try {
            Log.d(TAG, "Scraping movie details: " + detailUrl);

            // Parse with Jsoup
            Document doc = createConnection(detailUrl).get();

            // Extract movie ID from data-mid attribute
            Element midElement = doc.selectFirst("#mid[data-mid]");
            if (midElement != null) {
                movie.setId(midElement.attr("data-mid"));

                // Extract type from data-mode attribute
                String mode = midElement.attr("data-mode");
                if (!mode.isEmpty()) {
                    movie.setType(mode); // "movie" or "tv"
                }
            }

            // Extract title from <h1 class="card-title">
            Element titleElement = doc.selectFirst("div.card-body h1.card-title, h1.card-title");
            if (titleElement != null) {
                movie.setTitle(titleElement.text().trim());
            }

            // Extract description from <p> inside fst-italic div
            Element descElement = doc.selectFirst("div.fst-italic p");
            if (descElement != null) {
                movie.setDescription(descElement.text().trim());
            }

            // Extract metadata from the structured content
            Element cardBody = doc.selectFirst("div.card-body");
            if (cardBody != null) {
                // Extract Genre
                Element genreElement = cardBody.selectFirst("p:has(strong:contains(Genre:))");
                if (genreElement != null) {
                    Elements genreLinks = genreElement.select("a");
                    StringBuilder genres = new StringBuilder();
                    for (Element link : genreLinks) {
                        if (genres.length() > 0) genres.append(", ");
                        genres.append(link.text().trim());
                    }
                    movie.setGenre(genres.toString());
                }

                // Extract Actors
                Element actorElement = cardBody.selectFirst("p:has(strong:contains(Actor:))");
                if (actorElement != null) {
                    Elements actorLinks = actorElement.select("a");
                    StringBuilder actors = new StringBuilder();
                    for (Element link : actorLinks) {
                        if (actors.length() > 0) actors.append(", ");
                        actors.append(link.text().trim());
                    }
                    movie.setActors(actors.toString());
                }

                // Extract Director
                Element directorElement = cardBody.selectFirst("p:has(strong:contains(Director:))");
                if (directorElement != null) {
                    String directorText = directorElement.text().replace("Director:", "").trim();
                    movie.setDirector(directorText);
                }

                // Extract Country
                Element countryElement = cardBody.selectFirst("p:has(strong:contains(Country:))");
                if (countryElement != null) {
                    Element countryLink = countryElement.selectFirst("a");
                    if (countryLink != null) {
                        movie.setCountry(countryLink.text().trim());
                    }
                }

                // Extract Duration
                Element durationElement = cardBody.selectFirst("p:has(strong:contains(Duration:))");
                if (durationElement != null) {
                    String durationText = durationElement.text().replace("Duration:", "").trim();
                    movie.setDuration(durationText);
                }

                // Extract Quality
                Element qualityElement = cardBody.selectFirst("p:has(strong:contains(Quality:)) span.badge");
                if (qualityElement != null) {
                    movie.setQuality(qualityElement.text().trim());
                }

                // Extract Release Year
                Element releaseElement = cardBody.selectFirst("p:has(strong:contains(Release:))");
                if (releaseElement != null) {
                    Element releaseLink = releaseElement.selectFirst("a");
                    if (releaseLink != null) {
                        movie.setYear(releaseLink.text().trim());
                    }
                }

                // Extract IMDb rating
                Element imdbElement = cardBody.selectFirst("p:has(strong:contains(IMDb:))");
                if (imdbElement != null) {
                    String ratingText = imdbElement.text().replace("IMDb:", "").trim();
                    if (!ratingText.equals("-") && !ratingText.isEmpty()) {
                        movie.setRating(ratingText);
                    }
                }
            }

            // Extract keywords from card-footer
            Element keywordsElement = doc.selectFirst("div.card-footer");
            if (keywordsElement != null) {
                String keywordsText = keywordsElement.text().replace("Keywords:", "").trim();
                if (!keywordsText.equals("-") && !keywordsText.isEmpty()) {
                    movie.setKeywords(keywordsText);
                }
            }

            // Extract poster/thumbnail image
            Element posterElement = doc.selectFirst("div.col-lg-2 img.lazy, div.col-lg-2 img");
            if (posterElement != null) {
                String posterUrl = posterElement.attr("data-src");
                if (posterUrl.isEmpty()) {
                    posterUrl = posterElement.attr("src");
                }
                movie.setThumbnailUrl(posterUrl);
            }

            // Extract backdrop/cover image
            Element coverElement = doc.selectFirst("#cover-img");
            if (coverElement != null) {
                String backdropUrl = coverElement.attr("data-src");
                if (backdropUrl.isEmpty()) {
                    backdropUrl = coverElement.attr("src");
                }
                movie.setBackdropUrl(backdropUrl);
            }

            movie.setDetailUrl(detailUrl);

            Log.d(TAG, "Scraped movie details: " + movie.getTitle() + " - " + movie.getRating() + " - " + movie.getQuality());

        } catch (IOException e) {
            Log.e(TAG, "Error scraping movie details: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        return movie;
    }


    /**
     * Extract streaming URL from movie page using the site's AJAX API
     * Based on the actual implementation from the site's JavaScript
     */
    public static String extractStreamUrl(String detailUrl, String movieId, int serverNumber) {
        String fallback="https://netusa.xyz/watch/?v21#ZERDYTBCbGh4K1NkVzh3ZHR6d25XM0dDOEhSVndHL01rTFk1UjR3MFRmNE1wOUQ4SlRNUGZENGVXdFdUZGFMZDduM3VTQjJOdm13PQ";
        try {
            Log.d(TAG, "Extracting stream URL from: " + detailUrl);

            // Base64 encoded player URL: "aHR0cHM6Ly9uZXR1c2EueHl6"
            String plyURL = "https://netusa.xyz";

            if (movieId == null || movieId.isEmpty()) {
                Document doc = createConnection(detailUrl).get();
                Element midElement = doc.selectFirst("#mid[data-mid]");
                if (midElement != null) {
                    movieId = midElement.attr("data-mid");
                    Log.d(TAG, "Extracted movie ID: " + movieId);
                } else {
                    Log.e(TAG, "Could not find movie ID");
                    return fallback;
                }
            }

            // Build the request body JSON - matches the JavaScript implementation
            // fetchMoviesJSON uses: {"mid": "movie_id", "srv": "server_number"}
            String requestBody = String.format("{\"mid\":\"%s\",\"srv\":\"%d\"}", movieId, serverNumber);

            Log.d(TAG, "Requesting stream with server " + serverNumber);
            Log.d(TAG, "Request body: " + requestBody);

            // Make POST request to the player API
            Connection.Response response = Jsoup.connect(plyURL)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Referer", detailUrl)
                    .header("Origin", "https://ww4.fmovies.co")
                    .requestBody(requestBody)
                    .timeout(10000)
                    .execute();

            String responseBody = response.body();
            Log.d(TAG, "API Response: " + responseBody);

            // Parse the JSON response to extract the stream URL
            String streamUrl = parseStreamUrlFromResponse(responseBody);

            if (streamUrl != null && !streamUrl.isEmpty()) {
                Log.d(TAG, "Successfully extracted stream URL: " + streamUrl);
                return streamUrl;
            } else {
                Log.w(TAG, "No stream URL found in response for server " + serverNumber);
                return fallback;
            }

        } catch (IOException e) {
            Log.e(TAG, "IO Error extracting stream URL: " + e.getMessage());
            e.printStackTrace();
            return fallback;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return fallback;
        }

    }

    /**
     * Parse stream URL from API response
     */
    private static String parseStreamUrlFromResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return null;
        }

        try {
            // Remove any HTML tags if present
            jsonResponse = jsonResponse.replaceAll("<[^>]*>", "").trim();

            // Common JSON patterns to look for
            String[] patterns = {
                    "\"url\"\\s*:\\s*\"([^\"]+)\"",
                    "\"link\"\\s*:\\s*\"([^\"]+)\"",
                    "\"embed\"\\s*:\\s*\"([^\"]+)\"",
                    "\"src\"\\s*:\\s*\"([^\"]+)\"",
                    "\"file\"\\s*:\\s*\"([^\"]+)\"",
                    "\"stream\"\\s*:\\s*\"([^\"]+)\"",
                    "\"sources\"\\s*:\\s*\\[\\s*\"([^\"]+)\"",
                    "\"m3u8\"\\s*:\\s*\"([^\"]+)\""
            };

            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher m = p.matcher(jsonResponse);
                if (m.find()) {
                    String url = m.group(1);
                    // Unescape JSON string
                    url = url.replace("\\/", "/")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                            .replace("\\n", "")
                            .replace("\\r", "")
                            .replace("\\t", "");

                    // Validate URL
                    if (url.startsWith("http") || url.startsWith("//")) {
                        return url;
                    }
                }
            }

            Log.w(TAG, "No stream URL pattern matched in response");

        } catch (Exception e) {
            Log.e(TAG, "Error parsing stream URL: " + e.getMessage());
        }

        return null;
    }



    /**
     * Extract stream URL with all available servers
     */
    public static String extractStreamUrl(String detailUrl, String movieId) {
        // Try servers in order: 2, 1, 5 (as shown in the HTML)
        List<ServerInfo> allservers=getAvailableServers( detailUrl);
        Log.d(TAG, "Available servers: " + allservers);
        List<ServerInfo> servers = allservers;

        for (ServerInfo server : servers) {
            Log.d(TAG, "Attempting server " + server);
            String streamUrl = extractStreamUrl(detailUrl, movieId, server.serverNumber);
            if (streamUrl != null && !streamUrl.isEmpty()) {
                Log.d(TAG, "Successfully found stream on server " + server);
                return streamUrl;
            }
        }

        Log.w(TAG, "All servers failed, no stream URL found");
        return null;
    }

    /**
     * Overload for backward compatibility
     */
    public static String extractStreamUrl(String detailUrl) {
        Log.d(TAG, "Using default extractStreamUrl method");
        String movieId = "";
        try {
            Document doc = createConnection(detailUrl).get();
            // Extract movie ID from data-mid attribute
            Element midElement = doc.selectFirst("#mid[data-mid]");
            if (midElement != null) {
                movieId = midElement.attr("data-mid");
                Log.d(TAG, "extractStreamUrl: "+movieId);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return extractStreamUrl(detailUrl, movieId);
    }

    /**
     * Get available servers from the detail page
     */
    public static List<ServerInfo> getAvailableServers(String detailUrl) {
        List<ServerInfo> servers = new ArrayList<>();

        try {
            Document doc = createConnection(detailUrl).get();

            // Get movie ID
            Element midElement = doc.selectFirst("#mid[data-mid]");
            String movieId = midElement != null ? midElement.attr("data-mid") : null;

            // Get server buttons
            Elements serverButtons = doc.select("#srv-list button.server");

            for (Element button : serverButtons) {
                String serverId = button.attr("id");
                String serverName = button.text().trim();

                if (serverId.startsWith("srv-")) {
                    try {
                        int serverNum = Integer.parseInt(serverId.replace("srv-", ""));
                        servers.add(new ServerInfo(serverNum, serverName, movieId));
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Could not parse server number from: " + serverId);
                    }
                }
            }

            Log.d(TAG, "Found " + servers.size() + " servers");

        } catch (IOException e) {
            Log.e(TAG, "Error getting available servers: " + e.getMessage());
        }

        return servers;
    }

    /**
     * Search for movies
     */
    public static List<Movie> searchMovies(String query) {
        String searchUrl = BASE_URL + "/search?keyword=" + query.replace(" ", "+");
        return scrapeMoviesFromUrl(searchUrl);
    }
    /**
     * Helper class to store server information
     */
    public static class ServerInfo {
        public int serverNumber;
        public String serverName;
        public String movieId;

        public ServerInfo(int serverNumber, String serverName, String movieId) {
            this.serverNumber = serverNumber;
            this.serverName = serverName;
            this.movieId = movieId;
        }

        @Override
        public String toString() {
            return serverName + " (Server " + serverNumber + ")";
        }
    }
}