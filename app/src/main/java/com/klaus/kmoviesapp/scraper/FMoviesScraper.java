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
    private static final String BASE_URL2 = "https://ww4.fmovies.co";
    private static final String BASE_URL = "https://popcornmovies.org";
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
        return scrapeMoviesFromUrl(BASE_URL + "/tv-series");
    }

    /**
     * Scrape Top IMDb page
     */
    public static List<Movie> scrapeTopIMDb() {
        return scrapeMoviesFromUrl(BASE_URL + "/top-imdb");
    }
    private static List<Movie> scrapeMoviesFromUrl(String url) {
        List<Movie> movies = new ArrayList<>();

        try {
            Log.d(TAG, "Scraping URL: " + url);
            Document doc = createConnection(url).get();

            // Find movie cards in grid - updated selector for new structure
            Elements movieCards = doc.select("div.relative.group.overflow-hidden");

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
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        return movies;
    }

    private static Movie parseMovieCard(Element card) {
        Movie movie = new Movie();

        try {
            // Extract link and detail URL
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
                    for (int i = urlParts.length - 1; i >= 0; i--) {
                        if (!urlParts[i].isEmpty()) {
                            movie.setId(urlParts[i]);
                            break;
                        }
                    }
                }

                // Determine type from URL
                if (detailUrl.contains("/tv-show/")) {
                    movie.setType("tv");
                } else if (detailUrl.contains("/movie/")) {
                    movie.setType("movie");
                }
            }

            // Extract thumbnail
            Element imgElement = card.selectFirst("picture img");
            if (imgElement != null) {
                String thumbnail = imgElement.attr("data-src");
                if (thumbnail.isEmpty()) {
                    thumbnail = imgElement.attr("src");
                }
                movie.setThumbnailUrl(thumbnail);

                // Extract title from alt
                String altTitle = imgElement.attr("alt");
                if (!altTitle.isEmpty()) {
                    movie.setTitle(altTitle.trim());
                }
            }

            // Extract quality badge
            Element qualityElement = card.selectFirst("span.bg-primary-500");
            if (qualityElement != null) {
                movie.setQuality(qualityElement.text().trim());
            }

            // Extract rating from the circular progress indicator
            Element ratingElement = card.selectFirst("div.absolute.right-3.top-3 span.text-xs");
            if (ratingElement != null) {
                String rating = ratingElement.text().trim();
                if (!rating.equals("0.0")) {
                    movie.setRating(rating);
                }
            }

            // Extract title from h3 (bottom hover section)
            Element titleElement = card.selectFirst("h3.text-sm");
            if (titleElement != null) {
                String title = titleElement.text().trim();
                if (!title.isEmpty()) {
                    movie.setTitle(title);
                }
            }

            // Extract duration and year
            Elements metaSpans = card.select("div.text-xs.text-white\\/50 span");
            if (metaSpans.size() >= 2) {
                movie.setDuration(metaSpans.get(0).text().trim());
                movie.setYear(metaSpans.get(1).text().trim());
            } else if (metaSpans.size() == 1) {
                // Could be either duration or year
                String text = metaSpans.get(0).text().trim();
                if (text.matches("\\d{4}")) {
                    movie.setYear(text);
                } else {
                    movie.setDuration(text);
                }
            }

            // Extract genre
            Element genreElement = card.selectFirst("div.text-xs.text-white\\/50.gap-x-3.mt-1 span");
            if (genreElement != null) {
                movie.setGenre(genreElement.text().trim());
            }

            if (movie.getTitle() != null) {
                Log.d(TAG, "Parsed: " + movie.getTitle() + " (" + movie.getYear() + ")");
            }

            return movie;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing card: " + e.getMessage());
            return null;
        }
    }

    public static List<Movie> scrapeTopThisWeek(String url) {
        List<Movie> movies = new ArrayList<>();

        try {
            Log.d(TAG, "Scraping Top This Week: " + url);
            Document doc = createConnection(url).get();

            // Find the "Top this week" section
            Elements topWeekItems = doc.select("div.rounded-xl div.flex.space-x-8");

            Log.d(TAG, "Found " + topWeekItems.size() + " top week items");

            for (Element item : topWeekItems) {
                try {
                    Movie movie = parseTopWeekItem(item);
                    if (movie != null && movie.getTitle() != null) {
                        movies.add(movie);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing top week item: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Error scraping top week: " + e.getMessage());
        }

        return movies;
    }

    private static Movie parseTopWeekItem(Element item) {
        Movie movie = new Movie();

        try {
            // Extract link and URL
            Element linkElement = item.selectFirst("a[href]");
            if (linkElement != null) {
                String detailUrl = linkElement.attr("href");
                if (!detailUrl.startsWith("http")) {
                    detailUrl = BASE_URL + detailUrl;
                }
                movie.setDetailUrl(detailUrl);

                // Extract ID
                String[] urlParts = detailUrl.split("/");
                if (urlParts.length > 0) {
                    for (int i = urlParts.length - 1; i >= 0; i--) {
                        if (!urlParts[i].isEmpty()) {
                            movie.setId(urlParts[i]);
                            break;
                        }
                    }
                }

                // Determine type
                if (detailUrl.contains("/tv-show/")) {
                    movie.setType("tv");
                } else {
                    movie.setType("movie");
                }
            }

            // Extract thumbnail
            Element imgElement = item.selectFirst("img");
            if (imgElement != null) {
                String thumbnail = imgElement.attr("src");
                movie.setThumbnailUrl(thumbnail);
            }

            // Extract title
            Element titleElement = item.selectFirst("h3.text-sm");
            if (titleElement != null) {
                movie.setTitle(titleElement.text().trim());
            }

            // Extract year
            Element yearElement = item.selectFirst("div.text-xs span");
            if (yearElement != null) {
                movie.setYear(yearElement.text().trim());
            }

            // Extract genre
            Element genreElement = item.selectFirst("div.text-xs.text-white\\/50.space-x-3.mt-2 span");
            if (genreElement != null) {
                movie.setGenre(genreElement.text().trim());
            }

            // Extract rating
            Element ratingElement = item.selectFirst("div.relative.flex span.text-xs");
            if (ratingElement != null) {
                String rating = ratingElement.text().trim();
                if (!rating.equals("0.0")) {
                    movie.setRating(rating);
                }
            }

            return movie;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing top week item: " + e.getMessage());
            return null;
        }
    }

    /**
     * Scrape detailed information for a specific movie from popcornmovies.org
     * Updated to match the new HTML structure
     */
    public static Movie scrapeMovieDetails(String detailUrl) {
        Movie movie = new Movie();

        try {
            Log.d(TAG, "Scraping details: " + detailUrl);
            Document doc = createConnection(detailUrl).get();

            // Extract TMDB ID from the video sources section
            Element videoSection = doc.selectFirst("div[wire\\:snapshot]");
            if (videoSection != null) {
                String wireSnapshot = videoSection.attr("wire:snapshot");
                if (wireSnapshot.contains("tmdbId")) {
                    Pattern tmdbPattern = Pattern.compile("\"tmdbId\"\\s*:\\s*\"(\\d+)\"");
                    Matcher matcher = tmdbPattern.matcher(wireSnapshot);
                    if (matcher.find()) {
                        movie.setId(matcher.group(1));
                    }
                }
            }

            // Extract title from h1 or h2
            Element titleH1 = doc.selectFirst("h1.text-3xl");
            Element titleH2 = doc.selectFirst("h2.text-lg");
            Element titleH3 = doc.selectFirst("h3.text-xl");

            if (titleH1 != null) {
                movie.setTitle(titleH1.text().trim());
            } else if (titleH3 != null) {
                movie.setTitle(titleH3.text().trim());
            } else if (titleH2 != null) {
                movie.setTitle(titleH2.text().trim());
            }

            // Extract quality badge
            Element qualityElement = doc.selectFirst("span.bg-gray-500\\/50");
            if (qualityElement != null) {
                movie.setQuality(qualityElement.text().trim());
            }

            // Extract duration, year, and other metadata from the info section
            Elements infoSpans = doc.select("div.flex.items-center.text-gray-400 span");
            for (Element span : infoSpans) {
                String text = span.text().trim();

                // Check if it's a year (4 digits)
                if (text.matches("\\d{4}")) {
                    movie.setYear(text);
                }
                // Check if it's duration (contains "min")
                else if (text.contains("min")) {
                    movie.setDuration(text);
                }
                // Check if it's quality (HD, 4K, etc.)
                else if (text.matches("(HD|4K|CAM|TS)")) {
                    movie.setQuality(text);
                }
            }

            // Extract rating from circular progress
            Element ratingElement = doc.selectFirst("div.flex.relative span.text-xs");
            if (ratingElement != null) {
                String rating = ratingElement.text().trim();
                if (!rating.equals("0.0") && !rating.isEmpty()) {
                    movie.setRating(rating);
                }
            }

            // Extract description/plot
            Element descElement = doc.selectFirst("p.text-gray-400.mt-3");
            if (descElement != null) {
                movie.setDescription(descElement.text().trim());
            }

            // Extract metadata from the structured section
            Elements metadataRows = doc.select("div.my-6.space-y-2 > div.grid");

            for (Element row : metadataRows) {
                Element label = row.selectFirst("div.text-gray-500");
                Element value = row.selectFirst("div.font-medium");

                if (label != null && value != null) {
                    String labelText = label.text().trim().toLowerCase();

                    switch (labelText) {
                        case "country":
                            Element countryLink = value.selectFirst("a");
                            if (countryLink != null) {
                                movie.setCountry(countryLink.text().trim());
                            }
                            break;

                        case "genre":
                            Elements genreLinks = value.select("a");
                            StringBuilder genres = new StringBuilder();
                            for (Element link : genreLinks) {
                                if (genres.length() > 0) genres.append(", ");
                                genres.append(link.text().trim());
                            }
                            if (genres.length() > 0) {
                                movie.setGenre(genres.toString());
                            }
                            break;

                        case "released":
                            String releaseDate = value.text().trim();
                            // Extract year from release date (e.g., "24 Dec, 2025" -> "2025")
                            Pattern yearPattern = Pattern.compile("\\d{4}");
                            Matcher yearMatcher = yearPattern.matcher(releaseDate);
                            if (yearMatcher.find()) {
                                movie.setYear(yearMatcher.group());
                            }
                            break;

                        case "cast":
                            Elements castLinks = value.select("a");
                            StringBuilder actors = new StringBuilder();
                            for (Element link : castLinks) {
                                if (actors.length() > 0) actors.append(", ");
                                actors.append(link.text().trim());
                            }
                            if (actors.length() > 0) {
                                movie.setActors(actors.toString());
                            }
                            break;
                    }
                }
            }

            // Extract keywords/tags
            Elements tagLinks = doc.select("div.flex.flex-wrap.gap-2 a");
            StringBuilder keywords = new StringBuilder();
            for (Element tag : tagLinks) {
                if (keywords.length() > 0) keywords.append(", ");
                keywords.append(tag.text().trim());
            }
            if (keywords.length() > 0) {
                movie.setKeywords(keywords.toString());
            }

            // Extract poster/thumbnail
            Element posterElement = doc.selectFirst("div.aspect-\\[2\\/3\\] img");
            if (posterElement != null) {
                String posterUrl = posterElement.attr("src");
                if (posterUrl.isEmpty()) {
                    posterUrl = posterElement.attr("data-src");
                }
                movie.setThumbnailUrl(posterUrl);
            }

            // Extract backdrop/cover from wire:snapshot data
            if (videoSection != null) {
                String wireSnapshot = videoSection.attr("wire:snapshot");
                if (wireSnapshot.contains("cover")) {
                    Pattern coverPattern = Pattern.compile("\"cover\"\\s*:\\s*\"([^\"]+)\"");
                    Matcher matcher = coverPattern.matcher(wireSnapshot);
                    if (matcher.find()) {
                        String coverUrl = matcher.group(1).replace("\\/", "/");
                        movie.setBackdropUrl(coverUrl);
                    }
                }
            }

            // Extract stream sources
            List<Movie.StreamSource> sources = extractStreamSources(doc);
            movie.setStreamSources(sources);
            if (!sources.isEmpty()) {
                movie.setStreamUrl(sources.get(0).getUrl());
            }

            movie.setDetailUrl(detailUrl);

            if (movie.getType() == null || movie.getType().isEmpty()) {
                if (detailUrl.contains("/tv-show/")) {
                    movie.setType("tv");
                } else if (detailUrl.contains("/movie/")) {
                    movie.setType("movie");
                }
            }

            Log.d(TAG, "Scraped: " + movie.getTitle() + " - " + sources.size() + " sources");



        } catch (IOException e) {
            Log.e(TAG, "Error scraping details: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        return movie;
    }

    /**
     * Extract all stream sources from detail page
     */
    private static List<Movie.StreamSource> extractStreamSources(Document doc) {
        List<Movie.StreamSource> sources = new ArrayList<>();

        try {
            Elements serverButtons = doc.select("ul.inline-flex button");

            int index = 0;
            for (Element button : serverButtons) {
                Element nameSpan = button.selectFirst("span");
                String serverName = nameSpan != null ? nameSpan.text().trim() : "Server " + index;

                String onClick = button.attr("x-on:click");
                if (!onClick.isEmpty()) {
                    Pattern urlPattern = Pattern.compile("play\\('([^']+)'");
                    Matcher matcher = urlPattern.matcher(onClick);
                    if (matcher.find()) {
                        String url = matcher.group(1);
                        sources.add(new Movie.StreamSource(serverName, url, index));
                    }
                }

                index++;
            }

            Log.d(TAG, "Found " + sources.size() + " stream sources");

        } catch (Exception e) {
            Log.e(TAG, "Error extracting stream sources: " + e.getMessage());
        }

        return sources;
    }

    public static String extractStreamUrl(String detailUrl) {
        try {
            Log.d(TAG, "Extracting stream URL from: " + detailUrl);

            Document doc = createConnection(detailUrl).get();
            List<Movie.StreamSource> sources = extractStreamSources(doc);

            if (!sources.isEmpty()) {
                String streamUrl = sources.get(0).getUrl();
                Log.d(TAG, "Found stream URL: " + streamUrl);
                return streamUrl;
            } else {
                Log.w(TAG, "No stream sources found");
            }

        } catch (IOException e) {
            Log.e(TAG, "Error extracting stream URL: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

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