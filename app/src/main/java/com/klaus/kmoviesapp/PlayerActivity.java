package com.klaus.kmoviesapp;

import android.app.AppOpsManager;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.klaus.kmoviesapp.scraper.FMoviesScraper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UnstableApi
public class PlayerActivity extends FragmentActivity {
    private static final String TAG = "PlayerActivity";

    private PlayerView playerView;
    private ProgressBar loadingIndicator;
    private TextView titleTextView;
    private ImageButton qualityButton;
    private ImageButton subtitleButton;
    private View errorContainer;
    private TextView errorTextView;

    private PlayerManager mplayer;
    private String movieTitle;
    private String tmdbId;
    private Map<String, String> availableQualities;
    private List<PlayerManager.SubtitleInfo> subtitles;
    private int currentSubtitleIndex = -1; // -1 means no subtitle

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Disable Picture-in-Picture
        disablePictureInPicture();

        //tmdbId = getIntent().getStringExtra("tmdb_id");
        tmdbId = "278";
        movieTitle = getIntent().getStringExtra("movie_title");

        Log.d(TAG, "Received tmdb_id: " + tmdbId);
        Log.d(TAG, "Received movie_title: " + movieTitle);

        if (tmdbId == null || tmdbId.isEmpty()) {
            Log.e(TAG, "TMDB ID is null or empty");
            Toast.makeText(this, "Invalid movie ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadStreamData();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        titleTextView = findViewById(R.id.playerTitle);
        qualityButton = findViewById(R.id.qualityButton);
        subtitleButton = findViewById(R.id.subtitleButton);
        errorContainer = findViewById(R.id.errorContainer);
        errorTextView = findViewById(R.id.errorText);

        if (movieTitle != null) {
            titleTextView.setText(movieTitle);
        }

        // Initially hide error and show loading
        errorContainer.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        qualityButton.setEnabled(false);
        subtitleButton.setEnabled(false);
    }

    private void loadStreamData() {
        // Load stream data in background thread
        new Thread(() -> {
            try {
                Log.d(TAG, "Extracting stream URL for TMDB ID: " + tmdbId);

                FMoviesScraper.StreamResult result =
                        FMoviesScraper.extractStreamUrlWithSubtitles(tmdbId);

                if (result != null && result.streamUrl != null) {
                    Log.d(TAG, "Stream extraction successful");

                    // Store available qualities
                    availableQualities = result.qualities;

                    // Convert subtitles map to list
                    subtitles = convertSubtitlesToList(result.subtitles);

                    // Extract headers from the stream URL if needed
                    Map<String, String> headers = extractHeadersFromUrl(result.streamUrl);

                    runOnUiThread(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        initializePlayer(result.streamUrl, headers, subtitles);
                    });
                } else {
                    runOnUiThread(() -> showError("Failed to extract stream URL"));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error loading stream data: " + e.getMessage(), e);
                runOnUiThread(() -> showError("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void initializePlayer(String streamUrl, Map<String, String> headers,
                                  List<PlayerManager.SubtitleInfo> subtitles) {
        try {
            mplayer = new PlayerManager(this);
            mplayer.init(this, playerView, streamUrl, headers, subtitles);

            setupQualityButton();
            setupSubtitleButton();
            qualityButton.setEnabled(true);
            subtitleButton.setEnabled(subtitles != null && !subtitles.isEmpty());

            Log.d(TAG, "Player initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing player: " + e.getMessage(), e);
            showError("Failed to initialize player: " + e.getMessage());
        }
    }

    private List<PlayerManager.SubtitleInfo> convertSubtitlesToList(Map<String, String> subtitleMap) {
        List<PlayerManager.SubtitleInfo> subtitleList = new ArrayList<>();

        if (subtitleMap != null && !subtitleMap.isEmpty()) {
            for (Map.Entry<String, String> entry : subtitleMap.entrySet()) {
                String language = entry.getKey();
                String url = entry.getValue();

                // Determine subtitle type from URL
                String type = "vtt"; // Default
                if (url.toLowerCase().endsWith(".srt")) {
                    type = "srt";
                } else if (url.toLowerCase().endsWith(".ass")) {
                    type = "ass";
                }

                subtitleList.add(new PlayerManager.SubtitleInfo(url, language, type));
                Log.d(TAG, "Added subtitle: " + language + " - " + url);
            }
        }

        return subtitleList;
    }

    /**
     * Extract headers from URL if they're encoded in the URL
     * Example: URL might have {referer: "..."} at the end
     */
    private Map<String, String> extractHeadersFromUrl(String url) {
        Map<String, String> headers = new HashMap<>();

        try {
            // Check if URL contains encoded headers (common pattern)
            if (url.contains("%7B%22referer%22") || url.contains("{\"referer\"")) {
                // Extract and decode the referer
                String referer = "https://fsharetv.co/";
                headers.put("Referer", referer);
                headers.put("Origin", referer);
                headers.put("Accept", "*/*");
                headers.put("Accept-Language", "en-US,en;q=0.9");

                Log.d(TAG, "Extracted headers from URL: " + headers.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting headers: " + e.getMessage());
        }

        return headers;
    }

    private void setupQualityButton() {
        if (availableQualities == null || availableQualities.isEmpty()) {
            qualityButton.setVisibility(View.GONE);
            return;
        }

        qualityButton.setOnClickListener(v -> showQualityDialog());
    }

    private void setupSubtitleButton() {
        if (subtitles == null || subtitles.isEmpty()) {
            subtitleButton.setVisibility(View.GONE);
            return;
        }

        subtitleButton.setOnClickListener(v -> showSubtitleDialog());
    }

    private void showSubtitleDialog() {
        if (subtitles == null || subtitles.isEmpty()) {
            Toast.makeText(this, "No subtitles available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create subtitle list with "Off" option at the beginning
        List<String> subtitleLabels = new ArrayList<>();
        subtitleLabels.add("Off");

        for (PlayerManager.SubtitleInfo subtitle : subtitles) {
            subtitleLabels.add(subtitle.language);
        }

        String[] subtitleOptions = subtitleLabels.toArray(new String[0]);

        // Current selection (+1 because "Off" is at index 0)
        int currentSelection = currentSubtitleIndex + 1;

        new AlertDialog.Builder(this)
                .setTitle("Select Subtitles")
                .setSingleChoiceItems(subtitleOptions, currentSelection, (dialog, which) -> {
                    if (which == 0) {
                        // User selected "Off"
                        disableSubtitles();
                        currentSubtitleIndex = -1;
                        Toast.makeText(this, "Subtitles disabled", Toast.LENGTH_SHORT).show();
                    } else {
                        // User selected a subtitle track (subtract 1 to get actual index)
                        int subtitleIndex = which - 1;
                        enableSubtitle(subtitleIndex);
                        currentSubtitleIndex = subtitleIndex;
                        String language = subtitles.get(subtitleIndex).language;
                        Toast.makeText(this, "Subtitles: " + language, Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void enableSubtitle(int subtitleIndex) {
        if (mplayer != null && subtitles != null && subtitleIndex >= 0 && subtitleIndex < subtitles.size()) {
            mplayer.enableSubtitle(subtitleIndex);
            Log.d(TAG, "Enabled subtitle: " + subtitles.get(subtitleIndex).language);
        }
    }

    private void disableSubtitles() {
        if (mplayer != null) {
            mplayer.disableSubtitles();
            Log.d(TAG, "Disabled all subtitles");
        }
    }

    private void showQualityDialog() {
        if (availableQualities == null || availableQualities.isEmpty()) {
            Toast.makeText(this, "No quality options available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create quality list from available qualities
        List<String> qualityLabels = new ArrayList<>();
        List<String> qualityUrls = new ArrayList<>();

        // Add qualities in order of preference
        String[] preferredOrder = {"1080p", "720p", "480p", "360p"};
        for (String quality : preferredOrder) {
            if (availableQualities.containsKey(quality)) {
                qualityLabels.add(quality);
                qualityUrls.add(availableQualities.get(quality));
            }
        }

        // Add any remaining qualities
        for (Map.Entry<String, String> entry : availableQualities.entrySet()) {
            if (!qualityLabels.contains(entry.getKey())) {
                qualityLabels.add(entry.getKey());
                qualityUrls.add(entry.getValue());
            }
        }

        String[] qualities = qualityLabels.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Select Quality")
                .setItems(qualities, (dialog, which) -> {
                    String selectedQuality = qualities[which];
                    String selectedUrl = qualityUrls.get(which);
                    switchQuality(selectedUrl, selectedQuality);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void switchQuality(String newUrl, String qualityLabel) {
        if (mplayer == null) return;

        try {
            // Get current position
            long currentPosition = mplayer.getCurrentPosition();

            // Release current player
            mplayer.release();

            // Extract headers from new URL
            Map<String, String> headers = extractHeadersFromUrl(newUrl);

            // Reinitialize player with new URL
            mplayer = new PlayerManager(this);
            mplayer.init(this, playerView, newUrl, headers, subtitles);

            // Seek to previous position
            mplayer.seekTo(currentPosition);

            Toast.makeText(this, "Quality changed to " + qualityLabel, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Quality changed to: " + qualityLabel);

        } catch (Exception e) {
            Log.e(TAG, "Error switching quality: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to switch quality", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String message) {
        loadingIndicator.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
        qualityButton.setEnabled(false);
        subtitleButton.setEnabled(false);

        Log.e(TAG, "Showing error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mplayer != null) {
            mplayer.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mplayer != null) {
            mplayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mplayer != null) {
            mplayer.release();
        }
    }

    @Override
    public void onBackPressed() {
        if (mplayer != null) {
            mplayer.stop();
            mplayer.release();
        }
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onUserLeaveHint() {
        if (mplayer != null) {
            mplayer.pause();
        }
    }

    private void disablePictureInPicture() {
        // Nothing special needed - just don't call enterPictureInPictureMode()
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (mplayer != null) {
                    mplayer.stop();
                }
            }
        }
    }
}