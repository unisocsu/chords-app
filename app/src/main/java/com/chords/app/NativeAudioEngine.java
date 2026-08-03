package com.chords.app;

public class NativeAudioEngine {

    static {
        System.loadLibrary("chord_recognizer");
    }

    public static native boolean initWhisperEngine(String modelPath);

    public static native String processAudioFileBuffer(short[] pcmBuffer, int length, boolean isFinalChunk);

    public static native String processAudioBuffer(short[] audioData, int length);

    public static native double findCurrentTimestampByVoice(short[] micBuffer, int length);

    // פונקציית טרנספוזיציה לקובץ האודיו ב-C++
    public static native short[] transposeAudioPitch(short[] pcmBuffer, int length, int semitones);
}
