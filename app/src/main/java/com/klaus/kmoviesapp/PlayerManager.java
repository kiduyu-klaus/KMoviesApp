package com.klaus.kmoviesapp;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.database.DatabaseProvider;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.ui.PlayerView;

import java.io.File;

@UnstableApi
public class PlayerManager {
    private static final String TAG = "PlayerManager";
    private static final long CACHE_SIZE = 100 * 1024 * 1024; // 100 MB

    private final DataSource.Factory cacheDataSourceFactory;
    private ExoPlayer player;
    private long contentPosition;
    private Handler mainHandler;
    private DefaultTrackSelector trackSelector;
    private Context mContext;
    private final DefaultBandwidthMeter bandwidthMeter;
    protected String userAgent;
    private PlayerView playerView;
    private int currentQualityIndex = 0; // 0 = Auto
    private static Cache downloadCache;

    public PlayerManager(Context context) {
        mContext = context;
        userAgent = Util.getUserAgent(mContext, context.getString(R.string.app_name));

        // Initialize bandwidth meter
        bandwidthMeter = new DefaultBandwidthMeter.Builder(mContext).build();

        // Initialize track selector with default parameters
        trackSelector = new DefaultTrackSelector(mContext);

        // Configure track selector for adaptive streaming
        TrackSelectionParameters parameters = trackSelector.getParameters()
                .buildUpon()
                .setMaxVideoSizeSd() // Start with SD quality, can be changed
                .build();
        trackSelector.setParameters(parameters);

        // Main handler for the main thread
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize cache
        if (downloadCache == null) {
            downloadCache = getDownloadCache(context);
        }

        // Build cached data source factory
        cacheDataSourceFactory = buildCachedDataSourceFactory();
    }

    public void init(Context context, PlayerView playerView, String contentUrl) {
        this.playerView = playerView;

        // Log the received URL for debugging
        Log.i(TAG, "Initializing player with URL: " + contentUrl);

        boolean needNewPlayer = player == null;
        if (needNewPlayer) {
            // Create ExoPlayer instance with cache
            player = new ExoPlayer.Builder(context)
                    .setTrackSelector(trackSelector)
                    .setBandwidthMeter(bandwidthMeter)
                    .setMediaSourceFactory(new DefaultMediaSourceFactory(context)
                            .setDataSourceFactory(cacheDataSourceFactory))
                    .build();

            // Add listener
            player.addListener(new PlayerEventListener());

            // Set play when ready
            player.setPlayWhenReady(true);

            // Bind the player to the view
            playerView.setPlayer(player);
        }

        // Use provided URL or fallback to hardcoded one
        if (contentUrl == null || contentUrl.isEmpty()) {
            Log.w(TAG, "No URL provided, using default MKV URL");
            contentUrl = "https://test-videos.co.uk/vids/jellyfish/mkv/1080/Jellyfish_1080_10s_1MB.mkv";
        }

        // Validate URL format
//        if (!contentUrl.startsWith("http://") && !contentUrl.startsWith("https://") && !contentUrl.startsWith("file://")) {
//            Log.e(TAG, "Invalid URL format: " + contentUrl);
//            // Try to fix common issues
//            if (!contentUrl.contains("://")) {
//                Log.e(TAG, "URL missing protocol, cannot play");
//                return;
//            }
//        }

        Log.i(TAG, "Playing URL: " + contentUrl);

        // Check if URL is MKV file
        if (isMkvFile(contentUrl)) {
            // Use ProgressiveMediaSource for MKV files
            prepareMkvSource(contentUrl);
        } else {
            // Use default MediaItem for other formats
            Log.i(TAG, "Using standard MediaItem for non-MKV file");
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(contentUrl));
            player.setMediaItem(mediaItem);
        }

