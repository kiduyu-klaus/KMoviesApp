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

@UnstableApi
public class PlayerActivity extends FragmentActivity {
    private static final String TAG = "PlayerActivity";

    private PlayerView playerView;
    private ProgressBar loadingIndicator;
    private TextView titleTextView;
    private ImageButton qualityButton;

    private String streamUrl;
    private PlayerManager mplayer;
    private String movieTitle;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Disable Picture-in-Picture
        disablePictureInPicture();

        streamUrl = getIntent().getStringExtra("stream_url");
        movieTitle = getIntent().getStringExtra("movie_title");

        // Debug logging
        Log.d(TAG, "Received stream_url: " + streamUrl);
        Log.d(TAG, "Received movie_title: " + movieTitle);

        if (streamUrl == null || streamUrl.isEmpty()) {
            Log.w(TAG, "Stream URL is null or empty");
            Toast.makeText(this, "Invalid stream URL", Toast.LENGTH_SHORT).show();
        }

        initializeViews();
        mplayer = new PlayerManager(this);

        // Pass the actual streamUrl value, not null
        String urlToPlay =  "http://dl6.afradl.xyz/Movies/Global/G/Glass.Onion.A.Knives.Out.Mystery.2022/SoftSub/Glass.Onion.A.Knives.Out.Mystery.2022.1080p.WEB-DL.SoftSub.BlueMoviee.com.mkv";

        mplayer.init(this, playerView, urlToPlay);

        setupQualityButton();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        titleTextView = findViewById(R.id.playerTitle);
        qualityButton = findViewById(R.id.qualityButton);

        if (movieTitle != null) {
            titleTextView.setText(movieTitle);
        }
    }

    private void setupQualityButton() {
        qualityButton.setOnClickListener(v -> showQualityDialog());
    }

    private void showQualityDialog() {
        String[] qualities = {"Auto", "1080p", "720p", "480p", "360p"};
        int currentQuality = mplayer.getCurrentQualityIndex();

        new AlertDialog.Builder(this)
                .setTitle("Select Quality")
                .setSingleChoiceItems(qualities, currentQuality, (dialog, which) -> {
                    mplayer.changeQuality(which);
                    Toast.makeText(this, "Quality changed to " + qualities[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        // Stop and release player before finishing
        if (mplayer != null) {
            mplayer.stop();
            mplayer.release();
        }
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onUserLeaveHint() {
        // This is called when user presses home or switches apps
        // Disable PiP by stopping the player
        if (mplayer != null) {
            mplayer.pause();
        }
    }

    /**
     * Disable Picture-in-Picture mode
     */
    private void disablePictureInPicture() {
        // Nothing special needed - just don't call enterPictureInPictureMode()
        // and ensure manifest doesn't have android:supportsPictureInPicture="true"
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            // If somehow PiP is triggered, exit it immediately
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Stop playback in PiP mode
                if (mplayer != null) {
                    mplayer.stop();
                }
            }
        }
    }
}