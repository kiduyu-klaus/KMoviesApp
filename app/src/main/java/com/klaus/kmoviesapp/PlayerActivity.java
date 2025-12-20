package com.klaus.kmoviesapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.PlayerView;

public class PlayerActivity extends FragmentActivity {
    private static final String TAG = "PlayerActivity";

    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar loadingIndicator;
    private TextView titleTextView;

    private String streamUrl;
    private String movieTitle;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        streamUrl = getIntent().getStringExtra("stream_url");
        movieTitle = getIntent().getStringExtra("movie_title");

        if (streamUrl == null || streamUrl.isEmpty()) {
            Toast.makeText(this, "Invalid stream URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        initializePlayer();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        titleTextView = findViewById(R.id.playerTitle);

        if (movieTitle != null) {
            titleTextView.setText(movieTitle);
        }

        // Hide system UI for immersive experience
        hideSystemUI();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        try {
            // Create ExoPlayer instance
            player = new ExoPlayer.Builder(this)
                    .setMediaSourceFactory(new DefaultMediaSourceFactory(this))
                    .build();

            // Bind player to the view
            playerView.setPlayer(player);

            // Configure player
            player.setPlayWhenReady(true);
            playerView.setControllerAutoShow(true);
            playerView.setControllerHideOnTouch(true);

            // Add listener for player events
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    switch (playbackState) {
                        case Player.STATE_BUFFERING:
                            loadingIndicator.setVisibility(View.VISIBLE);
                            Log.d(TAG, "Buffering...");
                            break;
                        case Player.STATE_READY:
                            loadingIndicator.setVisibility(View.GONE);
                            Log.d(TAG, "Ready to play");
                            break;
                        case Player.STATE_ENDED:
                            Log.d(TAG, "Playback ended");
                            finish();
                            break;
                        case Player.STATE_IDLE:
                            Log.d(TAG, "Player idle");
                            break;
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    loadingIndicator.setVisibility(View.GONE);
                    Log.e(TAG, "Player error: " + error.getMessage());
                    Toast.makeText(PlayerActivity.this,
                            "Playback error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    if (isPlaying) {
                        titleTextView.setVisibility(View.GONE);
                    }
                }
            });

            // Create media item
            MediaItem mediaItem = MediaItem.fromUri(streamUrl);
            
            // Set media item and prepare
            player.setMediaItem(mediaItem);
            player.prepare();

            Log.d(TAG, "Player initialized with URL: " + streamUrl);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing player: " + e.getMessage());
            Toast.makeText(this, "Error initializing player", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            Log.d(TAG, "Player released");
        }
    }

    @Override
    public void onBackPressed() {
        releasePlayer();
        super.onBackPressed();
    }
}
