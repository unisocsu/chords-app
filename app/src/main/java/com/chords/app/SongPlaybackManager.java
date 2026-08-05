package com.chords.app;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public class SongPlaybackManager {

    private static final String TAG = "SongPlaybackManager";
    private MediaPlayer mediaPlayer;

    public void playSong(Context context, Uri audioUri) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(context, audioUri);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    Log.d(TAG, "השיר הסתיים");
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "שגיאה בהפעלת השיר", e);
        }
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * הגדרת מהירות השמעה (Tempo/Speed) מוגנת עבור תאימות ל-API 19
     */
    public void setPlaybackSpeed(float speed) {
        if (mediaPlayer == null) return;

        // PlaybackParams נתמך החל מ-API 23 (Android 6.0) בלבד
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                PlaybackParams params = mediaPlayer.getPlaybackParams();
                if (params == null) {
                    params = new PlaybackParams();
                }
                params.setSpeed(speed);
                mediaPlayer.setPlaybackParams(params);
            } catch (Exception e) {
                Log.e(TAG, "שגיאה בשינוי מהירות השמעה", e);
            }
        } else {
            // ב-API 19 (Android 4.4 KitKat) אין תמיכה מובנית ב-PlaybackParams
            Log.w(TAG, "שינוי מהירות השמעה אינו נתמך בגרסת Android זו (מצריך API 23+).");
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                Log.e(TAG, "שגיאה בקבלת מיקום נוכחי", e);
            }
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getDuration();
            } catch (Exception e) {
                Log.e(TAG, "שגיאה בקבלת אורך השיר", e);
            }
        }
        return 0;
    }

    public void seekTo(int positionMs) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.seekTo(positionMs);
            } catch (Exception e) {
                Log.e(TAG, "שגיאה בדילוג במיקום", e);
            }
        }
    }
}
