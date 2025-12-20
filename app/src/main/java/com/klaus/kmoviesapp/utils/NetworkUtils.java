package com.klaus.kmoviesapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    
    /**
     * Check if device has internet connection
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        
        return false;
    }

    /**
     * Get user agent string for web requests
     */
    public static String getUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
               "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    }

    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        return url.startsWith("http://") || 
               url.startsWith("https://") ||
               url.startsWith("rtmp://") ||
               url.startsWith("rtsp://");
    }

    /**
     * Extract domain from URL
     */
    public static String extractDomain(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        try {
            String domain = url;
            if (domain.contains("://")) {
                domain = domain.split("://")[1];
            }
            if (domain.contains("/")) {
                domain = domain.split("/")[0];
            }
            return domain;
        } catch (Exception e) {
            return "";
        }
    }
}
