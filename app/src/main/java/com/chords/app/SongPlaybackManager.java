package com.chords.app;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SongPlaybackManager {

    private static final String TAG = "SongPlayback";
    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;

    public interface OnSyncUpdateListener {
        void onTimeUpdated(double currentSeconds);
    }

    private OnSyncUpdateListener syncListener;

    public void setOnSyncUpdateListener(OnSyncUpdateListener listener) {
        this.syncListener = listener;
    }

    public void playSong(Context context, Uri audioUri) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(context, audioUri);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                isPlaying = true;
                startSyncLoop();
                Log.d(TAG, "הנגן התחיל לנגן את השיר");
            }
        } catch (Exception e) {
            Log.e(TAG, "שגיאה בהפעלת הנגן", e);
        }
    }

    private void startSyncLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlaying && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    double currentSeconds = mediaPlayer.getCurrentPosition() / 1000.0;
                    if (syncListener != null) {
                        syncListener.onTimeUpdated(currentSeconds);
                    }
                    handler.postDelayed(this, 100); // עדכון כל 100 מילישניות לסנכרון חלק
                }
            }
        }, 100);
    }

    public void stopSong() {
        isPlaying = false;
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "הנגן נעצר");
    }
}
