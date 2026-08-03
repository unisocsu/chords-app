#include <jni.h>
#include <string>
#include <vector>
#include "FileLyricsAnalyzer.h"
#include "FileChordsAnalyzer.h"

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_chords_app_NativeAudioEngine_initWhisperEngine(
        JNIEnv *env, jobject thiz, jstring model_path_jstr) {
    const char *path = env->GetStringUTFChars(model_path_jstr, nullptr);
    std::string modelPath(path);
    env->ReleaseStringUTFChars(model_path_jstr, path);

    return FileLyricsAnalyzer::initModel(modelPath) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_chords_app_NativeAudioEngine_processAudioFileBuffer(
        JNIEnv *env, jobject thiz, jshortArray pcm_buffer, jint length, jboolean is_final_chunk) {
    
    jshort *pcm_data = env->GetShortArrayElements(pcm_buffer, nullptr);
    if (!pcm_data) return env->NewStringUTF("");

    std::vector<short> rawPcm(pcm_data, pcm_data + length);
    env->ReleaseShortArrayElements(pcm_buffer, pcm_data, JNI_ABORT);

    // 1. קבלת ניתוח אקורדים מקובץ עבור המקטע
    std::string chordResult = FileChordsAnalyzer::processChordChunk(rawPcm);

    // 2. המרה ל-float עבור מנוע המילים (Whisper)
    std::vector<float> floatPcm(length);
    for (int i = 0; i < length; i++) {
        floatPcm[i] = (float)rawPcm[i] / 32768.0f;
    }

    // 3. קבלת ניתוח מילים מקובץ
    std::string lyricsResult = FileLyricsAnalyzer::processFileChunk(floatPcm);

    // החזרת פלט משולב (או טיפול בתוצאות בנפרד בהמשך)
    std::string combined = "Chord: " + chordResult + " | Lyrics: " + lyricsResult;
    return env->NewStringUTF(combined.c_str());
}

}
