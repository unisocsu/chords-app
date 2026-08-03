#include <jni.h>
#include <string>
#include <vector>
#include "FileLyricsAnalyzer.h"
#include "FileChordsAnalyzer.h"
#include "RealtimeChordsAnalyzer.h"

extern "C" {

// --- אתחול מנוע Whisper ---
JNIEXPORT jboolean JNICALL
Java_com_chords_app_NativeAudioEngine_initWhisperEngine(
        JNIEnv *env, jobject thiz, jstring model_path_jstr) {
    const char *path = env->GetStringUTFChars(model_path_jstr, nullptr);
    std::string modelPath(path);
    env->ReleaseStringUTFChars(model_path_jstr, path);
    return FileLyricsAnalyzer::initModel(modelPath) ? JNI_TRUE : JNI_FALSE;
}

// --- ניתוח קובץ שמע שלם (אקורדים + מילים ל-JSON) ---
JNIEXPORT jstring JNICALL
Java_com_chords_app_NativeAudioEngine_processAudioFileBuffer(
        JNIEnv *env, jobject thiz, jshortArray pcm_buffer, jint length, jboolean is_final_chunk) {
    
    jshort *pcm_data = env->GetShortArrayElements(pcm_buffer, nullptr);
    if (!pcm_data) return env->NewStringUTF("{\"lyrics\":[], \"chords\":[]}");

    std::vector<short> rawPcm(pcm_data, pcm_data + length);
    env->ReleaseShortArrayElements(pcm_buffer, pcm_data, JNI_ABORT);

    double currentTimestamp = 0.0; 
    std::string chordsJson = FileChordsAnalyzer::processChordChunk(rawPcm, currentTimestamp);

    std::vector<float> floatPcm(length);
    for (int i = 0; i < length; i++) {
        floatPcm[i] = (float)rawPcm[i] / 32768.0f;
    }
    std::string lyricsJson = FileLyricsAnalyzer::processFileChunk(floatPcm);

    std::string combinedJson = "{\"lyrics\":" + lyricsJson + ", \"chords\":" + chordsJson + "}";
    return env->NewStringUTF(combinedJson.c_str());
}

// --- אקורדים בלבד בזמן אמת (מיקרופון) ---
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

// --- חדש: מציאת השנייה הנוכחית בשיר לפי קול המשתמש במיקרופון (גלילה חכמה) ---
JNIEXPORT jdouble JNICALL
Java_com_chords_app_NativeAudioEngine_findCurrentTimestampByVoice(
        JNIEnv *env, 
        jobject thiz, 
        jshortArray mic_buffer, 
        jint length) {
            
    jshort *buffer = env->GetShortArrayElements(mic_buffer, nullptr);
    if (!buffer) return -1.0;

    std::vector<short> micData(buffer, buffer + length);
    env->ReleaseShortArrayElements(mic_buffer, buffer, JNI_ABORT);

    // כאן יתבצע בעתיד האלגוריתם להשוואת השמע של המשתמש מול מבנה השיר
    // כרגע נחזיר ערך לדוגמה
    double matchedTimestampSeconds = 0.0; 

    return matchedTimestampSeconds;
}

}
