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
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.ui.PlayerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .setMaxVideoSizeSd()
                .setPreferredTextLanguage("en") // Prefer English subtitles
                .build();
        trackSelector.setParameters(parameters);

        // Main handler for the main thread
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize cache
        if (downloadCache == null) {
            downloadCache = getDownloadCache(context);
        }

        // Build cached data source factory
        cacheDataSourceFactory = buildCachedDataSourceFactory(null);
    }

    public void init(Context context, PlayerView playerView, String contentUrl) {
        init(context, playerView, contentUrl, null, null);
    }

    public void init(Context context, PlayerView playerView, String contentUrl,
                     Map<String, String> headers, List<SubtitleInfo> subtitles) {
        this.playerView = playerView;

        // Log the received URL for debugging
        Log.i(TAG, "Initializing player with URL: " + contentUrl);
        if (headers != null && !headers.isEmpty()) {
            Log.i(TAG, "Headers: " + headers.toString());
        }
        if (subtitles != null && !subtitles.isEmpty()) {
            Log.i(TAG, "Subtitles: " + subtitles.size() + " tracks");
        }

        // IMPORTANT: Always recreate player with new headers
        if (player != null) {
            contentPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }

        // Create data source factory with headers if provided
        DataSource.Factory dataSourceFactory = (headers != null && !headers.isEmpty()) ?
                buildCachedDataSourceFactory(headers) : cacheDataSourceFactory;

        // Create ExoPlayer instance with cache
        player = new ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .setBandwidthMeter(bandwidthMeter)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(context)
                        .setDataSourceFactory(dataSourceFactory))
                .build();

        // Add listener
        player.addListener(new PlayerEventListener());

        // Set play when ready
        player.setPlayWhenReady(true);

        // Bind the player to the view
        playerView.setPlayer(player);

        if (contentUrl == null || contentUrl.isEmpty()) {
            Log.e(TAG, "Content URL is null or empty!");
            return;
        }

        Log.i(TAG, "Playing URL: " + contentUrl);

        // Prepare media source based on file type
        MediaSource mediaSource = prepareMediaSource(contentUrl, headers, subtitles);
        player.setMediaSource(mediaSource);
        player.seekTo(contentPosition);
        player.prepare();
    }

    /**
     * Prepare appropriate media source based on content type
     */
    private MediaSource prepareMediaSource(String contentUrl, Map<String, String> headers,
                                           List<SubtitleInfo> subtitles) {
        Uri uri = Uri.parse(contentUrl);

        // Build MediaItem with subtitles if available
        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder().setUri(uri);

        if (subtitles != null && !subtitles.isEmpty()) {
            List<MediaItem.SubtitleConfiguration> subtitleConfigs = new ArrayList<>();

            for (SubtitleInfo subtitle : subtitles) {
                try {
                    MediaItem.SubtitleConfiguration subtitleConfig =
                            new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle.url))
                                    .setMimeType(getMimeTypeForSubtitle(subtitle.type))
                                    .setLanguage(subtitle.language)
                                    .setLabel(subtitle.language)
                                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                                    .build();
                    subtitleConfigs.add(subtitleConfig);
                    Log.d(TAG, "Added subtitle: " + subtitle.language);
                } catch (Exception e) {
                    Log.e(TAG, "Error adding subtitle: " + subtitle.language, e);
                }
            }

            if (!subtitleConfigs.isEmpty()) {
                mediaItemBuilder.setSubtitleConfigurations(subtitleConfigs);
            }
        }

        MediaItem mediaItem = mediaItemBuilder.build();

        // Create data source factory with headers if provided
        DataSource.Factory dataSourceFactory = (headers != null && !headers.isEmpty()) ?
                buildCachedDataSourceFactory(headers) : cacheDataSourceFactory;

        if (isHlsFile(contentUrl)) {
            Log.i(TAG, "Using HlsMediaSource for HLS stream");
            return new HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(mediaItem);
        } else if (isMkvFile(contentUrl)) {
            Log.i(TAG, "Using ProgressiveMediaSource for MKV file");
            return new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem);
        } else {
            Log.i(TAG, "Using ProgressiveMediaSource for other formats");
            return new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem);
        }
    }

    /**
     * Check if the URL points to an HLS file
     */
    private boolean isHlsFile(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        String lowerUrl = url.toLowerCase();
        return lowerUrl.contains(".m3u8") || lowerUrl.contains("master.m3u8") ||
                lowerUrl.contains("/hls/");
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
     * Get MIME type for subtitle format
     */
    private String getMimeTypeForSubtitle(String type) {
        if (type == null) {
            return "text/vtt"; // Default
        }

        switch (type.toLowerCase()) {
            case "srt":
                return "application/x-subrip";
            case "vtt":
                return "text/vtt";
            case "ass":
            case "ssa":
                return "text/x-ssa";
            default:
                return "text/vtt"; // Default to VTT
        }
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

    /**
     * Get current playback position
     */
    public long getCurrentPosition() {
        if (player != null) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    /**
     * Seek to specific position
     */
    public void seekTo(long positionMs) {
        if (player != null) {
            player.seekTo(positionMs);
        }
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
     * Build cached data source factory with optional headers
     */
    private DataSource.Factory buildCachedDataSourceFactory(Map<String, String> headers) {
        DefaultHttpDataSource.Factory httpDataSourceFactory =
                new DefaultHttpDataSource.Factory()
                        .setUserAgent(userAgent)
                        .setTransferListener(bandwidthMeter)
                        .setAllowCrossProtocolRedirects(true);

        // Add custom headers if provided
        if (headers != null && !headers.isEmpty()) {
            httpDataSourceFactory.setDefaultRequestProperties(headers);
            Log.i(TAG, "Added headers to data source: " + headers.toString());
        }

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

    /**
     * Subtitle information class
     */
    public static class SubtitleInfo {
        public String url;
        public String language;
        public String type;

        public SubtitleInfo(String url, String language, String type) {
            this.url = url;
            this.language = language;
            this.type = type;
        }
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
                // Log available subtitle tracks
                if (trackGroup.getType() == C.TRACK_TYPE_TEXT) {
                    for (int i = 0; i < trackGroup.length; i++) {
                        if (trackGroup.isTrackSupported(i)) {
                            String language = trackGroup.getTrackFormat(i).language;
                            Log.i(TAG, "Subtitle track available: " + (language != null ? language : "unknown"));
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
            if (error.getCause() != null) {
                Log.e(TAG, "Error cause: " + error.getCause().getMessage());
            }
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