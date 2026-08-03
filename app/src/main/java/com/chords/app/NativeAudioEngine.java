package com.chords.app;

public class NativeAudioEngine {
    static {
        System.loadLibrary("chord_recognizer");
    }

    public native String processAudioBuffer(short[] buffer, int sampleRate);
}
