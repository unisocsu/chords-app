package com.chords.app;

public class NativeAudioEngine {

    // טעינת ספריית ה-C++ הממוקמת ב-CMake
    static {
        System.loadLibrary("chord_recognizer");
    }

    // 1. אתחול מנוע Whisper מקובץ המודל
    public static native boolean initWhisperEngine(String modelPath);

    // 2. עיבוד מקטע קובץ שמע (אקורדים + מילים) והחזרת JSON
    public static native String processAudioFileBuffer(short[] pcmBuffer, int length, boolean isFinalChunk);

    // 3. עיבוד פריים שמע בזמן אמת מהמיקרופון (אקורדים בלבד)
    public static native String processAudioBuffer(short[] audioData, int length);

    // 4. חדש: מציאת השנייה המדויקת בשיר לפי קולו של המשתמש במיקרופון לצורך גלילה חכמה
    public static native double findCurrentTimestampByVoice(short[] micBuffer, int length);
}