        player.seekTo(contentPosition);
        player.prepare();
    }

    /**
     * Check if the URL points to an MKV file
     */
    private boolean isMkvFile(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.toLowerCase().endsWith(".mkv");
    }

    /**
     * Prepare MKV source using ProgressiveMediaSource
     */
    private void prepareMkvSource(String contentUrl) {
        if (contentUrl == null || contentUrl.isEmpty()) {
            Log.e(TAG, "Cannot prepare MKV source: URL is null or empty");
            return;
        }

        Log.i(TAG, "Preparing MKV source for URL: " + contentUrl);

        // Create ProgressiveMediaSource for MKV
        androidx.media3.exoplayer.source.ProgressiveMediaSource mediaSource =
                new androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(contentUrl)));

        player.setMediaSource(mediaSource);
        Log.i(TAG, "Using ProgressiveMediaSource for MKV file");
    }

    /**
     * Change video quality
     * @param qualityIndex 0=Auto, 1=1080p, 2=720p, 3=480p, 4=360p
     */
    public void changeQuality(int qualityIndex) {
        if (player == null) return;

        currentQualityIndex = qualityIndex;
        long currentPosition = player.getCurrentPosition();

        TrackSelectionParameters.Builder parametersBuilder = trackSelector.getParameters().buildUpon();

        switch (qualityIndex) {
            case 0: // Auto
                parametersBuilder
                        .clearVideoSizeConstraints()
                        .clearViewportSizeConstraints();
                break;
            case 1: // 1080p
                parametersBuilder
                        .setMaxVideoSize(1920, 1080)
                        .setMinVideoSize(1920, 1080);
                break;
            case 2: // 720p
                parametersBuilder
                        .setMaxVideoSize(1280, 720)
                        .setMinVideoSize(1280, 720);
                break;
            case 3: // 480p
                parametersBuilder
                        .setMaxVideoSize(854, 480)
                        .setMinVideoSize(854, 480);
                break;
            case 4: // 360p
                parametersBuilder
                        .setMaxVideoSize(640, 360)
                        .setMinVideoSize(640, 360);
                break;
        }

        trackSelector.setParameters(parametersBuilder.build());

        // Seek to maintain position after quality change
        player.seekTo(currentPosition);

        Log.i(TAG, "Quality changed to index: " + qualityIndex);
    }

    public int getCurrentQualityIndex() {
        return currentQualityIndex;
    }

    public void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    public void resume() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    public void stop() {
        if (player != null) {
            player.stop();
            contentPosition = 0;
        }
    }

    public void reset() {
        if (player != null) {
            contentPosition = player.getContentPosition();
            player.release();
            player = null;
        }
    }

    public void release() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    /**
     * Get or create download cache
     */
    private static Cache getDownloadCache(Context context) {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(context.getCacheDir(), "media");
            DatabaseProvider databaseProvider = new StandaloneDatabaseProvider(context);
            downloadCache = new SimpleCache(
                    downloadContentDirectory,
                    new NoOpCacheEvictor(),
                    databaseProvider
            );
        }
        return downloadCache;
    }

    /**
     * Build cached data source factory
     */
    private DataSource.Factory buildCachedDataSourceFactory() {
        DefaultHttpDataSource.Factory httpDataSourceFactory =
                new DefaultHttpDataSource.Factory()
                        .setUserAgent(userAgent)
                        .setTransferListener(bandwidthMeter);

        DataSource.Factory upstreamFactory = new DefaultDataSource.Factory(
                mContext,
                httpDataSourceFactory
        );

        return new CacheDataSource.Factory()
                .setCache(downloadCache)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setCacheWriteDataSinkFactory(null) // Write to cache
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
    }

    /**
     * Clear cache
     */
    public static void clearCache(Context context) {
        try {
            if (downloadCache != null) {
                downloadCache.release();
                downloadCache = null;
            }
            File cacheDir = new File(context.getCacheDir(), "media");
            deleteRecursive(cacheDir);
            Log.i(TAG, "Cache cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cache", e);
        }
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    private class PlayerEventListener implements Player.Listener {

        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
            Log.i(TAG, "onTimelineChanged");
        }

        @Override
        public void onTracksChanged(Tracks tracks) {
            Log.i(TAG, "onTracksChanged");
            // Log available video tracks
            for (Tracks.Group trackGroup : tracks.getGroups()) {
                if (trackGroup.getType() == C.TRACK_TYPE_VIDEO) {
                    for (int i = 0; i < trackGroup.length; i++) {
                        if (trackGroup.isTrackSupported(i)) {
                            Log.i(TAG, "Video track available: " + trackGroup.getTrackFormat(i).height + "p");
                        }
                    }
                }
            }
        }

        @Override
        public void onIsLoadingChanged(boolean isLoading) {
            Log.i(TAG, "onIsLoadingChanged :: " + isLoading);
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            switch (playbackState) {
                case Player.STATE_IDLE:
                    Log.i(TAG, "STATE_IDLE");
                    break;
                case Player.STATE_BUFFERING:
                    Log.i(TAG, "STATE_BUFFERING");
                    break;
                case Player.STATE_READY:
                    Log.i(TAG, "STATE_READY");
                    break;
                case Player.STATE_ENDED:
                    Log.i(TAG, "STATE_ENDED");
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Log.i(TAG, "onRepeatModeChanged");
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Log.i(TAG, "onShuffleModeEnabledChanged");
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Log.e(TAG, "onPlayerError: " + error.getMessage(), error);
        }

        @Override
        public void onPositionDiscontinuity(Player.PositionInfo oldPosition,
                                            Player.PositionInfo newPosition,
                                            int reason) {
            Log.i(TAG, "onPositionDiscontinuity");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.i(TAG, "onPlaybackParametersChanged");
        }
    }
}