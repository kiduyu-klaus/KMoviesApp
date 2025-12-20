package com.klaus.kmoviesapp.scraper;

import android.os.AsyncTask;
import android.util.Log;

public class StreamUrlTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "StreamUrlTask";

    public interface StreamUrlCallback {
        void onStreamUrlExtracted(String streamUrl);
        void onStreamUrlError(String error);
    }

    private final StreamUrlCallback callback;

    public StreamUrlTask(StreamUrlCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }

        String detailUrl = params[0];
        try {
            return FMoviesScraper.extractStreamUrl(detailUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting stream URL: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String streamUrl) {
        if (streamUrl != null && !streamUrl.isEmpty()) {
            callback.onStreamUrlExtracted(streamUrl);
        } else {
            callback.onStreamUrlError("Failed to extract stream URL");
        }
    }
}
