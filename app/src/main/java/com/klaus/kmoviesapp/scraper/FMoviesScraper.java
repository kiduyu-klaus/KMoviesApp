package com.klaus.kmoviesapp.scraper;

import android.util.Log;

import com.klaus.kmoviesapp.models.Movie;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
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
                java.util.regex.Pattern yearPattern = java.util.regex.Pattern.compile("-(\\d{4})-|/(\\d{4})/");
                java.util.regex.Matcher matcher = yearPattern.matcher(url);
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
     * Updated to match structure: <section id="nxplayer-detail">
     */
    public static Movie scrapeMovieDetails(String detailUrl) {
        Movie movie = new Movie();

        try {
            Log.d(TAG, "Scraping movie details: " + detailUrl);

            // Fetch HTML using OkHttp
            //String html = fetchHtml(detailUrl);

            // Parse with Jsoup
            Document doc = createConnection(detailUrl).get();

            // Extract title from <h1 itemprop="name"> inside <section id="nxplayer-detail">
            Element titleElement = doc.selectFirst("section#nxplayer-detail h1[itemprop=name], section#nxplayer-detail h1");
            if (titleElement != null) {
                movie.setTitle(titleElement.text().trim());
            }

            // Extract description from <p> inside the body div
            Element descElement = doc.selectFirst("section#nxplayer-detail div.body p");
            if (descElement != null) {
                movie.setDescription(descElement.text().trim());
            }

            // Extract rating, quality, and duration from <div class="status">
            Element statusDiv = doc.selectFirst("section#nxplayer-detail div.status");
            if (statusDiv != null) {
                // Extract rating from <span class="imdb">
                Element ratingElement = statusDiv.selectFirst("span.imdb");
                if (ratingElement != null) {
                    String ratingText = ratingElement.text().trim();
                    // Remove star icon if present
                    ratingText = ratingText.replace("★", "").trim();
                    movie.setRating(ratingText);
                }

                // Extract quality from <span class="quality">
                Element qualityElement = statusDiv.selectFirst("span.quality");
                if (qualityElement != null) {
                    movie.setQuality(qualityElement.text().trim());
                }

                // Extract duration from remaining span (e.g., "129 min")
                Elements spans = statusDiv.select("span");
                for (Element span : spans) {
                    String text = span.text().trim();
                    if (text.contains("min") && !text.contains("imdb") && !span.hasClass("quality")) {
                        movie.setDuration(text);
                        break;
                    }
                }
            }

            // Extract metadata from <div class="meta">
            Element metaDiv = doc.selectFirst("section#nxplayer-detail div.meta");
            if (metaDiv != null) {
                Elements metaRows = metaDiv.select("div");

                for (Element row : metaRows) {
                    Element labelElem = row.selectFirst("div");
                    Element valueElem = row.selectFirst("span");

                    if (labelElem != null && valueElem != null) {
                        String label = labelElem.text().toLowerCase().trim().replace(":", "");
                        String value = valueElem.text().trim();

                        if (label.contains("country")) {
                            movie.setCountry(value);
                        } else if (label.contains("genre")) {
                            movie.setGenre(value);
                        } else if (label.contains("director")) {
                            movie.setDirector(value);
                        } else if (label.contains("release") || label.equals("release")) {
                            movie.setYear(value);
                        } else if (label.contains("type")) {
                            // Set movie type (Movies or TV-Shows)
                            if (value.toLowerCase().contains("tv")) {
                                movie.setType("tv");
                            } else {
                                movie.setType("movie");
                            }
                        }
                    }
                }
            }

            // Extract backdrop/poster image from <div class="poster"> or wallpaper
            Element posterElement = doc.selectFirst("section#nxplayer-detail div.poster img[itemprop=image]");
            if (posterElement != null) {
                String posterUrl = posterElement.attr("src");
                if (posterUrl.isEmpty()) {
                    posterUrl = posterElement.attr("data-src");
                }
                movie.setBackdropUrl(posterUrl);

                // Also set as thumbnail if not already set
                if (movie.getThumbnailUrl() == null || movie.getThumbnailUrl().isEmpty()) {
                    movie.setThumbnailUrl(posterUrl);
                }
            } else {
                // Try getting from wallpaper background
                Element wallpaperElement = doc.selectFirst("div.wallpaper-bg");
                if (wallpaperElement != null) {
                    String style = wallpaperElement.attr("style");
                    if (style.contains("background-image:url(")) {
                        String backdropUrl = style.substring(
                                style.indexOf("url(") + 4,
                                style.indexOf(")")
                        );
                        movie.setBackdropUrl(backdropUrl);
                    }
                }
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
     * Extract streaming URL from movie page
     */
    public static String extractStreamUrl(String detailUrl) {
        try {
            Log.d(TAG, "Extracting stream URL from: " + detailUrl);
            Document doc = createConnection(detailUrl).get();

            // Look for iframe sources
            Elements iframes = doc.select("iframe[src]");
            for (Element iframe : iframes) {
                String src = iframe.attr("src");
                if (src.contains("embed") || src.contains("player") || src.contains("stream")) {
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
            Elements dataElements = doc.select("[data-src], [data-url], [data-video]");
            for (Element elem : dataElements) {
                String dataSrc = elem.attr("data-src");
                if (dataSrc.isEmpty()) {
                    dataSrc = elem.attr("data-url");
                }
                if (dataSrc.isEmpty()) {
                    dataSrc = elem.attr("data-video");
                }
                if (dataSrc.contains(".m3u8") || dataSrc.contains(".mp4")) {
                    Log.d(TAG, "Found stream URL in data attribute: " + dataSrc);
                    return dataSrc;
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Error extracting stream URL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
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