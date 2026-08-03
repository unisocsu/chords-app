#include <jni.h>
#include <string>
#include <vector>
#include "FileLyricsAnalyzer.h"
#include "FileChordsAnalyzer.h"
#include "RealtimeChordsAnalyzer.h"

extern "C" {

// --- נתיב 1: אתחול מנוע Whisper לקבצים ---
JNIEXPORT jboolean JNICALL
Java_com_chords_app_NativeAudioEngine_initWhisperEngine(
        JNIEnv *env, jobject thiz, jstring model_path_jstr) {
    const char *path = env->GetStringUTFChars(model_path_jstr, nullptr);
    std::string modelPath(path);
    env->ReleaseStringUTFChars(model_path_jstr, path);
    return FileLyricsAnalyzer::initModel(modelPath) ? JNI_TRUE : JNI_FALSE;
}

// --- נתיב 2: ניתוח קובץ שמע שלם (אקורדים + מילים) ---
JNIEXPORT jstring JNICALL
Java_com_chords_app_NativeAudioEngine_processAudioFileBuffer(
        JNIEnv *env, jobject thiz, jshortArray pcm_buffer, jint length, jboolean is_final_chunk) {
    
    jshort *pcm_data = env->GetShortArrayElements(pcm_buffer, nullptr);
    if (!pcm_data) return env->NewStringUTF("");

    std::vector<short> rawPcm(pcm_data, pcm_data + length);
    env->ReleaseShortArrayElements(pcm_buffer, pcm_data, JNI_ABORT);

    // ניתוח אקורדים מקובץ
    std::string chordResult = FileChordsAnalyzer::processChordChunk(rawPcm);

    // המרה ל-float עבור מנוע ה-Whisper למילים
    std::vector<float> floatPcm(length);
    for (int i = 0; i < length; i++) {
        floatPcm[i] = (float)rawPcm[i] / 32768.0f;
    }
    std::string lyricsResult = FileLyricsAnalyzer::processFileChunk(floatPcm);

    std::string combined = "Chord: " + chordResult + " | Lyrics: " + lyricsResult;
    return env->NewStringUTF(combined.c_str());
}

// --- נתיב 3: אקורדים בלבד בזמן אמת (מיקרופון לגיטרה) ---
JNIEXPORT jstring JNICALL
Java_com_chords_app_NativeAudioEngine_processAudioBuffer(
        JNIEnv *env, jobject thiz, jshortArray audio_data, jint length) {
            
    jshort *buffer = env->GetShortArrayElements(audio_data, nullptr);
    if (!buffer) return env->NewStringUTF("None");

    std::vector<short> micChunk(buffer, buffer + length);
    env->ReleaseShortArrayElements(audio_data, buffer, JNI_ABORT);

    std::string realtimeChord = RealtimeChordsAnalyzer::processMicrophoneBuffer(micChunk);

    return env->NewStringUTF(realtimeChord.c_str());
}

}
