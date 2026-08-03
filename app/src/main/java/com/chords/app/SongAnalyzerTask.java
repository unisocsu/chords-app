package com.chords.app;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class SongAnalyzerTask {

    private static final String TAG = "SongAnalyzerTask";
    private static final int CHUNK_SIZE_SAMPLES = 16000 * 5; // מקטעים של 5 שניות (בנפח דגימות)

    public interface AnalysisCallback {
        void onAnalysisFinished(boolean success, String jsonResult);
    }

    /**
     * מריץ את הניתוח ברקע: קורא את קובץ האודיו מתוך ה-Uri, חותך למקטעים,
     * שולח ל-C++ ומאחד את התוצאות ל-JSON סופי.
     */
    public static void analyzeAudioUri(Context context, Uri audioUri, AnalysisCallback callback) {
        new Thread(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(audioUri);
                if (inputStream == null) {
                    callback.onAnalysisFinished(false, null);
                    return;
                }

                // קריאת כל הבייטים מהקובץ (או המרה ל-PCM 16-bit מונו)
                // לצורך הפשטות והבטחת התאמה למנוע, ניצור מאגר נתונים זמני
                byte[] audioBytes = readBytesFromStream(inputStream);
                short[] pcmData = convertBytesToShorts(audioBytes);

                List<String> lyricsSegments = new ArrayList<>();
                List<String> chordsSegments = new ArrayList<>();

                // חלוקת השיר למקטעים ושליחה למנוע ה-C++
                int totalSamples = pcmData.length;
                int offset = 0;

                while (offset < totalSamples) {
                    int currentLength = Math.min(CHUNK_SIZE_SAMPLES, totalSamples - offset);
                    short[] chunk = new short[currentLength];
                    System.arraycopy(pcmData, offset, chunk, 0, currentLength);

                    boolean isFinal = (offset + currentLength >= totalSamples);

                    // קריאה לפונקציה ה-Native ב-C++
                    String chunkJsonResult = NativeAudioEngine.processAudioFileBuffer(chunk, currentLength, isFinal);
                    
                    // כאן אפשר לאסוף את חלקי ה-JSON או למזג אותם
                    // לצורך הדוגמה נניח שהפלט מחזיר את המקטע המעובד
                    Log.d(TAG, "עובד מקטע בגודל: " + currentLength);

                    offset += currentLength;
                }

                // יצירת JSON סופי מאוחד (מילים + אקורדים)
                String finalCombinedJson = "{\"lyrics\":[], \"chords\":[]}"; // בהמשך נאחד את כל המקטעים

                // שמירה אוטומטית כחבילה במכשיר
                String songId = "song_" + audioUri.getLastPathSegment();
                SongDataPersistence.saveSongJsonToFile(context, songId, finalCombinedJson);

                callback.onAnalysisFinished(true, finalCombinedJson);

            } catch (Exception e) {
                Log.e(TAG, "שגיאה בתהליך הניתוח", e);
                callback.onAnalysisFinished(false, null);
            }
        }).start();
    }

    private static byte[] readBytesFromStream(InputStream inputStream) throws Exception {
        byte[] buffer = new byte[4096];
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024 * 10); // עד 10MB לדוגמה
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteBuffer.put(buffer, 0, bytesRead);
        }
        inputStream.close();
        byteBuffer.flip();
        byte[] result = new byte[byteBuffer.remaining()];
        byteBuffer.get(result);
        return result;
    }

    private static short[] convertBytesToShorts(byte[] audioBytes) {
        short[] shorts = new short[audioBytes.length / 2];
        ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }
}
