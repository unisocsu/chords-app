package com.chords.app;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class AnalysisTimeEstimator {

    private static final String TAG = "TimeEstimator";

    public enum WhisperModelType {
        TINY(39),   // ~39M parameters
        BASE(74),   // ~74M parameters
        SMALL(244); // ~244M parameters

        private final int paramCountMillions;

        WhisperModelType(int paramCount) {
            this.paramCountMillions = paramCount;
        }

        public int getParamCountMillions() {
            return paramCountMillions;
        }
    }

    /**
     * מחשבת את זמן העיבוד המשוער בשניות עבור קובץ אודיו
     * * @param context קונטקסט של האפליקציה לבדיקת זיכרון
     * @param audioDurationSeconds אורך השיר בשניות
     * @param modelType סוג המודל שנבחר (TINY, BASE, SMALL)
     * @param isChordsEnabled האם מנוע האקורדים (KissFFT) פועל במקביל
     * @return זמן עיבוד משוער בשניות
     */
    public static double estimateProcessingTimeSeconds(
            Context context, 
            double audioDurationSeconds, 
            WhisperModelType modelType, 
            boolean isChordsEnabled) {

        // 1. חישוב מקדם כוח המעבד (CPU Score)
        double cpuScore = getDeviceCpuScore();

        // 2. חישוב מקדם זיכרון RAM פנוי
        double ramFactor = getRamFactor(context);

        // 3. חישוב פקטור הציון המשוקלל של המכשיר (Real-time Factor Baseline)
        // ככל שהסקור גבוה יותר, המכשיר מהיר יותר
        double combinedDevicePerformance = cpuScore * ramFactor;

        // 4. עומס מודל ה-Whisper (Real-Time Factor - RTF)
        // RTF מייצג כמה שניות עיבוד נדרשות עבור שניה אחת של אודיו
        double whisperRtf;
        switch (modelType) {
            case TINY:
                whisperRtf = 0.08 / combinedDevicePerformance; // משהו כמו 1:12 ממהירות השיר
                break;
            case BASE:
                whisperRtf = 0.18 / combinedDevicePerformance; // משהו כמו 1:5 ממהירות השיר
                break;
            case SMALL:
            default:
                whisperRtf = 0.55 / combinedDevicePerformance; // משהו כמו 1:2 ממהירות השיר
                break;
        }

        // 5. עומס מנוע האקורדים (KissFFT + Chroma) - מהיר מאוד וחומרי זול
        double chordsRtf = isChordsEnabled ? 0.02 : 0.0;

        // 6. חישוב ה-RTF הכולל
        double totalRtf = whisperRtf + chordsRtf;

        // 7. זמן עיבוד כולל בשניות = אורך השיר * ה-RTF הכולל
        double estimatedTimeSeconds = audioDurationSeconds * totalRtf;

        Log.d(TAG, String.format("Audio Duration: %.1fs | Device Score: %.2f | Estimated Time: %.1fs",
                audioDurationSeconds, combinedDevicePerformance, estimatedTimeSeconds));

        // החזרת לפחות שנייה אחת כמינימום
        return Math.max(1.0, estimatedTimeSeconds);
    }

    /**
     * מחשב ציון מעבד (CPU Score) מבוסס על מספר הליבות וארכיטקטורת ARM
     */
    private static double getDeviceCpuScore() {
        int cores = Runtime.getRuntime().availableProcessors();
        boolean is64Bit = false;

        for (String abi : Build.SUPPORTED_ABIS) {
            if ("arm64-v8a".equals(abi)) {
                is64Bit = true;
                break;
            }
        }

        double baseScore = cores * 0.25;
        if (is64Bit) {
            baseScore *= 1.4; // מעבדי 64-bit מריצים NEON SIMD בצורה יעילה בהרבה
        }

        return Math.min(baseScore, 3.0); // נרמול מקסימלי
    }

    /**
     * מחשב פקטור ביצועים לפי ה-RAM הפנוי במכשיר ברגע זה
     */
    private static double getRamFactor(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
        }

        double availRamGB = memInfo.availMem / (1024.0 * 1024.0 * 1024.0);

        if (availRamGB >= 3.0) {
            return 1.2; // מספיק זיכרון, המערכת לא מבצעת Paging
        } else if (availRamGB >= 1.5) {
            return 1.0; // מצב תקין
        } else {
            return 0.7; // זיכרון נמוך, עלולות להיות השהיות
        }
    }
}
