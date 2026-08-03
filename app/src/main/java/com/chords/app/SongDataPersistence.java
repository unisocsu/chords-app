package com.chords.app;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SongDataPersistence {

    private static final String TAG = "SongPersistence";

    /**
     * שומר את מחרוזת ה-JSON של האקורדים והמילים כקובץ מקומי במכשיר
     */
    public static boolean saveSongJsonToFile(Context context, String songFileName, String jsonContent) {
        try {
            File file = new File(context.getFilesDir(), songFileName + "_chords.json");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonContent.getBytes(StandardCharsets.UTF_8));
            fos.close();
            Log.d(TAG, "הקובץ נשמר בהצלחה בנתיב: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "שגיאה בשמירת קובץ ה-JSON", e);
            return false;
        }
    }

    /**
     * טוען את קובץ ה-JSON המקומי אם הוא כבר קיים (מונע ניתוח מחדש)
     */
    public static String loadSongJsonFromFile(Context context, String songFileName) {
        try {
            File file = new File(context.getFilesDir(), songFileName + "_chords.json");
            if (!file.exists()) {
                return null; // הקובץ עוד לא קיים, צריך לנתח
            }

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            fis.close();

            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "שגיאה בטעינת קובץ ה-JSON", e);
            return null;
        }
    }
}
