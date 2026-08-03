package com.chords.app;

import android.content.Context;
import android.net.Uri;

public class ChordEngineManager {

    private final Context context;
    private final DeviceCapabilityDetector.EnginePerformanceMode deviceMode;

    public ChordEngineManager(Context context) {
        this.context = context;
        this.deviceMode = DeviceCapabilityDetector.detectPerformanceMode(context);
    }

    public String getRecommendedWhisperModel() {
        switch (deviceMode) {
            case HIGH_END:
                return "ggml-small.bin"; // דיוק גבוה בעברית (~460MB)
            case MID_RANGE:
                return "ggml-base.bin";  // דיוק בינוני (~140MB)
            case LOW_END:
            default:
                return "none";           // שימוש בקובצי LRC / תמליל בלבד
        }
    }

    public void processAudioFile(Uri fileUri, boolean userSelectedLyricsMode) {
        String whisperModel = getRecommendedWhisperModel();

        if (userSelectedLyricsMode && !"none".equals(whisperModel)) {
            // הזרמת קובץ המדיה לפענוח MediaCodec ועיבוד מקביל של אקורדים + מילים
        } else {
            // הפעלת מנוע אקורדים בלבד ב-C++ (מבוסס KissFFT)
        }
    }

    public DeviceCapabilityDetector.EnginePerformanceMode getDeviceMode() {
        return deviceMode;
    }
}
